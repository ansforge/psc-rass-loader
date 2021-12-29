package fr.ans.psc.pscload.model;

import java.io.BufferedReader;
import java.io.Externalizable;
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
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import fr.ans.psc.model.Profession;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@EqualsAndHashCode()
@Slf4j
public class MapsHandler implements Externalizable {

	{
		final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
		conf.registerClass(HashMap.class, Professionnel.class, Structure.class, SavoirFaire.class,
				SituationExercice.class, ExerciceProfessionnel.class, RefStructure.class);
	}

	private static final int ROW_LENGTH = 50;

	private Map<String, Professionnel> psMap = new HashMap<>();

	private Map<String, Structure> structureMap = new HashMap<>();

	public void loadMapsFromFile(File file) throws IOException {
		log.info("loading {} into list of Ps", file.getName());

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
				Professionnel psMapped = psMap.get(items[RassItems.NATIONAL_ID.column]);
				if (psMapped == null) {
					// create PS and add to map
					Professionnel psRow = new Professionnel(items, true);
					psMap.put(psRow.getNationalId(), psRow);
				} else {
					// if ps exists then add expro and situ exe.
					Optional<Profession> p = psMapped.getProfessionByCodeAndCategory(
							items[RassItems.EX_PRO_CODE.column], items[RassItems.CATEGORY_CODE.column]);
					if (p.isPresent()) {
						// add worksituation : it can't exists, otherwise it is a duplicate entry.
						SituationExercice situ = new SituationExercice(items);
						p.get().addWorkSituationsItem(situ);
					} else {
						// Add profession and worksituation
						ExerciceProfessionnel exepro = new ExerciceProfessionnel(items);
						psMapped.addProfessionsItem(exepro);

					}
				}
				// get structure in map by its reference from row
				if (structureMap.get(items[RassItems.STRUCTURE_TECHNICAL_ID.column]) == null) {
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

	public void serializeMaps(String filename) throws IOException {
		File mapsFile = new File(filename);
		FileOutputStream fileOutputStream = new FileOutputStream(mapsFile);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		FSTObjectOutput out = new FSTObjectOutput(oos);
		writeExternal(out);
		out.close();
	}

	public void deserializeMaps(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fileInputStream);
		FSTObjectInput in = new FSTObjectInput(ois);
		readExternal(in);
		in.close();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(psMap);
		out.writeObject(structureMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		psMap = (Map<String, Professionnel>) in.readObject();
		structureMap = (Map<String, Structure>) in.readObject();
	}

	public void clearMaps() {
		psMap.clear();
		structureMap.clear();
	}
}
