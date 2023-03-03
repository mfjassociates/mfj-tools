package net.mfjassociates.tools;

import static net.mfjassociates.tools.util.SwitchLoggingLevel.switchLevel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Class to test HTTP transactions using Spring RestTemplate and
 * Apache httpcomponents httpclient.
 * <p>
 * This will use the two configuration classes for RestTemplate and
 * httpclient in the net.mfjassociates.tools.config package.
 * <p>
 * If you want to write your own http client java class and you are
 * not using the net.mfjassociates.tools package then you should use
 * a @ComponentScan annotation for the net.mfjassociates.tools.config
 * package.
 * @author Mario Jauvin
 *
 */
@SpringBootApplication
public class HttpTest {

	private static final int REQUEST_TIMEOUT = 30000;
	private static final int CONNECT_TIMEOUT = 30000;
	private static final int SOCKET_TIMEOUT = 60000;
	private static final Logger logger=LogManager.getLogger(HttpTest.class);
	private static final Logger httpLogger=LogManager.getLogger("org.apache.http");
	private static final Logger springWebClient=LogManager.getLogger("org.springframework.web.client");
	private static final Logger wireLogger=LogManager.getLogger("org.apache.http.wire");
	private static final String SILENT_ARGUMENT = "silent";
	private static final String HEADERS_ARGUMENT = "headers";
	private static final String WIRE_ARGUMENT = "wire";
	private static final String ENV_ARGUMENT = "env";
	private static enum LOGGING_MODE {
		silent, headers, wire;
	}
	private static LOGGING_MODE defaultLoggingMode = LOGGING_MODE.headers;
	@Autowired
	private RestTemplate restTemplate=null;

	public static void main(String[] args) {
		SpringApplication app = null;
		// this will be redone later but it cannot be helped because I need access before creating the context
		// so that the application-{profile}.properties are used to build the context
		ApplicationArguments aArgs=new DefaultApplicationArguments(args);
		SpringApplicationBuilder sab=new SpringApplicationBuilder(HttpTest.class);
		app=processArguments(aArgs, sab).build();
		ConfigurableApplicationContext context = app.run(args);
		context.close();
	}
	
	@Bean
	public ApplicationRunner applrunner(ApplicationArguments theArgs, ConfigurableEnvironment env) {
		return args -> {
			processArguments(theArgs, null);
			args.getNonOptionArgs().forEach(this::urlHandler);

		};
	}
	
	private static final Set<String> validArgs=Stream.of(
			SILENT_ARGUMENT, HEADERS_ARGUMENT, WIRE_ARGUMENT, ENV_ARGUMENT
			).collect(Collectors.toSet());

	/**
	 * This method will process the arguments and create the resulting defaults
	 * 
	 * [--silent|--wire|--headers] <list of urls>
	 * you should use the spring.profiles property to specify the active profile.
	 * the application-{profile}.properties can be used to define the proxy property
	 * configuration
	 * @param args
	 * @param sab SpringApplicationBuilder used to build the application. if it is null handle all
	 * arguments not related to the environment.  If it is not null, only process the environment
	 * related arguments.
	 * @return the SpringApplicationBuilder
	 */
	private static SpringApplicationBuilder processArguments(ApplicationArguments args, SpringApplicationBuilder sab) {
		if (sab==null) { // process non env args
			LOGGING_MODE loggingMode=defaultLoggingMode;
			long mutually=Stream.of(args.containsOption(SILENT_ARGUMENT),
					args.containsOption(HEADERS_ARGUMENT),
					args.containsOption(WIRE_ARGUMENT))
					.filter(b -> b).count();
			if (mutually>1) throw new IllegalArgumentException("Only one of "+optionsList(SILENT_ARGUMENT, HEADERS_ARGUMENT, WIRE_ARGUMENT)+" can be specified");
			if (mutually==1) {
				if (args.containsOption(SILENT_ARGUMENT)) loggingMode=LOGGING_MODE.silent;
				if (args.containsOption(WIRE_ARGUMENT)) loggingMode=LOGGING_MODE.wire;
				if (args.containsOption(HEADERS_ARGUMENT)) loggingMode=LOGGING_MODE.headers;
			}
			String badArgs = optionsList(args.getOptionNames().stream().filter(name -> !validArgs.contains(name)));
			if (!badArgs.isEmpty()) throw new IllegalArgumentException("The following option(s) are invalid: "+badArgs);
			switch (loggingMode) {
			case headers:
				// do nothing, application.properties has the right defaults
				break;
			case silent:
				switchLevel(Level.INFO, wireLogger);
				switchLevel(Level.INFO, httpLogger);
				switchLevel(Level.INFO, springWebClient);
				break;
			case wire:
				switchLevel(Level.DEBUG, wireLogger);
				switchLevel(Level.DEBUG, httpLogger);
				switchLevel(Level.DEBUG, springWebClient);
				break;

			}
		} else { // process env related arguments
			if (args.containsOption(ENV_ARGUMENT)) {
				List<String> theEnv=args.getOptionValues(ENV_ARGUMENT);
				if (theEnv==null || theEnv.size()!=1) throw new IllegalArgumentException("You must specify exactly one value for the --env argument");
				sab.profiles(theEnv.get(0)); // add to active profiles
			}
		}
		return sab;
	}
	
	private static String optionsList(String ...args) {
		return optionsList(Stream.of(args));
	}
	private static String optionsList(Stream<String> ss) {
		Set<String> resultSet = ss.map(a -> "--"+a).collect(Collectors.toSet());
		if (resultSet.isEmpty()) return "";
		else return resultSet.toString();
	}
	
	private void urlHandler(String url) {
		ResponseEntity<String> resp=restTemplate.getForEntity(url, String.class);
		logger.info("size="+resp.getBody().length());
	}
	
//	@Bean
	public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory hcchrf) {
		return new RestTemplate(hcchrf);
	}
	
//	@Bean
	public CloseableHttpClient chc() {
		RequestConfig rConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(REQUEST_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT).build();
		return HttpClients.custom()
				.setDefaultRequestConfig(rConfig)
				.build();
	}
//	@Bean
	public HttpComponentsClientHttpRequestFactory chrf(CloseableHttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory chrf = new HttpComponentsClientHttpRequestFactory();
		chrf.setHttpClient(httpClient);
		return chrf;
	}

}
