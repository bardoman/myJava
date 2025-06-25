package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryLevel extends CommandLineArgument {

	private static final String keyword = "LEVEL";
	private static final String explaination = "The level to use.";

	public LibraryLevel(){
		super(keyword,explaination);
		addAlternativeName("CLEVEL");
		addAlternativeName("STAGINGLEVEL");
	}
}
