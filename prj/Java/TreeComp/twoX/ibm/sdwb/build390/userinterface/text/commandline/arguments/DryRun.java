package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DryRun extends BinaryCommandLineArgument {

	private static final String keyword = "DRYRUN";
	private static final String explaination = "\n<yes/(no)>" +
                                               "\nyes - Indication that this should be processed as a dry run\nin host(non updating)";

	public DryRun(){
		super(keyword,explaination,false);
                setDefaultValue("NO");
                changeValueToUpperCase();
	}
}
