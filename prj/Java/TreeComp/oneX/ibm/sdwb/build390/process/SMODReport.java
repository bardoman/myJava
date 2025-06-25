package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import java.util.*;

public class SMODReport extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private MBBuild build = null;
	private com.ibm.sdwb.build390.process.steps.SMODReport smodReportStep = null;
	
	public SMODReport(MBBuild tempBuild, String reportType, UserCommunicationInterface userComm){
		super("Get SMOD Report",1, userComm); 
		build = tempBuild;
		smodReportStep = new com.ibm.sdwb.build390.process.steps.SMODReport(build, reportType, this);
	}

	public void setShowFilesAfterRun(boolean showOut, boolean showPrint){
		smodReportStep.setShowFilesAfterRun(showOut, showPrint);
	}

	public void setIncludePathname(boolean tempInclude){
		smodReportStep.setIncludePathname(tempInclude);
	}

	public void setJustGetReport(boolean tempGet){
		smodReportStep.setJustGetReport(tempGet);
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return smodReportStep;
		}
		return null;
	}
}
