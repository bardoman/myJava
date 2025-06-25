package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ChangeTeam extends CommandLineArgument {

	private static final String keyword = "CHANGETEAM";
	private static final String explaination ="The 1 to 6 character identifier for the change team responsible for servicing the version (FMID).";

	public ChangeTeam(){
		super(keyword,explaination);
		addAlternativeName("TEAM");
	}
}
