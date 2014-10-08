package org.fusesource.fusesmoketest.hawtio;

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
import org.fusesource.fusesmoketest.SmokeTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class HawtioTest extends SmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(HawtioTest.class);
    @Rule
    public TestName testName = new TestName();
    private static final String HAWTIO_LOGIN_URL = "http://localhost:8181/hawtio/login/";
    private HttpHost host = null;
    private HttpClientContext localContext = null;
    private AuthCache authCache = null;
    private CloseableHttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        LOG.info("Starting test " + testName.getMethodName());
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        sshClient.disconnect();
        super.tearDown();
    }


    @Test(timeout = 30 * 1000)
    public void testHawtioConsoleLogin() throws IOException, InterruptedException {
        setUpHttpClient(HAWTIO_LOGIN_URL, FUSE_USER, FUSE_PASSWORD);
        HttpResponse response = executePost(httpClient, HAWTIO_LOGIN_URL, FUSE_USER, FUSE_PASSWORD);
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
}
