package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeHighLevelQualifier extends CommandLineArgument {

	private static final String keyword = "MVSHLQ";
	private static final String explaination ="The high level qualifier on the mainframe";

	public MainframeHighLevelQualifier(){
		super(keyword,explaination);
		addAlternativeName("CHILVL");
                changeValueToUpperCase();
	}
}
