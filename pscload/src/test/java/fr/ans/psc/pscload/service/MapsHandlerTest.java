package fr.ans.psc.pscload.service;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.model.MapsHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class MapsHandlerTest {

    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

    @Test
    @DisplayName("Serialize and deserialize file")
    public void serializerTest() throws IOException, ClassNotFoundException {
        File initialFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("Extraction_ProSanteConnect_Personne_activite_202112120512.txt").getPath());

        MapsHandler initialMaps = new MapsHandler();
        initialMaps.loadMapsFromFile(initialFile);
        assertEquals(5, initialMaps.getPsMap().size());

        File serializedFile = new File(Thread.currentThread().getContextClassLoader()
                .getResource("work").getPath() + File.separator + "maps.ser");
        initialMaps.serializeMaps(serializedFile.getAbsolutePath());

        MapsHandler deserializedMaps = new MapsHandler();
        deserializedMaps.deserializeMaps(serializedFile.getAbsolutePath());
        assertEquals(initialMaps, deserializedMaps);
    }
}
