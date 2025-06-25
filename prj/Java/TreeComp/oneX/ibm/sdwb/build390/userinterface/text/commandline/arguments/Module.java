package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Module extends CommandLineArgument {

	private static final String keyword = "MODULE";
	private static final String explaination ="The module to deal with.";

	public Module(){
		super(keyword,explaination);
		addAlternativeName("MOD");
	}
}
