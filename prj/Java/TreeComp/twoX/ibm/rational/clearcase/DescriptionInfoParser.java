package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;
import java.io.*;

class DescriptionInfoParser {

	static String ONEFILE_KEY = "B390FILE";
	static String DATE_KEY = "B390DATE";
	static String FILENAME_KEY = "B390FILENAME";
	static String VERSION_KEY = "B390VERSION";
	static String ATTRIBUTE_KEY="B390ATTRIBUTE";

	private BufferedReader sourceReader  = null;
	private Logger logger = null;
	private List descriptionInfoList = null;

	public DescriptionInfoParser(String inStr, Logger tempLogger) {
		sourceReader = new BufferedReader(new StringReader(inStr));
		logger = tempLogger;
		parseFile();
	}

	private void parseFile(){
		descriptionInfoList = new ArrayList();
		try {
			String oneStanza = null;
			while ((oneStanza = getNextPartStanza()) != null) {
				descriptionInfoList.add(parseOneStanza(oneStanza));
			}
		}catch (IOException ioe){
			throw new RuntimeException("Error parsing library partlist.", ioe);
		}
	}


	public List getInfo(){
		return descriptionInfoList;
	}

	private String getNextPartStanza() throws IOException{
		String openingLine = sourceReader.readLine();
		if (openingLine != null) {
			if (openingLine.trim().length() < 1) {
				return null;
			}
			if (!openingLine.trim().equals(getOpeningTag(ONEFILE_KEY))) {
				// since we control formatting, this should never happend, so blow up if it does.
				throw new RuntimeException("Unexpected line encountered parsing partlist output: " + openingLine);
			}
			StringWriter outputString = new StringWriter();
			BufferedWriter stanza = new BufferedWriter(outputString);
			for (String nextLine = sourceReader.readLine(); !nextLine.equals(getClosingTag(ONEFILE_KEY)); nextLine = sourceReader.readLine()) {
				stanza.write(nextLine);
				stanza.newLine();
			}
			stanza.close();
			return outputString.toString(); 
		}
		return null;
	}

	private DescriptionInfo parseOneStanza(String oneStanza) throws java.io.IOException{
		DescriptionInfo oneInfo = new DescriptionInfo();
		BufferedReader stanza = new BufferedReader(new StringReader(oneStanza));
		stanza.readLine(); // swallow Filename tag
		oneInfo.setFilename(stanza.readLine());
		stanza.readLine(); // swallow end filename tag
		stanza.readLine(); // swallow version tag
		oneInfo.setVersion(stanza.readLine());
		stanza.readLine(); // swallow end version tag
		stanza.readLine(); // swallow start changedate tah
		oneInfo.setChangeDate(stanza.readLine());
		stanza.readLine(); // swallow end changedate tag
		stanza.readLine(); // swallow attribute tag
		Map attributeMap = new HashMap();
		String testEnd = stanza.readLine(); 
		while (!testEnd.equals(getClosingTag(ATTRIBUTE_KEY))) {
			addAttributeSetting(testEnd,attributeMap);
			testEnd = stanza.readLine();
		}
		oneInfo.setAttributes(attributeMap);
		return oneInfo;
	}

	private void addAttributeSetting(String attributeLine, Map attributeMap){
		//Ken6.0
	}

	static String getOpeningTag(String baseName){
		return "<"+baseName+">";
	}

	static String getClosingTag(String baseName){
		return "</"+baseName+">";
	}
}
