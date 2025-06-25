
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class UnlockDriverSwitch extends CommandLineSwitch {

	private static final String keyword = "UNLOCK";
	private static final String explaination = "unlocks the driver assoctiated with the build.";

	public UnlockDriverSwitch(){
		super(keyword,explaination);
	}
}
