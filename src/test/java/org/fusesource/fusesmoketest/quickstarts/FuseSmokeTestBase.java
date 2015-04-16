package org.fusesource.fusesmoketest.quickstarts;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.fusesmoketest.quickstarts.utils.CamelQuickstartFileVisitor;
/**
 * Created by kearls on 25/08/14.
 */
public abstract class FuseSmokeTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger(FuseSmokeTestBase.class);
    protected static String FUSE_HOME;    // is there a good default?
    protected static final String EXAMPLES_URL_BASE = "http://localhost:8181/cxf";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FUSE_HOME = System.getProperty("FUSE_HOME");
        if (FUSE_HOME == null || FUSE_HOME.trim().equals("")) {
            FUSE_HOME = System.getenv("FUSE_HOME");
            if (FUSE_HOME == null || FUSE_HOME.trim().equals("")) {
                throw new Exception("FUSE_HOME must be set.");
            }
        }

        if (!FUSE_HOME.endsWith("/") && !FUSE_HOME.endsWith("\\")) {
            FUSE_HOME += "/";
        }
    }


    /**
     *
     *
     * @param root
     * @param filesToCopy
     * @param timeoutInSeconds
     * @return
     * @throws Exception
     */
    public List<Path> waitForFileCopy(Path root, int filesToCopy, int timeoutInSeconds) throws Exception {
        CamelQuickstartFileVisitor visitor = new CamelQuickstartFileVisitor();
        boolean done = false;
        int iterations = 0;
        List<Path> filesFound = new ArrayList<Path>();
        while (!done && iterations < timeoutInSeconds) {
            Files.walkFileTree(root, visitor);
            filesFound = visitor.getFoundFilesList();
            System.out.println(">>>>> Found " + filesFound.size() + " files");
            if (filesFound.size() >= filesToCopy) {
                done = true;
            } else {
                Thread.sleep(1000);
                iterations++;
            }
        }

        return filesFound;
    }
}
