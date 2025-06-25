package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DirectoryListFile extends CommandLineArgument {

	private static final String keyword = "DIRECTORY_PATH";
	private static final String explaination = "The full path to a file containing the list of directories to be included";

	public DirectoryListFile(){
		super(keyword,explaination);
	}
}
