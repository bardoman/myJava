package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.CreateVerbFiles;
import java.util.*;
import java.io.*;

public class GenerateBuiltStatusCheckVerbStep extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;
	private MBBuild build = null;
	private Set filesToCheck = null;
	private File builtStatusCheckVerb = null;

	public GenerateBuiltStatusCheckVerbStep(MBBuild tempBuild, Set tempFilesToCheck, File tempDriverCheck, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Generate Build Status Check Verb Step");
		build = tempBuild;
		filesToCheck = tempFilesToCheck;
		builtStatusCheckVerb = tempDriverCheck;
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
		if (!build.get_buildtype().equals("MIGRATE")) {
            getStatusHandler().updateStatus("Generate MVS instruction files", false);
			CreateVerbFiles verbWriter = new CreateVerbFiles(getLEP());
			try {
				BufferedWriter checkFileWriter = new BufferedWriter(new FileWriter(builtStatusCheckVerb));
				verbWriter.makePartlistBuiltStatusCheckFile(build,filesToCheck,checkFileWriter);
				checkFileWriter.close();
			}catch (java.io.IOException ioe){
				throw new com.ibm.sdwb.build390.GeneralError("An error occurred writing the shadow load and built status check files", ioe);
			}
		}
	}
}
