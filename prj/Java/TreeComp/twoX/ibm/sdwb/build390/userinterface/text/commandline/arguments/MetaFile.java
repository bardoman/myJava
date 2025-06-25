package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MetaFile extends CommandLineArgument {

	private static final String keyword = "METAFILE";
	private static final String explaination ="The pathname of a file for metadata input.";

	public MetaFile(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}
