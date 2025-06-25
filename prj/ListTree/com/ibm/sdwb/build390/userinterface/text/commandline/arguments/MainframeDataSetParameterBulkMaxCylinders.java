package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDataSetParameterBulkMaxCylinders extends CommandLineArgument {

	private static final String keyword = "MAXCYLINDERS";
	private static final String explaination = "Bulk dataset maximum size in cylinders";

	public MainframeDataSetParameterBulkMaxCylinders(){
		super(keyword, explaination);
		addAlternativeName("CMAXCYL");
	}
}
