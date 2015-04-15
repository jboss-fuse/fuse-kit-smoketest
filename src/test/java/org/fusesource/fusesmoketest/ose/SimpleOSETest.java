package org.fusesource.fusesmoketest.ose;

import com.jcraft.jsch.JSchException;
import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Created by openshift on 4/14/15.
 */
public class SimpleOSETest extends OSESmokeTestBase {
    // ssh -p 42777 admin@fud-mynamespace.openshift.example.com

    @Test
    public void testCreateOpenshiftContainer() throws Exception {
        sshInit();
        String profileList = sshClient.executeCommand("profile-list");
        System.out.println("result: " + profileList);

        String containerName = "test" + System.currentTimeMillis();

        String result = createOpenshiftContainer(OSE_USERNAME, OSE_PASSWORD, "quickstarts-cxf-rest", containerName);
        System.out.println(result);

        sshClient.disconnect();
    }

    @Test
    public void testListContainers() throws Exception {
        sshInit();
        String result = sshClient.executeCommand("container-list");
        System.out.println("------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------");
        sshClient.disconnect();
    }

    @Rule
    public TestName testName = new TestName();

}