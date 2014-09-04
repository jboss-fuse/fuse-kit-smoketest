package org.fusesource.fusesmoketest.quickstarts;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;

public class SoapExampleTest extends FuseSmokeTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(SoapExampleTest.class);

    @Ignore("https://issues.jboss.org/browse/ENTESB-1833")
    @Test
	public void testSimple() throws Exception {
		// Set up a connection to the web service
		URL url = new URL(EXAMPLES_URL_BASE + "/HelloWorld");
		URLConnection urlConnection = url.openConnection();
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);

		// This sends a soap request "sayHi" to "John Doe"
		OutputStream os = urlConnection.getOutputStream();
		InputStream fis =  this.getClass().getClassLoader().getResourceAsStream("request.xml");
		copyInputStream(fis, os);

		// Validate the response
		InputStream is = urlConnection.getInputStream();
		String response = getStringFromInputStream(is);
        LOG.info("Response: " + response);
		String expectedResponse = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:sayHiResponse xmlns:ns2=\"http://jaxws.cxf.quickstarts.fusesource.org/\"><return>Hello John Doe</return></ns2:sayHiResponse></soap:Body></soap:Envelope>";
		assertEquals(expectedResponse, response);
	}


	/**
     * Helper method to copy bytes from an InputStream to an OutputStream.
     */
    private static void copyInputStream(InputStream in, OutputStream out) throws Exception {
        int c = 0;
        try {
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } finally {
            in.close();
        }
    }

    /**
     * Helper method to read bytes from an InputStream and return them as a String.
     */
    private static String getStringFromInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyInputStream(in, bos);
        bos.close();
        return bos.toString();
    }

}
