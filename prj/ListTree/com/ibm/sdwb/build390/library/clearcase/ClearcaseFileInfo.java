package com.ibm.sdwb.build390.library.clearcase;

import java.io.*;
import java.util.*;
import com.ibm.rational.clearcase.*;

public class ClearcaseFileInfo extends com.ibm.sdwb.build390.info.FileInfo{

    static final long serialVersionUID = 1111111111111111L;
	private static final int MAX_VERSION_LENGTH_FOR_MAINFRAME = 23;
	private static final int MAX_DIRECTORY_LENGTH_FOR_MAINFRAME = 47;

	ClearcaseFileInfo(DescriptionInfo descInfo, File tempMount){
		super(computeDirectory(descInfo.getFilename(), tempMount.getPath()), computeBaseName(descInfo.getFilename()));
		setVersion(processVersion( descInfo.getVersion()));
		setDate(descInfo.getChangeDate());
		setTypeOfChange("ELEM");
		getMetadata().putAll(descInfo.getAttributes());
		setLocalFile(computeLocalFile(descInfo.getFilename(), tempMount));
    }

	private static String processVersion(String version){
		int beginningIndex = version.length();
		for (int slashCount = 0; slashCount < 2; slashCount++) {
			beginningIndex = version.lastIndexOf("\\", beginningIndex-1);
		}
		if (version.length() - beginningIndex > MAX_VERSION_LENGTH_FOR_MAINFRAME) {
			beginningIndex = version.length() - MAX_VERSION_LENGTH_FOR_MAINFRAME; 
		}
		return version.substring(beginningIndex);
	}

	private static String computeBaseName(String fullName){
		int lastSlash = fullName.lastIndexOf(File.separator);
		if (lastSlash < 0) {
			return fullName;
		}
		return fullName.substring(lastSlash+1);
	}

	private static String computeDirectory(String fullName, String mountPoint){
		if (fullName.startsWith(mountPoint)) {
			fullName = fullName.substring(mountPoint.length());
		}
		int lastSlash = fullName.lastIndexOf(File.separator);
		if (lastSlash < 0) {
			return new String();
		}
		fullName = fullName.substring(0,lastSlash);
		if (fullName.length() > MAX_DIRECTORY_LENGTH_FOR_MAINFRAME) {
			fullName = fullName.substring(MAX_DIRECTORY_LENGTH_FOR_MAINFRAME);
		}
		return fullName;
	}

	private static File computeLocalFile(String fullName, File mountPoint){
		if (fullName.startsWith(mountPoint.getPath())) {
			return new File(fullName);
		}else {
			return new File(mountPoint, fullName);
		}
	}

	public static void main(String[] args){
		DescriptionInfo fake = new DescriptionInfo();
		fake.setAttributes(new HashMap());
		fake.setChangeDate("111");
		fake.setFilename("M:\\someview\\somepath\\whee.asm");
		fake.setVersion("\\fake\\0");
		ClearcaseFileInfo test = new ClearcaseFileInfo(fake, new File("M:\\"));
System.out.println("Bruce Built RIGHT "+test.getDirectory()+"    " +    test.getLocalFile().getAbsolutePath());
	}
}
