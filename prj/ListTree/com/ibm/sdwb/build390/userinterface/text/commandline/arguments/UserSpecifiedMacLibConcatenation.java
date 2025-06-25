package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class UserSpecifiedMacLibConcatenation extends CommandLineArgument {

	private static final String keyword = "USERMAC";
	private static final String explaination ="Specifies a pds name to be concatenated\nahead of the driver macro library.";

	public UserSpecifiedMacLibConcatenation(){
		super(keyword,explaination);
	}
}
