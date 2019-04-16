package net.mfjassociates.tools;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class HttpTest {

	private static final int REQUEST_TIMEOUT = 30000;
	private static final int CONNECT_TIMEOUT = 30000;
	private static final int SOCKET_TIMEOUT = 60000;

	public static void main(String[] args) {
		SpringApplication.run(HttpTest.class);

	}
	@Bean
	public ApplicationRunner clrunner(RestTemplate aRestTemplate) {
		return args -> {
			System.out.println("# OptionArgs: "+args.getOptionNames().size());
			args.getOptionNames().forEach(optionName -> {System.out.println(optionName+"="+args.getOptionValues(optionName));});
			System.out.println("# NonOptionArgs: "+args.getNonOptionArgs().size());
			args.getNonOptionArgs().forEach(optionArg -> {System.out.println(optionArg+"="+args.getOptionValues(optionArg));});
			ResponseEntity<String> resp=aRestTemplate.getForEntity("https://www.google.ca", String.class);
			System.out.println("size="+resp.getBody().length());
		};
	}
	
	@Bean
	public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory hcchrf) {
		return new RestTemplate(hcchrf);
	}
	
	@Bean
	public CloseableHttpClient chc() {
		RequestConfig rConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(REQUEST_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT).build();
		return HttpClients.custom()
				.setDefaultRequestConfig(rConfig)
				.build();
	}
	@Bean
	public HttpComponentsClientHttpRequestFactory chrf(CloseableHttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory chrf = new HttpComponentsClientHttpRequestFactory();
		chrf.setHttpClient(httpClient);
		return chrf;
	}

}
