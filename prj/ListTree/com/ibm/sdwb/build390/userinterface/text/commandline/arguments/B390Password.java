
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class B390Password extends IndexedArgument {

	private static final String keyword = "B390_PASSWORD";
	private static final String explaination ="An environment variable to supply the password using the serverkey";

	public B390Password(){
		super(keyword,explaination);
                setAllowIndexOnly(false);
                setStopOnBrokenIndex(true);
                setAllowOrderedIndexEntry(true);
	}
}
