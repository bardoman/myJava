package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PDSSavePath extends CommandLineArgument {

	private static final String keyword = "DSNPATH";
	private static final String explaination ="A preallocated PDS to save output to.";

	public PDSSavePath(){
		super(keyword,explaination);
	}
}
