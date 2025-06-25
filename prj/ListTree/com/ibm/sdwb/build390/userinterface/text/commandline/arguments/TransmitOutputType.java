package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class TransmitOutputType extends CommandLineArgument {

	private static final String keyword = "XMITTYPE";
	private static final String explaination = "Determines type of object to transmit.\n(OBJ, PLNK, LKED) Used with XMITTO";


	public TransmitOutputType(){
		super(keyword, explaination);
	}
}
