/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.Structure;
import fr.ans.psc.pscload.state.exception.DiffException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;

/**
 * The Class FileExtracted.
 */
public class ReadyToComputeDiff extends ProcessState {


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
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, newMaps);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		oldMaps = (MapsHandler) kryo.readObject(input, MapsHandler.class);

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
}
