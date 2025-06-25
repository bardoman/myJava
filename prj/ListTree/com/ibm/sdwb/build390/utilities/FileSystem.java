package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;

//#TSTDEF:1535 added a method which lists all files under a filepath.
public class FileSystem {
    public static boolean deleteDirectoryTree(File fileOrDirectory){
        boolean deletionSuccessful = true;
        String [] files = fileOrDirectory.list();
        if (files != null) {
            // it's a directory, so handle entries and subdirectories first.
            for (int i=0; i < files.length; i++) {
                deleteDirectoryTree(new File(fileOrDirectory, files[i]));
            }
        }
        deletionSuccessful = deletionSuccessful & fileOrDirectory.delete();
        return deletionSuccessful;
    }

    public static List createListFromFile(File sourceFile)throws IOException{
        return createListFromFile(sourceFile,null);
    }

    public static List createListFromFile(File sourceFile, StringPostProcessing stringProcessor) throws IOException{
        List returnList = new ArrayList();
        BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFile));
        for (String oneLine = new String();oneLine!=null; oneLine = sourceReader.readLine() ) {
            if (oneLine != null) {
                oneLine = oneLine.trim();
                if (oneLine.length() > 0) {
                    if (stringProcessor!=null) {
                        oneLine = stringProcessor.processString(oneLine);
                    }
                    if (!returnList.contains(oneLine)) {
                        returnList.add(oneLine);
                    }
                }
            }
        }
        sourceReader.close();
        return returnList;
    }

    public static void  listFilesTree(java.io.File file,java.util.List list,java.io.FileFilter filter) {
        File[] entries =null;
        if (file.isDirectory()) {
            if (filter!=null) {
                entries = file.listFiles(filter);
            } else {
                entries = file.listFiles();
            }
            int maxlen = (entries == null ? 0 : entries.length);
            for (int i = 0; i < maxlen; i++) {
                listFilesTree(entries[i],list,filter);
            }
        } else if (file.isFile()) {
            list.add(file);

        }

    }


}
