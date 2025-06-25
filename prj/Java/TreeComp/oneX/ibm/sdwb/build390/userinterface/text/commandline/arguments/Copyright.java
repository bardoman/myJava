package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Copyright extends CommandLineArgument {

	private static final String keyword = "COPYRIGHT";
	private static final String explaination ="The version is priced, lists the 4 digit year in which the version was copyrighted. Defaults to the current year.";

	public Copyright(){
		super(keyword,explaination);
		addAlternativeName("COPYRT");
	}
}
