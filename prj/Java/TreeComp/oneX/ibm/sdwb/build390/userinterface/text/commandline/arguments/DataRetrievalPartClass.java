package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalPartClass extends CommandLineArgument {

	private static final String keyword = "LOGCLASS";
	private static final String explaination ="The class of the part (MODULE, MACRO, etc).\n";


	public DataRetrievalPartClass(){
		super(keyword,explaination);
	}
}
