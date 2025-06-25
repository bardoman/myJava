package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryAddress extends CommandLineArgument {

	private static final String keyword = "LIBRARYADDRESS";
	private static final String explaination ="The address of the library server.(no port)\n"+
	                                          "eg: colonel.storage.tucson.ibm.com\n";

	public LibraryAddress(){
		super(keyword,explaination);
	}
}
