package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class Value extends IndexedArgument {

	private static final String keyword = "VALUE";
	private static final String explaination ="The string content of a value in a 'keyword=value' pair";

	public Value(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(true);
                setAllowOrderedIndexEntry(true);
	}
}
