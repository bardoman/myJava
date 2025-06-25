package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriverSize extends CommandLineArgument {

	private static final String keyword = "DRIVERSIZE";
	private static final String explaination = "The site dependent specification of relative size\n"+
												"such as SMALL, MEDIUM, LARGE. Check with your administrator.\n";

	public MainframeDriverSize(){
		super(keyword, explaination);
		addAlternativeName("CDVRSIZE");
	}
}
