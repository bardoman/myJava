package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ParameterFile extends CommandLineArgument {

	private static final String keyword = "PARMFILE";
	private static final String explaination ="The parameter file to initialize setup values from.";

	public ParameterFile(){
		super(keyword,explaination);
	}
}
