package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PartClass extends CommandLineArgument {

	private static final String keyword = "CLASS";
	private static final String explaination ="The part class to deal with.";

	public PartClass(){
		super(keyword,explaination);
	}
}
