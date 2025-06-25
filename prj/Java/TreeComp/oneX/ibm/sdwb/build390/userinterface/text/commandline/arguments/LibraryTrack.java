package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryTrack extends CommandLineArgument {

	private static final String keyword = "TRACK";
	private static final String explaination ="The track in the library to use.";

	public LibraryTrack(){
		super(keyword,explaination);
		addAlternativeName("UTRACK");
		addAlternativeName("TRACKTOBUILD");
	}
}
