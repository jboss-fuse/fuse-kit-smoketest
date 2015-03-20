package org.fusesource.fusesmoketest.quickstarts.beginner;

import org.fusesource.fusesmoketest.quickstarts.FuseSmokeTestBase;
import org.junit.Test;

import java.io.*;
import java.nio.channels.FileChannel;

import static org.junit.Assert.*;

/**
 * Test the camel-log example.
 */
public class CamelLogExampleTest extends FuseSmokeTestBase {
    // Delay between log messages sent by the camel-log example
    private static final int LOG_MESSAGE_DELAY = 5000;

    @Test
    public void camelLogTest() throws Exception {
        File fuseLogFile = new File(FUSE_HOME + "/data/log/fuse.log");
        FileInputStream fis = new FileInputStream(fuseLogFile);
        BufferedReader bufferedLogFileReader = new BufferedReader(new InputStreamReader(fis));

        // Move to the end of the log file
        FileChannel fileChannel = fis.getChannel();
        long endOfFilePosition = fileChannel.size();
        fileChannel.position(endOfFilePosition);  // Positioning by using a FileChannel to reach EOF

        // Monitor incoming log messages.  The camel-log example produces a log message every 5 seconds
        int foundLogMessages = 0;
        int expectedLogMessages = 5;
        int iteration = 0;
        Thread.sleep(LOG_MESSAGE_DELAY);
        while ((foundLogMessages < expectedLogMessages) && iteration < (expectedLogMessages * 2)) {
            String line = bufferedLogFileReader.readLine();
            while (line != null) {
                LOG.debug(line);
                if (line.contains("Hello from Fabric based Camel route")) {
                    foundLogMessages++;
                }
                line = bufferedLogFileReader.readLine();
            }
            Thread.sleep(LOG_MESSAGE_DELAY);
            iteration++;
        }

        assertEquals("Expected " + expectedLogMessages + " log messages", expectedLogMessages, foundLogMessages);
    }

}
