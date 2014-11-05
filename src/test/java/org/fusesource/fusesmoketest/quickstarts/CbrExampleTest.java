package org.fusesource.fusesmoketest.quickstarts;

import org.fusesource.fusesmoketest.quickstarts.utils.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kearls on 25/08/14.
 *
 * FIXME need to do: osgi:install -s mvn:io.fabric8.quickstarts/karaf-beginner-camel-cbr/1.2.0.redhat-041
 */
public class CbrExampleTest extends FuseSmokeTestBase {
    private static String CBR_SOURCE_DATA_DIRECTORY;
    private static String CBR_WORK_INPUT_DIRECTORY;
    private static String CBR_WORK_OUTPUT_DIRECTORY;
    private static List<String> expectedFileNames = new ArrayList<String>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FuseSmokeTestBase.setUpBeforeClass();
        CBR_SOURCE_DATA_DIRECTORY = FUSE_HOME + "quickstarts/beginner/camel-cbr/src/main/fabric8/data";
        CBR_WORK_INPUT_DIRECTORY = FUSE_HOME + "work/cbr/input";
        CBR_WORK_OUTPUT_DIRECTORY = FUSE_HOME + "work/cbr/output";

        CBR_WORK_OUTPUT_DIRECTORY = CBR_WORK_OUTPUT_DIRECTORY.replaceAll("\\\\", "/");

        expectedFileNames.add(CBR_WORK_OUTPUT_DIRECTORY + "/others/order1.xml");
        expectedFileNames.add(CBR_WORK_OUTPUT_DIRECTORY + "/uk/order2.xml");
        expectedFileNames.add(CBR_WORK_OUTPUT_DIRECTORY + "/uk/order4.xml");
        expectedFileNames.add(CBR_WORK_OUTPUT_DIRECTORY + "/us/order3.xml");
        expectedFileNames.add(CBR_WORK_OUTPUT_DIRECTORY + "/us/order5.xml");
    }


    @Test
    /**
     * This test verifies that the quickstarts/cbr test works correctly.  It performs the following steps from the README.md
     *
     *2. Copy the files you find in this quick start's `src/main/resources/data` directory to the newly created `work/cbr/input`
     directory.
     3. Wait a few moments and you will find the same files organized by country under the `work/cbr/output` directory.
     * `order1.xml` in `work/cbr/output/others`
     * `order2.xml` and `order4.xml` in `work/cbr/output/uk`
     * `order3.xml` and `order5.xml` in `work/cbr/output/us``
     *
     * @throws java.io.IOException
     */
    public void test() throws IOException {
        // Copy the test files to the work directory, and wait a couple of seconds for them to get there
        TestUtils.copyDirectory(CBR_SOURCE_DATA_DIRECTORY, CBR_WORK_INPUT_DIRECTORY);

        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
        }

        List<String> outputFileNames = TestUtils.listFileNamesInDirectory(CBR_WORK_OUTPUT_DIRECTORY);
        assertEquals("Wrong number of files found", expectedFileNames.size(), outputFileNames.size());
        for (String expectedFileName : expectedFileNames) {
            assertTrue("Didn't find file " + expectedFileName, outputFileNames.contains(expectedFileName));
        }


        // TODO we could check the log here too
    }

}
