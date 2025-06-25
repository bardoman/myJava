package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class RootDirectory extends CommandLineArgument {

	private static final String keyword = "ROOTDIRECTORY";
	private static final String explaination ="Root path to where files reside.\nSimiliar to directory selected\nto extract to in a library system.";

	public RootDirectory(){
		super(keyword,explaination);
	}
}
