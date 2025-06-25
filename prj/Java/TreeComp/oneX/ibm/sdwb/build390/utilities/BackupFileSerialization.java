package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.MBBuildException;


public class BackupFileSerialization {

	private static Object saveLock = new Object();

	public static void saveObject(Object objectToSave, File fileHeader, int numberOfBackups) throws IOException{
		File fileToOverwrite = null;
		boolean fileFound = false;
		for (int fileIndex = 0;fileIndex < numberOfBackups & !fileFound; fileIndex++) {
			File testFile = new File(fileHeader.getAbsolutePath()+Integer.toString(fileIndex));
			if (!testFile.exists()) {
				fileToOverwrite = testFile;
				fileFound = true;
			}else {
				if (fileToOverwrite == null) {
					fileToOverwrite = testFile;
				}else if (testFile.lastModified() < fileToOverwrite.lastModified()) {
					fileToOverwrite = testFile;
				}
			}
		}
/*
THIS IS NECESSARY!  if you don't have this, and you save short files in rapid succession, the files will
show up with the same modification time.
*/
		synchronized(saveLock){
			try {
				Thread.currentThread().sleep(2000);
			}catch (InterruptedException ie){}
			ObjectOutputStream saveStream = new ObjectOutputStream(new FileOutputStream(fileToOverwrite));
			saveStream.writeObject(objectToSave);
			saveStream.close();
		}
	}

	public static Object readObject(File fileHeader, int numberOfBackups) throws IOException, ClassNotFoundException{
		File fileToRead = null;
		Vector badFiles = new Vector();
		while (badFiles.size() < numberOfBackups) {
			boolean aFileFound = false;
			for (int fileIndex = 0;fileIndex < numberOfBackups; fileIndex++) {
				File testFile = new File(fileHeader.getAbsolutePath()+Integer.toString(fileIndex));
				aFileFound = aFileFound | testFile.exists();
				if (testFile.exists() & !badFiles.contains(testFile)) {
					if (fileToRead == null) {
						fileToRead = testFile;
					}else if (testFile.lastModified() > fileToRead.lastModified()) {
						fileToRead = testFile;
					}
				}
			}
			if(!aFileFound) {
				return null;
			}
			if (fileToRead != null) {
				try {
					ObjectInputStream readStream = new ObjectInputStream(new FileInputStream(fileToRead));
					Object returnObject = readStream.readObject();
					readStream.close();
					return returnObject;
				}catch (IOException ioe){
					ioe.printStackTrace();
					badFiles.addElement(fileToRead);
				}
			}
		}

		return null;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException{
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
		testVector = null;
		testVector = (Vector) readObject(fileToSave, 4);
		System.out.println("got " + testVector);
	}

}

