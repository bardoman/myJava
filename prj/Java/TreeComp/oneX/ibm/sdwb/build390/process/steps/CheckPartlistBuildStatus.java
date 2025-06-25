package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import java.util.Iterator;
import java.io.File;

//***********************************************************
//09/11/2003 #DEF.TST1488: Inactivate option ignored
//***********************************************************


public class CheckPartlistBuildStatus extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;
    private MBBuild build = null;
    private MBChkFileParser partlistCheckOutputParser = null;
    private File partlistQueryFile = null;
    private static final String DRIVERCHK = new String("driverCheck");
    private boolean allPartsBuilt = false;
    private int hostRC = -1;

    public CheckPartlistBuildStatus(MBBuild tempBuild, File tempQueryFile, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath() + DRIVERCHK + "-"+tempBuild.getReleaseInformation().getMvsName()+"-"+tempBuild.getDriverInformation().getName(),"Check Partlist Built Status", tempProc);
        setUndoBeforeRerun(false);
        build = tempBuild;
        partlistQueryFile = tempQueryFile;
    }

    public MBChkFileParser getOutputParser() {
        return partlistCheckOutputParser;
    }

    public void setAllPartsBuilt(boolean allPartsBuilt) {
        this.allPartsBuilt = allPartsBuilt;
    }

    public boolean isAllPartsBuilt() {
        boolean isForceBuild = build.getOptions().getForce().equals("YES") | build.getOptions().getForce().equals("ALL");
        if ((getReturnCode() == 0)
            ||((getReturnCode() < 0) && allPartsBuilt)) {//this means this step wasn't executed.
                allPartsBuilt = !isForceBuild;
            }
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
        if (! (new String("MIGRATE")).equals(build.get_buildtype()) & partlistQueryFile!=null) {
            getStatusHandler().updateStatus("Uploading file for driver check", false);
            String MVSOrdersFile = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS("+build.get_buildid()+")";
            getLEP().LogSecondaryInfo(getName(),"Uploading "+partlistQueryFile+" to "+MVSOrdersFile);
            MBFtp mftp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
            if (!mftp.put(partlistQueryFile, MVSOrdersFile)) {
                throw new FtpError("Could not upload "+partlistQueryFile.getAbsolutePath()+" to "+MVSOrdersFile);
            }
        }
        LibraryInfo libInfo = build.getSetup().getLibraryInfo();

        if (!build.getOptions().isSkippingDriverCheck()) {
            String driverCheckCommand = "DRVRCHK CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER=\'"+build.getDriverInformation().getName()+"\', BUILDID="+build.get_buildid()+", "+libInfo.getDescriptiveStringForMVS();

            if (build.getOptions().getExtraDriverCheck()!=null) {
                driverCheckCommand+=", EXTRACHK="+build.getOptions().getExtraDriverCheck();
            }

            createMainframeCall(driverCheckCommand, "Checking file build status in driver", true, build.getSetup().getMainframeInfo());
            setPathToVerbFile(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS");
            runMainframeCall();
            getStatusHandler().updateStatus("Verifying results of file build status check", false);
            partlistCheckOutputParser = new MBChkFileParser(getOutputFile(), build.getMapOfPartNameToPartInfo(), getLEP());
            java.util.Set errorHash = partlistCheckOutputParser.getTableEntriesContaining("*ERROR*");
            if (!errorHash.isEmpty()) {
                throw new HostError("There were errors in the driverchk file", this);
            }


            //Begin PTM4389
            else if (getReturnCode() >4) {
                throw new HostError("There was an error executing the driver check command", this);
            }
            //End PTM4389

        }
    }
}
