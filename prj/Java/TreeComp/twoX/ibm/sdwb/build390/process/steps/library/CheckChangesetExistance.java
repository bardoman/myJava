package com.ibm.sdwb.build390.process.steps.library;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.SourceInfo;
import java.util.*;

public class CheckChangesetExistance extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private String releaseName = null;
	private String track = null;
	private SourceInfo sourceInfo = null;
	private boolean trackFound = false;

	public CheckChangesetExistance(SourceInfo tempInfo, String tempRelease, String tempTrack, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Check track existance in library");
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
		sourceInfo = tempInfo;
	}

	public boolean isTrackFound(){
		return trackFound;
	}
	
	/**
	 * This is the method that should be implemented to actually
	 * run the process.	Use executionArgument if you need to 
	 * access the argument from the execute method.
	 * 
	 * @return Object indicating output of the step.
	 */
	public void execute() throws com.ibm.sdwb.build390.MBBuildException {
		getLEP().LogSecondaryInfo(getFullName(),"Entry");
        getStatusHandler().updateStatus("Check source " + sourceInfo.getName() + "  existance in library ",false);
		trackFound = sourceInfo.isValidSource();
	}
}
