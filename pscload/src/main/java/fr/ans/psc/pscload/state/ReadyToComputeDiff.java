/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.state.exception.DiffException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ReadyToComputeDiff.
 */
@Slf4j
public class ReadyToComputeDiff extends ProcessState {

	private MapsHandler newMaps = new MapsHandler();
	private MapsHandler oldMaps = new MapsHandler();

	private CustomMetrics customMetrics;

	/**
	 * Instantiates a new ready to compute diff state.
	 */
	public ReadyToComputeDiff(CustomMetrics customMetrics) {
		super();
		this.customMetrics = customMetrics;
	}

	/**
	 * Instantiates a new ready to compute diff.
	 */
	public ReadyToComputeDiff() {
		super();
	}

	@Override
	public void nextStep() throws LoadProcessException {
		File fileToLoad = new File(process.getExtractedFilename());
		cleanup(fileToLoad.getParent());

		try {
			newMaps.loadMapsFromFile(fileToLoad);
			// we serialize new map now in a temp file (maps.{timestamp}.lock
			File tmpmaps = new File(
					fileToLoad.getParent() + File.separator + "maps." + process.getTimestamp() + ".lock");
			process.setTmpMapsPath(tmpmaps.getAbsolutePath());
			newMaps.serializeMaps(tmpmaps.getPath());
			// deserialize the old file if exists
			File maps = new File(fileToLoad.getParent() + File.separator + "maps.ser");
			if (maps.exists()) {
				oldMaps.deserializeMaps(fileToLoad.getParent() + File.separator + "maps.ser");
				setReferenceSizeMetricsAfterDeserializing(oldMaps.getPsMap());
			}
			// Launch diff
			MapDifference<String, Professionnel> diffPs = Maps.difference(oldMaps.getPsMap(), newMaps.getPsMap());
			fillChangesMaps(diffPs);

		} catch (IOException e) {
			throw new DiffException("I/O Error when deserializing file", e);
		} catch (ClassNotFoundException e) {
			throw new DiffException(".ser file not compatible with model", e);
		} catch (RuntimeException e) {
			throw new DiffException("RunTimeException has occurred", e);
		}

	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, newMaps);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		oldMaps = (MapsHandler) kryo.readObject(input, MapsHandler.class);

	}

	private void fillChangesMaps(MapDifference<String, Professionnel> diffPs) {

		log.info("filling changes maps");

		process.getMaps().forEach(map -> {
			switch (map.getOperation()) {
			case PS_UPDATE:
				diffPs.entriesDiffering().forEach((k, v) -> {
					map.put(k, v.rightValue());
					map.saveOldValue(k, v.leftValue());
				});
				break;
			case PS_DELETE:
				diffPs.entriesOnlyOnLeft().forEach(map::put);
				break;
			case PS_CREATE:
				diffPs.entriesOnlyOnRight().forEach(map::put);
				break;
			default:
				break;
			}
		});

		log.info("operation maps filled.");
	}

	private void setReferenceSizeMetricsAfterDeserializing(Map<String, Professionnel> psMap) {
		Arrays.stream(ID_TYPE.values()).forEach(id_type -> {
			String metricKey = String.join("_", "PS_REFERENCE", id_type.name(), "SIZE");
			SizeMetric metric = SizeMetric.valueOf(metricKey);

			customMetrics.setPsMetricSize(metric, Math.toIntExact(psMap.values().stream().filter(
					professionnel -> id_type.value.equals(professionnel.getIdType())).count()));
		});
	}

	/**
	 * Deletes all except latest files.
	 *
	 * @param filesDirectory the files directory
	 */
	private void cleanup(String filesDirectory) {
		log.info("Cleaning files repository, removing all but latest files");
		Map<String, List<File>> filesMap = zipsTextsNSers(new File(filesDirectory).listFiles());

		filesMap.values().forEach(filesList -> {
			filesList.sort(this::compare);
			if (filesList.size() > 0) {
				filesList.remove(filesList.size() - 1);
				filesList.forEach(file -> file.delete());
			}
		});
	}

	/**
	 * Zips and texts map.
	 *
	 * @param listOfFiles the list of files
	 * @return the map
	 */
	private Map<String, List<File>> zipsTextsNSers(File[] listOfFiles) {
		Map<String, List<File>> filesMap = new HashMap<>();
		filesMap.put("zips", new ArrayList<>());
		filesMap.put("txts", new ArrayList<>());
		filesMap.put("locks", new ArrayList<>());

		for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
			if (file.getName().endsWith(".zip")) {
				filesMap.get("zips").add(file);
			} else if (file.getName().endsWith(".txt")) {
				filesMap.get("txts").add(file);
			} else if (file.getName().endsWith(".lock")) {
				filesMap.get("locks").add(file);
			}
		}
		return filesMap;
	}

	private int compare(File f1, File f2) {
		try {
			return getDateFromFileName(f1).compareTo(getDateFromFileName(f2));
		} catch (ParseException e) {
			log.error("Error when parsing filename to check date", e);
		}
		return 0;
	}

	private Date getDateFromFileName(File file) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");

		String regex = ".*(\\d{12}).*";
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(file.getName());
		if (m.find()) {
			return dateFormatter.parse(m.group(1));
		}
		return new Date(0);
	}

	public CustomMetrics getCustomMetrics() {
		return customMetrics;
	}
}
