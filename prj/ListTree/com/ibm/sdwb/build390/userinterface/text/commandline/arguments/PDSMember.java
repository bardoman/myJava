package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PDSMember extends IndexedArgument {

	private static final String keyword = "PDSMEMBER";
	private static final String explaination ="Specify the member name of the member to be included";

	public PDSMember(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(true);
                setAllowOrderedIndexEntry(true);
	}
}
