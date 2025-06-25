package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ProcessServerPort extends CommandLineArgument {

	private static final String keyword = "PROCESSSERVERPORT";
	private static final String explaination ="The process server port of the  library server.";

	public ProcessServerPort(){
		super(keyword,explaination);
	}
}
