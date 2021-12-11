package fr.ans.psc.pscload.state;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.any23.encoding.TikaEncodingDetector;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.ExerciceProfessionnel;
import fr.ans.psc.pscload.model.Professionnel;
import fr.ans.psc.pscload.model.SituationExercice;
import fr.ans.psc.pscload.model.Structure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileExtracted extends ProcessState {

	private static final int ROW_LENGTH = 50;

	private static final long serialVersionUID = 1208602116799660764L;

	private final Map<String, Professionnel> newPsMap = new HashMap<>();

	private final Map<String, Structure> newStructureMap = new HashMap<>();

	private Map<String, Professionnel> oldPsMap = new HashMap<>();

	private Map<String, Structure> oldStructureMap = new HashMap<>();

	@Override
	public void runTask() {
		// TODO load maps
		File fileToLoad = new File(process.getExtractedFilename());
		try {
			loadMapsFromTextFile(fileToLoad, newPsMap, newStructureMap);
			// we serialize new map now in a temp file (maps.{timestamp}.lock
			File tmpmaps = new File(fileToLoad.getParent() + File.pathSeparator + "maps." + process.getTimestamp() + ".lock");
			serialize(tmpmaps.getPath());
			// deserialize the old file if exists
			File maps = new File(fileToLoad.getParent() + File.pathSeparator + "maps.ser");
			if (maps.exists()) {
				deserialize(fileToLoad.getParent() + File.pathSeparator + "maps.ser");
			}
			// Launch diff
			
			// Rename serialized file
			maps.delete();
			tmpmaps.renameTo(maps);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(newPsMap);
		out.writeObject(newStructureMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		oldPsMap = (Map<String, Professionnel>) in.readObject();
		oldStructureMap = (Map<String, Structure>) in.readObject();

	}

	private void serialize(String filename) throws IOException {
		File mapsFile = new File(filename);
		FileOutputStream fileOutputStream = new FileOutputStream(mapsFile);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		writeExternal(oos);
	}

	private void deserialize(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fileInputStream);
		readExternal(ois);
	}

	private void loadMapsFromTextFile(File file, Map<String, Professionnel> psMap, Map<String, Structure> structureMap)
			throws IOException {
		log.info("loading {} into list of Ps", file.getName());
		psMap.clear();
		structureMap.clear();
		// ObjectRowProcessor converts the parsed values and gives you the resulting
		// row.
		ObjectRowProcessor rowProcessor = new ObjectRowProcessor() {
			@Override
			public void rowProcessed(Object[] objects, ParsingContext parsingContext) {
				if (objects.length != ROW_LENGTH) {
					throw new IllegalArgumentException();
				}
				String[] items = Arrays.asList(objects).toArray(new String[ROW_LENGTH]);
				// test if exists by nationalId (item 2)
				Professionnel psMapped = psMap.get(items[2]);
				if (psMapped == null) {
					// create PS and add to map
					Professionnel psRow = new Professionnel(items, true);
					psMap.put(psRow.getNationalId(), psRow);
				} else {
					// if ps exists then add expro and situ exe.
					Optional<Profession> p = psMapped.getProfessionByCodeAndCategory(items[13], items[14]);
					if (p.isPresent()) {
						// add worksituation : it can't exists, otherwise it is a duplicate entry.
						SituationExercice situ = new SituationExercice(items);
						p.get().addWorkSituationsItem(situ);
					} else {
						// Add profession and worksituation
						ExerciceProfessionnel exepro = new ExerciceProfessionnel(items);
						psMapped.addProfessionsItem(exepro);
						;
					}
				}
				// get structure in map by its reference from row
				if (structureMap.get(items[28]) == null) {
					Structure newStructure = new Structure(items);
					structureMap.put(newStructure.getStructureTechnicalId(), newStructure);
				}
			}
		};

		CsvParserSettings parserSettings = new CsvParserSettings();
		parserSettings.getFormat().setLineSeparator("\n");
		parserSettings.getFormat().setDelimiter('|');
		parserSettings.setProcessor(rowProcessor);
		parserSettings.setHeaderExtractionEnabled(true);
		parserSettings.setNullValue("");

		CsvParser parser = new CsvParser(parserSettings);

		// get file charset to secure data encoding
		InputStream is = new FileInputStream(file);
		try {
			Charset detectedCharset = Charset.forName(new TikaEncodingDetector().guessEncoding(is));
			parser.parse(new BufferedReader(new FileReader(file, detectedCharset)));
		} catch (IOException e) {
			throw new IOException("Encoding detection failure", e);
		}
		log.info("loading complete!");
	}
}
