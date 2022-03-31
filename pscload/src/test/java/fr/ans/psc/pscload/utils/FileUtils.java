/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.utils;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

/**
 * The Class FileUtils.
 */
public class FileUtils {

    /**
     * Copy file to workspace.
     *
     * @param fileName the file name
     * @return the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static File copyFileToWorkspace(String fileName) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        File extractedFile = new File(cl.getResource(fileName).getPath());
        File workspaceFile = new File(extractedFile.getParent() + File.separator + "work" +
                File.separator + fileName);
        Files.copy(extractedFile, workspaceFile);
        return workspaceFile;
    }
}
