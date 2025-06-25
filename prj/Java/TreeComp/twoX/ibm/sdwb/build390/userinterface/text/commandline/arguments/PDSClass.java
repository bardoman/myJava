package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class PDSClass extends CommandLineArgument {

	private static final String keyword = "PDSCLASS";
	private static final String explaination ="The CLASS type of the members in the pds (in NORMAL mode).\n"+
                                                  "Part Class  is normally used to specify the part class of the part contained in the PDS.\n"+
                                                  "The PARTTYPE   of the members in the pds (in NOLIB  mode)\n" +
                                                  "Part Type consists of the file extension for the part as it would\nappear in a local directory (ie: ASM, C, ORD, MAC, PLX...).";

	public PDSClass(){
		super(keyword,explaination);
	}
}
