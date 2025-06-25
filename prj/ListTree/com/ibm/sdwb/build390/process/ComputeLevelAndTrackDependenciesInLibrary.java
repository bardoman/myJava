package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import java.util.*;

public class ComputeLevelAndTrackDependenciesInLibrary extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.ComputeLevelAndTrackDependenciesInLibrary computeDependencyStep = null;
	
	public ComputeLevelAndTrackDependenciesInLibrary(LibraryInfo source, Map releaseToTrackMap, Set tempStatesToHandle, boolean tempIncludeStatesListed, UserCommunicationInterface userComm) throws com.ibm.sdwb.build390.MBBuildException{
		super("Compute Level and Track",1, userComm); 
		computeDependencyStep = new com.ibm.sdwb.build390.process.steps.ComputeLevelAndTrackDependenciesInLibrary(source, releaseToTrackMap, tempStatesToHandle, tempIncludeStatesListed, this);
	}

	public Map getComputedDependencies(){
		return computeDependencyStep.getComputedDependencies();
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return computeDependencyStep;
		}
		return null;
	}
}
