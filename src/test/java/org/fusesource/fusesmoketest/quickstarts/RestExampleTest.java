package org.fusesource.fusesmoketest.quickstarts;

import org.fusesource.fusesmoketest.quickstarts.utils.TestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Test the rest quickstart.
 * 
 * @author kearls
 *
 */
public class RestExampleTest extends FuseSmokeTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(RestExampleTest.class);
    public static final String CUSTOMERSERVICE_URL = EXAMPLES_URL_BASE + "/crm/customerservice/customers/123";
    public static final String PRODUCT_ORDER_TEST_URL = EXAMPLES_URL_BASE + "/crm/customerservice/orders/223/products/323";
    public static final String CUSTOMER_SERVICE_URL = EXAMPLES_URL_BASE + "/crm/customerservice/customers";

	/**
	 * Test GET of customer 123.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCustomer123() throws Exception {
		URL url = new URL(EXAMPLES_URL_BASE + "/crm/customerservice/customers/123");
		InputStream in = url.openStream();
		String result = TestUtils.getStringFromInputStream(in);
        LOG.info("Result was [" + result + "]");
        assertTrue(result.contains("<id>123</id>"));
        assertTrue(result.contains("<name>John</name>"));

	}


	/**
	 * Test getting product 323 in order 223
	 * On the server side, it matches the Order's getProduct() method
	 */
	@Test
	public void testGetOrders223Product323() throws Exception {
		URL url = new URL(EXAMPLES_URL_BASE + "/crm/customerservice/orders/223/products/323");
		InputStream in = url.openStream();
		String result = TestUtils.getStringFromInputStream(in);

        LOG.info("Result was [" + result + "]");
        assertTrue(result.contains("<description>product 323</description>"));
        assertTrue(result.contains("<id>323</id>"));

	}
}
