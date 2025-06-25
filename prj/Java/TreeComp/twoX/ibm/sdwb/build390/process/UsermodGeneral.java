package com.ibm.sdwb.build390.process;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.test.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangeRequestMultipleUpdateEvent;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangeRequestPartitionedUpdateEvent;

public class UsermodGeneral extends AbstractProcess  implements TestInfoGenerator, ProcessActionListener {
    static final long serialVersionUID = 1111111111111111L;

    private static final String USERMODDIRECTORYPREFIX = "Usermod";
    private static final String AUTHORIZATIONTYPE = "S390Usermod";

    private ChangeRequestMultipleInfo processInfo = null;
    private ChangeRequestMultiple mainProcess = null;

    public UsermodGeneral(ChangeRequestMultipleInfo tempBuild, UserCommunicationInterface userComm) {
        super("Usermod set build",1, userComm); 
        processInfo = tempBuild;
        getCleanableEntity().setSetup(tempBuild.getSetup());
        getStatusHandler().updateStatus("Preparing to start usermod processinfo ("+processInfo.get_buildid()+")",false);
        addProcessActionListener(this);
    }

    protected void preExecution() throws com.ibm.sdwb.build390.MBBuildException {
        String errorString = new String();

        getCleanableEntity().addLocalFileOrDirectory(new File(processInfo.getBuildPath())); 

        for (Iterator changeRequestIterator = processInfo.getChangeRequests().iterator(); changeRequestIterator.hasNext();) {
            ChangeRequest oneChangeset = (ChangeRequest) changeRequestIterator.next();
            errorString += MBUtilities.validateTrackForSMOD(oneChangeset.getName());
        }
        if (errorString.trim().length()>0) {
            throw new LibraryError(errorString);//TST3362
        }

        //Begin TST3192
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep driverReportWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(getUserCommunicationInterface());
        com.ibm.sdwb.build390.process.steps.DriverReport driverReport = new com.ibm.sdwb.build390.process.steps.DriverReport(processInfo.getDriverInformation(),processInfo.getSetup().getMainframeInfo(), processInfo.getSetup().getLibraryInfo(), processInfo.getBuildPathAsFile(), driverReportWrapper);  
        driverReport.setAlwaysRun(true);
        driverReport.setSummaryType("ONLY");
        driverReport.setForceNewReport(true);
        driverReport.setCheckBaseNotThinDelta(true);
        driverReport.setCheckForLockFlag(true); /* check for LOCK=ON */
        driverReport.setCheckForMergeOnlyFlag(true); /* check for MERGONLY=OFF */
        driverReportWrapper.setStep(driverReport);
        driverReportWrapper.externalRun();
        //End TST3192

    }

    public void childSave() throws com.ibm.sdwb.build390.MBBuildException{
        processInfo.save();
    }


    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        switch (stepToGet) {
        case 0:
            mainProcess = new ChangeRequestMultiple(this);
            mainProcess.setProcessInfo(processInfo);
            Set completeStates = new HashSet();
            completeStates.add("commit");
            completeStates.add("complete");
            mainProcess.setStatesThatCountAsComplete(completeStates);
            FullProcess multipleProcessAsAStep = new FullProcess(mainProcess, this);
            return multipleProcessAsAStep;
        }
        return null;
    }

    public void handleProcessCompletion(AbstractProcess ap) {
        // do some error checking in here to see if we hit problems.
        try {
            if (getChangesetBuildErrorMessage()==null) {
                undoProcess();
            } else {
                ChangeRequestMultipleUpdateEvent oneEvent = new ChangeRequestMultipleUpdateEvent(processInfo);
                handleUIEvent(oneEvent);
            }
        } catch (MBBuildException mbe) {
            getLEP().LogException(mbe);
        }

    }

    public String getChangesetBuildErrorMessage() {
        if (mainProcess!=null) {
            return mainProcess.getChangesetBuildErrorMessage();
        }
        return null;
    }

/*
    private void removeUsermodSubdirectories() {
        processInfo.setBuildPathOptionalPart(null);
        File generalDir = new File(processInfo.getBuildPath());
        if (generalDir.exists()) {
            String[] filesInDir = generalDir.list();
            for (int i2 = 0; i2 < filesInDir.length & !stopped; i2++) {
                File buildDir = new File(generalDir, filesInDir[i2]);
                if (buildDir.isDirectory()) {
                    if (filesInDir[i2].startsWith(USERMODDIRECTORYPREFIX)) {
                        getStatusHandler().updateStatus("Deleting " + buildDir.getAbsolutePath() + " directory.",false);
                        MBUtilities.deleteDirectory(buildDir);
                    }
                }
            }
        }
    }
*/
    public void sendTestInformation() {
        MBUtilities.createTestMail(processInfo.get_buildid(), processInfo.getDriverInformation().getName() , processInfo.getReleaseInformation().getLibraryName(), 
                                   "USERMOD", getTimeOfLastRun(), hasCompletedSuccessfully(), processInfo.get_descr());
    }
}


