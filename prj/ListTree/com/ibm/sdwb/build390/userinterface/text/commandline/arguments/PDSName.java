package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PDSName extends CommandLineArgument {

	private static final String keyword = "PDSNAME";
	private static final String explaination ="Specify the fully qualified name\nof a partitioned data set on mvs";

	public PDSName(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}

