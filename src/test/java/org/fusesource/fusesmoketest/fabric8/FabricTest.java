package org.fusesource.fusesmoketest.fabric8;

import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.FabricSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricTest extends SmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(FabricTest.class);

    @BeforeClass
    public static void init() throws Exception {
        LOG.info(">>>> Creating fabric in init()");
        FabricSupport.createFabric();
    }

    @Before
    public void setUp() throws Exception {
        FabricSupport.sshInit();
    }

    @After
    public void tearDown() throws Exception {
        FabricSupport.disconnect();
    }

    @Test(timeout = 30 * 1000)
    public void cryptoAlgorithmCommandTest() throws Exception {
        String defaultCryptoAlgorithmName = "PBEWithMD5AndDES";
        String newCryptoAlgorithmName = "SHA-1";
        String response = FabricSupport.executeCommand("fabric:crypt-algorithm-get ").trim();
        LOG.info(">>>> response: [" + response + "]");
        assertTrue("Expected response to include " + defaultCryptoAlgorithmName, response.endsWith(defaultCryptoAlgorithmName));

        response = FabricSupport.executeCommand("fabric:crypt-algorithm-set " + newCryptoAlgorithmName).trim();
        LOG.info(">>>> set returned [" + response + "]");

        response = FabricSupport.executeCommand("fabric:crypt-algorithm-get ").trim();
        LOG.info(">>>> response: [" + response + "]");
        assertTrue("Expected response to include " + newCryptoAlgorithmName, response.endsWith(newCryptoAlgorithmName));

        // reset
        FabricSupport.executeCommand("fabric:crypt-algorithm-set " + defaultCryptoAlgorithmName);
    }


    @Test(timeout = 30 * 1000)
    public void cryptPasswordTest() throws Exception {
        String newPassword = "newPassword";
        String defaultPassword = "admin";

        String response = FabricSupport.executeCommand("fabric:crypt-password-get ");
        LOG.info(">>>> response: [" + response + "]");
        assertStringContains(response.trim(), defaultPassword);


        FabricSupport.executeCommand("fabric:crypt-password-set " + newPassword);

        response = FabricSupport.executeCommand("fabric:crypt-password-get ");
        assertStringContains(response.trim(), newPassword);

        // Set it back...
        FabricSupport.executeCommand("fabric:crypt-password-set " + defaultPassword);
    }


    @Test(timeout = 5 * 60 * 1000)
    public void createAndDeleteContainer() throws Exception {
        String newContainerName="newtestcontainer" + System.currentTimeMillis();
        String response= FabricSupport.executeCommand("container-list");
        LOG.info(">>>> Response from first list " + response);

        assertFalse(response.contains(newContainerName));
        FabricSupport.createChildContainer(newContainerName);
        response=FabricSupport.executeCommand("container-list " + newContainerName);
        LOG.info(">>>> Response after create [{}]",response);
        String response2=FabricSupport.executeCommand("container-info " + newContainerName);
        LOG.info(">>>> Response after create-2 [{}]", response2);

        assertTrue(response.contains(newContainerName));
        assertTrue(response.contains("success"));

        response=FabricSupport.executeCommand("container-delete " + newContainerName);
        LOG.info(">>>> Response from delete " + response);
        response=FabricSupport.executeCommand("container-list  " + newContainerName);
        LOG.info(">>>> Response after delete " + response);

        assertFalse(response.contains(newContainerName));
    }

    @Test(timeout = 30 * 1000)
    public void displayProfileTest() throws Exception {
        String response = FabricSupport.executeCommand("profile-display mq-amq");
        assertTrue(response.contains("parents: mq-base"));
        assertTrue(response.contains("Resource: broker.xml"));

        response = FabricSupport.executeCommand("profile-display --display-resources mq-amq");
        assertTrue(response.contains("the profile which runs a full JBoss A-MQ distribution and starts the broker on 61616 port"));
    }

    @Test(timeout = 60 * 1000)
    public void testProfileCreateModifyDelete() throws Exception  {
        String testProfileName = "test-profile-" + System.currentTimeMillis();

        String response = FabricSupport.executeCommand("profile-create " + testProfileName);
        response = FabricSupport.executeCommand("profile-display " + testProfileName);
        assertTrue(response.contains(testProfileName));
        assertFalse(response.contains("camel"));

        response = FabricSupport.executeCommand("profile-list | grep " + testProfileName);
        assertTrue(response.contains(testProfileName));

        FabricSupport.executeCommand("profile-edit --feature camel "+ testProfileName);
        response = FabricSupport.executeCommand("profile-display " + testProfileName);
        assertTrue(response.contains("camel"));

        FabricSupport.executeCommand("profile-edit --delete --feature camel " + testProfileName);
        response = FabricSupport.executeCommand("profile-display " + testProfileName);
        assertFalse(response.contains("camel"));

        // Cleanup
        FabricSupport.executeCommand("profile-delete " + testProfileName);
    }


    @Test(timeout = 5 * 60 * 1000)
    public void testProfileAssignModify()  throws Exception {
        String testContainerName = "test-container-" + System.currentTimeMillis();

        String testProfileName = "test-profile-" + System.currentTimeMillis();

        FabricSupport.createChildContainer(testContainerName,"",true);
        FabricSupport.waitForProvision(testContainerName,true);

        FabricSupport.executeCommand("profile-create "+ testProfileName);
        FabricSupport.executeCommand("container-add-profile "+ testContainerName + " " + testProfileName);
        FabricSupport.executeCommand("profile-edit --feature fabric-zookeeper-commands " + testProfileName);

        FabricSupport.waitForProvision(testContainerName,true);

        Thread.sleep(5000);

        String zk= FabricSupport.executeCommand("container-connect " + testContainerName + " zk:list");
        LOG.info(">>>>> ContainerName? [{}]", testContainerName);
        LOG.info(">>>>> ZK [{}]", zk);
        assertTrue("[" + zk + "] does not contains fabric", zk.contains("fabric"));
        assertTrue("does not contains zookeeper", zk.contains("zookeeper"));

        FabricSupport.executeCommand("container-delete " + testContainerName);
        String clean = FabricSupport.executeCommand("container-list | grep " + testContainerName );

        assertFalse("container shouldn't exist", clean.contains(testContainerName));
    }

}
