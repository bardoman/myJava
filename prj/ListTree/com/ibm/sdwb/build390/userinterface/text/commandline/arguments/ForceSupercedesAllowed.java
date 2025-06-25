package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ForceSupercedesAllowed extends BinaryCommandLineArgument {

	private static final String keyword = "ALLOWFORCESUPERCEDES";
	private static final String explaination ="The version is priced, lists the 4 digit year\nin which the version was copyrighted.\nDefaults to the current year.\n";

	public ForceSupercedesAllowed(){
		super(keyword,explaination, false);
		addAlternativeName("FSUPER");
	}
}
