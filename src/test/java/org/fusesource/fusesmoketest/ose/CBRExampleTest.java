package org.fusesource.fusesmoketest.ose;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by openshift on 4/17/15.
 */
public class CBRExampleTest extends OSESmokeTestBase {
    @Before
    public void setUp() throws Exception {
        sshInit();
    }

    @After
    public void tearDown() throws Exception {
        sshClient.disconnect();
    }


    public String tryCommand(String command) throws Exception {
        System.out.println("---------------------------------------------------------------");
        System.out.println("EXECUTING [" + command + "]");
        String result = sshClient.executeCommand(command);
        System.out.println(result);
        System.out.println("-----------------------------------------------------------------");
        return result;
    }

    @Test(timeout = 5 * 60 * 1000)
    /**
     * Create a container based on the cbr quickstart
     * Verify that it was created and is running
     * Copy *xml files from ??? to work input directory
     * Verify that files show up in the work output directory
     * Delete container
     * Verify that the container got deleted
     *
     */
    public void testCreateCBRExample()  throws Exception {
        //
        String containerName = "test" + System.currentTimeMillis();

        String result = createOpenshiftContainer(OSE_USERNAME, OSE_PASSWORD, "quickstarts-beginner-camel.cbr", containerName);
        System.out.println("------------------------------------------------------------------");
        System.out.println(result);

        result = sshClient.executeCommand("container-list");
        System.out.println("---------------------------------------------------------------------");
        System.out.println(result);

        result = sshClient.executeCommand("container-info " + containerName);
        System.out.println("--------------------------------------------------------------------");
        System.out.println(result);

        String uuid = this.getOpenShiftGearUuid(containerName);
        String pw = this.findFusePasswordForGear(uuid);
        System.out.println("UUID " + uuid + " PW " + pw);

        // TODO copy files here -- do I need to wait for the container to finish provisioning?
        // sudo Copy from /var/lib/openshift/UUID/fuse/container/quickstarts/beginner/camel-cbr/src/main/fabric8/data/order*.xml
        // to /var/lib/openshift/UUID/fuse/container/work/cbr/input
        String quickstartDataDirectory = "/var/lib/openshift/" + uuid
                + "/fuse/container/quickstarts/beginner/camel-cbr/src/main/fabric8/data/";
        String quickStartWorkInputDirectory = "/var/lib/openshift/" + uuid + "/fuse/container/work/cbr/input/";

        for (int i=1; i <=5; i++) {
            String command = "sudo cp " + quickstartDataDirectory + "order" + i + ".xml " + quickStartWorkInputDirectory;
            executeShellCommand(command);
        }

        // TODO did they get copied?  wait, then ls -alF?
        String quickStartWorkOutputDirectory = "/var/lib/openshift/" + uuid + "/fuse/container/work/cbr/output/";
        List<String> blah = executeShellCommand("sudo ls -l " + quickStartWorkOutputDirectory + "us");
        System.out.println("???? US" + blah.size() + " " + blah);
/*
        System.out.println("-------------------------------------------------------------------");
        String deleteResults = sshClient.executeCommand("container-delete " + containerName);
        System.out.println(deleteResults);

        result = sshClient.executeCommand("container-list");
        System.out.println("-------------------------------------------------------------------");
        System.out.println(result);

        result = sshClient.executeCommand("container-info " + containerName);
        System.out.println("-------------------------------------------------------------------");
        System.out.println(result);
*/

    }


}
