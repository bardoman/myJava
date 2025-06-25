
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeleteAllJobOutputSwitch extends CommandLineSwitch {

	private static final String keyword = "JOBS";
	private static final String explaination = "deletes all job output on the host releated to the the build.";

	public DeleteAllJobOutputSwitch(){
		super(keyword,explaination);
	}
}
