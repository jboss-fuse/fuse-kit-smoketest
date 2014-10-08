package org.fusesource.fusesmoketest.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: mmelko
 * Date: 3/11/14
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SSHClient {
    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(SSHClient.class);
    public static final int TIMEOUT = 240;

    private String hostname = "localhost";
    private String localHostName;
    private int port = 8101;
    private String username = "admin";
    private String password = "admin";

    private Session session;
    private Channel channel;
    JSch ssh = new JSch();

    public SSHClient() {
        // default values are used
    }

    public SSHClient(String localHostName) {
        this.localHostName = localHostName;
        //   SSHClient();
    }

    public SSHClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String executeCommand(String command) throws Exception {
        LOG.info("Command: " + command);

        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        String response = convertStreamToString(in);

        LOG.debug("Response: " + response);
        LOG.debug("---------- End of Response -----");

        return response;
    }


    private boolean waitForString(String command, String expectedString, int iterationSeconds, boolean shouldContain) throws Exception {
        int tryCount = 0;
        while (tryCount < iterationSeconds) {

            String commandResult = executeCommand(command);

            if (commandResult.contains(expectedString) == shouldContain) {
                return true;
            }
            tryCount++;
            Thread.sleep(1500);
        }
        throw new RuntimeException("LOG ERROR: Command \"" + command + "\" didn't contain \"" + expectedString + "\" string.");
    }

    public boolean waitCommandContainsString(String command, String expectedString, int iterationSeconds) throws Exception {
        return waitForString(command, expectedString, iterationSeconds, true);
    }

    public boolean waitCommandNotContainsString(String command, String expectedString, int iterationSeconds) throws Exception {
        return waitForString(command, expectedString, iterationSeconds, false);
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void init() throws JSchException {
        LOG.info("Connection initialized...");
        if (port == -1) {
            session = ssh.getSession(username, hostname);
        } else {
            session = ssh.getSession(username, hostname, port);
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);

        LOG.info("Connection establishing...");
        session.connect(180000);
        if (session.isConnected()) {
            LOG.info("Connection is established.");
        }
    }


    public void disconnect() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        LOG.info("Connection disconnected");
    }

    public boolean isConnected() {
        if (session != null) {
            return session.isConnected();
        }
        return false;
    }
}
