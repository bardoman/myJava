package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.Map;
import java.util.Iterator;
import java.io.File;

public class SMODReport extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;
	private MBBuild build = null;
	private MBSmodDrvrReport smodReport = null;
	private String reportType = null;
	private boolean showPrintFile = false;
	private boolean showOutFile = false;

	public SMODReport(MBBuild tempBuild, String tempReportType, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"SMOD Report");
		setUndoBeforeRerun(false);
		build = tempBuild;
		reportType = tempReportType;
		smodReport = new MBSmodDrvrReport(build, reportType, getStatusHandler(), getLEP());
	}

	public final void setShowFilesAfterRun(boolean tempShowOutFile, boolean tempShowPrintFile){
		showOutFile = tempShowOutFile;
		showPrintFile = tempShowPrintFile;
	}

	public void setIncludePathname(boolean tempInclude){
		smodReport.setIncludePathname(tempInclude);
	}

	public void setJustGetReport(boolean tempGet){
		smodReport.setJustGetReport(tempGet);
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
		getStatusHandler().updateStatus("Getting SMOD report", false);
		smodReport.initializeReport();
		if (showOutFile) {
			MBEdit outFileDisplay = new MBEdit(MBGlobals.Build390_path+"misc"+File.separator+"SMODRPT-"+reportType+"-"+build.getReleaseInformation().getLibraryName()+"-"+build.getDriverInformation().getName()+MBConstants.CLEARFILEEXTENTION, getLEP());
		}
		if (showPrintFile) {
			MBEdit printFileDisplay = new MBEdit(MBGlobals.Build390_path+"misc"+File.separator+"SMODRPT-"+reportType+"-"+build.getReleaseInformation().getLibraryName()+"-"+build.getDriverInformation().getName()+MBConstants.PRINTFILEEXTENTION, getLEP());
		}
	}

	public MBSmodDrvrReport getSmodReportHandler(){
		return smodReport;
	}
}
