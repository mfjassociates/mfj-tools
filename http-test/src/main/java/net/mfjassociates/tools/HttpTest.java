package net.mfjassociates.tools;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;

@SpringBootApplication
public class HttpTest {

	private static final int REQUEST_TIMEOUT = 30000;
	private static final int CONNECT_TIMEOUT = 30000;
	private static final int SOCKET_TIMEOUT = 60000;
	private static final Logger logger=LoggerFactory.getLogger(HttpTest.class);
	private static final ch.qos.logback.classic.Logger httpLogger=(ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.http");
	private static final ch.qos.logback.classic.Logger wireLogger=(ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.http.wire");
	private static final String SILENT_ARGUMENT = "silent";
	private static final String HEADERS_ARGUMENT = "headers";
	private static final String WIRE_ARGUMENT = "wire";
	private static enum LOGGING_MODE {
		silent, headers, wire;
	}
	private static LOGGING_MODE loggingMode = LOGGING_MODE.headers;
	@Autowired
	private RestTemplate restTemplate=null;

	public static void main(String[] args) {
		SpringApplication.run(HttpTest.class, args);

	}
	@Bean
	public ApplicationRunner clrunner() {
		return args -> {
			/**
			 * [--silent|--wire|--headers] <list of urls>
			 */
			long mutually=Stream.of(args.containsOption(SILENT_ARGUMENT),
					args.containsOption(HEADERS_ARGUMENT),
					args.containsOption(WIRE_ARGUMENT))
					.filter(b -> b).count();
			if (mutually>1) throw new IllegalArgumentException("Only one of "+
					Stream.of(SILENT_ARGUMENT, HEADERS_ARGUMENT, WIRE_ARGUMENT)
					.map(a -> "--"+a)
					.collect(Collectors.toList())
					.toString()+" can be specified");
			if (mutually==1) {
				if (args.containsOption(SILENT_ARGUMENT)) loggingMode=LOGGING_MODE.silent;
				if (args.containsOption(WIRE_ARGUMENT)) loggingMode=LOGGING_MODE.wire;
				if (args.containsOption(HEADERS_ARGUMENT)) loggingMode=LOGGING_MODE.headers;
			}
			switch (loggingMode) {
			case headers:
				// do nothing, application.properties has the right defaults
				break;
			case silent:
				wireLogger.setLevel(Level.INFO);
				httpLogger.setLevel(Level.INFO);
				break;
			case wire:
				wireLogger.setLevel(Level.DEBUG);
				httpLogger.setLevel(Level.DEBUG);
				break;

			}
			args.getNonOptionArgs().forEach(this::urlHandler);

		};
	}
	
	private void urlHandler(String url) {
		ResponseEntity<String> resp=restTemplate.getForEntity(url, String.class);
		logger.info("size="+resp.getBody().length());
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
