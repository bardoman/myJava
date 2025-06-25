package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Build extends CommandLineArgument {

	private static final String keyword = "BUILD";
	private static final String explaination ="The build ID of the build to be used for this process.";

	public Build(){
		super(keyword,explaination);
		addAlternativeName("BUILDID");
	}
}
