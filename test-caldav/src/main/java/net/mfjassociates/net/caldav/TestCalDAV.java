package net.mfjassociates.net.caldav;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.caldav4j.CalDAVConstants;
import com.github.caldav4j.methods.HttpPropFindMethod;
import com.github.caldav4j.util.CalDAVStatus;
import com.github.caldav4j.util.UrlUtils;

public class TestCalDAV {

    private static final Logger log = LoggerFactory.getLogger(TestCalDAV.class);
	public static void main(String[] args) throws IOException, DavException {
		Map<String, Object> info=new HashMap<String, Object>();
        HttpClient http = createHttpClient("https://"+System.getProperty("caldav.user")+":"+System.getProperty("caldav.password")+"@remote.mfjassociates.net/owa/", info);
        http=createHttpClient("https://"+System.getProperty("caldav.user")+":"+System.getProperty("caldav.password")+"@www.google.com/calendar/dav/marioja/", info);
        String collection = "collection_changeme/";
        String collectionPath = UrlUtils.removeDoubleSlashes((String)info.get("home") + collection);
        HttpHost hostConfig = new HttpHost(
                (String)info.get("host"), (int)info.get("port"), (String)info.get("protocol"));

        DavPropertyNameSet set = new DavPropertyNameSet();
        DavPropertyName resourcetype = DavPropertyName.create("resourcetype");
        set.add(resourcetype);
        HttpPropFindMethod propFindMethod =
                new HttpPropFindMethod(
                        collectionPath, set, CalDAVConstants.DEPTH_INFINITY);

        HttpResponse httpResponse = http.execute(hostConfig, propFindMethod);
        MultiStatusResponse[] e =
                propFindMethod.getResponseBodyAsMultiStatus(httpResponse).getResponses();

        for (MultiStatusResponse response : e) {
            DavPropertySet properties = response.getProperties(CalDAVStatus.SC_OK);
            log.info("HREF: " + response.getHref());
            for (DavProperty property : properties) {
                String nodeName = property.getName().toString();
                log.info("nodename: " + nodeName + "\n" + "value: " + property.getValue());
            }
        }
	}
    public static HttpClient createHttpClient(String uri, Map<String, Object> info) {
        // HttpClient 4 requires a Cred providers, to be added during creation of client
    	String user=null, password=null;
        try {
            URI server = new URI(uri);
            String protocol = server.getScheme().replaceAll("://", "");
            info.put("protocol", protocol);
            String host = server.getHost();
            info.put("host", host);
            // port = server.getPort() != -1 ? server.getPort() : (server.getScheme().endsWith("s")
            // ? 443: 80);
            int port = server.getPort(); // !A! For 3.1 ports 80 and 443 were assigned automatically.
            info.put("port", Integer.valueOf(port));
            // But fixing the https port to 443 led to error 502 -
            // Connection refused.
            String home = server.getPath().replace("\\w+/$", "");
            home = UrlUtils.ensureTrailingSlash(home);
            info.put("home", home);
            String userInfo = server.getUserInfo();
            if (userInfo != null) {
                user = userInfo.split(":")[0];
                password = userInfo.split(":")[1];
            }
        } catch (Exception e) {
            // noop
        }
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));

        return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
    }


}
