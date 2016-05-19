package org.fusesource.fusesmoketest.utils;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: mmelko
 * Date: 8/14/14
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 *
 * TODO some of this code should probably be moved to or combined with SSHClient.
 */
public class FabricSupport {
    public static boolean PATCH = false;
    private static SSHClient sshClient = new SSHClient();
    private static Logger LOG = LoggerFactory.getLogger(FabricSupport.class);
    public static String DEFAULT_VERSION = "1.0";

    public static boolean init() throws Exception {
        return sshInit();
    }

    /**
     * FIXME This code is based on tests I originally got from QE.  I'm not sure that the sleeps,
     * the disconnect/sshInit, or the waitForProvision are needed.
     *
     * @throws Exception
     */
    public static void createFabric() throws Exception {
        FabricSupport.sshInit();

        FabricSupport.executeCommand("fabric:create  --wait-for-provisioning");
        Thread.sleep(10000);
        disconnect();

        FabricSupport.sshInit();
        FabricSupport.waitForProvisioning("root", true);
        Thread.sleep(10000);
    }

    public static void shutdownFuse() throws Exception {
        executeCommand("shutdown --force");
    }

    public static void disconnect() {
        sshClient.disconnect();
    }

    public static boolean waitForProvisioning(String containerName, boolean checkFalse) throws Exception {
        return FabricSupport.waitForProvisioning(containerName, checkFalse, 120);
    }

    public static boolean waitForProvisioning(String containerName, boolean checkFalse, int iterationLimit) throws Exception {
        int iteration = 1;
        boolean done = false;
        while (!done && (iteration < iterationLimit)) {
            String response = sshClient.executeCommand("fabric:container-list " + containerName);
            String info = sshClient.executeCommand("fabric:container-info " + containerName );
            int provisionStatusLocation = info.indexOf("Provision Status:");
            String provisionStatus = info.substring(provisionStatusLocation + "Provision Status:".length()).trim();
            LOG.info(">>>>> In waitForProvisioning status: [{}]", provisionStatus); // TODO check provision status rather than response in the loop below?
            if ((response.contains("error") || response.contains("failed")) && (checkFalse)) {
                return false;
            }
            if (response.contains("success")) {
                done = true;
            } else {
                Thread.sleep(1000);
                LOG.info(">>>> End of iteration {}", iteration);
            }
            iteration++;
        }

        return done;
    }

    public static String executeCommand(String c) throws Exception {
        return sshClient.executeCommand(c);
    }

    /**
     * FIXME why are there two of these?
     * @param name
     * @param profile
     * @param checkError
     * @throws Exception
     */
    public static void createChildContainer(String name, String profile, boolean checkError) throws Exception {
        String state = "";
        if (profile.equals("")) {
            state = executeCommand("container-create-child  root " + name);
        } else {
            state = executeCommand("container-create-child --profile " + profile + " root " + name);
        }
        LOG.info(">>>>> Response from create-child: " + state);
        if (!state.toLowerCase().contains("error")) {
            FabricSupport.waitForProvisioning(name, checkError);
        } else {
            throw new Exception(state);
        }
    }


    public static void createChildContainer(String name, boolean checkError, String... profile) throws Exception {
        if (profile.equals("")) {
            executeCommand("container-create-child  root " + name);
        } else {
            String profiles = "";
            for (String s : profile) {
                profiles += "--profile " + s + " ";
            }
            executeCommand("container-create-child " + profiles + " root " + name);
        }
        FabricSupport.waitForProvisioning(name, checkError, 180);
        if (PATCH) {
            FabricSupport.waitForProvisioning(name, checkError);
        }
    }


    public static void createChildContainer(String name, String profile) throws Exception {
        createChildContainer(name, profile, true);
    }


    public static void createChildContainer(String name) throws Exception {
        createChildContainer(name, "", true);
    }


    public static String executeCommandOnChild(String child, String command) throws Exception {
        return executeCommand("container-connect " + child + " " + command);
    }


    public static String removeContainer(String name) throws Exception {
        return executeCommand("container-delete " + name);
    }


    public static boolean sshInit() throws JSchException, InterruptedException {
        int timeout = 100;
        int i = 0;

        while (i < timeout) {
            try {
                sshClient.init();
            } catch (JSchException e) {
                LOG.info("NOT ABLE to CONNECT, trying to reconnect");
            }
            if (sshClient.isConnected()) {
                return true;
            }

            Thread.sleep(1000);
            i++;
        }
        return false;
    }
}
