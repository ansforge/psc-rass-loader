package fr.ans.psc.pscload.component;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.PscloadApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
@AutoConfigureMockMvc
public class RegistrySerializationTest {

    @Autowired
    private ProcessRegistry registry;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Runner runner;

    @Autowired
    private MockMvc mockmvc;

    /** The http mock server. */
    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

    /**
     * Register pg properties.
     *
     * @param propertiesRegistry the properties registry
     */
    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
        propertiesRegistry.add("extract.download.url",
                () -> httpMockServer.baseUrl() + "/V300/services/extraction/Extraction_ProSanteConnect");
        propertiesRegistry.add("files.directory",
                () -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        propertiesRegistry.add("api.base.url", () -> httpMockServer.baseUrl());
        propertiesRegistry.add("use.x509.auth", () -> "false");
        propertiesRegistry.add("enable.scheduler", () -> "true");
        propertiesRegistry.add("scheduler.cron", () -> "0 0 1 15 * ?");
        propertiesRegistry.add("pscextract.base.url", () -> httpMockServer.baseUrl());
    }

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    void setup() {
        registry.clear();
        // clear work directory
        File outputfolder = new File(Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        File[] files = outputfolder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                f.delete();
            }
        }
    }



//    CAUTION
//    This method tests that the registry serialization ends properly when context shuts down.
//    So there isn't any assertion but if this tests failed, a KryoException would be thrown, then failing the test
//
//    Because of the Spring context destruction at the end of the test, this method MUST be placed alone in its own
//    test class
    @Test
    @DisplayName("test shutdown serialization")
    public void shutdownSerializationTest() throws Exception {
        // first day : populate ser
        String rassEndpoint = "/V300/services/extraction/Extraction_ProSanteConnect";
        String extractFilenameDay1 = "extract_small_202203170801.txt";
        zipFile("wiremock/" + extractFilenameDay1);
        String extractDay1Path = Thread.currentThread().getContextClassLoader().getResource("wiremock/" + extractFilenameDay1 + ".zip")
                .getPath();
        byte[] extractDay1Content = readFileToBytes(extractDay1Path);

        httpMockServer.stubFor(get(rassEndpoint).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/zip")
                .withHeader("Content-Disposition", "attachment; filename=" + extractFilenameDay1 + ".zip")
                .withBody(extractDay1Content)));
        httpMockServer.stubFor(any(urlMatching("/v2/ps")).willReturn(aResponse().withStatus(200)));
        httpMockServer.stubFor(any(urlMatching("/v2/structure")).willReturn(aResponse().withStatus(200)));
        runner.runScheduler();
        httpMockServer.stubFor(any(urlMatching("/generate-extract")).willReturn(aResponse().withStatus(200)));
        mockmvc.perform(post("/process/continue").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful()).andDo(print());
        registry.clear();


        // second day : generate diff
        String extractFilenameDay2 = "extract_small_202203170801.txt";
        zipFile("wiremock/" + extractFilenameDay2);
        String extractDay2Path = Thread.currentThread().getContextClassLoader().getResource("wiremock/" + extractFilenameDay2 + ".zip")
                .getPath();
        byte[] extractDay2Content = readFileToBytes(extractDay2Path);

        httpMockServer.stubFor(get(rassEndpoint).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/zip")
                .withHeader("Content-Disposition", "attachment; filename=" + extractFilenameDay2 + ".zip")
                .withBody(extractDay2Content)));
        runner.runScheduler();

        mockmvc.perform(post("/process/continue").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful()).andDo(print());

        log.warn("STARTING SHUTDOWN...");
        context.publishEvent(new ContextClosedEvent(context));
        log.warn("END OF TEST");
    }

    private static void zipFile(String filename) throws Exception {

        String filePath = Thread.currentThread().getContextClassLoader().getResource(filename).getPath();
        File file = new File(filePath);
        String zipFileName = file.getPath().concat(".zip");

        FileOutputStream fos = new FileOutputStream(zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);

        zos.putNextEntry(new ZipEntry(file.getName()));

        byte[] bytes = readFileToBytes(filePath);
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
        zos.close();
    }

    private static byte[] readFileToBytes(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];

        // funny, if can use Java 7, please uses Files.readAllBytes(path)
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        return bytes;
    }
}
