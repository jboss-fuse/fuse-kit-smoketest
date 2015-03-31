package org.fusesource.fusesmoketest.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

/**
 * Created by kearls on 3/20/15.
 */
public class TailLog {

    /**
     * Tail a log file and search for messages
     *
     * @param logFileName
     * @param targetMessageCount
     * @param delay
     * @param searchTarget
     * @return
     * @throws Exception
     */
    public static int tailAndSearchLog(String logFileName, int targetMessageCount, int delay, String searchTarget) throws Exception {
        File fuseLogFile = new File(logFileName);
        FileInputStream fis = new FileInputStream(fuseLogFile);
        BufferedReader bufferedLogFileReader = new BufferedReader(new InputStreamReader(fis));

        // Move to the end of the log file
        FileChannel fileChannel = fis.getChannel();
        long endOfFilePosition = fileChannel.size();
        fileChannel.position(endOfFilePosition);  // Positioning by using a FileChannel to reach EOF

        // Monitor incoming log messages.  The camel-log example produces a log message every 5 seconds
        int foundLogMessages = 0;
        int iteration = 0;
        Thread.sleep(delay);
        while ((foundLogMessages < targetMessageCount) && iteration < (targetMessageCount * 2)) {
            String line = bufferedLogFileReader.readLine();
            while (line != null) {
                if (line.contains(searchTarget)) { // and log-route
                    foundLogMessages++;
                }
                line = bufferedLogFileReader.readLine();
            }
            Thread.sleep(delay);
            iteration++;
        }

        return foundLogMessages;
    }
}
