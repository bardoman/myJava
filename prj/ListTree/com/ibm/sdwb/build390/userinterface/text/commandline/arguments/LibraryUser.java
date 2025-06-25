package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryUser extends CommandLineArgument {

	private static final String keyword = "LIBRARYUSER";
	private static final String explaination ="The name of the library user to use.";

	public LibraryUser(){
		super(keyword,explaination);
	}
}
