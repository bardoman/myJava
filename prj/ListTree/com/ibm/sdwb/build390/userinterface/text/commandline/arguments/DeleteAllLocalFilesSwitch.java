
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DeleteAllLocalFilesSwitch extends CommandLineSwitch {

	private static final String keyword = "LOCAL";
	private static final String explaination = "deletes all other local files associated with the build.";

	public DeleteAllLocalFilesSwitch(){
		super(keyword,explaination);
	}
}
