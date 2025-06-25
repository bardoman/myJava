package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;

// don't have the threading done yet, will finish this later

public class SynchronizedFileAccess extends File {

    private static Map synchronizedFileMap = new HashMap();
    private BufferedWriter fileWriter = null;

    private SynchronizedFileAccess(File originalFile){
        super(originalFile.toURI());
    }

    public static SynchronizedFileAccess getSychronizedFile(File originalFile){
        SynchronizedFileAccess synchFile = null;
        synchronized(synchronizedFileMap){
            synchFile = (SynchronizedFileAccess) synchronizedFileMap.get(originalFile);
            if (synchFile==null) {
                synchFile = new SynchronizedFileAccess(originalFile);
                synchronizedFileMap.put(originalFile, synchFile);
            }
        }
        return synchFile;
    }

    public BufferedWriter getBufferedWriter(boolean append) throws FileNotFoundException{
        fileWriter = new BufferedWriter(new OutputStreamWriter(new SynchronizedFileOutputStream(this, append)));
        return fileWriter;
    }

    public BufferedReader getBufferedReader() throws FileNotFoundException{
        return new BufferedReader(new FileReader(this));
    }

    private class SynchronizedFileOutputStream extends FileOutputStream{
        private File outputFile = null;

        private SynchronizedFileOutputStream(File tempOut, boolean append) throws FileNotFoundException{
            super(tempOut, append);
            outputFile = tempOut;
        }

        public void close(){
            synchronized(outputFile){
                outputFile.notifyAll();
            }
        }
    }
}
