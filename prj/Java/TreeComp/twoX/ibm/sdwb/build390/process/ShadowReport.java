package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class ShadowReport extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.ShadowReport getShadowReport = null;
	
	public ShadowReport(Setup tempSetup, String tempCmvcRelease, UserCommunicationInterface userComm){
		super("Get Shadow report",1, userComm); 
		getShadowReport = new com.ibm.sdwb.build390.process.steps.ShadowReport(tempSetup, tempCmvcRelease, this);
	}

	public void setShowFilesAfterRun(boolean showOut, boolean showPrint){
		getShadowReport.setShowFilesAfterRun(showOut, showPrint);
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return getShadowReport;
		}
		return null;
	}
}
