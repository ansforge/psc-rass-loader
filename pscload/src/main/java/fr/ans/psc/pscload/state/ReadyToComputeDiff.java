/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.state;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;
import fr.ans.psc.pscload.model.operations.OperationMap;
import fr.ans.psc.pscload.model.operations.OperationType;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.metrics.CustomMetrics.ID_TYPE;
import fr.ans.psc.pscload.metrics.CustomMetrics.SizeMetric;
import fr.ans.psc.pscload.model.entities.ExerciceProfessionnel;
import fr.ans.psc.pscload.model.entities.Professionnel;
import fr.ans.psc.pscload.model.entities.RassEntity;
import fr.ans.psc.pscload.model.entities.RassItems;
import fr.ans.psc.pscload.model.entities.SituationExercice;
import fr.ans.psc.pscload.state.exception.DiffException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * The Class ReadyToComputeDiff.
 */
@Slf4j
public class ReadyToComputeDiff extends ProcessState {
    private static final int ROW_LENGTH = RassItems.values().length + 1;
    private Map<String, Professionnel> newPsMap = new HashMap<>();
    private Map<String, Professionnel> oldPsMap = new HashMap<>();
    private PsApi psApi;
    private List<String> excludedProfessionCodes=Collections.emptyList();

    private CustomMetrics customMetrics;

    /**
     * Instantiates a new ready to compute diff state.
     */
    public ReadyToComputeDiff(List<String> excludedProfessionCodes, CustomMetrics customMetrics, String apiBaseUrl) {
        super();
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiBaseUrl);
        this.psApi = new PsApi(apiClient);
        this.customMetrics = customMetrics;
        this.excludedProfessionCodes=Objects.requireNonNull(excludedProfessionCodes,"excludedProfessionCodes cannot be null");
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
            newPsMap = loadMapsFromFile(fileToLoad);
            Map<String, List<ExerciceProfessionnel>> fusedProfessions = new HashMap<>();
            oldPsMap = loadMapFromDB(fusedProfessions);
            if (!oldPsMap.isEmpty()) {
                setReferenceSizeMetricsAfterDeserializing(oldPsMap);
            }
            // Launch diff
            MapDifference<String, Professionnel> diffPs = Maps.difference(oldPsMap, newPsMap);
            fillChangesMaps(diffPs);

            // Filter unchanged fused CREATEs to avoid unnecessary HTTP calls
            filterUnchangedFusedCreates(fusedProfessions);

            // Clear maps immediately after use to free memory
            log.info("Clearing oldPsMap and newPsMap to release memory...");
            oldPsMap.clear();
            oldPsMap = null;
            newPsMap.clear();
            newPsMap = null;
            fusedProfessions.clear();
            System.gc();

        } catch (IOException e) {
            throw new DiffException("I/O Error when deserializing file", e);
        } catch (RuntimeException e) {
            throw new DiffException("RunTimeException has occurred", e);
        }

    }

    private Map<String, Professionnel> loadMapFromDB(Map<String, List<ExerciceProfessionnel>> fusedProfessions) {
        log.info("retrieving all Ps");
        int page = 0;
        BigDecimal size = BigDecimal.valueOf(50000);
        boolean outOfPages = false;
        List<Ps> psList = new ArrayList<>();

        log.debug("Excluding the following profession from delete : {}",excludedProfessionCodes);
        while (!outOfPages) {
            try {
                log.debug("get all Ps, page {}", page);
                List<Ps> psPage = psApi.getPsByPage(BigDecimal.valueOf(page), size);
                log.debug("page {} received", page);

                // Collect fused professions from PSI accounts before filtering them out
                for (Ps ps : psPage) {
                    if (isPsi(ps) && ps.getIds() != null && ps.getProfessions() != null) {
                        for (String id : ps.getIds()) {
                            if (!isUUID(id)) {
                                List<ExerciceProfessionnel> profs = ps.getProfessions().stream()
                                        .filter(p -> id.equals(p.getSourceId()))
                                        .map(ExerciceProfessionnel::new)
                                        .collect(Collectors.toList());
                                if (!profs.isEmpty()) {
                                    fusedProfessions.put(id, profs);
                                }
                            }
                        }
                    }
                }

                // Force clear alternativeIds to reduce memory footprint
                psPage.forEach(ps -> ps.setAlternativeIds(null));

                // Debug: Check if projection is working
                if (!psPage.isEmpty()) {
                    Ps firstPs = psPage.get(0);
                    if (firstPs.getAlternativeIds() == null) {
                        log.info("Page {}: alternativeIds is NULL (projection working)", page);
                    } else {
                        log.warn("Page {}: alternativeIds NOT NULL, size = {} (projection NOT working!)",
                                 page, firstPs.getAlternativeIds().size());
                    }
                }

                // Debug: Check memory usage
                if (page % 10 == 0) {
                    Runtime runtime = Runtime.getRuntime();
                    long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
                    log.info("Page {}: Memory used = {} MB, psList size = {} objects",
                             page, usedMemory, psList.size());
                }

                List<Ps> adeliFiltered = psPage.stream()
                        .filter(ps -> ps.getDeactivated() == null || ps.getDeactivated() < ps.getActivated())
                        .filter(ps -> !(
                                ps.getIdType().equals(ID_TYPE.ADELI.value)
                                && ps.getProfessions().stream().anyMatch(profession -> this.excludedProfessionCodes.contains(profession.getCode()))
                            )
                        )
                        // Exclude PSI identities (not from RASS) to prevent deletion
                        .filter(ps -> !isPsi(ps))
                .collect(Collectors.toList());
                log.debug("filtering successful for page {}", page);
                psList.addAll(adeliFiltered);
                page++;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode().equals(HttpStatus.GONE)) {
                    outOfPages = true;
                } else {
                    log.error("Unexpected error while loading existing Ps Map. Aborting process...");
                    throw new DiffException("Error when loading existing Ps Map", e);
                }
            } catch (Exception e) {
                log.error("something wrong happened", e);
            }
        }
        Map<String, Professionnel> psMap = psList.stream().map(Professionnel::new).collect(
                Collectors.toMap(Professionnel::getNationalId, Function.identity()));

        return psMap;
    }

    /**
     * Check if a PS is a PSI identity (ProSanté Identity).
     * PSI identities are not managed by RASS and should not be deleted.
     * PSI identities use UUID format for nationalId OR have alternativeIds with origine='PSI'.
     * 
     * @param ps the PS to check
     * @return true if PSI identity, false otherwise
     */
    private boolean isPsi(Ps ps) {
        // Check 1: PSI uses UUID format for nationalId (8-4-4-4-12 hexadecimal pattern)
        // Matches UUID v4, v7, etc. Examples: 550e8400-e29b-41d4-a716-446655440000, 019c5172-a5c0-7188-b81f-5c16807b9140
        String nationalId = ps.getNationalId();
        if (nationalId != null && nationalId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return true;
        }
        
        // Check 2: Check if alternativeIds contains a PSI identity
        // Even if nationalId is RPPS/ADELI/SIRET, if there's a PSI alternative, protect the account
        if (ps.getAlternativeIds() != null && !ps.getAlternativeIds().isEmpty()) {
            boolean hasPsiAlternativeId = ps.getAlternativeIds().stream()
                .anyMatch(altId -> "PSI".equalsIgnoreCase(altId.getOrigine()));
            if (hasPsiAlternativeId) {
                log.debug("PS {} has PSI alternativeId, protecting from deletion", ps.getNationalId());
                return true;
            }
        }
        
        // Check 3: PSI may have empty idType (but this alone is not sufficient, must have UUID nationalId)
        // This check is removed as it was too permissive and marked non-PSI accounts as PSI
        return false;
    }

    public Map<String, Professionnel> loadMapsFromFile(File file) throws IOException, IllegalArgumentException, DataProcessingException {
        log.info("loading {} into list of Ps", file.getName());
        Map<String, Professionnel> psMap = new HashMap<>();

        // ObjectRowProcessor converts the parsed values and gives you the resulting
        // row.
        ObjectRowProcessor rowProcessor = new ObjectRowProcessor() {
            @Override
            public void rowProcessed(Object[] objects, ParsingContext parsingContext) {

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
        try (InputStream is = new FileInputStream(file)) {
            Charset detectedCharset = Charset.forName(new TikaEncodingDetector().guessEncoding(is));
            log.debug("detected charset is : " + detectedCharset.displayName());
            parser.parse(new BufferedReader(new FileReader(file, detectedCharset)));
        } catch (IOException e) {
            throw new IOException("Encoding detection failure", e);
        }
        log.info("loading complete!");

        return psMap;
    }


    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, newPsMap);
        kryo.writeObject(output, oldPsMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        newPsMap = (Map<String, Professionnel>) kryo.readObject(input, HashMap.class);
        oldPsMap = (Map<String, Professionnel>) kryo.readObject(input, HashMap.class);
    }

    private void fillChangesMaps(MapDifference<String, Professionnel> diffPs) {

        log.info("filling changes maps");

        process.getMaps().forEach(map -> {
            switch (map.getOperation()) {
                case UPDATE:
                    diffPs.entriesDiffering().forEach((k, v) -> {
                        map.put(k, v.rightValue());
                        map.saveOldValue(k, v.leftValue());
                    });
                    break;
                case DELETE:
                    // Schema RG01 pre-filter: ADELI ids that disappear from RASS must NOT be
                    // deleted directly. They are cleaned up only via the Phase 2 RPPS→ADELI
                    // cascade inside fused accounts.
                    diffPs.entriesOnlyOnLeft().forEach((k, v) -> {
                        if (ID_TYPE.ADELI.value.equals(v.getIdType())) {
                            log.debug("Skipping ADELI {} from PsDeleteMap (schema pre-filter)", k);
                            return;
                        }
                        map.put(k, v);
                    });
                    break;
                case CREATE:
                    map.putAll(diffPs.entriesOnlyOnRight());
                    break;
                default:
                    break;
            }
        });

        log.info("operation maps filled.");
    }

    /**
     * Filters out CREATE entries for fused RPPS whose professions haven't changed.
     * This avoids unnecessary HTTP POST calls for RPPS accounts that are already
     * fused into a PSI with identical profession data.
     */
    private void filterUnchangedFusedCreates(Map<String, List<ExerciceProfessionnel>> fusedProfessions) {
        if (fusedProfessions.isEmpty()) return;

        OperationMap<String, RassEntity> createMap = process.getMaps().stream()
                .filter(m -> m.getOperation() == OperationType.CREATE)
                .findFirst().orElse(null);
        if (createMap == null) return;

        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, RassEntity> entry : createMap.entrySet()) {
            String nationalId = entry.getKey();
            if (fusedProfessions.containsKey(nationalId)) {
                Professionnel prof = (Professionnel) entry.getValue();
                List<ExerciceProfessionnel> existingProfs = fusedProfessions.get(nationalId);
                List<ExerciceProfessionnel> newProfs = prof.getExercicesProfessionels();
                if (existingProfs.size() == newProfs.size() && existingProfs.containsAll(newProfs)) {
                    toRemove.add(nationalId);
                }
            }
        }

        toRemove.forEach(createMap::remove);
        log.info("Filtered {} unchanged fused CREATEs out of {} fused entries",
                toRemove.size(), fusedProfessions.size());
    }

    private static boolean isUUID(String id) {
        return id != null && id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private void setReferenceSizeMetricsAfterDeserializing(Map<String, Professionnel> psMap) {
        Arrays.stream(ID_TYPE.values()).forEach(id_type -> {
            String metricKey = String.join("_", "REFERENCE", id_type.name(), "SIZE");
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
                filesList.forEach(File::delete);
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
