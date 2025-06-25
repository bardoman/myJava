package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Editor extends CommandLineArgument {

	private static final String keyword = "EDITOR";
	private static final String explaination ="The name of the editor to use.";

	public Editor(){
		super(keyword,explaination);
	}
}
