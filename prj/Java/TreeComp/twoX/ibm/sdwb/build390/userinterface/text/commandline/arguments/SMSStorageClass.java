package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SMSStorageClass extends CommandLineArgument {

	private static final String keyword = "SMSSTORAGECLASS";
	private static final String explaination = "SMS Storage class";

	public SMSStorageClass(){
		super(keyword,explaination);
		addAlternativeName("CSTGCLS");
	}
}
