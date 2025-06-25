package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;
import java.util.*;
import java.io.File;
import javax.swing.JInternalFrame;

//***********************************************************************
//09/06/2003 #DEF.TST1492: PDS FASTRACK FAILS
//***********************************************************************

public class BuildPhaseOnMVS extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private Set jobsCreatedByDriverBuildCall = null;
    private SourceInfo sourceInfo = null;
    private boolean doSpecialBuildOrderProcessing = false;
    private String fastTrackArguments = null;
    private MBPhaseResultFileParser resultFileParser = null;
    private int thisHostPhase = -1;
    private int returnCodeToFailOn = -1;

    private static final String PHASERESULTS = new String("phaseresults");

    public BuildPhaseOnMVS(MBBuild tempBuild, int tempHostPhase, int tempRCToFailOn, AbstractProcess tempProc) {
        super(tempBuild.getBuildPath() + PHASERESULTS+tempHostPhase,"MVS Phase "+tempHostPhase, tempProc);
        thisHostPhase = tempHostPhase;
        build = tempBuild;
        returnCodeToFailOn = tempRCToFailOn;
        setUndoBeforeRerun(false);
        jobsCreatedByDriverBuildCall = new HashSet();
    }

    public void setBuildSource(SourceInfo tempSource) {
        sourceInfo = tempSource;
    }

    public void setFastTrackArguments(String tempArgs) {
        fastTrackArguments = tempArgs;
    }

    public void setDoSpecialBuildOrderProcessing() {
        doSpecialBuildOrderProcessing = true;
    }

    /**
     * Determines if this action can be rolled back, so that
     * systems are in the state they were in before this
     * occurred.   For things like reports, or simple queries,
     * this should be true because, since they don't update
     * anything, there's no state change, so things are alreay
     * in the state they were in previously.   CMVC level commit
     * is the only thing I am currently SURE this will be false
     * for.
     *
     * @return true if this action can be undone, or is a non-state changing
     *         action
     */
    public boolean isUndoable() {
        return false;
    }

    public Set getJobsCreatedByDriverBuildCall() {
        return jobsCreatedByDriverBuildCall;
    }

    public MBPhaseResultFileParser getResultParser() {
        return resultFileParser;
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
        getStatusHandler().updateStatus("Phase " + thisHostPhase, false);

        String driverBuildCommand = createMVSDriverBuildCommand(build,returnCodeToFailOn);
        createMainframeCall(driverBuildCommand, "Phase " + thisHostPhase+": Submitting Build command", true, build.getSetup().getMainframeInfo());
        setPathToVerbFile(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+".ORDERS");
        unset_clrout();
        runMainframeCall();

        getStatusHandler().updateStatus("Phase " + thisHostPhase + ": processing results", false);
        resultFileParser = new MBPhaseResultFileParser(getPrintFile());
        if(resultFileParser.getJobInfo()!=null) {
            jobsCreatedByDriverBuildCall.addAll(resultFileParser.getJobInfo().values());
        }
        getCleanableEntity().addAllHeldJobs(jobsCreatedByDriverBuildCall);
        if(fastTrackArguments!=null) {
            String prefix = resultFileParser.getFileSetting("PREFIX:");
            String fastTrackHLQ = resultFileParser.getFileSetting("FTHLQ:");
            getCleanableEntity().addMVSFileSet(prefix+"."+build.get_buildid());
            if(fastTrackHLQ !=null) {
                getCleanableEntity().addMVSFileSet(fastTrackHLQ+"."+prefix+"."+build.get_buildid());
            }
        }
    }

    private String createMVSDriverBuildCommand(MBBuild build, int buildConditionCode) throws LibraryError{
        LibraryInfo libInfo = build.getSetup().getLibraryInfo();
        String DriverBuildVerbCommand = new String();

        //Begin TST3344
        boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();
        boolean isPDSBuild = false;
        boolean isNolib=false;

        if(build instanceof MBUBuild) {

            isPDSBuild = ((MBUBuild) build).getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
        }

        if(isFakeLib & isPDSBuild) {
            isNolib = true;
        }
        //End TST3344

        if(fastTrackArguments != null) {
            if(fastTrackArguments.trim().startsWith(",")) {
                fastTrackArguments = fastTrackArguments.substring(1);
            }

            DriverBuildVerbCommand = "FASTRACK OP=BUILD, "+fastTrackArguments;
        }
        else {
            String buildtype = build.get_buildtype();
            if(doSpecialBuildOrderProcessing) {
                buildtype = "NONE";
            }
            DriverBuildVerbCommand = "DRVRBLD OP=BUILD, BLDTYPE="+buildtype+", AUTOBLD="+build.getOptions().getAutoBuild()+
                                     ", RUNSCAN="+BinarySettingUtilities.convertToMainframeSetting(build.getOptions().isRunScanners())+", LISTGEN="+build.getOptions().getListGen()+", PHASE="+thisHostPhase;

        }

        //Begin TST3344
        if(isNolib) {
            DriverBuildVerbCommand+= ", NOLIB=YES";
        }
        //End TST3344

        DriverBuildVerbCommand += ", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER="+build.getDriverInformation().getName()+ 
                                  ", "+libInfo.getDescriptiveStringForMVS()+
                                  ", BUILDID="+build.get_buildid()+", BUILDCC="+buildConditionCode;


        String itemString = sourceInfo.getSourceIdentifyingStringForMVS();

        if(!build.getSource().isIncludingCommittedBase()) {
            itemString+=" DELTA";
        }
        else {
            itemString+=" FULL";
        }


        //UserBldUpdate0
        if(build instanceof MBUBuild) {
            switch(((MBUBuild)build).getSourceType()) { /* TST1740 */
            case MBUBuild.LOCAL_SOURCE_TYPE :
                itemString =  "Local parts  ";
                break;
            case MBUBuild.PDS_SOURCE_TYPE :
                itemString =  "PDS " ;
                break;
            default:
                break;
            }

            itemString += ((MBUBuild)build).getLocalParts()[0];
        }
        //UserBldUpdate0

        if(itemString !=null && itemString.length() > 0) {
            itemString = itemString.replaceAll("^[ \\t]+|[ \\t]+$",""); /* trim starting and ending space and tab char */
        }

        DriverBuildVerbCommand += ", QUERY=\'"+(itemString.length() >=50 ? itemString.substring(0,49) :  itemString) +"\'"; /*TST1806 */


        if(build.getOptions() instanceof com.ibm.sdwb.build390.info.BuildOptionsLocal) {
            String[] userMacs  =((com.ibm.sdwb.build390.info.BuildOptionsLocal) build.getOptions()).getUserMacs();
            if(userMacs != null) {
                for(int i = 0 ; i != userMacs.length; i++) {
                    DriverBuildVerbCommand = DriverBuildVerbCommand + ", PVT" + (i + 1)+ "=" + userMacs[i];
                }
            }
        }
        // build build command

        if(build.getOptions().getXmitTo() != null) {
            if(build.getOptions().getXmitTo().trim().length() > 0) {
                if(build.getOptions().getXmitType() != null) {
                    DriverBuildVerbCommand = DriverBuildVerbCommand + ", XMITOBJ="+build.getOptions().getXmitTo()+", OBJTYPE="+build.getOptions().getXmitType();
                }
            }
        }

        if(build.getOptions().isGeneratingDebugFiles()) {
            if(build.getOptions().isSaveDebugFiles()) {
                DriverBuildVerbCommand = DriverBuildVerbCommand + ", LOGIC=DEBUGSAVE";
            }
            else {
                DriverBuildVerbCommand = DriverBuildVerbCommand + ", LOGIC=DEBUG";
            }

            if(build.getOptions().isXmitDebugFiles()) {
                DriverBuildVerbCommand = DriverBuildVerbCommand + ", XMITDBUG="+build.getOptions().getXmitDebugFileLocation();
            }
        }


        // SyncDriver
        if(build.getOptions().isSynchronizingDriver() & (thisHostPhase == 1)) {
            DriverBuildVerbCommand = DriverBuildVerbCommand + ", SYNCH=YES";
        }
        if(build.getOptions().getForce().equals("YES") | build.getOptions().getForce().equals("ALL")) {
            DriverBuildVerbCommand = DriverBuildVerbCommand + ", FORCE="+build.getOptions().getForce();
        }
        if(build.getOptions().isDryRun()) {
            DriverBuildVerbCommand = DriverBuildVerbCommand + ", DRYRUN=YES";
        }

        for (Map.Entry<String,String> entry : build.getBuildSettings().entrySet()) {
            String oneKeyword = entry.getKey();
            if(oneKeyword.length() > 0) {
                DriverBuildVerbCommand = DriverBuildVerbCommand + ", "+oneKeyword+"=";
                String oneValue = entry.getValue().trim();
                // add the value
                if(!(oneValue.startsWith("'") & oneValue.endsWith("'"))) {
                    oneValue = oneValue.toUpperCase();
                }
                DriverBuildVerbCommand = DriverBuildVerbCommand + oneValue;
            }
        }

        return DriverBuildVerbCommand;
    }

}
