package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Defect extends CommandLineArgument {

	private static final String keyword = "DEFECT";
	private static final String explaination = "Name of the defect";

	public Defect(){
		super(keyword,explaination);
	}
}
