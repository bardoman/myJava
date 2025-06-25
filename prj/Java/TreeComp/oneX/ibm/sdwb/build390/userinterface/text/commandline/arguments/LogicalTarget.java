
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LogicalTarget extends IndexedArgument {

	private static final String keyword = "LOGICALTARGET";
	private static final String explaination = "The Track for which the  IF-REQ is going to be associated with";

	public LogicalTarget(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(false);
                setAllowOrderedIndexEntry(true);
	}
}
