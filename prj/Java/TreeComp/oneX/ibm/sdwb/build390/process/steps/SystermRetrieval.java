package com.ibm.sdwb.build390.process.steps;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.mainframe.*;


public class SystermRetrieval extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	private MBMainframeInfo mainInfo = null;
	private String processId = null;
	private String phaseToFetchSystermFor = null;
	private File retrievedSystermFile = null;
	private boolean displayFile = false;
        private String mvsSystermLocationPrefix = "";

	public SystermRetrieval(String tempProcessId, String tempPhaseToFetchSystermFor, MBMainframeInfo tempInfo, String outputPath, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc, "Systerm Retrieval");
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
		mainInfo = tempInfo;
		processId = tempProcessId;
		phaseToFetchSystermFor = tempPhaseToFetchSystermFor;
		retrievedSystermFile = new File(outputPath + "SYSTERM-Phase"+phaseToFetchSystermFor);
	}

        public void setMVSSystermLocationPrefix(String tempMVSSystermLocationPrefix){
            this.mvsSystermLocationPrefix = tempMVSSystermLocationPrefix;
        }

	public File getRetrievedSystermFile(){
		return retrievedSystermFile;
	}

	public void setDisplayFileAfterFetch(boolean display){
		displayFile = display;
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
		String mvsSystermLocation = mvsSystermLocationPrefix +"."+processId+".P"+phaseToFetchSystermFor+ "T";
		retrievedSystermFile.delete();
		MBFtp mftp = new MBFtp(mainInfo,getLEP());
		if (!mftp.get(mvsSystermLocation, retrievedSystermFile.getAbsolutePath(), true)) {
			throw new FtpError("Could not download systerm file, SYSTERM may not exist for this phase");
		}
		if (displayFile) {
			MBEdit showFile = new MBEdit(retrievedSystermFile.getAbsolutePath(), getLEP());
		}
	}
}
