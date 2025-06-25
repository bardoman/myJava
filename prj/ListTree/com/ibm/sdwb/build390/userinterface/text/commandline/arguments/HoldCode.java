package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class HoldCode extends CommandLineArgument {

	private static final String keyword = "HOLDCODE";
	private static final String explaination ="The hold code.";


	public HoldCode(){
		super(keyword,explaination);
	}
}
