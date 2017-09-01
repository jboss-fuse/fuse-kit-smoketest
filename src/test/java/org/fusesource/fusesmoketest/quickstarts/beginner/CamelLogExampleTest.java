package org.fusesource.fusesmoketest.quickstarts.beginner;

import org.fusesource.fusesmoketest.quickstarts.FuseSmokeTestBase;
import org.fusesource.fusesmoketest.utils.TailLog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test the camel-log example.
 */
public class CamelLogExampleTest extends FuseSmokeTestBase {
    // Delay between log messages sent by the camel-log example
    private static final int LOG_MESSAGE_DELAY = 5000;

    @Test
    public void camelLogTest() throws Exception {
        int expectedLogMessages = 5;

	System.out.println ("== Tailing file " + FUSE_HOME + "/data/log/fuse.log");
        int foundLogMessages = TailLog.tailAndSearchLog(FUSE_HOME + "/data/log/fuse.log", expectedLogMessages, LOG_MESSAGE_DELAY, "Hello from Fuse based Camel route"); // and log-route?
        assertEquals("Expected " + expectedLogMessages + " log messages", expectedLogMessages, foundLogMessages);
    }
}


