package org.fusesource.fusesmoketest.ose;

import com.jcraft.jsch.JSchException;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OSESmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(OSESmokeTestBase.class);
    protected static String FUSE_USER ="admin";
    protected static String FUSE_PASSWORD ="SgF65e97RfGe";
    protected static String OSE_HOSTNAME="fud-mynamespace.openshift.example.com";
    protected static Integer OSE_PORT=61887;
    protected static String OSE_USERNAME="demo";
    protected static String OSE_PASSWORD="openshift";

    @BeforeClass
    public static void init() throws Exception {
        FUSE_USER = System.getProperty("FUSE_USER", FUSE_USER);
        FUSE_PASSWORD = System.getProperty("FUSE_PASSWORD", FUSE_PASSWORD);
        OSE_USERNAME = System.getProperty("OSE_USERNAME", OSE_USERNAME);
        OSE_PASSWORD = System.getProperty("OSE_PASSWORD", OSE_PASSWORD);
        OSE_HOSTNAME = System.getProperty("OSE_HOSTNAME", OSE_HOSTNAME);
        OSE_PORT = Integer.parseInt(System.getProperty("OSE_PORT", OSE_PORT.toString()));
    }

    @Rule
    public TestName testName = new TestName();
    protected static final SSHClient sshClient = new SSHClient();
    protected static final int MAX_SSH_CONNECTION_ATTEMPTS = 60;

    protected void sshInit() throws JSchException, InterruptedException {
        int attempt = 0;
        while(attempt < MAX_SSH_CONNECTION_ATTEMPTS){
            try{
                sshClient.setHostname(OSE_HOSTNAME);
                sshClient.setPort(OSE_PORT);
                sshClient.setUsername(FUSE_USER);
                sshClient.setPassword(FUSE_PASSWORD);
                sshClient.init();
                if(sshClient.isConnected()) {
                    return;
                }
            } catch (JSchException e ){
                LOG.info("Connection to " + OSE_HOSTNAME + ":" + OSE_PORT +
                        " failed with " + e.getMessage() + ", retrying...");
            }

            Thread.sleep(1000);
            attempt++;
        }
    }

    /**
     * TODO add wait-for-provisioning when creating containers
     *
     * @param oseUser
     * @param osePassword
     * @param profileName
     * @param containerName
     * @return
     * @throws Exception
     */
    public String createOpenshiftContainer(String oseUser, String osePassword,
                                           String profileName, String containerName) throws Exception {
        StringBuilder command = new StringBuilder();
        command.append("container-create-openshift --login ");
        command.append(oseUser);
        command.append(" --password ");
        command.append(osePassword);
        command.append(" --server-url localhost ");
        if (profileName !=null && !profileName.trim().equals("")) {
            command.append("--profile ");
            command.append(profileName);
            command.append(" ");
        }
        command.append("--gear-profile xpaas ");
        command.append(containerName);
        System.out.println("Executing: [" + command.toString() + "]");
        String result = sshClient.executeCommand(command.toString());

        return result;
    }


    /**
     *
     * @param containerName
     * @throws Exception
     */
    public void waitTillProvisioned(String containerName)throws Exception {
        boolean provisioned = false;
        int attempts = 0;
        while (!provisioned & attempts < 20) {
            String provisionStatus = sshClient.executeCommand("container-info " + containerName + " | grep \"Provision Status:\"");
            LOG.info("ProvisionStatus on attempt " + attempts + " : " + provisionStatus);
            if (provisionStatus.trim().endsWith("sucess")) {
                provisioned = true;
            } else {
                attempts++;
                Thread.sleep(6000);
            }
        }
    }

    /**
     *
     * @param openshiftGearUuid
     * @return
     * @throws Exception
     */
    public String findFusePasswordForGear(String openshiftGearUuid) throws Exception {
        String location = "/var/lib/openshift/" + openshiftGearUuid + "/fuse/container/etc/users.properties";
        List<String> lines = executeShellCommand("sudo tail -1 " + location);
        // TODO this should return only 1 line
        String lastLine = lines.get(lines.size() - 1);
        int equalsPostion = lastLine.indexOf("=");
        int firstCommaPosition = lastLine.indexOf(",");
        String password = lastLine.substring(equalsPostion + 1, firstCommaPosition);
        System.out.println("PASSWORD [" + password + "]");

        return password;
    }

    public String getOpenShiftGearUuid(String appName) throws Exception {
        String uuid="";
        List<String> output = executeShellCommand("rhc app show " + appName);
        for (String line : output) {
            System.out.println("[" + line + "]");
            line = line.trim();
            if (line.startsWith("SSH:")) {
                int aerobase = line.indexOf("@");
                uuid = line.substring(4, aerobase).trim();
                System.out.println("UUID [" + uuid + "]");
                return uuid;
            }
        }

        return uuid;  // TODO Throw an exception if we fall thru?
    }


    public List<String> executeShellCommand(String command) throws Exception {
        System.out.println("EXECUTING: [" + command + "]");
        Runtime runtime = Runtime.getRuntime();
        Process p = runtime.exec(command);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        List<String> results = new ArrayList<>();
        String line;
        while ((line = input.readLine()) != null) {
            results.add(line);
        }

        return results;
    }
}
