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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.model.Ps;
import fr.ans.psc.pscload.model.entities.RassItems;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Phase 2 state: purge secondary identifiers that have disappeared from the RASS file.
 *
 * After Phase 1 (CREATE/UPDATE/DELETE), this state re-reads the RASS file to build a
 * HashSet of all nationalIds, then iterates PSI accounts via the paginated API.
 * For each PSI's secondary identifier that is NOT in the RASS file, it calls the
 * purge endpoint to remove the associated professions, ids, and alternativeIds.
 */
@Slf4j
public class PurgingSecondaryIds extends ProcessState {

    private String apiBaseUrl;
    private transient PsApi psApi;

    private static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    /**
     * Constructor for Kryo deserialization.
     */
    public PurgingSecondaryIds() {
        super();
    }

    /**
     * Constructor for programmatic use.
     *
     * @param apiBaseUrl base URL of psc-ps-api
     */
    public PurgingSecondaryIds(String apiBaseUrl) {
        super();
        this.apiBaseUrl = apiBaseUrl;
        initPsApi();
    }

    private void initPsApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(apiBaseUrl);
        this.psApi = new PsApi(apiClient);
    }

    @Override
    public void nextStep() throws LoadProcessException {
        log.info("Phase 2: Purging disappeared secondary identifiers...");

        // Phase A: Build HashSet of all RASS nationalIds
        Set<String> rassIds = loadRassNationalIds();
        log.info("Loaded {} nationalIds from RASS file", rassIds.size());

        // Phase B: Iterate PSI accounts and purge stale secondary IDs
        int purgedCount = 0;
        int psiCount = 0;
        int page = 0;
        BigDecimal size = BigDecimal.valueOf(50000);
        boolean outOfPages = false;

        while (!outOfPages) {
            try {
                List<Ps> psPage = psApi.getPsByPage(BigDecimal.valueOf(page), size);
                for (Ps ps : psPage) {
                    if (!isUUID(ps.getNationalId())) {
                        continue; // Skip non-PSI accounts
                    }
                    if (ps.getIds() == null || ps.getIds().size() <= 1) {
                        continue; // No secondary IDs to check
                    }

                    psiCount++;
                    for (String secondaryId : new ArrayList<>(ps.getIds())) {
                        if (secondaryId.equals(ps.getNationalId())) {
                            continue; // Skip the PSI's own UUID
                        }
                        if (isUUID(secondaryId)) {
                            continue; // Skip other UUID references
                        }

                        if (!rassIds.contains(secondaryId)) {
                            try {
                                log.info("Purging source {} from PSI {}", secondaryId, ps.getNationalId());
                                psApi.deletePsSourceProfessions(
                                        URLEncoder.encode(ps.getNationalId(), StandardCharsets.UTF_8),
                                        URLEncoder.encode(secondaryId, StandardCharsets.UTF_8));
                                purgedCount++;
                            } catch (RestClientResponseException e) {
                                log.warn("Failed to purge source {} from PSI {}: HTTP {}",
                                        secondaryId, ps.getNationalId(), e.getRawStatusCode());
                            } catch (RestClientException e) {
                                log.error("Error purging source {} from PSI {}: {}",
                                        secondaryId, ps.getNationalId(), e.getMessage());
                            }
                        }
                    }
                }
                page++;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode().equals(HttpStatus.GONE)) {
                    outOfPages = true;
                } else {
                    log.error("Error fetching page {} during purge phase", page, e);
                    break; // Abort purge loop gracefully
                }
            }
        }

        // Phase C: Cleanup
        rassIds.clear();
        log.info("Phase 2 complete: {} sources purged from {} PSI accounts", purgedCount, psiCount);
    }

    /**
     * Re-read the RASS file, extract ONLY nationalIds into a HashSet.
     * Memory efficient: ~75MB for 1.5M entries (just strings, no Professionnel objects).
     * Reuses same CSV parser config as ReadyToComputeDiff.loadMapsFromFile().
     */
    private Set<String> loadRassNationalIds() {
        Set<String> ids = new HashSet<>();
        File file = new File(process.getExtractedFilename());

        ObjectRowProcessor rowProcessor = new ObjectRowProcessor() {
            @Override
            public void rowProcessed(Object[] objects, ParsingContext parsingContext) {
                String[] items = Arrays.asList(objects).toArray(new String[0]);
                if (items.length > RassItems.NATIONAL_ID.column) {
                    String nationalId = items[RassItems.NATIONAL_ID.column];
                    if (nationalId != null && !nationalId.isEmpty()) {
                        ids.add(nationalId);
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
        try (InputStream is = new FileInputStream(file)) {
            Charset detectedCharset = Charset.forName(
                    new TikaEncodingDetector().guessEncoding(is));
            parser.parse(new BufferedReader(new FileReader(file, detectedCharset)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to re-read RASS file for Phase 2", e);
        }

        return ids;
    }

    /**
     * Check if a string is a valid UUID (PSI identifier format).
     */
    private static boolean isUUID(String id) {
        return id != null && id.matches(UUID_REGEX);
    }

    @Override
    public boolean isAlreadyComputed() {
        return true;
    }

    @Override
    public boolean isExpirable() {
        return false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeString(apiBaseUrl);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        apiBaseUrl = input.readString();
        initPsApi();
    }
}
