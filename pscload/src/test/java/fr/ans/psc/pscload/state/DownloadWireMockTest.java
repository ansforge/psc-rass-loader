package fr.ans.psc.pscload.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DownloadWireMockTest {
	
	@RegisterExtension
    static WireMockExtension rassMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()
            		.dynamicHttpsPort()
            		.keystorePath("https-certs-keystore.jks")
            		.keystorePassword("verysecret!")
            		.keystoreType("JKS")
            		.needClientAuth(true)
            		.trustStorePath("/path/to/trust-store.jks")
            		.trustStorePassword("trustme"))
            .build();

    @Test
    void downloadTest(WireMockRuntimeInfo wmRuntimeInfo) {
        // The static DSL will be automatically configured for you
        stubFor(get("/static-dsl").willReturn(ok()));
             
        // Info such as port numbers is also available
        int port = wmRuntimeInfo.getHttpPort();
        
        // Do some testing...
    }
}
