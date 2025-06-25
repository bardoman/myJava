package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PathToWorkFrom extends CommandLineArgument {

	private static final String keyword = "PATH";
	private static final String explaination = "Specifies the base directory to search for user files\nspecifying build parameters (such as logic or comments).\n"+
												"Unneeded if they are in the current working directory.";

	public PathToWorkFrom(){
		super(keyword,explaination);
	}
}
