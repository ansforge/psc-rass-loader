package fr.ans.psc.pscload.service;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import fr.ans.psc.model.Profession;
import fr.ans.psc.pscload.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class MapsManager {

    private static final int ROW_LENGTH = 50;

    public MapsHandler loadMapsFromFile(File file) throws IOException {
        log.info("loading {} into list of Ps", file.getName());
        MapsHandler newMaps = new MapsHandler();

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
                Professionnel psMapped = newMaps.getPsMap().get(items[RassItems.NATIONAL_ID.column]);
                if (psMapped == null) {
                    // create PS and add to map
                    Professionnel psRow = new Professionnel(items, true);
                    newMaps.getPsMap().put(psRow.getNationalId(), psRow);
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
                if (newMaps.getStructureMap().get(items[RassItems.STRUCTURE_TECHNICAL_ID.column]) == null) {
                    Structure newStructure = new Structure(items);
                    newMaps.getStructureMap().put(newStructure.getStructureTechnicalId(), newStructure);
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
        return newMaps;
    }

    public void serializeMaps(String filename, MapsHandler mapsHandler) throws IOException {
        File mapsFile = new File(filename);
        FileOutputStream fileOutputStream = new FileOutputStream(mapsFile);
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        mapsHandler.writeExternal(oos);
        oos.close();
    }

    public void deserializeMaps(String filename, MapsHandler mapsHandler) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fileInputStream);
        mapsHandler.readExternal(ois);
        ois.close();
    }
}
