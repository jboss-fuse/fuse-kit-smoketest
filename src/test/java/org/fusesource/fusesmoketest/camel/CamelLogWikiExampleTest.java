package org.fusesource.fusesmoketest.camel;

import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.FabricSupport;
import org.fusesource.fusesmoketest.utils.TailLog;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the camel-log-wiki quickstart.  Unlike many of the quickstart tests, this one
 * requires a fabric, which is why it is in this package rather than the quickstarts
 * packages.  TODO: create a quickstarts with fabric package?
 *
 */
public class CamelLogWikiExampleTest extends SmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(CamelLogWikiExampleTest.class);
    protected static String FUSE_HOME ="";
    protected String childContainerName = "camel1-" + System.currentTimeMillis();

    @BeforeClass
    public static void init() throws Exception {
        LOG.info(">>>> Creating fabric in init()");
        FabricSupport.createFabric();       // TODO do we need this?
        FUSE_HOME = System.getProperty("FUSE_HOME");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("Starting test " + testName.getMethodName());
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        FabricSupport.removeContainer(childContainerName);
        sshClient.disconnect();
    }

    /**
     * Create a child container based on the example-camel-mq profile;  It should contain two routes,
     * route1 and route2
     * @throws Exception
     */
    @Test(timeout = 5 * 60 * 1000)
    public void testCreateChildContainer() throws Exception {

        FabricSupport.createChildContainer(childContainerName, "quickstarts-beginner-camel.log.wiki");
        String response = sshClient.executeCommand("container-list");
        LOG.info(">>> Response0 " + response);
        assertTrue("[" + response + "] should contain container name ]" + childContainerName + "]", response.contains(childContainerName));

        int expectedLogMessages = 5;
        int iteration = 0;
        int foundLogMessages = 0;

        while (foundLogMessages < expectedLogMessages && iteration < 10) {
            foundLogMessages = TailLog.tailAndSearchLog(FUSE_HOME + "/instances/" + childContainerName + "/data/log/fuse.log", expectedLogMessages, 5000, "Hello from Fabric based Camel route"); // and log-route?
            LOG.info(">>>> received " + foundLogMessages + " messages on iteration " + iteration);
            iteration++;
        }


        assertEquals("Expected " + expectedLogMessages + " log messages", expectedLogMessages, foundLogMessages);
    }

}
