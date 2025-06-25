package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class AparPrefix extends CommandLineArgument {

	private static final String keyword = "APARPREFIX";
	private static final String explaination ="The starting prefix character to be used for ++APARs. Must be an uppercase letter. Defaults to 'A'.";

	public AparPrefix(){
		super(keyword,explaination);
		setValue("A");
		addAlternativeName("PREFIX");
	}
}
