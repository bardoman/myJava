

package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class LogicalReq extends IndexedArgument {

	private static final String keyword = "LOGICALREQ";
	private static final String explaination = "Specifies the logical condition as per the REQTYPE."+
                                                   "If REQTYPE is IFREQ, \nthe condition is FMID.SYSMOD.Multiple conditions are delimited by space";

	public LogicalReq(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(false);
                setAllowOrderedIndexEntry(true);
                changeValueToUpperCase();
	}
}
