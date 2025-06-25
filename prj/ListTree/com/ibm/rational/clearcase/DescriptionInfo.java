package com.ibm.rational.clearcase;

import java.util.*;

public class DescriptionInfo {
	private String version="";
	private Map attributes = null;
	private String fileName = null;
	private String dateOfChange = null;
	private String elementType = null;


	public DescriptionInfo() {
	}

	public String getFilename() {
		return fileName;
	}

	public void setFilename(String tempFilename) {
		fileName = tempFilename;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String tempVersion) {
		version = tempVersion;
	}

	public String getChangeDate() {
		return dateOfChange;
	}

	public void setChangeDate(String tempChange) {
		dateOfChange = tempChange;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String tempType) {
		elementType = tempType;
	}

	public Map getAttributes() {
		return attributes;
	}

	public void setAttributes(Map newAttributes) {
		attributes = newAttributes;
	}

	public String toString() {
		String str = 
		"Description Information=>"+"\n"+

		"filename: "+fileName+"\n"+ 
			"version: "+version+"\n"+ 

		"changeDate: "+dateOfChange +"\n"+ 

		"attributes:\n";          

		for (Iterator keyIter = attributes.keySet().iterator(); keyIter.hasNext(); ) {
			String nextKey = (String) keyIter.next();
			str += "   "+nextKey+" = "+(String)attributes.get(nextKey)+"\n";
		}
		return str+"\n";
	}

}     











