package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class KeyWord extends IndexedArgument {

	private static final String keyword = "KEYWORD";
	private static final String explaination ="The name of a keyword in a 'keyword=value' pair";

	public KeyWord(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(true);
                setAllowOrderedIndexEntry(true);
	}
}
