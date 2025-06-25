package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import java.util.*;

public class HeldJobOutputRetrieval extends MainframeCommunication {
	static final long serialVersionUID = 1111111111111111L;

	private MBMainframeInfo mainInfo = null;
	private Set jobsToHandle = null;
	private Map jobNameToOutputLocationMap = null;
	private String outputPath = null;

	public HeldJobOutputRetrieval(Set tempJobsToHandle, MBMainframeInfo tempInfo, String tempOutputPath, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(null, "HeldJobOutputRetrieval", tempProc);
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
		mainInfo = tempInfo;
		jobsToHandle = tempJobsToHandle;
		outputPath = tempOutputPath;
	}

	public Map getJobNameToOutputLocationMap(){
		return jobNameToOutputLocationMap;
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
		Iterator jobStringIterator = jobsToHandle.iterator();
		while (jobStringIterator.hasNext()) {
			String jobString = ((String) jobStringIterator.next()).toUpperCase();
			// Build the paths to the output files
			setOutputHeaderLocation(outputPath+"JOBOUTPUT"+com.ibm.sdwb.build390.utilities.ParsingFunctions.cleanString(jobString));
			if (!getOutputFile().exists()) {
				createMainframeCall(jobString,  "Retrieving held job output for "+jobString,mainInfo);
				setSysout();
				setDelsysout();
				unset_clrout();
				runMainframeCall();
			} else{
                            getStatusHandler().updateStatus("Retrieving the cached held job output for " + jobString,false);
                            MBEdit edit   =  new MBEdit(getOutputFile().getAbsolutePath(),false,getLEP());
                        }
		}
	}
}
