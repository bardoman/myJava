package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDataSetParameterBulkPrimaryCylinders extends CommandLineArgument {

	private static final String keyword = "BULKPRIMARYCYLINDERS";
	private static final String explaination = "Bulk dataset primary space in cylinders";

	public MainframeDataSetParameterBulkPrimaryCylinders(){
		super(keyword, explaination);
		addAlternativeName("CBLKP");
	}
}
