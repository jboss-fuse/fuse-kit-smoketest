package org.fusesource.fusesmoketest.ose;

import com.jcraft.jsch.JSchException;
import org.fusesource.fusesmoketest.SmokeTestBase;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by openshift on 4/14/15.
 */
public class SimpleOSETest extends OSESmokeTestBase {

    @Rule
    public TestName testName = new TestName();
    @Test
    public void testCreateOpenshiftContainer() throws Exception {
        //sshInit();
        String containerName = "test" + System.currentTimeMillis();

        String result = createOpenshiftContainer(OSE_USERNAME, OSE_PASSWORD, "quickstarts-cxf-rest", containerName);
        System.out.println(result);

        System.out.println("-------------------------------------------------------------------");
        String deleteResults = sshClient.executeCommand("container-delete " + containerName);
        System.out.println(deleteResults);

        //sshClient.disconnect();
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

    @Test
    public void testCommandLine() throws Exception {
        System.out.println(executeShellCommand("rhc apps"));
        System.out.println(executeShellCommand("sudo ls -ltr /var/lib/openshift/"));
    }

}