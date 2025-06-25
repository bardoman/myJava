package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.MBBuildException;
//***************************************************************
//Java class is a patched version of BackupFileSerialization class. 
//This basically saves files incrementally as follow
//filename.ser
//filename.ser0
//filename.ser1 depending upon the number of backups requested.
//We need to coalesce into  a single class.
//***************************************************************
// 11/12/2004 Birth of the class.
// 11/12/2004 PTM3767 backup builds 
// 12/07/2004 TST2024 Reduce the Thread.sleep timing to 100ms. 
// 12/09/2004 TST2024 Automate serial backups using renames.
//***************************************************************

public class BackupBuilds {
    synchronized public static void saveObject(Object objectToSave, File fileHeader, int numberOfBackups)    throws IOException   {
        File fileToOverwrite = null;
        boolean fileFound = false;

        File initialFile = new File(fileHeader.getAbsolutePath());

        fileToOverwrite = initialFile;

        if (!initialFile.exists()) {
            fileFound = true;
        }

        File swap = initialFile; /*start with build.ser */
        for (int fileIndex = 0; fileIndex < numberOfBackups  & !fileFound; fileIndex++) {
            File testFile = new File(fileHeader.getAbsolutePath()+Integer.toString(fileIndex));
            /*   if (!testFile.exists()) {
                   swap.renameTo(testFile);
                   fileFound = true;
               } else {
                   if((fileIndex+1) < numberOfBackups){
                       File itestFile = new File(fileHeader.getAbsolutePath()+Integer.toString(fileIndex+1));
   
                   }
                   testFile.delete();
                   swap.renameTo(testFile);
                   swap = testFile;
               }
               */

            fileFound = seriallyRollBackups(fileIndex,numberOfBackups,fileHeader,swap);
        }

        ObjectOutputStream saveStream = new ObjectOutputStream(new FileOutputStream(fileToOverwrite));
        saveStream.writeObject(objectToSave);
        saveStream.close();
    }

    /**
     * Lets say the 
     * Number of backups=3.
     * Then we would have three backup files
     * build.ser     - this is the latest.
     * build.ser0    - second latest.
     * build.ser1    - least . 
     * The order of incremental serial backups happen 
     * from(ordered by latest saved file to the least)
     * >> build.ser
     * >> build.ser0
     * >> build.ser1
     * 
     * This method is called recursively.
     * When the loop starts the 
     * swap file contains "build.ser" file pointer.
     * We check if build.ser1 exists.
     * If it(build.ser1) DOESNOT exists, we return from this method
     * with a boolean of true, and rename 
     * build.ser to build.ser1 and procced to save stuff into build.ser.
     * Now if it exists, then
     * we recurse again with the file pointer incremented by one.
     * ie. if these three files(build.ser, build.ser0,build.ser1),
     * 
     *  The sequence of steps are,
     * ------------------------------
     * rename build.ser to build.ser0
     * rename build.ser0 to build.ser1
     * and save new build.ser.
     * 
     * @param currentIndex
     * @param numberOfBackups
     * @param currentBackup
     * @param swap
     * 
     * @return 
     */
    private static boolean  seriallyRollBackups(int currentIndex,int numberOfBackups,File fileHeader,File swap) {
        File currentBackup = new File(fileHeader.getAbsolutePath()+Integer.toString(currentIndex));
        if (!currentBackup.exists()) {
            swap.renameTo(currentBackup);
            return true;
        } else {
            if ((currentIndex+1) < numberOfBackups) {
                seriallyRollBackups(currentIndex+1,numberOfBackups,fileHeader,currentBackup); /* recurse again */
            }

            if (currentBackup.exists()) currentBackup.delete();

            swap.renameTo(currentBackup);

        }

        return true;

    }

    public static Object readObject(File fileHeader, int numberOfBackups) throws IOException, ClassNotFoundException,Exception {
        File fileToRead = null;
        List exceptions = new LinkedList();
        fileToRead = new File(fileHeader.getAbsolutePath());

        if (!fileToRead.exists()) {
            throw new FileNotFoundException("\nFile:"+ fileToRead.getAbsolutePath() +" was not found"); 
        }

        if (fileToRead != null) {
            ObjectInputStream readStream = new ObjectInputStream(new FileInputStream(fileToRead));
            try {
                Object returnObject = readStream.readObject();
                return returnObject;
            } catch (IOException ioe) {
                exceptions.add(ioe);
            } finally {
                readStream.close();
            }
        }

        for (int fileIndex = 0;fileIndex != numberOfBackups; fileIndex++) {

            fileToRead = new File(fileHeader.getAbsolutePath()+Integer.toString(fileIndex));

            if (!fileToRead.exists()) {
                throw new FileNotFoundException("\nFile:"+ fileToRead.getAbsolutePath() +" was not found"); 
            }

            if (fileToRead != null) {
                ObjectInputStream readStream = new ObjectInputStream(new FileInputStream(fileToRead));
                try {
                    Object returnObject = readStream.readObject();
                    return returnObject;
                } catch (IOException ioe) {
                    exceptions.add(ioe);
                } finally {
                    readStream.close();
                }
            }
        }

        if (exceptions.size() > 0) {
            MultipleConcurrentException  sendBackException  = new MultipleConcurrentException("Errors encountered loading builds.");
            for (Iterator iter = exceptions.iterator();iter.hasNext();) {
                sendBackException.addException((IOException)iter.next());
            }

            throw  sendBackException;
        }

        throw new Exception("Unable to load build in path " + fileHeader.getAbsolutePath());
    }


    public static void main(String[] args) throws Exception {
        if (args[0].startsWith("1")) {
            testFileSave();
            testFileRead();
        }
        if (args[0].startsWith("2")) testFileRead();
    }


    private static void  testFileSave()  throws IOException, ClassNotFoundException,Exception  {
        File fileToSave = new File("test.ser");
        Vector testVector = new Vector();
        testVector.addElement("first");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("second");   
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("third");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("fourth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("fifth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("sixth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("seventh");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("eighth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("nineth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("tenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("eleventh");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("twelveth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("thirteenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("fourteenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("fifteenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("sixteenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("seventeenth");
        saveObject(testVector, fileToSave, 4);
        testVector.addElement("eighteenth");
        saveObject(testVector, fileToSave, 4); 
    }


    private static void testFileRead()  throws IOException, ClassNotFoundException,Exception   {
        File fileToSave = new File("test.ser");
        Vector testVector = (Vector) readObject(fileToSave, 4);
        System.out.println("got " + testVector);

    }

}

