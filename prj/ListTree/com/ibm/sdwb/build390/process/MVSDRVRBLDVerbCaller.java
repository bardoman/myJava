package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.mainframe.parser.DriverReportParser;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.library.*;
import java.util.*;
import java.io.File;
import javax.swing.JInternalFrame;
import com.ibm.sdwb.build390.mainframe.*;

//****************************************************************
//09/11/2003 #DEF.TST1533: IndexOutOfBoundException doing ++USERMOD
//09/15/2003 #DEF.TST1554: No prompt to select correct build type
//09/23/2003 #DEF.TST1624: Fastrack does not work
//12/31/2003 INT1655       FSTRKDEL doest not cleanup HFS stuff
//****************************************************************

public abstract class MVSDRVRBLDVerbCaller extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    public MVSDRVRBLDVerbCaller(String processName, int numberOfPhases, UserCommunicationInterface userComm) {
        super(processName, numberOfPhases, userComm); 
    }

    protected class CallDRVRBLD extends AbstractProcess {

        private com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = null;
        private MBBuild build = null;
        private int hostPhase = -1;
        private int returnCodeToFailOn = -1;
        private int totalNumberOfMVSPhases = -1;
        private String fastTrackArguments = null;
        private boolean invalidBuildType = false;
        private boolean doSpecialBuildOrderHandling = false;
        // assume we need another phase 

        private boolean anotherPhaseIsNecessary = true;

        private boolean fastTrack = false;

        private SourceInfo source = null;

        private BuildPhaseOnMVS mvsBuildPhaseStep = null;
        private CheckJobStatus jobChecker = null;
        private transient JInternalFrame parentInternalFrame = null;
        private AbstractProcess parentProcess =null;
        static final long serialVersionUID = 1111111111111111L;

        protected CallDRVRBLD(MBBuild tempBuild, com.ibm.sdwb.build390.process.steps.DriverReport tempDriverReportStep, int tempHostPhase, boolean tempHandleBuildOrder, AbstractProcess parentProcess) {
            super("MVS Phase "+tempHostPhase , 5,parentProcess);
            hostPhase = tempHostPhase;
            driverReportStep = tempDriverReportStep;
            build = tempBuild;
            doSpecialBuildOrderHandling = tempHandleBuildOrder;
            this.parentProcess = parentProcess;
            returnCodeToFailOn = getReturnCodeToFailOn(build.getOptions(), hostPhase);

        }

        public void setBuildSource(SourceInfo tempSource) {
            source = tempSource;
        }

        public void setParentInternalFrame(JInternalFrame tempFrame) {
            this.parentInternalFrame = tempFrame;
        }

        public void setFastTrackArguments(String tempArgs) {
            fastTrackArguments = tempArgs;
            if (fastTrackArguments!=null) {
                /* we don't need to run anything after purging the job output
                */
                setNumberOfSteps(4); 
            }
        }

        public int getTotalNumberOfMVSPhases() {
            return totalNumberOfMVSPhases;
        }

        public boolean isAnotherIterationNecessary() {
            if (doSpecialBuildOrderHandling) {
                // always do the load of the build order only once.
                return false;
            } else {
                return anotherPhaseIsNecessary;
            }
        }

        /**
         * This method is used to return the steps to run to accomplish
         * a process.   The step to run first, then the next step to run
         * and so on.  If you need to have a step repeated, that should be
         * handled in the step, not here.                   	 * @param stepToGet
         *
         * @return The step to run, null if there are no more steps
         */
        protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
            switch (stepToGet) {
            case 0:

                //Begin #DEF.TST1624:

                if (build.getClass().getName().equals("com.ibm.sdwb.build390.MBUBuild")) {
                    fastTrack =((MBUBuild) build).getFastTrack();
                }

                //Begin #DEF.TST1554:
                //if(!doSpecialBuildOrderHandling)
                if (!doSpecialBuildOrderHandling & !fastTrack)
                //End #DEF.TST1624:
                {
                    DriverInformation drvInfo = build.getDriverInformation();

                    List types = drvInfo.getBuildTypes();

                    if (!types.contains(build.get_buildtype())) {
                        Vector buildTypes = new Vector();

                        for (int i=0;i!=types.size();i++) {
                            buildTypes.add((String)types.get(i));
                        }

                        if (parentInternalFrame!=null) {
                            MBListBox lb = new MBListBox("Invalid Buildtype entered, Buildtype List",buildTypes, false, parentInternalFrame,getLEP());
                            String buildtypeselected =lb.getElementSelected();
                            if (buildtypeselected == null) {
                                throw new GeneralError("Build was cancelled on user request");
                            }
                            build.set_buildtype(buildtypeselected);
                            com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent buildtypeUpdate = new com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent(this);
                            buildtypeUpdate.setBuildtype(buildtypeselected);
                            handleUIEvent(buildtypeUpdate);
                        }
                        /* else
                         {
                             throw new GeneralError("Build type reset, the build type you entered " + build.get_buildtype() + " is not valid.  Here are valid buildtypes:"+ buildTypes);
                         }*/
                    }
                }
                //End #DEF.TST1554:

                if (fastTrackArguments==null) {
                    if (driverReportStep.getParser()==null) {
                        return driverReportStep;
                    }
                }
                return new NoOp(this);
            case 1:
                mvsBuildPhaseStep = new BuildPhaseOnMVS(build,hostPhase,returnCodeToFailOn,this);
                if (fastTrackArguments!=null) {
                    mvsBuildPhaseStep.setFastTrackArguments(fastTrackArguments);
                }
                if (doSpecialBuildOrderHandling) {
                    mvsBuildPhaseStep.setDoSpecialBuildOrderProcessing();
                }
                mvsBuildPhaseStep.setBuildSource(source);
                return mvsBuildPhaseStep;
            case 2:
                jobChecker = new CheckJobStatus(mvsBuildPhaseStep.getJobsCreatedByDriverBuildCall(),hostPhase, build.getSetup(),build.getBuildPath(),"DRVRBLD-Phase-"+hostPhase,this);
                jobChecker.setBuild(build);
                jobChecker.setFailureReturnCodesForIndividualJobs(mvsBuildPhaseStep.getResultParser().getBuildCCIntegerForJobMap());
                jobChecker.setParentInternalFrame(this.parentInternalFrame);
                if (!doSpecialBuildOrderHandling) {
                    jobChecker.setListGen(mvsBuildPhaseStep.getResultParser().getListGenSetting());
                    jobChecker.setListCopy(mvsBuildPhaseStep.getResultParser().getListCopySetting());
                }
                jobChecker.setMinimumFailureReturnCode(build.getOptions().getBuildCC());
                jobChecker.setPhaseFailureReturnCode(returnCodeToFailOn);
                jobChecker.setWorkPath(mvsBuildPhaseStep.getResultParser().getworkpath());
                return jobChecker;
            case 3:
                if (build.getOptions().isPurgeJobs() & !getCleanableEntity().getAllHeldJobs().isEmpty()) {
                    PurgeJobOutput jobPurgeStep = new PurgeJobOutput(getCleanableEntity(), build.getBuildPath(), this);
                    return jobPurgeStep;
                }
                return new NoOp(this);
            case 4:
                if (totalNumberOfMVSPhases < 0 &  !invalidBuildType) {
                    totalNumberOfMVSPhases = mvsBuildPhaseStep.getResultParser().getTotalNumberOfPhases();
                    // Client must not rely on parsed metadata from initial phase1 if BuildType=NONE.
                    if (!doSpecialBuildOrderHandling | !build.get_buildtype().equals("NONE")) {
                        // Get LSTCOPY from phase results, if not found, warn user if he selected LISTGEN=FAIL or ALL
                        // LSTCOPY: CLRTEST.PJSTEST.PAT.LISTINGS
                        String lstcopyLine = mvsBuildPhaseStep.getResultParser().getListCopySetting();
                        if (lstcopyLine == null) {
                            String listgen = mvsBuildPhaseStep.getResultParser().getListGenSetting();
                            if (listgen!=null) {
                                if (listgen.equals("FAIL")) {
                                    new MBMsgBox("Warning", "You selected the 'Save failed listings' option,\n but LSTCOPY is not defined in your BLDORDER.\n Failed listings will be left in held output.");
                                } else if (listgen.equals("ALL")) {
                                    new MBMsgBox("Warning", "You selected the 'Save all listings' option,\n but LSTCOPY is not defined in your BLDORDER.\n Failed listings will be left in held output.");
                                }
                            }
                        }
                    }
                }
                if ((doSpecialBuildOrderHandling | build.get_buildtype().equals("NONE")) & fastTrackArguments==null) {
                    String phase0FileName =  mvsBuildPhaseStep.getPrintFile().getAbsolutePath(); // start off with the original name
                    phase0FileName = phase0FileName.replace('1', '0'); // turn the phase number 1 to phase number 0
                    File phase0File = new File(phase0FileName);  // make it a file
                    mvsBuildPhaseStep.getPrintFile().renameTo(phase0File); // rename the results cause we have to rerun phase 1
                    driverReportStep.setForceNewReport(true);
                    return driverReportStep;
                }
                return new NoOp(this);
            }
            return null;

        }

        protected void postStep(int stepRun, int stepIteration)throws com.ibm.sdwb.build390.MBBuildException {
            switch (stepRun) {
            case 1:
                if (mvsBuildPhaseStep.getResultParser().getworkpath()!=null & fastTrackArguments != null) { /* INT1655  */
                    getCleanableEntity().addMVSFileSet(mvsBuildPhaseStep.getResultParser().getworkpath());
                }
                break;
            case 2:
                getCleanableEntity().addAllHeldJobs(jobChecker.getJobsHandled());
                if (!jobChecker.getFailedJobs().isEmpty()) {
                    handleFailure();
                }
                /*check a bunch of failure conditions 
                 Determine if a invalid buildtype was specified and first run of phase=1
                 if so, don't throw errors, drop down to buildtype handling code
                */
                if (mvsBuildPhaseStep.getReturnCode() == 8  &  mvsBuildPhaseStep.getResultParser().getBuildTypeError() & doSpecialBuildOrderHandling) {
                    invalidBuildType = true;
                } else if (mvsBuildPhaseStep.getResultParser().getErrorInfo()) {
                    throw new HostError("Error found in phase results", mvsBuildPhaseStep);
                } else if (mvsBuildPhaseStep.getReturnCode() >= returnCodeToFailOn) {
                    throw new HostError("Build command failed with return code "+mvsBuildPhaseStep.getReturnCode(),mvsBuildPhaseStep);
                }
                if (!jobChecker.getFailedJobs().isEmpty()) {
                    throw new HostError("Jobs failed during phase "+hostPhase, mvsBuildPhaseStep);
                }

                //Begin #DEF.TST1624:
                if (fastTrack) {
                    anotherPhaseIsNecessary = false;
                }
                //End #DEF.TST1624:

                break;
            case 4:
                if ((doSpecialBuildOrderHandling | build.get_buildtype().equals("NONE")) & fastTrackArguments==null) {
                    handleBuildOrderUpdateOfBuildTypes();
                }

                DriverReportParser rpt = driverReportStep.getParser();

                String build_type = build.get_buildtype();

                List tmpList =  rpt.getPhaseInforamtion(build.get_buildtype());

                if (tmpList.size()==0) {
                    throw  new GeneralError("Invalid build type");
                }

                com.ibm.sdwb.build390.mainframe.PhaseInformation tmpPhas = (com.ibm.sdwb.build390.mainframe.PhaseInformation)tmpList.get(hostPhase);

                String nameOfPhase = tmpPhas.getName(); 

                setName(getName()+" - " + nameOfPhase);
                if (hostPhase >= totalNumberOfMVSPhases | fastTrackArguments!=null) {
                    anotherPhaseIsNecessary = false;
                }
                break;
            }
        }

        private int getReturnCodeToFailOn(BuildOptions options, int hostPhase) {
            if (options.getBuildCCPhaseOverrides()!=null) {
                if (options.getBuildCCPhaseOverrides().length > hostPhase) {
                    if (options.getBuildCCPhaseOverrides()[hostPhase] != -1) {
                        int returnCode = options.getBuildCCPhaseOverrides()[hostPhase];
                        getLEP().LogSecondaryInfo( "Buildcc Override","buildcc for phase "+ hostPhase + " has an override of "+ returnCode);
                        return returnCode;
                    }
                }
            }
            return options.getBuildCC();
        }

        private void handleBuildOrderUpdateOfBuildTypes() throws GeneralError{
            Vector buildTypes = driverReportStep.getParser().getBuildTypes();

            String selectedBuildType = null;

            if ((new String("NONE")).equals(build.get_buildtype())) {
                if (buildTypes.size() == 1) {
                    selectedBuildType =  ((String) buildTypes.firstElement()).trim();
                    build.set_buildtype(selectedBuildType);
                    com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent buildtypeUpdate = new com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent(this);
                    buildtypeUpdate.setBuildtype(build.get_buildtype());
                    handleUIEvent(buildtypeUpdate);
                } else {
                    //selectedBuildType is null.
                }

            } else {
                if (buildTypes.contains(build.get_buildtype())) {
                    selectedBuildType = build.get_buildtype(); //just to make sure selectedBuildType is not null.
                }
            }



            if (parentInternalFrame!=null) {
                if (selectedBuildType==null) {
                    MBListBox lb = new MBListBox("Invalid Buildtype entered, Buildtype List",buildTypes, false, parentInternalFrame,getLEP());
                    selectedBuildType  =lb.getElementSelected();
                    if (selectedBuildType == null) {
                        throw new GeneralError("Build was cancelled on user request");
                    }

                    build.set_buildtype(selectedBuildType.trim());
                    com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent buildtypeUpdate = new com.ibm.sdwb.build390.userinterface.event.build.BuildtypeUpdateEvent(this);
                    buildtypeUpdate.setBuildtype(selectedBuildType.trim());
                    handleUIEvent(buildtypeUpdate);
                }
            } else {
                if (selectedBuildType==null) {
                    throw new GeneralError("Build type reset, the build type you entered " + build.get_buildtype() + " is not valid.  Here are valid buildtypes:"+ buildTypes);
                }
            }

            build.setLocked(driverReportStep.getParser().getBuildid());
            driverReportStep.getParser().setBuildInformation(build);
            setName(getName()+" - " + ((com.ibm.sdwb.build390.mainframe.PhaseInformation) driverReportStep.getParser().getPhaseInforamtion(build.get_buildtype()).get(hostPhase)).getName());
            anotherPhaseIsNecessary = true;
        }

        private void handleFailure() {
            Thread failureHandler = new Thread() {
                public void run() {
                    try {
                        Set jobsToPurgeAfterFailure = null;
                        if (parentInternalFrame !=null) {
                            Set successJobs = jobChecker.getSuccessfulJobs();
                            MBMsgBox question = null;
                            if (!successJobs.isEmpty() || 
                                jobChecker.getFailedJobs().size() < jobChecker.getJobsHandled().size()) {
                                question = new MBMsgBox("Host Job Errors", "Some jobs failed.  Which host job results should be deleted?", parentInternalFrame, true, true);
                            } else {
                                question = new MBMsgBox("Host Job Errors", "All jobs failed.  Should host job results be deleted?", parentInternalFrame, true);
                            }
                            if (question.isAnswerAll() | question.isAnswerYes()) {
                                jobsToPurgeAfterFailure = jobChecker.getJobsHandled();
                            } else if (question.isAnswerSuccessful()) {
                                jobsToPurgeAfterFailure = jobChecker.getSuccessfulJobs();
                            }
                        }
                        if (jobsToPurgeAfterFailure!=null) {
                            PurgeJobOutput jobPurgeStep = new PurgeJobOutput(getCleanableEntity(), build.getBuildPath(), build.getProcessForThisBuild());
                            jobPurgeStep.setSubsetOfAllJobsToPurge(jobsToPurgeAfterFailure);
                            jobPurgeStep.externalExecute();
                        }
                    } catch (MBBuildException mbe) {
                        getLEP().LogException(mbe);
                    }
                }
            };
            failureHandler.start();
        }
    }
}
