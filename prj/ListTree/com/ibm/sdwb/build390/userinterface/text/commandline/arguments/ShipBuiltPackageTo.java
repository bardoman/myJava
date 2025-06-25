package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ShipBuiltPackageTo extends CommandLineArgument {

	private static final String keyword = "SHIPTO";
	private static final String explaination = "Specifies a node.userid to send the built package to.";//TST3464

	public ShipBuiltPackageTo(){
		super(keyword,explaination);
	}
}
