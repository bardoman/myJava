package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;

public class ModelFileName extends IndexedArgument {

	private static final String keyword = "MODELNAME";
	private static final String explaination ="Specifies the name of the model parts."+
                                                  "Modeling is only allowed in FASTTRACK mode";

	public ModelFileName(){
		super(keyword,explaination);
                setAllowIndexOnly(true);
                setStopOnBrokenIndex(false);
                setAllowOrderedIndexEntry(false);
	}
}
