package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalPartName extends CommandLineArgument {

	private static final String keyword = "LOGNAME";
	private static final String explaination ="The name of the part that you want information about.\n";


	public DataRetrievalPartName(){
		super(keyword,explaination);
	}
}
