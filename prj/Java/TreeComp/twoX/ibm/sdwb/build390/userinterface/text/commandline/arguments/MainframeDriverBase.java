package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriverBase extends CommandLineArgument {

	private static final String keyword = "BASEDRIVER";
	private static final String explaination ="The name of the driver on the mainframe to use as a base.\nSet this field to \'release.driver\' to base the new driver\n"+
										   "on a driver in a different release.";

	public MainframeDriverBase(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}
