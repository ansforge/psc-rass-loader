/*
 * Copyright © 2022-2026 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.ObjectRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.model.AlternativeIdentifier;
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
 * Phase 2 state: purge secondary identifiers that have disappeared from the RASS file
 * from fused accounts, implementing the schema RG01 rules (E01-U005).
 *
 * <p>Rules applied (schema RG01):
 * <ul>
 *   <li>Iterate <b>all fused accounts</b> (PSI or non-PSI) — any Ps whose {@code ids}
 *       contains at least one non-UUID id other than its {@code nationalId}.</li>
 *   <li>For each such account, compute the set of secondary ids that must be purged
 *       directly: non-UUID, absent from RASS, and whose {@code origine} is NOT
 *       {@code ADELI} (ADELI pre-filter from the schema).</li>
 *   <li>Call {@code DELETE /v2/ps/{id}/source/{sourceId}} for each.</li>
 *   <li><b>Cascade RPPS→ADELI</b>: after the direct purges, if the remaining
 *       {@code alternativeIds} no longer contains any {@code origine=RPPS} entry but
 *       still contains one or more {@code origine=ADELI}, purge those ADELI entries
 *       too (an ADELI without its parent RPPS is considered obsolete).</li>
 * </ul>
 */
@Slf4j
public class PurgingSecondaryIds extends ProcessState {

    private String apiBaseUrl;
    private transient PsApi psApi;

    private static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final String ORIGIN_RPPS = "RPPS";
    private static final String ORIGIN_ADELI = "ADELI";

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
        log.info("Phase 2: Purging disappeared secondary identifiers (schema RG01)...");

        // Phase A: Build HashSet of all RASS nationalIds
        Set<String> rassIds = loadRassNationalIds();
        log.info("Loaded {} nationalIds from RASS file", rassIds.size());

        // Phase B: Iterate all fused accounts and purge stale secondary IDs + cascade
        int directPurges = 0;
        int cascadeAdeliPurges = 0;
        int fusedAccountsScanned = 0;
        int page = 0;
        BigDecimal size = BigDecimal.valueOf(50000);
        boolean outOfPages = false;

        while (!outOfPages) {
            try {
                List<Ps> psPage = psApi.getPsByPage(BigDecimal.valueOf(page), size);
                for (Ps ps : psPage) {
                    if (!isFused(ps)) {
                        continue;
                    }
                    fusedAccountsScanned++;

                    // Step 1: compute toPurge = non-UUID secondary ids not in RASS,
                    //         excluding ADELI (schema pre-filter)
                    List<String> toPurge = new ArrayList<>();
                    for (String id : ps.getIds()) {
                        if (id == null || id.equals(ps.getNationalId()) || isUUID(id)) {
                            continue;
                        }
                        if (rassIds.contains(id)) {
                            continue;
                        }
                        if (ORIGIN_ADELI.equalsIgnoreCase(getOrigine(ps, id))) {
                            // Pre-filter ADELI : n'est jamais purgé directement
                            continue;
                        }
                        toPurge.add(id);
                    }

                    for (String id : toPurge) {
                        if (purgeSource(ps.getNationalId(), id)) {
                            directPurges++;
                        }
                    }

                    // Step 2: evaluate cascade on the account's final state
                    //         (altIds remaining after the direct purges above)
                    List<AlternativeIdentifier> remainingAltIds =
                            ps.getAlternativeIds() == null
                                    ? Collections.emptyList()
                                    : ps.getAlternativeIds().stream()
                                            .filter(alt -> alt != null
                                                    && !toPurge.contains(alt.getIdentifier()))
                                            .collect(Collectors.toList());

                    boolean hasRpps = remainingAltIds.stream()
                            .anyMatch(alt -> ORIGIN_RPPS.equalsIgnoreCase(alt.getOrigine()));

                    List<String> adeliToCascade = remainingAltIds.stream()
                            .filter(alt -> ORIGIN_ADELI.equalsIgnoreCase(alt.getOrigine()))
                            .map(AlternativeIdentifier::getIdentifier)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (!hasRpps && !adeliToCascade.isEmpty()) {
                        log.info("Cascade RPPS→ADELI : purging {} ADELI id(s) from account {} "
                                        + "(no RPPS left in altIds)",
                                adeliToCascade.size(), ps.getNationalId());
                        for (String adeliId : adeliToCascade) {
                            if (purgeSource(ps.getNationalId(), adeliId)) {
                                cascadeAdeliPurges++;
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
                    break;
                }
            }
        }

        rassIds.clear();
        log.info("Phase 2 complete: {} direct purges + {} ADELI cascade purges from {} "
                        + "fused accounts scanned",
                directPurges, cascadeAdeliPurges, fusedAccountsScanned);
    }

    /**
     * An account is "fused" if it carries at least one non-UUID secondary id in addition
     * to its primary nationalId. Standalone accounts (single id = nationalId) and
     * accounts whose secondary ids are all UUIDs are skipped.
     */
    static boolean isFused(Ps ps) {
        if (ps == null || ps.getIds() == null || ps.getIds().size() <= 1) {
            return false;
        }
        String primary = ps.getNationalId();
        for (String id : ps.getIds()) {
            if (id == null || id.equals(primary) || isUUID(id)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Resolve the {@code origine} of an id by looking up {@link Ps#getAlternativeIds()}.
     * Falls back to deriving the origine from the first character of the id when no
     * matching entry is found (some legacy rows may lack the metadata).
     */
    static String getOrigine(Ps ps, String id) {
        if (ps.getAlternativeIds() != null) {
            for (AlternativeIdentifier alt : ps.getAlternativeIds()) {
                if (alt != null && id.equals(alt.getIdentifier())) {
                    return alt.getOrigine();
                }
            }
        }
        return deriveOrigineFromId(id);
    }

    /**
     * Derive the {@code origine} from the first character of a RASS-style id,
     * following the same convention as {@code psc-ps-api} ApiUtils.
     */
    private static String deriveOrigineFromId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        switch (id.charAt(0)) {
            case '0': return ORIGIN_ADELI;
            case '1': return "CAB_ADELI";
            case '3': return "FINESS";
            case '4': return "SIREN";
            case '5': return "SIRET";
            case '6': return "CAB_RPPS";
            case '8': return ORIGIN_RPPS;
            default:  return null;
        }
    }

    private boolean purgeSource(String psId, String sourceId) {
        try {
            log.info("Purging source {} from account {}", sourceId, psId);
            psApi.deletePsSourceProfessions(
                    URLEncoder.encode(psId, StandardCharsets.UTF_8),
                    URLEncoder.encode(sourceId, StandardCharsets.UTF_8));
            return true;
        } catch (RestClientResponseException e) {
            log.warn("Failed to purge source {} from account {}: HTTP {}",
                    sourceId, psId, e.getRawStatusCode());
        } catch (RestClientException e) {
            log.error("Error purging source {} from account {}: {}",
                    sourceId, psId, e.getMessage());
        }
        return false;
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
    static boolean isUUID(String id) {
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
