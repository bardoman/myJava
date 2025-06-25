package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriverNew extends CommandLineArgument {

	private static final String keyword = "NEWDRIVER";
	private static final String explaination ="The name of the new driver on the mainframe";

	public MainframeDriverNew(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}
