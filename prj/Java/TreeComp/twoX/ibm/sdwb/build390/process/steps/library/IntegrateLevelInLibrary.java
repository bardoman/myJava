package com.ibm.sdwb.build390.process.steps.library;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;

public class IntegrateLevelInLibrary extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private ChangesetGroup sourceInfo = null;

	public IntegrateLevelInLibrary(ChangesetGroup tempInfo, String tempRelease, String tempLevel, String tempAuthorizationTypeToUse, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Integrate in library");
		setVisibleToUser(false);
		setUndoBeforeRerun(true);
		sourceInfo = tempInfo;
	}

	public final boolean isUndoable() {
		return false;
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
		getStatusHandler().updateStatus("Integrate " + sourceInfo.getName() + " in library ",false);
		sourceInfo.setStateToIntegrate();
	}
}
