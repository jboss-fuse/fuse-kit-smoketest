package org.fusesource.fusesmoketest.quickstarts.utils;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kearls on 25/08/14.
 */
public class TestUtils {
    public static final String EXAMPLES_URL_BASE = "http://localhost:8181/cxf";

    /**
     * Copy the contents of the source directory to the destination directory.
     *
     * @param sourceDirectoryName
     * @param destinationDirectoryName
     * @throws java.io.IOException
     */
    public static void copyDirectory(String sourceDirectoryName, String destinationDirectoryName) throws IOException {
        File source = new File(sourceDirectoryName);
        File destination = new File(destinationDirectoryName);
        FileUtils.cleanDirectory(destination);
        FileUtils.copyDirectory(source, destination);
    }

    /**
     * Return a list of the names of all files in a directroy
     *
     * @param directoryName
     * @return
     * @throws java.io.IOException
     */
    public static List<String> listFileNamesInDirectory(String directoryName) throws IOException {
        File directory = new File(directoryName);
        @SuppressWarnings("unchecked")
        Collection<File> files = FileUtils.listFiles(directory, null, true);
        List<String> fileNames = new ArrayList<String>();
        for (File f : files) {
            fileNames.add(f.getCanonicalPath().replaceAll("\\\\", "/"));
            //fileNames.add(f.getAbsolutePath());
        }

        return fileNames;
    }


    /**
     * Given a list of directory names, use the FileUtils.cleanDirectory
     * method to clean out all files from those directories.
     * @param directoryNames
     */
    public static void cleanUpDirectories(String... directoryNames) {
        try {
            for (String directoryName : directoryNames) {
                try {
                    File directory = new File(directoryName);
                    FileUtils.cleanDirectory(directory);
                } catch (IllegalArgumentException ie) {
                    // ignore if directory does not exist yet.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /*
     * Helper to return the contents of an InputStream as a String
     */
    public static String getStringFromInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = 0;
        while ((c = in.read()) != -1) {
            bos.write(c);
        }
        in.close();
        bos.close();
        return bos.toString();
    }
}
