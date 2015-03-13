package org.fusesource.fusesmoketest.quickstarts.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * This is a File Visitor specifically for Camel quickstarts.  It can be used to find all files in
 * a (presumably) work directory which end in .xml, are not in the input directory, and are not
 * temporary .camel files.
 *
 * Created by kearls on 2/16/15.
 */
public class CamelQuickstartFileVisitor implements FileVisitor<Path> {
    Set<Path> nonDirectoryFilesFound = new HashSet<Path>();

    public List<Path> getFoundFilesList() {
        List<Path> result = new ArrayList<Path>(nonDirectoryFilesFound);
        return result;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fullName = file.toString();
        if (fullName.endsWith(".xml") && (!fullName.contains("/input/") && (!fullName.contains(".camel")))) {
            nonDirectoryFilesFound.add(file);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }
}
