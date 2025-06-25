package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeRelease extends CommandLineArgument {

	private static final String keyword = "MVSRELEASE";
	private static final String explaination ="The name of the release (or shadow) on the mainframe";

	public MainframeRelease(){
		super(keyword,explaination);
		addAlternativeName("NEWSHADOW");
                changeValueToUpperCase();
	}
}
