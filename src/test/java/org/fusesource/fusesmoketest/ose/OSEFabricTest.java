package org.fusesource.fusesmoketest.ose;

import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.FabricSupport;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class OSEFabricTest extends OSESmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(OSEFabricTest.class);

    @Before
    public void setUp() throws Exception {
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        sshClient.disconnect();
    }

    @Test(timeout = 30 * 1000)
    public void cryptoAlgorithmCommandTest() throws Exception {
        String defaultCryptoAlgorithmName = "PBEWithMD5AndDES";
        String newCryptoAlgorithmName = "SHA-1";
        String response = sshClient.executeCommand("fabric:crypt-algorithm-get ");
        LOG.info(">>>> response: [" + response + "]");
        assertEquals(defaultCryptoAlgorithmName, response.trim());

        response = sshClient.executeCommand("fabric:crypt-algorithm-set " + newCryptoAlgorithmName);
        LOG.info(">>>> set returned " + response.trim());

        response = sshClient.executeCommand("fabric:crypt-algorithm-get ");
        LOG.info(">>>> response: [" + response + "]");
        assertEquals(newCryptoAlgorithmName, response.trim());

        // reset
        sshClient.executeCommand("fabric:crypt-algorithm-set " + defaultCryptoAlgorithmName);
    }


    @Test(timeout = 30 * 1000)
    public void cryptPasswordTest() throws Exception {
        String newPassword = "newPassword";
        String defaultPassword = FUSE_PASSWORD;

        String response = sshClient.executeCommand("fabric:crypt-password-get ");
        assertEquals(defaultPassword, response.trim());

        sshClient.executeCommand("fabric:crypt-password-set " + newPassword);

        response = sshClient.executeCommand("fabric:crypt-password-get ");
        assertEquals(newPassword, response.trim());

        // Set it back...
        sshClient.executeCommand("fabric:crypt-password-set " + defaultPassword);
    }

    @Test(timeout = 5 * 60 * 1000)
    public void createAndDeleteContainer() throws Exception {
        String newContainerName="newtestcontainer" + System.currentTimeMillis();
        String response= sshClient.executeCommand("container-list");
        LOG.info(">>>> Response from first list " + response);

        assertFalse(response.contains(newContainerName));
        createOpenshiftContainer(OSE_USERNAME, OSE_PASSWORD, null, newContainerName);
        response=sshClient.executeCommand("container-list " + newContainerName);
        LOG.info(">>>> Response from container-list [{}]",response);
        String response2=sshClient.executeCommand("container-info " + newContainerName);
        LOG.info(">>>> Response from container-info {} [{}]", newContainerName, response2);

        assertTrue(response.contains(newContainerName));
        assertTrue(response.contains("success"));

        response=sshClient.executeCommand("container-delete " + newContainerName);
        LOG.info(">>>> Response from delete " + response);
        response=sshClient.executeCommand("container-list  " + newContainerName);
        LOG.info(">>>> Response after delete " + response);

        assertFalse(response.contains(newContainerName));
    }

    @Test(timeout = 30 * 1000)
    public void displayProfileTest() throws Exception {
        String response = sshClient.executeCommand("profile-display mq-amq");
        assertTrue(response.contains("parents: mq-base"));
        assertTrue(response.contains("Resource: broker.xml"));

        response = sshClient.executeCommand("profile-display --display-resources mq-amq");
        assertTrue(response.contains("the profile which runs a full JBoss A-MQ distribution and starts the broker on 61616 port"));
    }

    @Test(timeout = 60 * 1000)
    public void testProfileCreateModifyDelete() throws Exception  {
        String testProfileName = "test-profile-" + System.currentTimeMillis();

        String response = sshClient.executeCommand("profile-create " + testProfileName);
        response = sshClient.executeCommand("profile-display " + testProfileName);
        assertTrue(response.contains(testProfileName));
        assertFalse(response.contains("camel"));

        response = sshClient.executeCommand("profile-list | grep " + testProfileName);
        assertTrue(response.contains(testProfileName));

        sshClient.executeCommand("profile-edit --feature camel "+ testProfileName);
        response = sshClient.executeCommand("profile-display " + testProfileName);
        assertTrue(response.contains("camel"));

        sshClient.executeCommand("profile-edit --delete --feature camel " + testProfileName);
        response = sshClient.executeCommand("profile-display " + testProfileName);
        assertFalse(response.contains("camel"));

        // Cleanup
        sshClient.executeCommand("profile-delete " + testProfileName);
    }

    @Ignore("FIXME")
    @Test(timeout = 5 * 60 * 1000)
    public void testProfileAssignModify()  throws Exception {
        String testContainerName = "test-container-" + System.currentTimeMillis();

        String testProfileName = "test-profile-" + System.currentTimeMillis();

       // sshClient.createChildContainer(testContainerName,"",true);
       // sshClient.waitForProvision(testContainerName,true);

        sshClient.executeCommand("profile-create "+ testProfileName);
        sshClient.executeCommand("container-add-profile "+ testContainerName + " " + testProfileName);
        sshClient.executeCommand("profile-edit --feature fabric-zookeeper-commands " + testProfileName);

        //sshClient.waitForProvision(testContainerName,true);

        Thread.sleep(5000);

        String zk= sshClient.executeCommand("container-connect " + testContainerName + " zk:list");
        LOG.info(">>>>> ContainerName? [{}]", testContainerName);
        LOG.info(">>>>> ZK [{}]", zk);
        assertTrue("[" + zk + "] does not contains fabric", zk.contains("fabric"));
        assertTrue("does not contains zookeeper", zk.contains("zookeeper"));

        sshClient.executeCommand("container-delete " + testContainerName);
        String clean = sshClient.executeCommand("container-list | grep " + testContainerName );

        assertFalse("container shouldn't exist", clean.contains(testContainerName));
    }

}
