package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import java.util.*;
import java.io.File;

public class CheckMVSMetadataValidity extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.CheckMVSMetadataValidity checkMetadataStep = null;
	
	public CheckMVSMetadataValidity(MBBuild tempBuild, Vector tempPartSelectionCriteria, UserCommunicationInterface userComm){
		super("Check MVS metadata validity",1, userComm); 
		checkMetadataStep = new com.ibm.sdwb.build390.process.steps.CheckMVSMetadataValidity(tempBuild,tempPartSelectionCriteria, this);
	}

	public void setParentFrame(MBInternalFrame tempFrame) {
		checkMetadataStep.setParentFrame(tempFrame);
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return checkMetadataStep;
		}
		return null;
	}
}
