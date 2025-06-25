package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;
import java.io.*;

public class LibraryPartlistGeneration extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;
	private MBBuild build = null;
    private static final String PARTLISTtrc     = "partlist.trace";

	public LibraryPartlistGeneration(MBBuild tempBuild, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Library Partlist Generation");
		build = tempBuild;
		setUndoBeforeRerun(false);
	}

	/**
	 * This is the method that should be implemented to actually
	 * run the process.	Use executionArgument if you need to 
	 * access the argument from the execute method.
	 * 
	 * @return Object indicating output of the step.
	 */
	public void execute() throws com.ibm.sdwb.build390.MBBuildException{
		getLEP().LogSecondaryInfo(getFullName(),"Entry");
        getStatusHandler().updateStatus("Generating part list", false);
		try{
			BufferedWriter partlistTraceWriter = new BufferedWriter(new FileWriter(build.getBuildPath()+PARTLISTtrc));
			build.setPartInfo( build.getSource().getSetOfParts());
			partlistTraceWriter.close();
		}catch (java.io.IOException ioe){
			throw new com.ibm.sdwb.build390.GeneralError("Writing to the partlist trace file", ioe);
		}
	}
}
