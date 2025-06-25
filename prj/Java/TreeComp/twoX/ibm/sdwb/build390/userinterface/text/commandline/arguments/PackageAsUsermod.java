package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PackageAsUsermod extends BinaryCommandLineArgument {

	private static final String keyword = "BUILDUSERMOD";
	private static final String explaination = "YES to package a built ++APAR as a usermod, NO otherwise (Defaults to NO)";

	public PackageAsUsermod(){
		super(keyword,explaination,false);
	}
}
