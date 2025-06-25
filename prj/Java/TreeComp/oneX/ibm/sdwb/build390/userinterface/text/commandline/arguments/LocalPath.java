package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LocalPath extends CommandLineArgument {

	private static final String keyword = "LOCALPATH";
	private static final String explaination ="A local path to save files to,\ndefaults to <Build390InstallDirectory>\\logfiles.";

	public LocalPath(){
		super(keyword,explaination);
	}
}
