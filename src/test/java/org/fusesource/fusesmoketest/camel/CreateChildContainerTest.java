package org.fusesource.fusesmoketest.camel;

import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.FabricSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kearls on 07/10/14.
 */
public class CreateChildContainerTest extends SmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(CreateChildContainerTest.class);
    //private static final SSHClient sshClient = new SSHClient();

    @BeforeClass
    public static void init() throws Exception {
        LOG.info(">>>> Creating fabric in init()");
        FabricSupport.createFabric();
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

    /**
     * Create a child container based on the example-camel-mq profile;  It should contain two routes,
     * route1 and route2
     * @throws Exception
     */
    @Test(timeout = 5 * 60 * 1000)
    public void testCreateChildContainer() throws Exception {
        String childContainerName = "camel1-" + System.currentTimeMillis();
        FabricSupport.createChildContainer(childContainerName, "example-camel-mq");
        String response = sshClient.executeCommand("container-list");
        LOG.info(">>> Response0 " + response);
        assertTrue("[" + response + "] should contain container name ]" + childContainerName + "]", response.contains(childContainerName));

        response = FabricSupport.executeCommandOnChild(childContainerName, "camel:route-list");
        LOG.info(">>>>Response 1 [" + response.toString() + "]");
        assertTrue(response.contains("route1"));
        assertTrue(response.contains("route2"));
        assertTrue(response.contains("fabric-camel-demo"));

        response = FabricSupport.executeCommandOnChild(childContainerName, "camel:route-info route1");
        LOG.info(">>>>Response 2 [" + response.toString() + "]");
        assertTrue(response.contains("Inflight Exchanges:"));
        assertTrue(response.contains("Fabric Camel Example:"));

        response = sshClient.executeCommand("container-delete " + childContainerName);
        LOG.info(">>>>Response 4 " + response);
        response = sshClient.executeCommand("container-list");
        assertFalse(response.contains(childContainerName));
    }

}
