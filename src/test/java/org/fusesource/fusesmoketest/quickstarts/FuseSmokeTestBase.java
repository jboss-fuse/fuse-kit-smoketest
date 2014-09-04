package org.fusesource.fusesmoketest.quickstarts;

import org.junit.BeforeClass;

/**
 * Created by kearls on 25/08/14.
 */
public abstract class FuseSmokeTestBase {
    protected static String FUSE_HOME;	// is there a good default?
    protected static final String EXAMPLES_URL_BASE = "http://localhost:8181/cxf";

    @BeforeClass      // TODO at least the first part needs to be done for all tests.
    public static void setUpBeforeClass() throws Exception {
        FUSE_HOME = System.getProperty("FUSE_HOME");
        if (FUSE_HOME == null || FUSE_HOME.trim().equals("")) {
            throw new Exception("FUSE_HOME must be set.");
        }

        if (!FUSE_HOME.endsWith("/")  && !FUSE_HOME.endsWith("\\")) {
            FUSE_HOME += "/";
        }
    }
}
