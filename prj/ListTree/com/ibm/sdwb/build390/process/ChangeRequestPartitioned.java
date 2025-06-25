package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.*;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.*;
import java.util.*;
import java.io.*;

public class ChangeRequestPartitioned extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private ChangeRequestPartitionedInfo processInfo = null;


    public ChangeRequestPartitioned(ChangeRequestPartitionedInfo tempBuild, UserCommunicationInterface userCom) {
        super("Build Single Changeset Group",1, userCom); 
        processInfo = tempBuild;
    }

    protected void preExecution() {
        Map projectToChangesetMap = new HashMap();
        Set individualProjectInfos = new HashSet();
        for (Iterator changeRequestIterator = processInfo.getChangeRequestsInGroup().iterator(); changeRequestIterator.hasNext();) {
            ChangeRequest oneChangeRequest = (ChangeRequest) changeRequestIterator.next();
            for (Iterator projectIterator = oneChangeRequest.getIndividualSourceInfos().iterator(); projectIterator.hasNext(); ) {
                SourceInfo oneInfo = (SourceInfo) projectIterator.next();
                Set trackSet = (Set) projectToChangesetMap.get(oneInfo.getProject());
                if (trackSet == null) {
                    trackSet = new HashSet();
                    projectToChangesetMap.put(oneInfo.getProject(), trackSet);
                }
                trackSet.add(oneInfo);
            }
        }
        for (Iterator projectIterator = projectToChangesetMap.keySet().iterator(); projectIterator.hasNext();) {
            String project = (String) projectIterator.next();
            Set sourceInfoSetForProject = (Set) projectToChangesetMap.get(project);
            ChangesetGroupInfo oneInfo = new ChangesetGroupInfo(getLEP(), processInfo);
            oneInfo.setReleaseInformation(processInfo.getSetup().getMainframeInfo().getReleaseByLibraryName(project, processInfo.getSetup().getLibraryInfo()));
            oneInfo.setSetOfChangesets(sourceInfoSetForProject);
            individualProjectInfos.add(oneInfo);
        }
        processInfo.setSingleProjectBuildSet(individualProjectInfos);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws MBBuildException {
        switch (stepToGet) {
            case 0:
                return createSingleProjectProcesses();
        }
        return null;
    }

    private ConcurrentSteps createSingleProjectProcesses() throws MBBuildException{
        ConcurrentSteps theStep = new ConcurrentSteps("Individual project changeset group builds", this);
        for (Iterator infoIterator = processInfo.getSingleProjectBuildSet().iterator(); infoIterator.hasNext();) {
            ChangesetGroupInfo aGroup = (ChangesetGroupInfo)infoIterator.next();
            ChangesetGroup oneProjectProcess = new ChangesetGroup(aGroup, this);
            aGroup.setProcessForThisBuild(oneProjectProcess);
            FullProcess oneProjectFullProcessAsAStep = new FullProcess(oneProjectProcess, this);
            ChangesetGroupUpdateEvent newEvent = new ChangesetGroupUpdateEvent(aGroup);
            handleUIEvent(newEvent);
            theStep.addStepToRun(oneProjectFullProcessAsAStep);
        }
        return theStep;
    }

    SingleChangeRequestGroupBuildAsAStep getStepVersionOfProcess(Set tempCompleted, Set tempBroken, AbstractProcess parentProcess) {
        return new SingleChangeRequestGroupBuildAsAStep(tempCompleted, tempBroken, this, parentProcess);
    }

    private   class SingleChangeRequestGroupBuildAsAStep extends FullProcess implements ProcessActionListener {
        static final long serialVersionUID = 1111111111111111L;
        Set changesetGroupsCompleted = null; 
        Set changesetGroupsBroken = null;

        protected SingleChangeRequestGroupBuildAsAStep(Set tempCompleted, Set tempBroken, ChangeRequestPartitioned processToRunAsAStep, AbstractProcess parentProcess) {
            super(processToRunAsAStep,parentProcess);
            changesetGroupsCompleted = tempCompleted;
            changesetGroupsBroken = tempBroken;
            processToRunAsAStep.addProcessActionListener(this);
        }

        public boolean isReadyForExecution() {
            return processInfo.isPrereqsSatisfied(changesetGroupsCompleted);
        }

        public boolean canEverBeReadyForExecution() {
            return !processInfo.isPrereqChangeRequestGroupBroken(changesetGroupsBroken);
        }

        public void handleProcessCompletion(AbstractProcess ap) {
            // mark all these done
            if (ap.hasCompletedSuccessfully()) {
                changesetGroupsCompleted.add(processInfo.get_buildid());
            } else {
                changesetGroupsBroken.add(processInfo.get_buildid());

            }

        }

    }
}
