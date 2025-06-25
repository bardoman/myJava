package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeUserid extends CommandLineArgument {

	private static final String keyword = "BUILDSERVERUSERID";
	private static final String explaination ="The userid  of the mainframe server.";

	public MainframeUserid(){
		super(keyword,explaination);
	}
}
