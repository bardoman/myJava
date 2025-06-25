package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class OutputFile extends CommandLineArgument {

	private static final String keyword = "FILE";
	private static final String explaination ="File to store output in.";


	public OutputFile(){
		super(keyword,explaination);
	}
}
