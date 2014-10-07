package org.fusesource.fusesmoketest.hawtio;

import com.jcraft.jsch.JSchException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class HawtioTest {
    protected static final Logger LOG = LoggerFactory.getLogger(HawtioTest.class);
    @Rule
    public TestName testName = new TestName();

    protected static String FUSE_USER ="admin";
    protected static String FUSE_PASSWORD ="admin";

    private static final SSHClient sshClient = new SSHClient();
    private static final String HAWTIO_LOGIN_URL = "http://localhost:8181/hawtio/login/";

    private HttpHost host = null;
    private HttpClientContext localContext = null;
    private AuthCache authCache = null;
    private CloseableHttpClient httpClient;

    @BeforeClass
    public static void init() throws Exception {
        FUSE_USER = System.getProperty("FUSE_USER", FUSE_USER);
        FUSE_PASSWORD = System.getProperty("FUSE_PASSWORD", FUSE_PASSWORD);
    }


    @Before
    public void setUp() throws Exception {
        LOG.info("Starting test " + testName.getMethodName());
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        sshClient.disconnect();
    }

    @Test(timeout = 30 * 1000)
    public void testHawtioConsoleLogin() throws IOException, InterruptedException {
        setUpHttpClient(HAWTIO_LOGIN_URL, "admin", "admin");  // TODO pickup from command line like in AMQ tests
        HttpResponse response = executePost(httpClient, HAWTIO_LOGIN_URL, "admin", "admin");
        StatusLine statusLine = response.getStatusLine();
        assertEquals(200, statusLine.getStatusCode());
        assertEquals("OK", statusLine.getReasonPhrase());
    }


    public CloseableHttpClient setUpHttpClient(String urlString, String username, String password) {
        URI uri = URI.create(urlString);
        host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        UsernamePasswordCredentials usernamePasswordCredentials =  new UsernamePasswordCredentials(username, password);
        CredentialsProvider credentialProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(uri.getHost(), uri.getPort());
        credentialProvider.setCredentials(authScope, usernamePasswordCredentials);
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicScheme = new BasicScheme();
        authCache.put(host, basicScheme);
        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.setDefaultCredentialsProvider(credentialProvider);
        httpClient = clientBuilder.build();

        return httpClient;
    }


    public HttpResponse executePost(CloseableHttpClient httpClient, String urlString, String username, String password) throws ClientProtocolException, IOException {
        URI uri = URI.create(urlString);

        HttpPost httpPost = new HttpPost(uri);
        localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        HttpResponse response = httpClient.execute(host, httpPost, localContext);
        return response;
    }

    // FIXME  we have the same thing in FabricSupport, put it in one place
    private static void sshInit() throws JSchException, InterruptedException {
        int MAX_ATTEMPTS = 100;
        int attempt = 0;

        while(attempt < MAX_ATTEMPTS){
            try{
                sshClient.init();
                if(sshClient.isConnected()) {      // TODO is it possible to get here and not be connected?
                    return;
                }
            } catch (JSchException e ){
                LOG.info("Connect failed with " + e.getMessage() + ", retrying...");
            }

            Thread.sleep(1000);
            attempt++;
        }

    }


}
