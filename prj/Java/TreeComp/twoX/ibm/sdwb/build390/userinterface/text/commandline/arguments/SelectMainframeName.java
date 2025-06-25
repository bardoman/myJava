package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SelectMainframeName extends CommandLineArgument {

	private static final String keyword = "SELECTBUILDSERVERNAME";
        private static final String explaination ="set the active BUILD server from the stored setup using shortname." + 
                                                  "\nThe shortname format is as follows.\n"+
                                                   "<user>@<first portion of the BUILD server name upto the .(dot)>";

	public SelectMainframeName(){
		super(keyword,explaination);
	}
}
