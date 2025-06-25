package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class SMSManagementClass extends CommandLineArgument {

	private static final String keyword = "SMSMANAGEMENTCLASS";
	private static final String explaination = "SMS Management class";

	public SMSManagementClass(){
		super(keyword,explaination);
		addAlternativeName("CMGTCLS");
	}
}
