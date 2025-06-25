package com.ibm.sdwb.build390.process;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.MBUBuild;
import com.ibm.sdwb.build390.MBUtilities;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.test.TestInfoGenerator;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.build.ProcessUpdateEvent;

//*************************************************************************
//05/30/2003 #Feat.INT1178:  Enhance /test parm for improved tracking
//08/20/2003 #DEF.TST1380: MDE changes not stored in unimodc for user build
//*************************************************************************

public class UserBuildProcess extends MVSDRVRBLDVerbCaller implements TestInfoGenerator {
    static final long serialVersionUID = 1111111111111111L;

    protected transient JInternalFrame parentInternalFrame = null;

    protected MBUBuild build = null;
    protected com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = null;
    protected LocalPartlistGeneration partlistGenerationStep = null;
    protected CheckPartlistAgainstShadow loadOrderCheckStep = null;
    protected CheckPartlistAgainstShadow generalPartlistCheckStep = null;
    protected LoadMissingFilesOntoMVS generalUploadStep = null;
    protected CheckPartlistBuildStatus builtStatusQueryStep = null;
    protected GenerateBuildVerbStep buildVerbCreationStep = null;
    protected GenerateLoadorderAndGeneralShadowPartlist shadowCheckFileGeneration = null;
    protected Set unbuiltFiles = new java.util.HashSet();
    protected Set rebuildFiles = new java.util.HashSet();
    protected File loadorderCheckFile = null;
    protected File shadowCheckFile = null;
    protected File shadowLoadFile = null;
    protected File builtStatusCheckFile = null;
    protected File buildVerbFile = null;
    private boolean isPartsInDriverUpToDate =false;
    private Map  metadataMap;

    private static final String AUTHORIZATIONNAME = "S390UserBuild";

    public UserBuildProcess(MBUBuild tempBuild, com.ibm.sdwb.build390.userinterface.UserCommunicationInterface userComm) {
        super("User Build Process", 17, userComm); 
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
    }

    //Begin #DEF.TST1380:
    /** Since the fileinfo gets created only in the localpartlistgeneration Step, we have to feed the metadata keywords
     * in the post localpartlistgeneration step.
     * I dont like the above fix though. We could probably populate FileInfo in the panel and store metadata in FileInfo object/get rid of this method
     */ 
    public void setMetadata(Map metadataMap) {
        this.metadataMap=metadataMap;
    }
    //End #DEF.TST1380:

    public void prepareRestart(int tempStepNumberToStartWith, int tempIterationToStartWith, UserCommunicationInterface tempComm) {
        super.prepareRestart(tempStepNumberToStartWith,tempIterationToStartWith,tempComm);
        isPartsInDriverUpToDate =false;
    }

    public boolean isPDSBuild() {
        return build.getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
    }

    public String getRootPath() {
        return build.getLocalParts()[0];
    }

    public void setParentInternalFrame(JInternalFrame tempParentFrame) {
        parentInternalFrame = tempParentFrame;
    } 

    public void preExecution() throws com.ibm.sdwb.build390.MBBuildException {
        // something probably changes so we need to clean up on the host now.   Remember we need to clean this.
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
            driverReportStep.setCheckForControlledFlag(true);
            driverReportStep.setForceNewReport(true);
            driverReportStep.setSummaryType("ONLY");
            driverReportStep.setVisibleToUser(false);
            return driverReportStep;
        case 1:
            if (driverReportStep.getParser().getBuildid().equals(build.get_buildid())) {
                return new NoOp(this);
            } else {
                DriverControl driverLockStep = new DriverControl(build.get_buildid(), build.getDriverInformation(), getCleanableEntity(), build.getBuildPath(), DriverControl.LOCKDRIVER, this);
                driverLockStep.setVisibleToUser(false);
                return driverLockStep;
            }
        case 2:
            partlistGenerationStep = new LocalPartlistGeneration(build, this);
            return partlistGenerationStep;
        case 3:

            shadowCheckFileGeneration = new GenerateLoadorderAndGeneralShadowPartlist(build, loadorderCheckFile,shadowCheckFile, this);
            return shadowCheckFileGeneration;
        case 4:
            if (!shadowCheckFileGeneration.isLoadorderUpdated() | isPDSBuild()) {
                return new NoOp(this);
            } else {
                loadOrderCheckStep = new CheckPartlistAgainstShadow(build,loadorderCheckFile, "LoadOrder", this);
                return loadOrderCheckStep;
            }
        case 5:
            if (!shadowCheckFileGeneration.isLoadorderUpdated() | isPDSBuild()) {
                return new NoOp(this);
            } else {
                LoadMissingFilesOntoMVS loadOrderUploadStep = new LoadMissingFilesOntoMVS(build,loadOrderCheckStep.getOutputFileOfShadowCheck(),AUTHORIZATIONNAME, this);
                loadOrderUploadStep.setName("Upload loadorder to MVS");
                return loadOrderUploadStep;
            }
        case 6:
            if (!shadowCheckFileGeneration.isLoadorderUpdated()) {
                return new NoOp(this);
            } else {
                LoadPartsIntoShadow loadOrderLoadStep = new LoadPartsIntoShadow(build,loadorderCheckFile, this);

                //Begin UserBldUpdate0
                if (isPDSBuild()) {
                    loadOrderLoadStep.setPartClass(build.getPDSMemberClass());
                }
                //End UserBldUpdate0

                loadOrderLoadStep.setLoadOrderProcessing(true);
                return loadOrderLoadStep;
            }
        case 7:
            if (isPDSBuild()) {
                return new NoOp(this);
            } else {
                generalPartlistCheckStep = new CheckPartlistAgainstShadow(build,shadowCheckFile, "General", this);
                return generalPartlistCheckStep;
            }
        case 8:
            if (isPDSBuild()) {
                return new NoOp(this);
            } else {
                generalUploadStep = new LoadMissingFilesOntoMVS(build,generalPartlistCheckStep.getOutputFileOfShadowCheck(),AUTHORIZATIONNAME, this);
                generalUploadStep.setName("Upload missing partlist files to MVS");
                return generalUploadStep;
            }
        case 9:
            Set filesToLoad = new java.util.HashSet();
            if (isPDSBuild()) {
                filesToLoad.addAll(build.getPartInfoSet());
            } else {
                filesToLoad.addAll(generalUploadStep.getShadowCheckResultParser().getMissingFiles());
            }
            GenerateLoadVerbStep generateLoadVerb = new GenerateLoadVerbStep(build, filesToLoad, shadowLoadFile, this);
            return generateLoadVerb;
        case 10:
            if (shadowLoadFile!=null && shadowLoadFile.length() > 0) { //INT3551
                LoadPartsIntoShadow generalLoadStep = new LoadPartsIntoShadow(build, shadowLoadFile, this);
                if (isPDSBuild()) {
                    generalLoadStep.setPartClass(build.getPDSMemberClass());
                }
                return generalLoadStep;
            } else {
                return new NoOp(this);
            }
        case 11:
            Set filesToCheck = new java.util.HashSet();
            if (isPDSBuild()) {
                filesToCheck.addAll(build.getPartInfoSet());
            } else {
                filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getLoadedFiles());
                filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getMissingFiles());
                filesToCheck.addAll(generalUploadStep.getShadowCheckResultParser().getDeletedFiles());
            }
            GenerateBuiltStatusCheckVerbStep generalBuiltStatusCheck = new GenerateBuiltStatusCheckVerbStep(build, filesToCheck, builtStatusCheckFile, this);
            return generalBuiltStatusCheck;
        case 12:
            builtStatusQueryStep = new CheckPartlistBuildStatus(build, builtStatusCheckFile, this);
            if (builtStatusCheckFile!=null && builtStatusCheckFile.length() > 0) { //INT3551
                return builtStatusQueryStep;
            } else {
                builtStatusQueryStep.setAllPartsBuilt(true);
                return new NoOp(this);
            }
        case 13:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                buildVerbCreationStep = new GenerateBuildVerbStep(build,unbuiltFiles, rebuildFiles, buildVerbFile, this);
                return buildVerbCreationStep;
            } else {
                return new NoOp(this);
            }
        case 14:
            if (builtStatusQueryStep.isAllPartsBuilt()) {
                return new NoOp(this);
            }
            if (buildVerbCreationStep.isBuildOrderUpdated()) {
                CallDRVRBLD runMVSBuildPhaseProcess = new CallDRVRBLD(build, driverReportStep, stepIteration+1, true, this);
                FullProcess mvsBuildPhaseStep = new FullProcess(runMVSBuildPhaseProcess, this);

                runMVSBuildPhaseProcess.setBuildSource(build.getSource());//UserBldUpdate0

                runMVSBuildPhaseProcess.setParentInternalFrame(parentInternalFrame);
                return mvsBuildPhaseStep;
            } else {
                return new NoOp(this);
            }
        case 15:
            if (!builtStatusQueryStep.isAllPartsBuilt()) {
                CallDRVRBLD runMVSBuildPhaseProcess = new CallDRVRBLD(build, driverReportStep, stepIteration+1, false, this);
                FullProcess mvsBuildPhaseStep = new FullProcess(runMVSBuildPhaseProcess, this);

                runMVSBuildPhaseProcess.setBuildSource(build.getSource());//UserBldUpdate0

                runMVSBuildPhaseProcess.setParentInternalFrame(parentInternalFrame);
                return mvsBuildPhaseStep;
            } else {
                return new NoOp(this);
            }
        case 16:
            DriverControl driverUnlockStep = new DriverControl(build.get_buildid(), build.getDriverInformation(), getCleanableEntity(), build.getBuildPath(), DriverControl.UNLOCKDRIVER, this);
            return driverUnlockStep;
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
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
            Setup setup = build.getSetup();
            thingsToClean.setSetup(setup);
            thingsToClean.addDriverLock(build.get_buildid(),build.getDriverInformation());
            build.setLocked(driverReportStep.getParser().getBuildid());
            driverReportStep.getParser().setBuildInformation(build);  
            break;
        case 2:
            //do we need this thing. doesn't every fileInfo carry its own metadata now ??  
            // Ok. i just printed it out, and build.getFileInf0(...).getMetadata(..) is empty.
            // This is a change that is needed in the populating class (UserBuildPanel).
            if (metadataMap!=null) {
                for (Iterator iter = metadataMap.keySet().iterator(); iter.hasNext();) {
                    String fileName = (String)iter.next(); 

                    String version = MBUtilities.getLocalFileVersion(fileName, build);

                    /*TO-DO:we dont have to add the version into a hash. We have a setMetadataVersion method now.
                    *But this change will affect the verb file creator
                    */
                    Map singleMetadataMap = (Map) metadataMap.get(fileName);
                    singleMetadataMap.put(MBConstants.METADATAVERSIONKEYWORD, version);

                    if (isPDSBuild()) {
                        String basicName =  new File(fileName).getName();
                        build.getFileInfo(build.getPDSMemberClass(),basicName).setMetadata(singleMetadataMap);

                    } else {
                        String tempDirPath = getRootPath() + File.separator +  fileName.trim();
                        File tempFile = new File(tempDirPath);
                        String basicName = tempFile.getName();       

                        String pathString =  tempDirPath.substring((getRootPath()+File.pathSeparator).length(), tempDirPath.length()-basicName.length()).replace(File.separatorChar, '/').trim();
                        if (pathString.equals("/")) {
                            pathString = "";
                        }
                        build.getFileInfo(pathString,basicName).setMetadata(singleMetadataMap);
                    }
                }
            }
            break;

        case 3:
            if (shadowCheckFileGeneration.isPartlistEmpty()) {
                throw new com.ibm.sdwb.build390.GeneralError("The partlist was empty");
            }
            break;
        case 12:
            Set problemIndicators = new java.util.HashSet();
            problemIndicators.add("NOT IN DRIVER");
            problemIndicators.add("FAIL=ON");
            problemIndicators.add("NOT BUILT");
            problemIndicators.add("INACTIVE");
            problemIndicators.add("METADATA LEVEL MISMATCH");

            unbuiltFiles = new java.util.HashSet();
            rebuildFiles = new java.util.HashSet();

            if (builtStatusQueryStep.getOutputParser() !=null) {
                if (!(build.getOptions().getForce().equals("YES") | build.getOptions().getForce().equals("ALL") | build.getOptions().isSkippingDriverCheck())) {
                    unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getTableEntriesContaining(problemIndicators));
                    if ((unbuiltFiles.size()==0) & (builtStatusQueryStep.isAllPartsBuilt())) {
                        isPartsInDriverUpToDate=true;
                        getStatusHandler().updateStatus("Driver " + build.getDriverInformation().getName() + " is up to date.",false);
                    }
                } else {
                    unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getMissingFiles());
                    unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getDeletedFiles());

                    if (generalUploadStep!=null) {
                        rebuildFiles.addAll(generalUploadStep.getShadowCheckResultParser().getLoadedFiles());
                    } else {
                        rebuildFiles.addAll(builtStatusQueryStep.getOutputParser().getLoadedFiles());
                    }

                    //PTM4089
                    unbuiltFiles.addAll(builtStatusQueryStep.getOutputParser().getTableEntriesContaining(problemIndicators));
                }
            }
            break;
        case 16:
            thingsToClean.removeDriverLock(build.get_buildid());
            File restartFile = new File(com.ibm.sdwb.build390.MBGlobals.Build390_path+"misc"+java.io.File.separator+"build.ser");
            restartFile.delete();
            break;
        }
    }

    protected void addExceptionToBeThrown(int stepNumber, int iterationNumber, MBBuildException exception) {
        int iterationToThrowErrorOn = iterationNumber;
        if (stepNumber==15) {//PTM4089 
            java.util.List phaseInfoList = driverReportStep.getParser().getPhaseInforamtion(build.get_buildtype());
            if (phaseInfoList.size() > iterationNumber+2) {
                com.ibm.sdwb.build390.mainframe.PhaseInformation phaseInfo = (com.ibm.sdwb.build390.mainframe.PhaseInformation) driverReportStep.getParser().getPhaseInforamtion(build.get_buildtype()).get(iterationNumber+1);// add one because the info includes phase 0
                iterationToThrowErrorOn = phaseInfo.getPhaseNumberToHaltOnIfErrorsFound()-1;//PTM4089
            }
        }
        super.addExceptionToBeThrown(stepNumber,iterationToThrowErrorOn,exception);
    }

    //Begin #Feat.INT1178:
    public void sendTestInformation() {
        String type="User";
        if (build.getFastTrack()==true) {
            type = "Fastrack";
        }

        String driverName ="";
        String releaseName ="";
        if (build.getDriverInformation()!=null) {
            driverName = build.getDriverInformation().getName();
        }

        if (build.getReleaseInformation()!=null) {
            releaseName = build.getReleaseInformation().getLibraryName();
        }
        MBUtilities.createTestMail(build.get_buildid(), driverName, releaseName, 
                                   type, getTimeOfLastRun(), hasCompletedSuccessfully(), build.get_descr());
    }
    //End #Feat.INT1178:

    public boolean isPartsInDriverUpToDate() {
        return isPartsInDriverUpToDate;
    }
}
