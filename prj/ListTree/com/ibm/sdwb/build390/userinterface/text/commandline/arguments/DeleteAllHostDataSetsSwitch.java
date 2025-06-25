
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeleteAllHostDataSetsSwitch extends CommandLineSwitch {

	private static final String keyword = "HOSTDS";
	private static final String explaination = "deletes all host data sets releated to the build.";

	public DeleteAllHostDataSetsSwitch(){
		super(keyword,explaination);
	}
}
