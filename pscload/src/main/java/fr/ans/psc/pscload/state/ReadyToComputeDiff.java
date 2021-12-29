/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.DiffException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileExtracted.
 */
@Slf4j
public class ReadyToComputeDiff extends ProcessState {


	private static final long serialVersionUID = 1208602116799660764L;

	private MapsHandler newMaps = new MapsHandler();
	private MapsHandler oldMaps = new MapsHandler();
	

	/**
	 * Instantiates a new file extracted.
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
				setUploadSizeMetricsAfterDeserializing(oldMaps.getPsMap(), oldMaps.getStructureMap());
			}
			// Launch diff
			MapDifference<String, Professionnel> diffPs = Maps.difference(oldMaps.getPsMap(), newMaps.getPsMap());
			MapDifference<String, Structure> diffStructures = Maps.difference(oldMaps.getStructureMap(), newMaps.getStructureMap());

			fillChangesMaps(diffPs, diffStructures);

		} catch (IOException e) {
			throw new DiffException("I/O Error when deserializing file", e);
		} catch (ClassNotFoundException e) {
			throw new DiffException(".ser file not compatible with model", e);
		}

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(newMaps);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		oldMaps = (MapsHandler) in.readObject();

	}

	private void fillChangesMaps(MapDifference<String, Professionnel> diffPs, MapDifference<String, Structure> diffStructures) {
		process.setPsToCreate((ConcurrentHashMap<String, Professionnel>) diffPs.entriesOnlyOnRight().entrySet().stream()
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue)));
		//Updates
		Map<String, ValueDifference<Professionnel>> pstmpmap;
		pstmpmap = (ConcurrentHashMap<String, ValueDifference<Professionnel>>) diffPs.entriesDiffering().entrySet().stream()
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
		//Convert ValueDifference to PscValueDifference for serialization
		Map<String, Professionnel> pstu = process.getPsToUpdate();
		pstmpmap.forEach((k, v) -> pstu.put(k,v.rightValue()));

		process.setPsToDelete((ConcurrentHashMap<String, Professionnel>) diffPs.entriesOnlyOnLeft().entrySet().stream()
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue)));
		//Structures

		process.setStructureToCreate((ConcurrentHashMap<String, Structure>) diffStructures.entriesOnlyOnRight().entrySet().stream()
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue)));
		// updates
		Map<String, ValueDifference<Structure>> structtmpmap;
		structtmpmap = (ConcurrentHashMap<String, ValueDifference<Structure>>) diffStructures.entriesDiffering().entrySet().stream()
				.collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, Structure> structtu = process.getStructureToUpdate();
		structtmpmap.forEach((k, v) -> structtu.put(k, v.rightValue()));

	}
	
	private void setUploadSizeMetricsAfterDeserializing(Map<String, Professionnel> psMap, Map<String, Structure> structureMap) {
		process.getUploadMetrics().setPsAdeliUploadSize(Math.toIntExact(psMap.values().stream()
				.filter(professionnel -> ID_TYPE.ADELI.value.equals(professionnel.getIdType())).count()));

		process.getUploadMetrics().setPsFinessUploadSize(Math.toIntExact(psMap.values().stream()
				.filter(professionnel -> ID_TYPE.FINESS.value.equals(professionnel.getIdType())).count()));

		process.getUploadMetrics().setPsSiretUploadSize(Math.toIntExact(psMap.values().stream()
				.filter(professionnel -> ID_TYPE.SIRET.value.equals(professionnel.getIdType())).count()));

		process.getUploadMetrics().setPsRppsUploadSize(Math.toIntExact(psMap.values().stream()
				.filter(professionnel -> ID_TYPE.RPPS.value.equals(professionnel.getIdType())).count()));

		process.getUploadMetrics().setStructureUploadSize(structureMap.values().size());
	}

	/**
	 * Deletes all except latest files.
	 *
	 * @param filesDirectory the files directory
	 */
	private void cleanup(String filesDirectory) {
		log.info("Cleaning files repository, removing all but latest files");
		Map<String, List<File>> filesMap = zipsTextsNSers(new File(filesDirectory).listFiles());

		List<File> listOfZips = filesMap.get("zips");
		List<File> listOfExtracts = filesMap.get("txts");
		List<File> listOfSers = filesMap.get("sers");

		// Order files lists from oldest to newest by comparing parsed dates,
		// but honestly same result if we had used file name String to compare
		listOfZips.sort(this::compare);
		listOfExtracts.sort(this::compare);
		listOfSers.sort(this::compare);

		if (listOfZips.size() > 0) {
			listOfZips.remove(listOfZips.size() -1);
		}
		if (listOfExtracts.size() > 0) {
			listOfExtracts.remove(listOfExtracts.size() -1);
		}
		if (listOfSers.size() > 0) {
			listOfSers.remove(listOfSers.size() -1);
		}

		for (File file : listOfZips) {
			file.delete();
		}
		for (File file : listOfExtracts) {
			file.delete();
		}
		for (File file : listOfSers) {
			file.delete();
		}
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
		filesMap.put("sers", new ArrayList<>());

		for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
			if (file.getName().endsWith(".ser")) {
				filesMap.get("sers").add(file);
			} else if (file.getName().endsWith(".zip")) {
				filesMap.get("zips").add(file);
			} else if (file.getName().endsWith(".txt")) {
				filesMap.get("txts").add(file);
			}
		}
		return filesMap;
	}

	private int compare(File f1, File f2) {
		try {
			return getDateFromFileName(f1).compareTo(getDateFromFileName(f2));
		} catch (ParseException e) {
			e.printStackTrace();
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
}
