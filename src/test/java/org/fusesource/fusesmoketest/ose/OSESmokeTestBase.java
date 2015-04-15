package org.fusesource.fusesmoketest.ose;

import com.jcraft.jsch.JSchException;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSESmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(OSESmokeTestBase.class);
    protected static String FUSE_USER ="admin";
    protected static String FUSE_PASSWORD ="jbXPmnSeYhDu";
    protected static String OSE_HOSTNAME="fud-mynamespace.openshift.example.com";
    protected static Integer OSE_PORT=42777;
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

       /* String bigResult = sshClient.executeCommand("fabric:container-create-openshift --login " +
                OSE_USERNAME + " --password " + OSE_PASSWORD
                + " --server-url localhost --profile quickstarts-cxf-rest " +
                "--gear-profile xpaas thename");*/

        return result;
    }
}
