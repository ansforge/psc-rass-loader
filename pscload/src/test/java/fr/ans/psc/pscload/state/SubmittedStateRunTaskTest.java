/**
 * Copyright (C) 2022-2023 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.pscload.state;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.model.LoadProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Class IdleStateRunTaskTest.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
public class SubmittedStateRunTaskTest {

	/** The http rass mock server. */
	@RegisterExtension
	static WireMockExtension httpRassMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.dynamicPort()
					.usingFilesUnderClasspath("wiremock"))
			.configureStaticDsl(true).build();

	/**
	 * Register pg properties.
	 *
	 * @param propertiesRegistry the properties registry
	 */
	@DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("extract.download.url", 
          () -> httpRassMockServer.baseUrl());
		propertiesRegistry.add("files.directory", () -> Thread.currentThread().getContextClassLoader().getResource(".").getPath());
    }

	/**
	 * Http download test.
	 *
	 * @throws Exception the exception
	 */
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
				.getResource("work").getPath();
		String extracturl = httpRassMockServer.baseUrl() + contextPath;
		LoadProcess p = new LoadProcess(new Submitted(extracturl, filesDirectory),"1");
		p.nextStep();
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
