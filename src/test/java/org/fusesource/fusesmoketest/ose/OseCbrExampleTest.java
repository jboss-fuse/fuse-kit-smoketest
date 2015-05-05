package org.fusesource.fusesmoketest.ose;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static  org.junit.Assert.*;

/**
 * Created by openshift on 4/17/15.
 */
public class OseCbrExampleTest extends OSESmokeTestBase {

    protected static final Logger LOG = LoggerFactory.getLogger(OseCbrExampleTest.class);

    @Before
    public void setUp() throws Exception {
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        sshClient.disconnect();
    }


    @Test(timeout = 5 * 60 * 1000)
    /**
     * Create a container based on the cbr quickstart, copy files as specified in the README, and verify that they got copied
     *
     * This version differs from the normal CBR quickstat test of file permissions in openshift; you need to use
     * sudo to access the work directories
     *
     */
    public void testCreateCBRExample()  throws Exception {
        String testContainerName = "test" + System.currentTimeMillis();

        String result = createOpenshiftContainer(OSE_USERNAME, OSE_PASSWORD, "quickstarts-beginner-camel.cbr", testContainerName);
        LOG.info("CreateOpenshiftContainer Result: " + result);

        // Copy the input files here, we need sudo because of openshift file protections
        String gearUUID = getOpenShiftGearUuid(testContainerName);
        String quickstartDataDirectory = "/var/lib/openshift/" + gearUUID
                + "/fuse/container/quickstarts/beginner/camel-cbr/src/main/fabric8/data/";
        String quickStartWorkInputDirectory = "/var/lib/openshift/" + gearUUID + "/fuse/container/work/cbr/input/";
        for (int i=1; i <=5; i++) {
            String command = "sudo cp " + quickstartDataDirectory + "order" + i + ".xml " + quickStartWorkInputDirectory;
            executeShellCommand(command);
        }

        Thread.sleep(2 * 1000);

        // Make sure they got copied
        String quickStartWorkOutputDirectory = "/var/lib/openshift/" + gearUUID + "/fuse/container/work/cbr/output/";
        List<String> usFiles = executeShellCommand("sudo ls " + quickStartWorkOutputDirectory + "us/");
        List<String> ukFiles = executeShellCommand("sudo ls " + quickStartWorkOutputDirectory + "uk/");
        List<String> otherFiles = executeShellCommand("sudo ls " + quickStartWorkOutputDirectory + "others/");

        assertEquals("Wrong number of files in US directory", 2, usFiles.size());
        assertEquals("Wrong number of files in UK directory", 2, ukFiles.size());
        assertEquals("Wrong number of files in others directory", 1, otherFiles.size());

        // Cleanup
        sshClient.executeCommand("container-delete " + testContainerName);
        String infoResult = sshClient.executeCommand("container-info " + testContainerName);
        assertEquals("container-delete may have failed", "Container " + testContainerName + " does not exists!", infoResult.trim());
    }


}
