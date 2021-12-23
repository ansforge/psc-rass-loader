/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.component.ProcessRegistry;
import fr.ans.psc.pscload.metrics.CustomMetrics;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class ChangesAppliedTest {

    /**
     * The mock mvc.
     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomMetrics customMetrics;

    @Autowired
    private ProcessRegistry registry;

    /**
     * The http api mock server.
     */
    @RegisterExtension
    static WireMockExtension httpApiMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock/api"))
            .configureStaticDsl(true).build();

    // For use with mockMvc
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
        propertiesRegistry.add("api.base.url",
                () -> httpApiMockServer.baseUrl());
        propertiesRegistry.add("deactivation.excluded.profession.codes", () -> "0");
        propertiesRegistry.add("pscextract.base.url", () -> httpApiMockServer.baseUrl());
    }


    // CAS 100% PASSANT : pas de message généré, appel à extract

    // QQ REPONSES 4xx : dépile les maps et génère mail, appel à extract

    // QQ REPONSES 5xx : dépile les maps et modifie le ser, appel à extract
}
