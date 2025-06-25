package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Delimiter extends CommandLineArgument {

	private static final String keyword = "DELIMITER";
	private static final String explaination = "String value used to delimit output data.";

	public Delimiter(){
		super(keyword,explaination);
	}
}
