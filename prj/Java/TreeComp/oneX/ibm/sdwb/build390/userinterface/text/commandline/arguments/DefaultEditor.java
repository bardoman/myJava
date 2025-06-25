package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class DefaultEditor extends BinaryCommandLineArgument {

	private static final String keyword = "DEFAULTEDITOR";
        private static final String explaination = "\n<yes/(no)>" +
                                                   "\nyes - Use the default editor." +
                                                   "\nno  - Donot use the default editor.";

	public DefaultEditor(){
		super(keyword,explaination,true);
	}
}
