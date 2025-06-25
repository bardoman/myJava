package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

public class MetadataCriteria extends CommandLineArgument {

	private static final String keyword = "CRITERIA";
	private static final String explaination ="The selection criteria for filtering parts from the driver\nin the format CRITERIA=VALUE.\n"+
                                                  "Example: CRITERIA1=\"DESC EQ DESCRIPTION\"";

	public MetadataCriteria(){
		super(keyword,explaination);
	}
}
