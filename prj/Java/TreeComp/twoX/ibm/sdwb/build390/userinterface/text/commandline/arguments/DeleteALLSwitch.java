
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeleteALLSwitch extends CommandLineSwitch {

	private static final String keyword = "DELETEALL";
	private static final String explaination = "deletes all data associated with the build.";

	public DeleteALLSwitch(){
		super(keyword,explaination);
	}
}
