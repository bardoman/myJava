package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DataRetrievalSendTo extends CommandLineArgument {

	private static final String keyword = "SENDTO";
	private static final String explaination ="The node.userid to send the information to.\n";


	public DataRetrievalSendTo(){
		super(keyword,explaination);
	}
}
