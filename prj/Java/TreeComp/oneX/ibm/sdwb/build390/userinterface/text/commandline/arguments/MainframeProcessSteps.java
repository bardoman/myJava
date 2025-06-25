package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeProcessSteps extends CommandLineArgument {

	private static final String keyword = "MVSPROCESSSTEPS";
	private static final String explaination ="The number of additional process steps.";

	public MainframeProcessSteps(){
		super(keyword,explaination);
		addAlternativeName("CPRCS");
	}
}
