package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangesetGroupUpdateEvent;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.*;
import java.util.*;
import java.io.*;


public class ChangesetGroup extends AbstractProcess implements ProcessActionListener {
    static final long serialVersionUID = 1111111111111111L;

    private String authorizationToUse = null;

    private ChangesetGroupInfo processInfo = null;
    private com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = null;
    private LibraryLevelRequisiteCheck checkLevel = null;
    private DriverInformation baseDriver = null;
    private ConcurrentSteps partsVerificationSteps = null;
    private ConcurrentSteps packagingSteps  = null;
    private com.ibm.sdwb.build390.process.DriverBuildProcess buildTheDriverStep = null;
    private com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease listFMIDs = null;
    private Boolean usermodNoShippableProcessingEnabled = null;

    public ChangesetGroup(ChangesetGroupInfo tempBuild, UserCommunicationInterface userComm) throws com.ibm.sdwb.build390.MBBuildException{
        super("Single project changeset group build",12, userComm); 
        processInfo = tempBuild;
        addProcessActionListener(this);
    }

    public void preExecution() {
        baseDriver = processInfo.getReleaseInformation().getDriverByName(processInfo.getParentInfo().getParent().getDriverInformation().getName());
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {

        switch (stepToGet) {
        case 0:
            if (!processInfo.getParentInfo().getParent().isDryRun() ) {
                driverReportStep = new com.ibm.sdwb.build390.process.steps.DriverReport(baseDriver, processInfo.getMainframeInfo(), processInfo.getLibraryInfo(), processInfo.getBuildPathAsFile(), this);
                driverReportStep.setForceNewReport(true);
                driverReportStep.setSummaryType("ONLY");
                driverReportStep.setHoldNextUsermod(true);
                driverReportStep.setVisibleToUser(false);
                return driverReportStep;
            } else {
                return new NoOp(this);
            }
        case 1:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                CreateLevelInLibrary createLevel = new CreateLevelInLibrary(processInfo.getChangesetGroup(), this);
                return createLevel;
            } else {
                return new NoOp(this);
            }
        case 2:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                return createTrackRemovalSteps();
            } else {
                return new NoOp(this);
            }
        case 3:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                ManipulateTracksInCMVC addTrackStep = new ManipulateTracksInCMVC(processInfo.getSetOfChangesets(),processInfo.getChangesetGroup(), this);
                addTrackStep.setAddMode();
                return addTrackStep;
            } else {
                return new NoOp(this);
            }
        case 4:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                checkLevel = new LibraryLevelRequisiteCheck(processInfo.getSource(), this);
                return checkLevel;
            } else {
                return new NoOp(this);
            }
        case 5:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                // this looks unnecessary, however we need it in case this is a multi release build, then we need the right driver for each release.
                DriverInformation newDriver = new DriverInformation(processInfo.getName());
                newDriver.setBaseDriver(baseDriver);
                newDriver.setFull(false);
                newDriver.setReleaseInfomation(processInfo.getReleaseInformation());
                processInfo.setDriverInformation(newDriver);
                com.ibm.sdwb.build390.process.CreateMVSDriver driverCreate = new com.ibm.sdwb.build390.process.CreateMVSDriver(processInfo,baseDriver, newDriver,true, new ReleaseAndDriverParameters(), this);
                driverCreate.setDriverSize("SMALL");
                FullProcess createWrapper = new FullProcess(driverCreate, this);
                return createWrapper;
            } else {
                return new NoOp(this);
            }
        case 6:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                buildTheDriverStep = new com.ibm.sdwb.build390.process.DriverBuildProcess(processInfo, this) {
                    static final long serialVersionUID = 4329577230223470399L;
                    public boolean isUndoable() {
                        // pretend this is undoable, since we'll be nuking the driver which is the same basic idea.
                        return true;
                    }
                };
                FullProcess buildProcess = new FullProcess(buildTheDriverStep, this);
                return buildProcess;
            } else {
                return new NoOp(this);
            }
        case 7:
            listFMIDs = new com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease(processInfo.getReleaseInformation().getLibraryName(), processInfo.getSetup(), new File(processInfo.getBuildPath()), this);
            listFMIDs.setDriver(processInfo.getDriverInformation().getName());
            return listFMIDs;
        case 8 :
            return createPartsVerificationStepsToRun();
        case 9 :
            return createPackagingStepsToRun();
        case 10 :
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                MergeMVSDriverIntoBase mergeStep = new MergeMVSDriverIntoBase(processInfo.getMainframeInfo(), processInfo.getBuildPath(), processInfo.getDriverInformation(), this);
                return mergeStep;
            } else {
                return new NoOp(this);
            }
        case 11:
            if (!processInfo.getParentInfo().getParent().isDryRun()) {
                CommitLevelInLibrary commitStep = new CommitLevelInLibrary(processInfo.getChangesetGroup(), this);
                return commitStep;
            } else {
                return new NoOp(this);
            }
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws GeneralError {
        switch (stepRun) {
        case 0:
            if (driverReportStep.getParser().getNextUsermod()==null) {
                throw new RuntimeException("SMODPFX is not defined in BLDORDER of  release "+processInfo.getReleaseInformation().getLibraryName());
            }
            processInfo.setName(driverReportStep.getParser().getNextUsermod());
            com.ibm.sdwb.build390.library.ChangesetGroup oneGroup = processInfo.getLibraryInfo().getChangesetGroup(processInfo.getName(), processInfo.getReleaseInformation().getLibraryName());
            oneGroup.setIncludingCommittedBase(false);
            processInfo.setSource(oneGroup);
            return;

        case 5:
            ChangesetGroupUpdateEvent createDriverEvent = new ChangesetGroupUpdateEvent(processInfo);
            handleUIEvent(createDriverEvent);
            return;
        case 8:
            // if there are no parts to be built in all the fids and empty usermod processing isn't enabled, abort. by throwing an error
            boolean tempUsermodNoShippableProcessingEnabled = true;
            for (Iterator iter = partsVerificationSteps.getStepSetToRun().iterator();iter.hasNext();) {
                PartsVerificationForUsermodPackaging partsVerifyStep = (PartsVerificationForUsermodPackaging)iter.next();
                if (partsVerifyStep.isPartsExistForProcessing()) {
                    return;
                } else {
                    // if it's turned on everywhere then continue
                    tempUsermodNoShippableProcessingEnabled = tempUsermodNoShippableProcessingEnabled & partsVerifyStep.isUsermodProcessingWithoutShippablesEnabled();

                }
            }
            usermodNoShippableProcessingEnabled = Boolean.valueOf(tempUsermodNoShippableProcessingEnabled);
            ChangesetGroupUpdateEvent event1 = new ChangesetGroupUpdateEvent(processInfo);
            handleUIEvent(event1);
            if (!tempUsermodNoShippableProcessingEnabled) {
                throw new GeneralError("The parts verification step(SMODBLD OP=CHECK) call for all given function(FMID) indicates that there are no parts for that function, hence no USERMODs were built.");
            }
            return;
        case 9:
            java.util.List tempList = new ArrayList();
            for (Iterator iter = packagingSteps.getStepSetToRun().iterator();iter.hasNext();) {
                PackageMainframeSystemModifications packagingStep  = (PackageMainframeSystemModifications)iter.next();
                processInfo.getFMIDToSMODMap().put((String)listFMIDs.getFMIDMap().get(packagingStep.getFMID()), packagingStep.getSMODName());
            }
            ChangesetGroupUpdateEvent event = new ChangesetGroupUpdateEvent(processInfo);
            handleUIEvent(event);
            return;
        }
    }

    public Boolean getUsermodNoShippableProcessingEnabled() {
        return usermodNoShippableProcessingEnabled;
    }

    public  void handleProcessCompletion(AbstractProcess ap) {
        if (hasCompletedSuccessfully()) {
            getStatusHandler().updateStatus("Project build for " + processInfo.getName() + " complete", false);
        }
    }

    private ConcurrentSteps createTrackRemovalSteps() {
        ConcurrentSteps trackRemovalStep = new ConcurrentSteps("Changeset removal",this);
        for (Iterator changesetIterator = processInfo.getSetOfChangesets().iterator(); changesetIterator.hasNext();) {
            Changeset oneChangeset = (Changeset) changesetIterator.next();
            if (oneChangeset.getChangesetGroupContainingChangeset() != null) {
                Set setWrapper = new HashSet();
                setWrapper.add(oneChangeset);
                ManipulateTracksInCMVC removeTrackStep = new ManipulateTracksInCMVC(setWrapper, oneChangeset.getChangesetGroupContainingChangeset(), this);
                removeTrackStep.setRemoveMode();
                trackRemovalStep.addStepToRun(removeTrackStep);
            }
        }
        return trackRemovalStep;
    }

    private ConcurrentSteps createPartsVerificationStepsToRun() {
        partsVerificationSteps = new ConcurrentSteps("Parts verification",this);
        boolean uploadFile =  true;
        if (!processInfo.getParentInfo().getParent().isDryRun()) {
            for (Iterator fmidIterator = listFMIDs.getFMIDMap().keySet().iterator(); fmidIterator.hasNext();) {
                String oneFMID =(String) fmidIterator.next();
                PartsVerificationForUsermodPackaging singlePartsVerifyStep  = new PartsVerificationForUsermodPackaging(processInfo,oneFMID,buildTheDriverStep.getGeneralShadowCheckFile(),this);
                singlePartsVerifyStep.setUploadFile(uploadFile);
                partsVerificationSteps.addStepToRun(singlePartsVerifyStep);
                uploadFile = false; // after the first run, we dont' want to upload files, so set this to false.
            }
        }
        return partsVerificationSteps;
    }

    private ConcurrentSteps createPackagingStepsToRun() {
        packagingSteps = new ConcurrentSteps("Packaging steps",this);
        if (!processInfo.getParentInfo().getParent().isDryRun()) {
            String SMODName = processInfo.getName();// set the initial one to the smod name
            File packageContentsOrder = buildTheDriverStep.getGeneralShadowCheckFile();
            for (Iterator iter = partsVerificationSteps.getStepSetToRun().iterator();iter.hasNext();) {
                PartsVerificationForUsermodPackaging partsVerifyStep = (PartsVerificationForUsermodPackaging)iter.next();
                if (partsVerifyStep.isPartsExistForProcessing()) {

                    processInfo.getFMIDToSMODMap().put((String)listFMIDs.getFMIDMap().get(partsVerifyStep.getFMID()), new String());
                    PackageMainframeSystemModifications usermodPackagingStep =new PackageMainframeSystemModifications(processInfo, SMODName, PackageMainframeSystemModifications.USERMOD, "'*'", this);
                    usermodPackagingStep.setPackageContentsOrder(packageContentsOrder);
                    usermodPackagingStep.setCommentsToUse("*");
                    usermodPackagingStep.setFMID(partsVerifyStep.getFMID());
                    usermodPackagingStep.setLogicToUse(processInfo.createLogicString());
                    usermodPackagingStep.setXMITInformation(processInfo.getParentInfo().getMainframeUserAddressToSendOutputTo());
                    usermodPackagingStep.setSaveDatasetName(processInfo.getParentInfo().getMainframeDatasetToStoreOutputIn());
                    usermodPackagingStep.doNotReturnOutputDataAsPrintData();
                    packagingSteps.addStepToRun(usermodPackagingStep);
                    SMODName=null; // for any remaining ones, clear the SMODname
                    packageContentsOrder = null;  // for runs after the first, don't upload file, so set it to null
                }
            }
        }

        ChangesetGroupUpdateEvent event = new ChangesetGroupUpdateEvent(processInfo);
        handleUIEvent(event);

        return packagingSteps;
    }
}

