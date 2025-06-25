package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDataSetParameterBulkSecondaryCylinders extends CommandLineArgument {

	private static final String keyword = "BULKSECONDARYCYLINDERS";
	private static final String explaination = "Bulk dataset secondary space in cylinders";

	public MainframeDataSetParameterBulkSecondaryCylinders(){
		super(keyword, explaination);
		addAlternativeName("CBLKS");
	}
}
