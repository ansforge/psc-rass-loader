package fr.ans.psc.pscload.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.any23.encoding.TikaEncodingDetector;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.UploadMetrics;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.state.ChangesApplied;
import fr.ans.psc.pscload.state.DiffComputed;
import fr.ans.psc.pscload.state.Idle;
import fr.ans.psc.pscload.state.ProcessState;
import fr.ans.psc.pscload.state.ReadyToComputeDiff;
import fr.ans.psc.pscload.state.ReadyToExtract;
import fr.ans.psc.pscload.state.UploadingChanges;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@EqualsAndHashCode()
@Slf4j
public class MapsHandler implements KryoSerializable {

	private static Kryo kryo;

	{
		kryo = new Kryo();
		kryo.register(HashMap.class, 9);
		kryo.register(ArrayList.class, 10);
		kryo.register(Professionnel.class, 11);
		kryo.register(ExerciceProfessionnel.class, 12);
		kryo.register(SavoirFaire.class, 13);
		kryo.register(SituationExercice.class, 14);
		kryo.register(RefStructure.class, 15);
		kryo.register(Structure.class, 16);
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
		Output output = new Output(fileOutputStream);
		write(kryo, output);
		output.close();
	}

	public void deserializeMaps(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		Input input = new Input(fileInputStream);
		read(kryo, input);
		input.close();
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, psMap);
		kryo.writeObject(output, structureMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		psMap = (Map<String, Professionnel>) kryo.readObject(input, HashMap.class);
		structureMap = (Map<String, Structure>) kryo.readObject(input, HashMap.class);
	}

	public void clearMaps() {
		psMap.clear();
		structureMap.clear();
	}
}
