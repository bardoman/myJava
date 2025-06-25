package com.ibm.sdwb.build390.utilities;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;


public class ParsingFunctions {

    public static String stripNonNumeric(String origString) {
        String returnString = new String();
        for (int i = 0; i < origString.length();i++) {
            if (Character.isDigit(origString.charAt(i))) {
                returnString += (new Character(origString.charAt(i))).toString();
            }
        }
        return returnString;
    }

    public static boolean checkResultFile(com.ibm.sdwb.build390.mainframe.MainframeOutputSourceInterface fileSource, String data,boolean stopped) throws MBBuildException {
        try {
            BufferedReader ResultFileReader = new BufferedReader(new FileReader(fileSource.getPrintFile()));
            String currentLine = new String();
            String alllines = new String();
            while (!stopped & currentLine != null) {
                if ((currentLine = ResultFileReader.readLine()) != null) {
                    if (currentLine.trim().indexOf(data)>-1) {
                        ResultFileReader.close();
                        return true;
                    } else alllines = alllines+currentLine+"\n";
                }
            }
            ResultFileReader.close();
            throw new HostError("The following error occurred when processing the request\n"+alllines, fileSource);
        } catch (IOException ioe) {
            throw new GeneralError("Problem reading file "+ fileSource.getPrintFile().getAbsolutePath(), ioe);
        }
    }


    public static String AppendSpaceAtEndOfString(String tempstr,int formatpos){
        int    templen = tempstr.length();
        if (templen<formatpos) {
            for (int i=0;i<=(formatpos-templen);i++) {
                tempstr +=" ";
            }

        }
        return tempstr;
    }

	/** cleanString - take a string and remove all non-alphanumeric characters
	* @param String dirtyString
	* @return String cleanedString
	*/
	public static String cleanString(String dirtyString) {
		StringBuffer cleanedString = new StringBuffer();
		for (int i = 0; i < dirtyString.length(); i++) {
			if (Character.isLetterOrDigit(dirtyString.charAt(i))) {
				cleanedString.append(dirtyString.substring(i,i+1));
			}
		}
		return cleanedString.toString();
	}

	public static String getNumericSuffix(String input){
		String returnString = null;
		for (int stringIndex = input.length(); stringIndex>0; stringIndex--) {
			String testString = input.substring(stringIndex);
			try {
				Integer.parseInt(testString);
				returnString = testString;// if we get to this line, it must be a valid integer
			}catch (NumberFormatException nfe){
				// swallow it;
			}
		}
		return returnString;
	}
}
