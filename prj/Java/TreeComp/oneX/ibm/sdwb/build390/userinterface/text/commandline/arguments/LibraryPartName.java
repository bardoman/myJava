package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryPartName extends CommandLineArgument {

	private static final String keyword = "LIBPARTNAME";
	private static final String explaination ="The complete pathname of a part in the library";

	public LibraryPartName(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}
