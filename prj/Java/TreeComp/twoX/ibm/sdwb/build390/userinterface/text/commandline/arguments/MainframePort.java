package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframePort extends CommandLineArgument {

	private static final String keyword = "BUILDSERVERPORT";
	private static final String explaination ="The port number  of the mainframe server.";

	public MainframePort(){
		super(keyword,explaination);
	}
}
