package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class MainframeVolumeID extends CommandLineArgument {

	private static final String keyword = "MVSVOLUMEID";
	private static final String explaination = "The DASD volume serial";

	public MainframeVolumeID(){
		super(keyword, explaination);
		addAlternativeName("CVOLID");
	}
}
