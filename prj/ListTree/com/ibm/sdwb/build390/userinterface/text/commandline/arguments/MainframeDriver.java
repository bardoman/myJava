package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeDriver extends CommandLineArgument {

	private static final String keyword = "DRIVER";
	private static final String explaination ="The name of the driver on the mainframe";

	public MainframeDriver(){
		super(keyword,explaination);
                changeValueToUpperCase();
	}
}
