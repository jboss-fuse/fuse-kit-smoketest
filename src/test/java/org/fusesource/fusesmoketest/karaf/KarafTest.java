package org.fusesource.fusesmoketest.karaf;

import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.FabricSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KarafTest extends SmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(KarafTest.class);
    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void init() throws Exception {
        LOG.info(">>>> Creating fabric in init()");
        FabricSupport.createFabric();
    }

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

    @Ignore
    @Test(timeout = 30 * 1000)
    public void testBrokerStarted() throws Exception {
        String response = sshClient.executeCommand("activemq:list");
        LOG.info("RESPONSE : [" + response);
        LOG.info(response.replaceAll("(\\r|\\n)", ""));             // What is this?
        assertEquals("FUSE should contain running activemq broker ", "brokerName = amq", response.replaceAll("(\\r|\\n)", ""));
    }


    @Test(timeout = 30 * 1000)
    public void testFeaturesList() throws Exception {
        String response = sshClient.executeCommand("features:list");
        LOG.debug(response);
        assertNotNull("Command features:list should return list of available features", response);
        assertTrue("FUSE should contain hawtio feature", response.contains("hawtio"));
        assertTrue("FUSE should contain spring feature", response.contains("spring"));
        assertTrue("FUSE should contain fabric feature", response.contains("fabric"));
        assertTrue("FUSE should contain camel feature", response.contains("camel"));
        assertTrue("FUSE should contain cxf feature", response.contains("cxf"));
    }


    @Test(timeout = 30 * 1000)
    public void testOsgiList() throws Exception {
        String[] bundleNameStrings = {"Fabric8 :: Karaf Commands", "activemq-karaf", "camel-core", "hawtio :: hawtio-web"};
        List<String> bundleNames = Arrays.asList(bundleNameStrings);
        for (String bundleName : bundleNames) {
            String response = sshClient.executeCommand("osgi:list | grep '" + bundleName + "'");
            LOG.debug(">>>> Response for [" + bundleName + " ] is: " + response);
            assertTrue("FUSE should contain \"Active\" bundle " + bundleName, response.contains("Active"));
        }
    }


    @Test(timeout = 30 * 1000)
    public void testCommandsExist() throws Exception {
        String[] commandStrings = {"activemq", "camel", "config", "cxf", "dev", "fab", "fabric",
                "features", "help", "jaas", "log", "osgi", "packages", "patch","scr", "shell", "ssh"};
        List<String> commands = Arrays.asList(commandStrings);
        List<String> failures = new ArrayList<String>();
        String response = sshClient.executeCommand("*:help");
        LOG.debug(">>>>> RESPONSE [" + response + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        for (String command : commands) {
            if (!response.contains(command)) {
                LOG.debug(">>>> FAILED: " + command);
                failures.add(command);
            }
        }

        StringBuilder builder = new StringBuilder();
        if (!failures.isEmpty()) {
            builder.append("Command(s) [");
            for (String command : failures) {
                LOG.info("COMMAND [" + command + " ] not found");
                builder.append(command + " ");
            }
            builder.append("] not found");
        }

        assertEquals(builder.toString(), 0, failures.size());
    }

    @Test(timeout = 30 * 1000)
    public void testFabricCreateCommandExists() throws Exception {
        String response = sshClient.executeCommand("*:help | grep fabric:create");
        LOG.info(response);
        assertNotNull("Started FUSE should contain 'fabric:create' command", response);
        assertTrue("Started FUSE should contain 'fabric:create' command, but response was: " + response,
                response.contains("fabric:create"));
    }
}
