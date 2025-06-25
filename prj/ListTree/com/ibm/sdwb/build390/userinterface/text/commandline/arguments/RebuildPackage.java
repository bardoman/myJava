package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class RebuildPackage extends BinaryCommandLineArgument {

	private static final String keyword = "REBUILD";
	private static final String explaination = "Determines if the last build will be rebuilt\nwith a new REWORK date.\n<yes> to rebuild,\nno(default) otherwise";

	public RebuildPackage(){
		super(keyword,explaination, false);
	}
}
