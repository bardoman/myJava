package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class BuildType extends CommandLineArgument {

	private static final String keyword = "BUILDTYPE";
	private static final String explaination ="The buildtype to perform (defined in the BLDORDER)";

	public BuildType(){
		super(keyword,explaination);
	}
}
