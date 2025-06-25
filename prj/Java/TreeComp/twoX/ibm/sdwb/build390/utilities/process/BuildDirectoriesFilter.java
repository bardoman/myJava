package com.ibm.sdwb.build390.utilities.process;

import java.io.*;

public class BuildDirectoriesFilter implements FilenameFilter {
    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        if(name.toUpperCase().equals("MISC") | name.toUpperCase().equals("GENERICBLD")) {
            return false;
        }
        return(new File(dir, name)).isDirectory();
    }
}


