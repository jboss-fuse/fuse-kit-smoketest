package org.fusesource.fusesmoketest.quickstarts;

import org.fusesource.fusesmoketest.quickstarts.utils.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kearls on 25/08/14.
 */
public class ErrorsExampleTest extends FuseSmokeTestBase {
    private static String ERRORS_SOURCE_DATA_DIRECTORY;
    private static String ERRORS_WORK_INPUT_DIRECTORY;
    private static String errorsValidationDirectory = FUSE_HOME + "/work/errors/validation";
    private static String deadLetterDirectory = FUSE_HOME + "/work/errors/deadletter";
    private static String doneDirectory = FUSE_HOME + "/work/errors/done";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FuseSmokeTestBase.setUpBeforeClass();

        ERRORS_SOURCE_DATA_DIRECTORY = FUSE_HOME + "quickstarts/beginner/camel-errorhandler/src/main/fabric8/data";
        String ERRORS_WORK_DIRECTORY = FUSE_HOME + "work/errors/";
        ERRORS_WORK_DIRECTORY = ERRORS_WORK_DIRECTORY.replaceAll("\\\\", "/");
        ERRORS_WORK_INPUT_DIRECTORY = ERRORS_WORK_DIRECTORY + "input";
        errorsValidationDirectory = ERRORS_WORK_DIRECTORY + "validation";
        deadLetterDirectory = ERRORS_WORK_DIRECTORY + "deadletter";
        doneDirectory = ERRORS_WORK_DIRECTORY + "done";

        // Clear out destination directories
        TestUtils.cleanUpDirectories(errorsValidationDirectory, deadLetterDirectory, doneDirectory);
    }

    @Test
    /**
     * This test verifies whether the ESB quickstarts/errors test works correctly.  I
     *
     * @throws java.io.IOException
     */
    public void test() throws IOException {
        // TODO is there someway to verify that this quickstart is installed?
        // Copy the test files to the work directory, and wait a couple of seconds for them to get there
        TestUtils.copyDirectory(ERRORS_SOURCE_DATA_DIRECTORY, ERRORS_WORK_INPUT_DIRECTORY);
        // TODO is there a better way to do this?
        try {Thread.sleep(15 * 1000); } catch (InterruptedException e) {}

        // order4.xml will always end up in the validation directory
        List<String> outputFileNames = TestUtils.listFileNamesInDirectory(errorsValidationDirectory);
        assertEquals(1, outputFileNames.size());
        assertTrue(outputFileNames.contains(errorsValidationDirectory + "/order4.xml"));

        // ** other files will end up in `work/errors/done` or `work/errors/deadletter` depending on the runtime exceptions that occur
        List<String> deadLetterFileNames = TestUtils.listFileNamesInDirectory(deadLetterDirectory);
        List<String> doneFileNames = TestUtils.listFileNamesInDirectory(doneDirectory);

        assertEquals(4, deadLetterFileNames.size() + doneFileNames.size());
        assertTrue(doneFileNames.contains(doneDirectory + "/order1.xml") || deadLetterFileNames.contains(deadLetterDirectory + "/order1.xml"));
        assertTrue(doneFileNames.contains(doneDirectory + "/order2.xml") || deadLetterFileNames.contains(deadLetterDirectory + "/order2.xml"));
        assertTrue(doneFileNames.contains(doneDirectory + "/order3.xml") || deadLetterFileNames.contains(deadLetterDirectory + "/order3.xml"));
        assertTrue(doneFileNames.contains(doneDirectory + "/order5.xml") || deadLetterFileNames.contains(deadLetterDirectory + "/order5.xml"));
    }


}
