package fr.ans.psc.pscload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.ans.psc.ApiClient;

@Configuration
public class ApiClientConfig {

	@Value("${api.base.url}")
	private String apiBaseUrl;

	@Bean
	public ApiClient apiclient() {
		ApiClient client = new ApiClient();
		client.setBasePath(apiBaseUrl);
		return client;

	}
}
