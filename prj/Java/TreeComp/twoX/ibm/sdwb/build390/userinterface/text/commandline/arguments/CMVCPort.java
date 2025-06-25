package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class CMVCPort extends CommandLineArgument {

	private static final String keyword = "CMVCPORT";
	private static final String explaination ="The cmvc port of the cmvc library server.";

	public CMVCPort(){
		super(keyword,explaination);
	}
}
