package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryRelease extends CommandLineArgument {

	private static final String keyword = "LIBRELEASE";
	private static final String explaination ="The name of the release in the library system";

	public LibraryRelease(){
		super(keyword,explaination);
	}
}
