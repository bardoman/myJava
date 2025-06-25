package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeAccountInformation extends CommandLineArgument {

	private static final String keyword = "BUILDSERVERACCTINFO";
	private static final String explaination ="The accounting information of the mainframe server.";

	public MainframeAccountInformation(){
		super(keyword,explaination);
	}
}
