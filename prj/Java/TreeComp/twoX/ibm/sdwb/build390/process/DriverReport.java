package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import java.util.*;
import java.io.File;

public class DriverReport extends AbstractProcess {
	static final long serialVersionUID = 1111111111111111L;

	private com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = null;
	
	public DriverReport(DriverInformation tempDriver, MBMainframeInfo tempMain, LibraryInfo tempLib, File saveLocation, UserCommunicationInterface userComm){
		super("Get Driver Report",1, userComm);
        if (saveLocation == null) {
			saveLocation = new File(MBGlobals.Build390_path+File.separator+MBConstants.GENERICBUILDDIRECTORY);
		}
		driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(tempDriver, tempMain, tempLib, saveLocation, this);
	}

	public void setShowFilesAfterRun(boolean showOut, boolean showPrint){
		driverReportStep.setShowFilesAfterRun(showOut, showPrint);
	}

	public void setIncludePathname(boolean tempInclude){
		driverReportStep.setIncludePathname(tempInclude);
	}

	public void setJustGetReport(boolean tempGet){
		driverReportStep.setJustGetReport(tempGet);
	}

	public void setSummaryType(String summaryType){
		driverReportStep.setSummaryType(summaryType);
	}

	protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
		switch (stepToGet) {
			case 0:
				return driverReportStep;
		}
		return null;
	}
}
