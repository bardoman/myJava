package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.CreateVerbFiles;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.io.*;

public class GenerateBuildVerbStep extends ProcessStep {
	static final long serialVersionUID = 9145081845635846416L;
	private MBBuild build = null;
	private boolean buildOrderUpdated = false;
	private File buildVerbFile = null;
	private String fastTrackArguments = null;
	private Set unbuiltFiles = null;
	private Set rebuildFiles = null;
	private boolean allPartsBuilt = false;
	private transient Writer buildVerbWriter = null;

	public GenerateBuildVerbStep(MBBuild tempBuild, Set tempUnbuilt, Set tempRebuild, File tempBuildVerbFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"Generate Build Verb Step");
		setUndoBeforeRerun(false);
		build = tempBuild;
		buildVerbFile  = tempBuildVerbFile;
		unbuiltFiles = tempUnbuilt;
		rebuildFiles = tempRebuild;
	}

	public void setBuildVerbWriter(Writer tempBuildVerbWriter){
		buildVerbWriter = tempBuildVerbWriter;
	}

	public boolean isBuildOrderUpdated(){
		return buildOrderUpdated;
	}

        public void setBuildOrderUpdated(boolean buildOrderUpdated){
            this.buildOrderUpdated = buildOrderUpdated;
        }
	public boolean getAllPartsBuilt(){
		return allPartsBuilt;
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
		getStatusHandler().updateStatus("Build verb file being created.", false);
		CreateVerbFiles verbWriter = new CreateVerbFiles(getLEP());
		try {
			if (buildVerbFile!=null) {
				buildVerbWriter  = new BufferedWriter(new FileWriter(buildVerbFile));
			}
			verbWriter.makeMVSBuildVerbFile(build, unbuiltFiles, rebuildFiles, buildVerbWriter);
			buildVerbWriter.close();
		}catch (java.io.IOException ioe){
			throw new com.ibm.sdwb.build390.GeneralError("Writing build verb file", ioe);
		}
		if (!build.get_buildtype().equals("MIGRATE") & buildVerbFile!=null) {
			buildOrderUpdated = verbWriter.isBuildOrderUpdated();
			if (!verbWriter.isBuildUpToDate()) {
				String MVSOrdersFile = new String(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS("+build.get_buildid()+")");
				getLEP().LogSecondaryInfo(getName(),"Uploading "+buildVerbFile.getAbsolutePath()+" to "+MVSOrdersFile);
				getStatusHandler().updateStatus("Uploading build order file ",false);
				MBFtp mftp = new MBFtp(build.getMainframeInfo(),getLEP());
				if (!mftp.put(buildVerbFile, MVSOrdersFile)) {
					throw new FtpError("Could not upload "+buildVerbFile.getAbsolutePath()+" to "+MVSOrdersFile);
				}
			} else {
				allPartsBuilt = true;
			}
		}
	}
}
