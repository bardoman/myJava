package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDataSetParameterBulkMaxExtents extends CommandLineArgument {

	private static final String keyword = "MAXEXTENTS";
	private static final String explaination = "Bulk dataset maximum extents";

	public MainframeDataSetParameterBulkMaxExtents(){
		super(keyword, explaination);
		addAlternativeName("CMAXEXT");
	}
}
