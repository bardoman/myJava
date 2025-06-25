package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeCollectors extends CommandLineArgument {

	private static final String keyword = "COLLECTORS";
	private static final String explaination ="The number of additional collectors.";

	public MainframeCollectors(){
		super(keyword,explaination);
		addAlternativeName("CCLTR");
	}
}
