package org.fusesource.fusesmoketest;

import com.jcraft.jsch.JSchException;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.fusesource.fusesmoketest.utils.SSHClient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmokeTestBase extends CamelTestSupport {
    protected static final Logger LOG = LoggerFactory.getLogger(SmokeTestBase.class);
    protected static String FUSE_USER ="admin";
    protected static String FUSE_PASSWORD ="admin";
    protected static String AMQ_OPENWIRE_URL = "tcp://localhost:61616";

    @BeforeClass
    public static void init() throws Exception {
        FUSE_USER = System.getProperty("FUSE_USER", FUSE_USER);
        FUSE_PASSWORD = System.getProperty("FUSE_PASSWORD", FUSE_PASSWORD);
    }

    @Rule
    public TestName testName = new TestName();
    protected static final SSHClient sshClient = new SSHClient();
    protected static final int MAX_SSH_CONNECTION_ATTEMPTS = 60;

    protected void sshInit() throws JSchException, InterruptedException {
        int attempt = 0;
        while(attempt < MAX_SSH_CONNECTION_ATTEMPTS){
            try{
                sshClient.init();
                if(sshClient.isConnected()) {
                    return;
                }
            } catch (JSchException e ){
                LOG.info("Connect failed with " + e.getMessage() + ", retrying...");
            }

            Thread.sleep(1000);
            attempt++;
        }
    }
}
