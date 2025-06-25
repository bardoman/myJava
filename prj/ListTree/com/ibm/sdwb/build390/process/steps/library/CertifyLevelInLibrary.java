package com.ibm.sdwb.build390.process.steps.library;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import java.util.*;

public class CertifyLevelInLibrary extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private ChangesetGroup sourceInfo = null;

	public CertifyLevelInLibrary(ChangesetGroup tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Certify in library");
		setVisibleToUser(false);
		setUndoBeforeRerun(true);
		sourceInfo = tempInfo;
	}

	public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        getStatusHandler().updateStatus("Ready "+ sourceInfo.getName()+" in library ",false);
		sourceInfo.setStateToReady();
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
        getStatusHandler().updateStatus("Certify "+ sourceInfo.getName()+" in library ",false);
		sourceInfo.setStateToCertify();
	}
}
