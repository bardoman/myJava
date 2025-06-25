package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeName extends CommandLineArgument {

	private static final String keyword = "BUILDSERVERNAME";
	private static final String explaination ="The address of the mainframe server.\n"+
	                                          "eg: snjeds3.storage.sanjose.ibm.com.\n";

	public MainframeName(){
		super(keyword,explaination);
	}
}
