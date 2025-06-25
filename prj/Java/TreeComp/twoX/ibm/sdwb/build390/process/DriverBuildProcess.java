package com.ibm.sdwb.build390.process;

import java.io.File;
import java.util.*;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.test.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.build.ProcessUpdateEvent;


public class DriverBuildProcess extends MVSDRVRBLDVerbCaller implements TestInfoGenerator {
    static final long serialVersionUID = 1111111111111111L;

    private transient JInternalFrame parentInternalFrame = null;

    private MBBuild build = null;
    private com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = null;
    private LibraryPartlistGeneration partlistGenerationStep = null;
    private CheckPartlistAgainstShadow loadOrderCheckStep = null;
    private GenerateLoadorderAndGeneralShadowPartlist shadowCheckFileGeneration = null;
    private CheckPartlistAgainstShadow generalPartlistCheckStep = null;
    private LoadMissingFilesOntoMVS generalUploadStep = null;
    private CheckPartlistBuildStatus builtStatusQueryStep = null; 
    private GenerateBuildVerbStep buildVerbCreationStep = null;
    private Set unbuiltFiles = new java.util.HashSet();
    private File loadorderCheckFile = null;
    private File shadowCheckFile = null;
    private File shadowLoadFile = null;
    private File builtStatusCheckFile = null;
    private File buildVerbFile = null;
    private boolean isPartsInDriverUpToDate =false;

    private static final String AUTHORIZATIONNAME = "S390DriverBuild";

    public DriverBuildProcess(MBBuild tempBuild, UserCommunicationInterface userComm) {
        super("Driver Build Process", 19,userComm); 
        build = tempBuild;
        if (build.getProcessForThisBuild()==null) {
            build.setProcessForThisBuild(this);
        }
        loadorderCheckFile = new File(build.getBuildPath()+MBConstants.ORDERFILE);
        shadowCheckFile = new File(build.getBuildPath()+MBConstants.QUERYFILE);
        shadowLoadFile = new File(build.getBuildPath()+MBConstants.PARTLISTLOAD);
        builtStatusCheckFile = new File(build.getBuildPath()+MBConstants.PARTLISTDCHK);
        buildVerbFile = new File(build.getBuildPath()+MBConstants.DRIVERCHKVRB);
        getCleanableEntity().setSetup(tempBuild.getSetup());
        addProcessActionListener(new TestNotifyListener());
/*
        getCleanableEntity().addDriverLock("F4189A88", build.getDriverInformation());
        DriverControl driverUnlockStep = new DriverControl("F4189A88", build.getDriverInformation(), getCleanableEntity(),build.getBuildPath(), DriverControl.UNLOCKDRIVER, this);
        try {
            System.out.println("about to unlock");
            driverUnlockStep.execute();
            System.out.println("done unlock");
        }catch(Exception e){
            e.toString();
        }
*/  
    }

    public File getGeneralShadowCheckFile() {
        return shadowCheckFile;
    }

    public File getBuiltStatusCheckFile() {
        return builtStatusCheckFile;
    }

    public void setParentInternalFrame(JInternalFrame tempParentFrame) {
        parentInternalFrame = tempParentFrame;
    }  

    public void prepareRestart(int tempStepNumberToStartWith, int tempIterationToStartWith, UserCommunicationInterface tempComm) {
        super.prepareRestart(tempStepNumberToStartWith,tempIterationToStartWith,tempComm);
        isPartsInDriverUpToDate =false;
    }

    public void preExecution() throws com.ibm.sdwb.build390.MBBuildException{
        // something probably changed so we need to clean up on the host now.   Remember we need to clean this.
        getCleanableEntity().addMVSBuildID(build.get_buildid(), build.getDriverInformation());
        getCleanableEntity().addLocalFileOrDirectory(new File(build.getBuildPath())); /** ken-verify **/

    }

    public void childSave() throws com.ibm.sdwb.build390.MBBuildException{
        build.save();
        build.save(com.ibm.sdwb.build390.MBGlobals.Build390_path+"misc"+java.io.File.separator+"build.ser");
    }

    /**
     * This method is used to return the steps to run to accomplish
     * a process.   The step to run first, then the next step to run
     * and so on.  If you need to have a step repeated, that should be
     * handled in the step, not here.
     * 
     * @param stepToGet
     *
     * @param stepIteration
     *
     * @return The step to run, null if there are no more steps
     */
    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0: 
            driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(build.getDriverInformation(), build.getMainframeInfo(), build.getLibraryInfo(), build.getBuildPathAsFile(), this);
            driverReportStep.setAlwaysRun(true);
            driverReportStep.setCheckForHLQAndDriver(true);
            driverReportStep.setForceNewReport(true);
            driverReportStep.setSummaryType("ONLY");
            driverReportStep.setVisibleToUser(false);
            return driverReportStep;
        case 1:
            if (driverReportStep.getParser().getBuildid().equals(build.get_buildid())) {
                return new NoOp(this);
            } else {
                DriverControl driverLockStep = new DriverControl(build.get_buildid(), build.getDriverInformation(),getCleanableEntity(),build.getBuildPath(), DriverControl.LOCKDRIVER, this);
                driverLockStep.setVisibleToUser(false);
                return driverLockStep;
            }
        case 2:
            partlistGenerationStep = new LibraryPartlistGeneration(build, this);
            return partlistGenerationStep;
        case 3:
            shadowCheckFileGeneration = new GenerateLoadorderAndGeneralShadowPartlist(build, loadorderCheckFile,shadowCheckFile, this);
            return shadowCheckFileGeneration;
        case 4:
            if (!shadowCheckFileGeneration.isLoadorderUpdated()) {
                return new NoOp(this);
            } else {
                loadOrderCheckStep = new CheckPartlistAgainstShadow(build,loadorderCheckFile, "LoadOrder", this);
                return loadOrderCheckStep;
            }
        case 5:
            if (!shadowCheckFileGeneration.isLoadorderUpdated()) {
                return new NoOp(this);
            } else {
                LoadMissingFilesOntoMVS loadOrderUploadStep = new LoadMissingFilesOntoMVS(build,loadOrderCheckStep.getOutputFile().getAbsolutePath(),AUTHORIZATIONNAME, this);
                loadOrderUploadStep.setName("Upload loadorder to MVS");
                return loadOrderUploadStep;
            }
        case 6:
            if (!shadowCheckFileGeneration.isLoadorderUpdated()) {
                return new NoOp(this);
            } else {
                LoadPartsIntoShadow loadOrderLoadStep = new LoadPartsIntoShadow(build,loadorderCheckFile, this);
                loadOrderLoadStep.setLoadOrderProcessing(true);
                return loadOrderLoadStep;
            }
        case 7:
            generalPartlistCheckStep = new CheckPartlistAgainstShadow(build,shadowCheckFile, "General", this);
            return generalPartlistCheckStep;
        case 8:
            generalUploadStep = new LoadMissingFilesOntoMVS(build,generalPartlistCheckStep.getOutputFileOfShadowCheck(),AUTHORIZATIONNAME, this);
            generalUploadStep.setName("Upload missing partlist files to MVS");
            return generalUploadStep;
        case 9:
            Set filesToLoad = new java.util.HashSet();
            filesToLoad.addAll(generalUploadStep.getShadowCheckResultParser().getMissingFiles());
            GenerateLoadVerbStep generateLoadVerb = new GenerateLoadVerbStep(build, filesToLoad, shadowLoadFile, this);
            return generateLoadVerb;
        case 10:
            LoadPartsIntoShadow generalLoadStep = new LoadPartsIntoShadow(build, shadowLoadFile, this);
            return generalLoadStep;
        case 11:
            Set filesToCheck = new java.util.HashSet();
            filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getLoadedFiles());
            filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getMissingFiles());
            filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getDeletedFiles());
            GenerateBuiltStatusCheckVerbStep generalBuiltStatusCheck = new GenerateBuiltStatusCheckVerbStep(build, filesToCheck, builtStatusCheckFile, this);
            return generalBuiltStatusCheck;
        case 12:
            builtStatusQueryStep = new CheckPartlistBuildStatus(build, builtStatusCheckFile, this);
            return builtStatusQueryStep;
        case 13:  
            Set problemIndicators = new java.util.HashSet();
            problemIndicators.add("NOT IN DRIVER");
            problemIndicators.add("FAIL=ON");
            problemIndicators.add("NOT BUILT");
            problemIndicators.add("INACTIVE");
            problemIndicators.add("METADATA LEVEL MISMATCH");

            unbuiltFiles = new java.util.HashSet();
            Set extraCheckIndicator = new java.util.HashSet();
            extraCheckIndicator.add("EXTRA PART INACTIVATED");
/*Client must treat inactivated part as not built.
When EXTRACHK=INACTIVE on a driver check and the host inactivates an extra driver  part as a result, 
the client must proceed with the build as though one or more unbuilt parts were returned.
The message returned is:
*WARN*  &CLASS &MOD &VHJN Extra part inactivated.
Whenever the client sees this message anywhere in the driver check output file (one or more times), 
then it must proceed with the build even if all of the parts in the part list are built, 
i.e. it must act as if the 'skip driver check' switch was turned on by the user in that case. 
However, since it in fact did perform a driver check, it should only put the unbuilt parts in the new part list and, 
if no parts were unbuilt, then just select one part which does not have a status of DELETE from the check file at random 
and put it in the list.
*/

            if (build.getOptions().getExtraDriverCheck()!=null && build.getOptions().getExtraDriverCheck().equals("INACTIVE") && 
                builtStatusQueryStep.getOutputParser()!=null && builtStatusQueryStep.getOutputParser().getTableEntriesContaining(extraCheckIndicator).isEmpty()) {
                if (builtStatusQueryStep.isAllPartsBuilt()) {
                    Set temp = builtStatusQueryStep.getOutputParser().getTableEntriesContaining("*INFO*");
                    for (Iterator fileIterator=temp.iterator();(fileIterator.hasNext() && unbuiltFiles.isEmpty());) {
                        com.ibm.sdwb.build390.info.FileInfo tempInfo =(com.ibm.sdwb.build390.info.FileInfo) fileIterator.next();
                        if (!tempInfo.getTypeOfChange().equals("DELETE")) {
                            unbuiltFiles.add(tempInfo);
                        }

                    }
                    builtStatusQueryStep.setAllPartsBuilt(unbuiltFiles.isEmpty());
                } else {
                    unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getTableEntriesContaining(problemIndicators));
                }

            } else if (!(build.getOptions().getForce().equals("YES") | build.getOptions().getForce().equals("ALL") | build.getOptions().isSkippingDriverCheck())) {
                unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getTableEntriesContaining(problemIndicators));
                if ((unbuiltFiles.size()==0) & (builtStatusQueryStep.isAllPartsBuilt())) {
                    isPartsInDriverUpToDate=true;
                    getStatusHandler().updateStatus("Driver " + build.getDriverInformation().getName() + " is up to date.",false);
                }
            } else {
                if (generalUploadStep.getShadowCheckResultParser() !=null) {
                    unbuiltFiles.addAll(generalUploadStep.getShadowCheckResultParser().getLoadedFiles());
                    unbuiltFiles.addAll(generalUploadStep.getShadowCheckResultParser().getMissingFiles());
                    unbuiltFiles.addAll(generalUploadStep.getShadowCheckResultParser().getDeletedFiles());
                }
            }
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                buildVerbCreationStep = new GenerateBuildVerbStep(build, unbuiltFiles, new HashSet(), buildVerbFile, this);
                return buildVerbCreationStep;
            } else {
                return new NoOp(this);
            }
        case 14:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                CreateListOfTracksInBuild createListOfTracksInBuildStep = new CreateListOfTracksInBuild(build, build.getBuildPathAsFile(), this);
                return createListOfTracksInBuildStep;
            } else {
                return new NoOp(this);
            }
        case 15:
            /*
            this step should handle any special build order processing.   Eventually we'll make a special process just for it to clean things up 
            but for now, recycle calldrvrbld
            */
            if (builtStatusQueryStep.isAllPartsBuilt()) {
                return new NoOp(this);
            }
            if (buildVerbCreationStep.isBuildOrderUpdated() | build.get_buildtype().equals("NONE") | buildTheBUILDORDERonHost()) {
                CallDRVRBLD handleBuildOrderProcessing = new CallDRVRBLD(build, driverReportStep, stepIteration+1, true, this);
                FullProcess mvsBuildOrderHandling = new FullProcess(handleBuildOrderProcessing, this);
                handleBuildOrderProcessing.setBuildSource(build.getSource());
                handleBuildOrderProcessing.setParentInternalFrame(parentInternalFrame);
                return mvsBuildOrderHandling;
            } else {
                return new NoOp(this);
            }
        case 16:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                CallDRVRBLD runMVSBuildPhaseProcess = new CallDRVRBLD(build, driverReportStep, stepIteration+1, false, this);
                FullProcess mvsBuildPhaseStep = new FullProcess(runMVSBuildPhaseProcess, this);
                runMVSBuildPhaseProcess.setBuildSource(build.getSource());
                runMVSBuildPhaseProcess.setParentInternalFrame(parentInternalFrame);
                return mvsBuildPhaseStep;
            } else {
                return new NoOp(this);
            }
        case 17:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
//                SetModMacAndShippedPartsData sendModMacAndShippedParts = new SetModMacAndShippedPartsData(build,build.getUTracks(),this);
//                return sendModMacAndShippedParts;
            } else {
                return new NoOp(this);
            }
        case 18:
            DriverControl driverUnlockStep = new DriverControl(build.get_buildid(), build.getDriverInformation(),getCleanableEntity(),build.getBuildPath(), DriverControl.UNLOCKDRIVER, this);
            return driverUnlockStep;
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
        switch (stepRun) {
        case 0:
            if (driverReportStep!=null) {
                if (driverReportStep.getParser()!=null) {
                    MBBuild tempBuild = build.getClone();
                    tempBuild.setLocked(tempBuild.get_buildid());
                    if (driverReportStep.getParser().isDriverLocked(tempBuild)) {
                        ProcessUpdateEvent processUpdateevent = new ProcessUpdateEvent(this);
                        processUpdateevent.setStartFromBeginning();
                        handleUIEvent(processUpdateevent);
                        driverReportStep.getParser().doDriverLockCheck(tempBuild);
                    }
                }
            }
            break;
        case 1:
            thingsToClean.setSetup(build.getSetup());
            thingsToClean.addDriverLock(build.get_buildid(), build.getDriverInformation());
            build.setLocked(driverReportStep.getParser().getBuildid());
            driverReportStep.getParser().setBuildInformation(build);
            break;
        case 13:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                if (builtStatusQueryStep.getOutputParser() ==null) { //happens during drivercheck only.
                    if (build.getOptions().isSkippingDriverCheck() && (driverReportStep.getParser().getPhaseInforamtion(build.get_buildtype())).size() <= 0) {
                        buildVerbCreationStep.setBuildOrderUpdated(true);
                    }
                }
            }
            break;
        case 18:
            thingsToClean.removeDriverLock(build.get_buildid());
            File restartFile = new File(com.ibm.sdwb.build390.MBGlobals.Build390_path+"misc"+java.io.File.separator+"build.ser");
            restartFile.delete();
            break;
        }
    }

    protected void addExceptionToBeThrown(int stepNumber, int iterationNumber, MBBuildException exception) {
        int iterationToThrowErrorOn = iterationNumber;
        if (stepNumber==16) {
            List phaseInfoList = driverReportStep.getParser().getPhaseInforamtion(build.get_buildtype());
            if (phaseInfoList.size() > iterationNumber+2) {
                // only get iterationToThrowErrorOn if the phase info exists. If it doesn't throw the exception now.
                com.ibm.sdwb.build390.mainframe.PhaseInformation phaseInfo = (com.ibm.sdwb.build390.mainframe.PhaseInformation) phaseInfoList.get(iterationNumber+1);// add one because the info includes phase 0
                iterationToThrowErrorOn = phaseInfo.getPhaseNumberToHaltOnIfErrorsFound()-1;
            }
        }
        super.addExceptionToBeThrown(stepNumber,iterationToThrowErrorOn,exception);
    }

    //Begin #Feat.INT1178:
    public void sendTestInformation() {
        MBUtilities.createTestMail(build.get_buildid(), build.getDriverInformation().getName(), build.getReleaseInformation().getLibraryName(), 
                                   "Driver", getTimeOfLastRun(), hasCompletedSuccessfully(),build.get_descr());
    }
    //End #Feat.INT1178:

    public boolean isPartsInDriverUpToDate() {
        return isPartsInDriverUpToDate;
    }

    public boolean buildTheBUILDORDERonHost() {
        List  types = driverReportStep.getParser().getBuildTypes();
        if (types.size()==1) {
            return types.contains("NONE");
        }
        return false;
    }
}
