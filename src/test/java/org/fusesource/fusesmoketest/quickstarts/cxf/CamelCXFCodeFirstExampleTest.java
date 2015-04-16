package org.fusesource.fusesmoketest.quickstarts.cxf;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple integration test for the camel-cxf-code-first quickstart.  This code is mostly taken from the test found here:
 * https://github.com/jboss-fuse/fabric8/blob/1.2.0.redhat-6-2-x-patch/quickstarts/cxf/camel-cxf-code-first/src/test/java/camelinaction/order/OrderTest.java
 */
public class CamelCXFCodeFirstExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(CamelCXFCodeFirstExampleTest.class);

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

    @Test
    public void sendRequest() throws Exception {

        String res;
        /*
         * Set up the URL connection to the web service address
         */
        URLConnection connection = new URL("http://localhost:8181/cxf/orderCode").openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);

        /*
         * We have prepared a SOAP request in an XML file, so we send the contents of that file to our web service...
         */
        OutputStream os = connection.getOutputStream();
        InputStream fis = CamelCXFCodeFirstExampleTest.class.getResourceAsStream("/cxf-code-first-request.xml");
        copyInputStream(fis, os);

        /*
         * ... and afterwards, we just read the SOAP response message that is sent back by the server.
         */
        InputStream is = connection.getInputStream();
        LOG.info("the response is ====> ");
        res = getStringFromInputStream(is);
        LOG.info(res);
        Assert.assertTrue(res.contains("OK"));
    }
}
