package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;

public class asynchReader extends Thread {
	BufferedReader source = null;
	String returnString = null;
	IOException runProblem = null;

	public asynchReader(BufferedReader tempReader) {
		source = tempReader;
		start();
	}

	public void run() {
		try {
			char[] buff = new char[1024];
			int bytesRead = source.read(buff);
			if (bytesRead >=0) {
				returnString = new String(buff, 0, bytesRead);
				while ((bytesRead = source.read(buff)) >=0) {
					returnString += new String(buff, 0, bytesRead);
				}
			}
		} catch (IOException ioe) {
			runProblem = ioe;
		}
	}

	public IOException getEncounteredException() {
		return runProblem;
	}

	public String getRead() {
		return returnString;
	}
}

