package net.mfjassociates.tools.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 */
/**
 * - Supports both HTTP and HTTPS
 * - Uses a connection pool to re-use connections and save overhead of creating connections.
 * - Has a custom connection keep-alive strategy (to apply a default keep-alive if one isn't specified)
 * - Starts an idle connection monitor to continuously clean up stale connections.
 * 
 * @author mxj037
 *
 */
@Configuration
@EnableScheduling
//@PropertySource(ignoreResourceNotFound=true, value="classpath:HttpClientConfig-${spring.profiles.active}.properties")
public class HttpClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientConfig.class);

    // Determines the timeout in milliseconds until a connection is established.
    private static final int CONNECT_TIMEOUT = 30000;
    
    // The timeout when requesting a connection from the connection manager.
    private static final int REQUEST_TIMEOUT = 30000;
    
    // The timeout for waiting for data
    private static final int SOCKET_TIMEOUT = 60000;

    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;
    private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS = 30;

	public static enum PROXY_AUTHENTICATION_MODE {
		ntlm, userPassword, direct;
//		public static AUTHENTICATION_MODE getMode(String sMode) {
//			return Arrays.stream(AUTHENTICATION_MODE.values()).filter(v -> sMode.contentEquals(v.name())).findFirst().get();
//		}
	}
    
    
    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            logger.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error("Pooling Connection Manager Initialisation failure because of " + e.getMessage(), e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        return poolingConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();

                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return DEFAULT_KEEP_ALIVE_TIME_MILLIS;
            }
        };
    }

    @Bean
    @DependsOn("applrunner")
    public CloseableHttpClient httpClient(@Autowired(required=false) HttpHost proxy, @Autowired(required=false) NTCredentials credential, @Qualifier("proxyAuthenticationMode") PROXY_AUTHENTICATION_MODE aProxyAuthenticationMode) {
        HttpClientBuilder hcb=null;
        switch (aProxyAuthenticationMode) {
		case ntlm:
			hcb = WinHttpClients.custom()
					.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
			break;

		case userPassword:
			CredentialsProvider credProv = new BasicCredentialsProvider();
			credProv.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), credential);
			hcb = HttpClients.custom()
					.setDefaultCredentialsProvider(credProv)
					.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
			break;
		
		case direct:
			hcb = HttpClients.custom();
			break;

		}
        return common(hcb).build();
    }

    private HttpClientBuilder common(HttpClientBuilder httpClient) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();

    	return httpClient
              .setDefaultRequestConfig(requestConfig)
              .setConnectionManager(poolingConnectionManager())
              .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
              .setKeepAliveStrategy(connectionKeepAliveStrategy());
    }
    @Value("${defaultProxyUser:}")
    private String defaultProxyUser;
    @Value("${defaultProxyPassword:}")
    private String defaultProxyPassword;
    @Value("${defaultProxyDomain:}")
    private String defaultProxyDomain;

    /**
     * You need to have a property proxyAuthenticationMode set to the value of the enumeration as the default for this application
     * @param args
     * @param aMode
     * @return
     */
	@Bean
	@DependsOn("applrunner")
	public PROXY_AUTHENTICATION_MODE proxyAuthenticationMode(ApplicationArguments args, @Value("#{ T(net.mfjassociates.tools.config.HttpClientConfig.PROXY_AUTHENTICATION_MODE).${proxyAuthenticationMode} }") PROXY_AUTHENTICATION_MODE aMode) {
		return aMode;
	}

//	@Autowired
//    @Qualifier("proxyAuthenticationMode")
//    private PROXY_AUTHENTICATION_MODE proxyAuthenticationMode;
    
	@Bean
	@DependsOn("applrunner")
	public NTCredentials ntCredential(@Qualifier("proxyAuthenticationMode") PROXY_AUTHENTICATION_MODE proxyAuthenticationMode) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Default proxy credentials: user:{}, password:{}, domain:{}", defaultProxyUser, defaultProxyPassword.replaceAll(".", "*"), defaultProxyDomain);
		}
		NTCredentials credential = null;
		if (proxyAuthenticationMode==PROXY_AUTHENTICATION_MODE.userPassword) credential=new NTCredentials(defaultProxyUser, defaultProxyPassword, null, defaultProxyDomain);
		return credential;
	}
	@Bean
	public PROXY_AUTHENTICATION_MODE defaultProxyAuthenticationMode() {
		return PROXY_AUTHENTICATION_MODE.direct;
	}
	@Value("${defaultProxyHost:}")
	private String defaultProxyHost=null;
	@Value("${dfaultProxyPort:0}")
	private int defaultProxyPort;

	@Bean
	public HttpHost proxy() {
		return new HttpHost(defaultProxyHost, defaultProxyPort);
	}

	@Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (connectionManager != null) {
                        logger.trace("run IdleConnectionMonitor - Closing expired and idle connections...");
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS, TimeUnit.SECONDS);
                    } else {
                        logger.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                } catch (Exception e) {
                    logger.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
                }
            }
        };
    }

}
