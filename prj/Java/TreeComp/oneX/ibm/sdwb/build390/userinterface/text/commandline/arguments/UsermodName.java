package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class UsermodName extends CommandLineArgument {

	private static final String keyword = "USERMODNAME";
	private static final String explaination = "Name of the Usermod the apar would be packaged as. "+
												"The format must be 7 characters, 2 alphas followed by 5 numerics. Default is a system chosen value";

	public UsermodName(){
		super(keyword,explaination);
	}
}
