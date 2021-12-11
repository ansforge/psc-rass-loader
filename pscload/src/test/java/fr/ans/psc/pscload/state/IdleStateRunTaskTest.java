package fr.ans.psc.pscload.state;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.service.LoadProcess;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
public class IdleStateRunTaskTest {

	@RegisterExtension
	static WireMockExtension httpRassMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.dynamicPort()
					.usingFilesUnderClasspath("wiremock"))
			.configureStaticDsl(true).build();

	
	@Test
	@DisplayName("Nominal case : http without cert auth")
	void httpDownloadTest() throws Exception {
		//Configure the mock service to serve zipfile
		String contextPath = "/V300/services/extraction/Extraction_ProSanteConnect";
		String filename = "Extraction_ProSanteConnect_Personne_activite_202112090858.txt";
		zipFile("wiremock/" + filename);
		String path = Thread.currentThread().getContextClassLoader()
				.getResource("wiremock/" + filename + ".zip").getPath();
		byte[] content = readFileToBytes(path);
		httpRassMockServer.stubFor(get(contextPath).willReturn(
				aResponse().withStatus(200)
				.withHeader("Content-Type", "application/zip")
				.withHeader("Content-Disposition", "attachment; filename=" + filename + ".zip")
				.withBody(content)));
		// Download test
		String filesDirectory = Thread.currentThread().getContextClassLoader()
				.getResource(".").getPath();
		String extracturl = httpRassMockServer.baseUrl() + contextPath;
		LoadProcess p = new LoadProcess(new Idle(extracturl, filesDirectory));
		p.runtask();
		String zipFilePath = p.getDownloadedFilename();
		File downloadedFile = new File(zipFilePath);
		assertTrue(isValid(downloadedFile));
	}
	
	private boolean isValid(File file) {
	    ZipFile zipfile = null;
	    ZipInputStream zis = null;
	    try {
	        zipfile = new ZipFile(file);
	        zis = new ZipInputStream(new FileInputStream(file));
	        ZipEntry ze = zis.getNextEntry();
	        if(ze == null) {
	            return false;
	        }
	        while(ze != null) {
	            // if it throws an exception fetching any of the following then we know the file is corrupted.
	            zipfile.getInputStream(ze);
	            ze.getCrc();
	            ze.getCompressedSize();
	            ze.getName();
	            ze = zis.getNextEntry();
	        } 
	        return true;
	    } catch (ZipException e) {
	        return false;
	    } catch (IOException e) {
	        return false;
	    } finally {
	        try {
	            if (zipfile != null) {
	                zipfile.close();
	                zipfile = null;
	            }
	        } catch (IOException e) {
	            return false;
	        } try {
	            if (zis != null) {
	                zis.close();
	                zis = null;
	            }
	        } catch (IOException e) {
	            return false;
	        }

	    }
	}
	
	
	private static byte[] readFileToBytes(String filePath) throws IOException {

	    File file = new File(filePath);
	    byte[] bytes = new byte[(int) file.length()];

	    // funny, if can use Java 7, please uses Files.readAllBytes(path)
	    try(FileInputStream fis = new FileInputStream(file)){
	        fis.read(bytes);
	    }
	    return bytes;
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
}
