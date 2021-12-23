package fr.ans.psc.pscload.service;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.model.MapsHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class MapsManagerTest {

    @Autowired
    private MapsManager mapsManager;

    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

    @Test
    @DisplayName("Serialize and deserialize file")
    public void serializerTest() throws IOException, ClassNotFoundException {
        File initialFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());

        MapsHandler initialMaps = new MapsHandler();
        initialMaps = mapsManager.loadMapsFromFile(initialFile);

        File serializedFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource(".").getPath() + File.separator + "maps.ser");

        mapsManager.serializeMaps(serializedFile.getName(), initialMaps);

        MapsHandler deserializedMaps = new MapsHandler();
        mapsManager.deserializeMaps(serializedFile.getName(), deserializedMaps);

        assertEquals(initialMaps, deserializedMaps);
    }
}
