package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class IncludePathname extends BinaryCommandLineArgument {

	private static final String keyword = "PATHNAME";
	private static final String explaination ="\n<yes/(no)>" +
            "\nyes - Include the library path in the report.";

	public IncludePathname(){
		super(keyword,explaination, false);
	}
}
