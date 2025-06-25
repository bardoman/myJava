package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LogicForPackaging extends CommandLineArgument {

	private static final String keyword = "LOGIC";
	private static final String explaination = "Specifies the logic to use when building a package.\n"+
												"AUTO(default) will use the current recommended logic from PDT.\n"+
												"Otherwise, you may specify a filename which contains custom logic.\n";

	public LogicForPackaging(){
		super(keyword,explaination);
	}
}
