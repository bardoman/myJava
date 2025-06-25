package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ComponentListFile extends CommandLineArgument {

	private static final String keyword = "COMPONENTS_PATH";
	private static final String explaination = "The full path to a file containing the list of components to be included";

	public ComponentListFile(){
		super(keyword,explaination);
	}
}
