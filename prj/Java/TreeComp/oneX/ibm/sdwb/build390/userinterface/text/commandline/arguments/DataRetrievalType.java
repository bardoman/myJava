package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalType extends CommandLineArgument {

	private static final String keyword = "LOGTYPE";
	private static final String explaination ="The type of information that you want to retrieve\n"+
                                     "such as SRC, OBJ, LST, SYM, METADATA, or DEPENDENCY.\n"+
                                     "Collectors can be specified as well (C1...Cn).\n";


	public DataRetrievalType(){
		super(keyword,explaination);
	}
}
