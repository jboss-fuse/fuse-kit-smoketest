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
        //FabricSupport.createFabric();
        FabricSupport.sshInit();
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
     * Create a child container based on the feature-camel and cbr quickstart profile;  Verify that it contains
     * cbr-route, and check the response of cambel:route-info on cbr-route
     *
     * To reproduce manually, do the following:
     *
     * container-create-child --profile feature-camel --profile quickstarts-beginner-camel.cbr  root camel1-1463654880913
     * -- wait for provisioning
     * container-connect camel1-1463654880913 camel:route-list
     * container-connect camel1-1463654880913 camel:route-info cbr-route
     *
     * @throws Exception
     */
    @Test(timeout = 5 * 60 * 1000)
    public void testCreateChildContainer() throws Exception {
        String childContainerName = "camel1-" + System.currentTimeMillis();
        FabricSupport.createChildContainer(childContainerName, true, "feature-camel", "quickstarts-beginner-camel.cbr");
        String response = sshClient.executeCommand("container-list");
        LOG.info(">>> Response0 " + response);
        assertTrue("[" + response + "] should contain container name ]" + childContainerName + "]",
                response.contains(childContainerName));

        response = FabricSupport.executeCommandOnChild(childContainerName, "camel:route-list");
        LOG.info(">>>>Response 1 [" + response.toString() + "]");
        assertTrue("camel:route-list for " + childContainerName + " did not return cbr-route ", response.contains("cbr-route"));

        response = FabricSupport.executeCommandOnChild(childContainerName, "camel:route-info cbr-route");
        LOG.info(">>>>Response 2 [" + response.toString() + "]");
        assertTrue("For " + childContainerName + " camel:route-info cbr-route did not container Exchanges Inflight",
                response.contains("Exchanges Inflight:"));
        assertTrue("For " + childContainerName + " camel:route-info cbr-route did not container cbr-example-context",
                response.contains("cbr-example-context"));
    }
}
