package org.fusesource.fusesmoketest.quickstarts;

import org.fusesource.fusesmoketest.quickstarts.utils.TestUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kearls on 03/09/14.
 */
public class EipExampleTest extends FuseSmokeTestBase {
    private static String EIP_SOURCE_DATA_DIRECTORY;
    private static String EIP_WORK_INPUT_DIRECTORY;
    private static String EIP_WORK_OUTPUT_DIRECTORY;
    private static List<String> expectedFileNames = new ArrayList<String>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FuseSmokeTestBase.setUpBeforeClass();
        EIP_SOURCE_DATA_DIRECTORY = FUSE_HOME + "quickstarts/beginner/camel-eips/src/main/fabric8/data";
        EIP_WORK_INPUT_DIRECTORY = FUSE_HOME + "work/eip/input";
        EIP_WORK_OUTPUT_DIRECTORY = FUSE_HOME + "work/eip/output";
        EIP_WORK_OUTPUT_DIRECTORY = EIP_WORK_OUTPUT_DIRECTORY.replaceAll("\\\\", "/");

        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/AMER/2012_0003.xml");
        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/AMER/2012_0005.xml");
        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/APAC/2012_0020.xml");
        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/EMEA/2012_0001.xml");
        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/EMEA/2012_0002.xml");
        expectedFileNames.add(EIP_WORK_OUTPUT_DIRECTORY + "/EMEA/2012_0004.xml");
    }


    @Test
    /**
     * This test verifies whether the FUSE quickstarts/eip example works correctly.
     *
     * @throws java.io.IOException
     */
    public void test() throws IOException {
        // Copy the test files to the work directory, and wait a couple of seconds for them to get there
        TestUtils.copyDirectory(EIP_SOURCE_DATA_DIRECTORY, EIP_WORK_INPUT_DIRECTORY);

        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
        }

        System.out.println(">>>> Looking for output in " + EIP_WORK_OUTPUT_DIRECTORY);
        List<String> outputFileNames = TestUtils.listFileNamesInDirectory(EIP_WORK_OUTPUT_DIRECTORY);
        for (String name : outputFileNames) {
            System.out.println("\tGot " + name);
        }

        assertEquals(expectedFileNames.size(), outputFileNames.size());
        if (!outputFileNames.containsAll(expectedFileNames)) {
            Collections.sort(expectedFileNames);
            System.out.println("***** Expected *****");
            System.out.println(expectedFileNames);
            System.out.println("***** Actual *****");
            Collections.sort(outputFileNames);
            System.out.println(outputFileNames);
        }

        assertTrue(outputFileNames.containsAll(expectedFileNames));
        // TODO we could check the log here too
    }
}
