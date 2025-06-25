package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LibraryPasswordAuthentication extends BinaryCommandLineArgument {

	private static final String keyword = "LIBRARYPASSWORDAUTHENTICATION";
        private static final String explaination = "\n<yes/(no)>" +
                                                   "\nyes - Library requires password authentication." +
                                                   "\nno  - Library doesnot require password authentication.";

	public LibraryPasswordAuthentication(){
		super(keyword,explaination,false);
                setDefaultValue("NO");
	}
}
