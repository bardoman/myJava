package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDataSetParameterUnibankCylinders extends CommandLineArgument {

	private static final String keyword = "UNIBANKCYLINDERS";
	private static final String explaination = "Bulk dataset secondary space in cylinders";

	public MainframeDataSetParameterUnibankCylinders(){
		super(keyword, explaination);
		addAlternativeName("CUBKP");
	}
}
