package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;
import com.ibm.sdwb.build390.info.*;
import java.io.File;

public class ManualHoldDataOperations extends MainframeCommunication {
	static final long serialVersionUID = 1111111111111111L;

	private String track = null;
	private String holdCode = null;
	private MBBuild build = null;
	private File holdDataFile = null;
	private boolean setHoldData = false;
	private boolean getHoldData = false;

    private static String HOLDDATACONTAINER = "COMMENTS";

	public ManualHoldDataOperations(MBBuild tempBuild, String tempTrack, String tempCode, File tempHoldFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempBuild.getBuildPath()+"SETHOLD-"+tempTrack,"Manual Hold Data Operations", tempProc);
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
		build = tempBuild;
		track = tempTrack;
		holdCode = tempCode;
		holdDataFile = tempHoldFile;
	}

	public File getHoldDataFile(){
		return holdDataFile;
	}

	public void doSet(){
		setHoldData = true;
		getHoldData = false;
	}

	public void doGet(){
		getHoldData = true;
		setHoldData = false;
	}
	
	/**
	 * This is the method that should be implemented to actually
	 * run the process.	Use executionArgument if you need to 
	 * access the argument from the execute method.
	 * 
	 * @return Object indicating output of the step.
	 */
	public void execute() throws com.ibm.sdwb.build390.MBBuildException {
		getLEP().LogSecondaryInfo(getFullName(),"Entry");
        MBUtilities.validateTrackForSMOD(track);

		if (getHoldData) {
			String MVSFilename = new String(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+ "."+HOLDDATACONTAINER+"."+holdCode+track);
			MBFtp theFtp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
			if (!theFtp.get(MVSFilename, holdDataFile.getAbsolutePath(), true)) {
				throw new FtpError("Could not download "+holdDataFile+" from "+MVSFilename);
			}
		}else if (setHoldData) {
			if (holdDataFile.exists()) {
				FileInfo tempFileArgs = new FileInfo(holdDataFile.getParent(), holdDataFile.getName());
				tempFileArgs.setMainframeRecordType("F");
				tempFileArgs.setMainframeRecordLength(80);
				tempFileArgs.setFileType("ASCII");
                tempFileArgs.setLocalFile(holdDataFile);
                String MVSFilename = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+ "."+track;
				MBFtp theFtp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
				if (!theFtp.put(tempFileArgs, MVSFilename, false)) {
					throw new FtpError("Could not upload "+holdDataFile+" to "+MVSFilename);
				}
				com.ibm.sdwb.build390.library.LibraryInfo libInfo = build.getSetup().getLibraryInfo();
				String cmd = "USRHOLD "+libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', INDSN=\'"+MVSFilename.toUpperCase()+"\', HCOD=\'"+holdCode+"\', TRACK=\'"+track+"\'";
				createMainframeCall(cmd, "Loading Hold Data for change set " + track, build.getSetup().getMainframeInfo());
				runMainframeCall();
			} else {
				throw new GeneralError("Hold Data file " + holdDataFile + " not found");
			}
		} else{
			throw new GeneralError(getFullName()+" Set or get not selected");
		}
	}
}
