package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class CommentsForPackaging extends CommandLineArgument {

	private static final String keyword = "COMMENTS";
	private static final String explaination = "Specifies comments to use when packaging.\n*(default) uses the current service comments if they exist.\n"+
												"Otherwise a filename may be supplied to add or replace current comments.";

	public CommentsForPackaging(){
		super(keyword,explaination);
	}
}
