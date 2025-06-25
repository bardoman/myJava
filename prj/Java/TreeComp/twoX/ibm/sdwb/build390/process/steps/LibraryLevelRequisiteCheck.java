package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import java.util.*;

public class LibraryLevelRequisiteCheck extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private String authorizationTypeToUse = null;
	private SourceInfo sourceInfo = null;
	private Set requisiteTracksMissingFromLevel = null;

	public LibraryLevelRequisiteCheck(SourceInfo tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Check Level in Library");
		setVisibleToUser(false);
		sourceInfo = tempInfo;
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
        getStatusHandler().updateStatus("Running \'Level - check\' (to verify prereq/coreq) for "+sourceInfo.getName()+" in library",false);
		requisiteTracksMissingFromLevel = new HashSet(sourceInfo.getListOfMissingRequisites());
	}

	public Set getRequisiteTracksMissingFromLevel(){
		return requisiteTracksMissingFromLevel;
	}
}
