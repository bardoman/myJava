package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Description extends CommandLineArgument {

	private static final String keyword = "DESCRIPTION";
	private static final String explaination = "The text that describes this process (such as build of GA code)";

	public Description(){
		super(keyword,explaination);
	}
}
