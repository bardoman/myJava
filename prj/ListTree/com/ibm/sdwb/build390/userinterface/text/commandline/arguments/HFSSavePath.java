package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class HFSSavePath extends CommandLineArgument {

	private static final String keyword = "HFSPATH";
	private static final String explaination ="A HFS path to save output to.";

	public HFSSavePath(){
		super(keyword,explaination);
	}
}
