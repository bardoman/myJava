package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;

public class CheckConnectionToLibrary extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private LibraryInfo libInfo = null;
	private String release = null;

	public CheckConnectionToLibrary(LibraryInfo tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Check connection to library");
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
		libInfo = tempInfo;
	}

	public void setRelease(String tempRelease){
		release = tempRelease;
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
		if (release==null) {
			libInfo.isLibraryConnectionValid();
		}else {
			libInfo.isValidLibraryProject(release);
		}
	}
}
