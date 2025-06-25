package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.CreateVerbFiles;
import java.util.Set;
import java.io.*;

public class GenerateLoadVerbStep extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;
	private MBBuild build = null;
	private File shadowLoadVerb = null;
	private Set filesToCheck = null;

	public GenerateLoadVerbStep(MBBuild tempBuild, Set tempFilesToCheck, File tempShadowLoad, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Generate Load Verb Step");
		build = tempBuild;
		filesToCheck = tempFilesToCheck;
		shadowLoadVerb = tempShadowLoad;
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
				BufferedWriter shadowLoadWriter = new BufferedWriter(new FileWriter(shadowLoadVerb));
				verbWriter.makePartlistShadowLoadFile(build,filesToCheck,shadowLoadWriter);
				shadowLoadWriter.close();
			}catch (java.io.IOException ioe){
				throw new com.ibm.sdwb.build390.GeneralError("An error occurred writing the shadow load and built status check files", ioe);
			}
		}
	}
}
