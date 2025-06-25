package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Apar extends CommandLineArgument {

	private static final String keyword = "APAR";
	private static final String explaination = "Apar to use";

	public Apar(){
		super(keyword,explaination);
		addAlternativeName("APARNAME");
	}
}
