package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class NumberOfPartsInDriver extends CommandLineArgument {

	private static final String keyword = "NUMPARTS";
	private static final String explaination = "Number of parts that will be in the driver\n(used for size determination)";

	public NumberOfPartsInDriver(){
		super(keyword, explaination);
	}
}
