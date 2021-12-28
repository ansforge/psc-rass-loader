/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.component.DuplicateKeyException;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import fr.ans.psc.pscload.model.MapsHandler;
import fr.ans.psc.pscload.service.EmailService;
import fr.ans.psc.pscload.service.LoadProcess;
import fr.ans.psc.pscload.service.MapsManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class ChangesAppliedTest {

    @Autowired
    private MapsManager mapsManager;

    @Autowired
    private CustomMetrics customMetrics;

    @Autowired
    private ProcessRegistry registry;

    @Autowired
    private EmailService emailService;

    @Mock
    private JavaMailSender javaMailSender;

    /**
     * The http api mock server.
     */
    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock/api"))
            .configureStaticDsl(true).build();

    // For use with mockMvc
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
        propertiesRegistry.add("deactivation.excluded.profession.codes", () -> "0");
        propertiesRegistry.add("pscextract.base.url", () -> httpMockServer.baseUrl());
    }

    @BeforeEach
    public void setup() throws Exception {
        File outputfolder = new File(Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        File[] files = outputfolder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                f.delete();
            }
        }

        httpMockServer.stubFor(any(urlMatching("/generate-extract"))
                .willReturn(aResponse().withStatus(200)));

        MockitoAnnotations.openMocks(this).close();
        emailService.setEmailSender(javaMailSender);
    }

    // CAS 100% PASSANT : pas de message généré, appel à extract
    @Test
    @DisplayName("Changes applied with no errors")
    public void changesApplied() throws DuplicateKeyException, IOException, ClassNotFoundException {
        // SET UP : updates ok, 2 different 4xx on Ps, 5xx on structure
        httpMockServer.stubFor(post("/ps")
                .willReturn(aResponse().withStatus(409)));
        httpMockServer.stubFor(put("/ps")
                .willReturn(aResponse().withStatus(200)));
        httpMockServer.stubFor(delete("/ps/810107592585")
                .willReturn(aResponse().withStatus(404)));
        httpMockServer.stubFor(post("/structure")
                .willReturn(aResponse().withStatus(500)));

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String rootpath = cl.getResource(".").getPath();
        File mapser = new File(rootpath + File.separator + "maps.ser");
        if (mapser.exists()) {
            mapser.delete();
        }
        //Day 1 : Generate old ser file
        LoadProcess p = new LoadProcess(new ReadyToComputeDiff(mapsManager));
        p.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());
        p.nextStep();
        p.setState(new ChangesApplied(customMetrics, httpMockServer.baseUrl(), mapsManager));
        p.getState().setProcess(p);
        p.nextStep();
        // Day 2 : Compute diff
        LoadProcess p2 = new LoadProcess(new ReadyToComputeDiff(mapsManager));
        registry.register(Integer.toString(registry.nextId()), p2);
        p2.setExtractedFilename(cl.getResource("Extraction_ProSanteConnect_Personne_activite_202112120515.txt").getPath());
        p2.nextStep();
        p2.setState(new DiffComputed(customMetrics));
        p2.nextStep();
        // Day 2 : upload changes
        String[] exclusions = {"90"};
        p2.setState(new UploadingChanges(exclusions, httpMockServer.baseUrl()));
        p2.getState().setProcess(p2);
        p2.nextStep();

        // 2xx return status should have been removed from update map
        assertEquals(1, p2.getPsToCreate().size());
        assertEquals(1, p2.getPsToDelete().size());
        assertEquals(0, p2.getPsToUpdate().size());
        assertEquals(1, p2.getStructureToCreate().size());
        assertEquals(HttpStatus.CONFLICT.value(), p2.getPsToCreate().get("810100375103").getReturnStatus());
        assertEquals(HttpStatus.NOT_FOUND.value(), p2.getPsToDelete().get("810107592585").getReturnStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), p2.getStructureToCreate().get("R10100000063415").getReturnStatus());

        // Apply changes and generate new ser
        p2.setState(new ChangesApplied(customMetrics, httpMockServer.baseUrl(), mapsManager));
        p2.getState().setProcess(p2);
        p2.nextStep();

        // check ser file : 4xx create should be in, 4xx delete should not, 5xx are in the previous state
        MapsHandler serializedMaps = new MapsHandler();
        mapsManager.deserializeMaps(mapser.getAbsolutePath(), serializedMaps);
        assert serializedMaps.getPsMap().get("810100375103") != null;
        assert serializedMaps.getPsMap().get("810107592585") == null;
        assert serializedMaps.getStructureMap().get("R10100000063415") == null;
    }

}
