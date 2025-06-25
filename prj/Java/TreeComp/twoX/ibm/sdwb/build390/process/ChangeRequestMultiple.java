package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.userinterface.event.multiprocess.ChangeRequestPartitionedUpdateEvent; 
import com.ibm.sdwb.build390.info.*;
import java.util.*;

public class ChangeRequestMultiple extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;
    private String authorization = null;
    private ChangeRequestMultipleInfo processInfo = null;
    private Set statesThatCountAsComplete = null;
    private String changesetBuildErrorMessage = null;
    private com.ibm.sdwb.build390.process.ComputeLevelAndTrackDependenciesInLibrary showLevelRelationships = null;

    public ChangeRequestMultiple(UserCommunicationInterface userComm) {
        super("ChangeRequestMultiple",2, userComm); 
    }

    public void setProcessInfo(ChangeRequestMultipleInfo tempInfo) {
        processInfo = tempInfo;
        getCleanableEntity().setSetup(tempInfo.getSetup());
        getStatusHandler().updateStatus("Preparing to start usermod build, buildid("+processInfo.get_buildid()+")",false);
    }

    public void setStatesThatCountAsComplete(Set tempStateSet) {
        statesThatCountAsComplete = tempStateSet;
    }


    public void preExecution() throws com.ibm.sdwb.build390.MBBuildException{
        processInfo.save();
        String changesetErrors = new String();
        // verify that all objects to build are in the correct state
        for (Iterator changesetIterator = processInfo.getChangeRequests().iterator(); changesetIterator.hasNext();) {
            ChangeRequest oneChangeset = (ChangeRequest) changesetIterator.next();
            if (!oneChangeset.isReadyForPackageProcessing()) {
                changesetErrors += oneChangeset.getName() + "\n";
            }
        }
        if (changesetErrors.trim().length() > 0) {
            throw new LibraryError("The following change sets are not in the proper state for processing:\n"+changesetErrors+"Please move them to the buildable state before restarting.");
        }
    }

    public void childSave() throws com.ibm.sdwb.build390.MBBuildException{
        processInfo.save();

    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        switch (stepToGet) {
        case 0:
            Map projectToChangesetMap = getReleaseToTrackMapChangeRequests(processInfo.getChangeRequests());
            showLevelRelationships = new com.ibm.sdwb.build390.process.ComputeLevelAndTrackDependenciesInLibrary(processInfo.getSetup().getLibraryInfo(), projectToChangesetMap, statesThatCountAsComplete,false, this);
            FullProcess showRelationshipsWrapper = new FullProcess(showLevelRelationships, this);
            return showRelationshipsWrapper;

        case 1:
            LibraryError badChangesetsError = null;
            if (changesetBuildErrorMessage !=null) {
                badChangesetsError = new LibraryError(changesetBuildErrorMessage);
                if ((processInfo.getChangesetGroups().size()>0) && !processInfo.isDryRun()) {
                    // this means there are change set groups we can process so keep going even if we hit an error
                    getLEP().LogException(badChangesetsError);

                } else {
                    //System.out.println("hey dude its an ptf error ");
                    throw badChangesetsError;
                }
            }
            if (!processInfo.isDryRun()) {
                return createIndivualChangesetGroupProcesses();
            } else {
                return new NoOp(this);
            } 
        }
        return null;
    }

    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
        switch (stepRun) {
        case 0:

            if (processInfo.isBundled()) {
                RequisiteGraphNode massiveNode = new RequisiteGraphNode();
                for (Iterator nodeIterator = showLevelRelationships.getComputedDependencies().keySet().iterator(); nodeIterator.hasNext();) {
                    String nodeKey = (String) nodeIterator.next();
                    RequisiteGraphNode tempNode = (RequisiteGraphNode) showLevelRelationships.getComputedDependencies().get(nodeKey);
                    massiveNode.mergeNode(tempNode);
                    showLevelRelationships.getComputedDependencies().put(nodeKey, massiveNode);
                }
            }

            processInfo.setChangesetGroups(createChangesetGroupsToProcess());
            break;

        }
    }

    public void handleProcessCompletion(AbstractProcess ap) {
        // do some error checking in here to see if we hit problems.
        try {
            undoProcess();
        } catch (MBBuildException mbe) {
            getLEP().LogException(mbe);
        }

    }


    private com.ibm.sdwb.build390.process.steps.ConcurrentSteps createIndivualChangesetGroupProcesses() throws MBBuildException{
        com.ibm.sdwb.build390.process.steps.ConcurrentSteps theStep = new com.ibm.sdwb.build390.process.steps.ConcurrentSteps("Build individual sets", this);
        Set completedChangesetGroup = Collections.synchronizedSet(new HashSet());
        Set brokenChangesetGroup = Collections.synchronizedSet(new HashSet());
        for (Iterator changesetGroupInfoIterator = processInfo.getChangesetGroups().iterator(); changesetGroupInfoIterator.hasNext();) {
            ChangeRequestPartitionedInfo oneSet = (ChangeRequestPartitionedInfo)  changesetGroupInfoIterator.next();
            ChangeRequestPartitioned oneProcess = new ChangeRequestPartitioned(oneSet, this);
            oneSet.setProcessForThisBuild(oneProcess);
            ProcessStep step  = oneProcess.getStepVersionOfProcess(completedChangesetGroup, brokenChangesetGroup, this);
            ChangeRequestPartitionedUpdateEvent oneEvent = new ChangeRequestPartitionedUpdateEvent(oneSet);
            handleUIEvent(oneEvent);
            theStep.addStepToRun(step);
        }
        return theStep;
    }

    private Map getReleaseToTrackMapChangeRequests(Set changeRequestSet) throws MBBuildException{
        Map releaseMap = new HashMap();
        for (Iterator infoIterator = changeRequestSet.iterator(); infoIterator.hasNext();) {
            ChangeRequest oneChangeRequest = (ChangeRequest) infoIterator.next();
            for (Iterator listRelIter = oneChangeRequest.getIndividualSourceInfos().iterator() ; listRelIter.hasNext();) {
                Changeset oneChangeset = (Changeset) listRelIter.next();
                String currRelease = oneChangeset.getProject();
                Set changesetsInRelease = (Set) releaseMap.get(currRelease);
                if (changesetsInRelease == null) {
                    changesetsInRelease = new HashSet();
                    releaseMap.put(currRelease, changesetsInRelease);
                }
                changesetsInRelease.add(oneChangeset);
            }
        }
        return releaseMap;
    }


    private Set createChangesetGroupsToProcess() throws MBBuildException{
        Set buildsToReturn = new HashSet();
        Map handledChangesetToNodeMap = new HashMap();
        boolean unhandledNodes = false;
        for (boolean continueHandlingNodes = true; continueHandlingNodes;) {
            unhandledNodes = false;
            continueHandlingNodes = false;

            for (Iterator nodeIterator = showLevelRelationships.getComputedDependencies().values().iterator(); nodeIterator.hasNext();) {
                RequisiteGraphNode oneNode = (RequisiteGraphNode)nodeIterator.next();

                boolean isNodeUnhandled = false;
                for (Iterator memberIterator = oneNode.getMembers().iterator() ; memberIterator.hasNext();) {
                    isNodeUnhandled = isNodeUnhandled | !handledChangesetToNodeMap.containsKey(memberIterator.next());
                }

                if (isNodeUnhandled) {
                    unhandledNodes = true;
                    boolean isNodePrereqsSatisfied = true;
                    Set preqNodes = new HashSet();
                    for (Iterator prereqIterator = oneNode.getPrereqs().iterator(); prereqIterator.hasNext();) {
                        String prereqChangeset = prereqIterator.next().toString();
                        if (handledChangesetToNodeMap.containsKey(prereqChangeset)) {
                            String prereqNode = ((ChangeRequestPartitionedInfo) handledChangesetToNodeMap.get(prereqChangeset)).get_buildid();
                            preqNodes.add(prereqNode);
                        } else {
                            isNodePrereqsSatisfied = false;
                        }
                    }
                    boolean isNodeCoreqsPresent = true;
                    for (Iterator memberIterator = oneNode.getMembers().iterator(); memberIterator.hasNext();) {
                        String memberChangeset = memberIterator.next().toString();
                        boolean changesetFound = false;
                        for (Iterator infoIterator  = processInfo.getChangeRequests().iterator(); infoIterator.hasNext();) {
                            ChangeRequest myInfo = (ChangeRequest) infoIterator.next();
                            changesetFound = changesetFound | myInfo.getName().equals(memberChangeset);
                        }
                        isNodeCoreqsPresent = isNodeCoreqsPresent & changesetFound;
                    }
                    if (isNodePrereqsSatisfied & isNodeCoreqsPresent) {
                        continueHandlingNodes = true;
                        ChangeRequestPartitionedInfo oneChangesetGroup = new ChangeRequestPartitionedInfo(processInfo, getLEP());
                        oneChangesetGroup.setRequisiteNode(oneNode);
                        oneChangesetGroup.setPrereqChangeRequestGroups(preqNodes);
                        Set nodeChangesetSet = new HashSet();
                        for (Iterator infoIterator  = processInfo.getChangeRequests().iterator(); infoIterator.hasNext();) {
                            ChangeRequest myInfo = (ChangeRequest) infoIterator.next();
                            if (oneNode.getMembers().contains(myInfo.getName())) {
                                nodeChangesetSet.add(myInfo);
                                handledChangesetToNodeMap.put(myInfo.getName(), oneChangesetGroup);
                            }
                        }
                        oneChangesetGroup.setSelectedChangeRequests(nodeChangesetSet);
                        buildsToReturn.add(oneChangesetGroup);
                    }
                }
            }
        }
        if (unhandledNodes) {
            StringBuilder strb = new StringBuilder();
            Formatter formatter = new Formatter(strb);
            formatter.format("%s%n%s%n", "The following errors are due to unbuilt change sets or change sets with failed test records.",
                             "You must build them before the selected change sets can be built.");
            Set nodeErrorsHandled = new HashSet();
            for (Iterator nodeIterator = showLevelRelationships.getComputedDependencies().values().iterator(); nodeIterator.hasNext();) {
                RequisiteGraphNode oneNode = (RequisiteGraphNode)nodeIterator.next();
                if (!nodeErrorsHandled.contains(oneNode)) {
                    nodeErrorsHandled.add(oneNode);
                    boolean isNodeUnhandled = false;
                    String missingCoreqs = new String();
                    for (Iterator memberIterator = oneNode.getMembers().iterator(); memberIterator.hasNext();) {
                        String memberChangeset = memberIterator.next().toString();
                        isNodeUnhandled = isNodeUnhandled | !handledChangesetToNodeMap.containsKey(memberChangeset);
                        boolean changesetFound = false;
                        for (Iterator infoIterator = processInfo.getChangeRequests().iterator(); infoIterator.hasNext();) {
                            ChangeRequest myInfo = (ChangeRequest) infoIterator.next();
                            changesetFound = changesetFound | myInfo.getName().equals(memberChangeset);
                        }
                        if (!changesetFound) {
                            if (missingCoreqs.trim().length() > 0) {
                                missingCoreqs += ", ";
                            }
                            missingCoreqs += memberChangeset;
                        }
                    }
                    String missingPrereqs = new String();
                    if (isNodeUnhandled) {
                        for (Iterator prereqIterator = oneNode.getPrereqs().iterator(); prereqIterator.hasNext();) {
                            String prereqChangeset = prereqIterator.next().toString();
                            if (!handledChangesetToNodeMap.containsKey(prereqChangeset)) {
                                if (missingPrereqs.trim().length() > 0) {
                                    missingPrereqs += ", ";
                                }
                                missingPrereqs += prereqChangeset;
                            }
                        }
                    }
                    if (missingCoreqs.length() > 0 | missingPrereqs.length()>0) {
                        formatter.format("%s%n","For the change set group consisting of");
                        for (Iterator memberIterator = oneNode.getMembers().iterator(); memberIterator.hasNext();) {
                            formatter.format("%s ",memberIterator.next());
                        }
                        formatter.format("%n");
                        if (missingCoreqs.length() > 0) {
                            formatter.format("%5s%-15s:%s%n","","Missing Coreqs",missingCoreqs);

                        }
                        if (missingPrereqs.length() > 0) {
                            formatter.format("%5s%-15s:%s%n","","Missing Prereqs",missingPrereqs);
                        }
                        changesetBuildErrorMessage = strb.toString();
                    }
                }
            }
        }
        return buildsToReturn;
    }

    protected String getChangesetBuildErrorMessage() {
        return changesetBuildErrorMessage;
    }


    public void sendTestInformation() {
        MBUtilities.createTestMail(processInfo.get_buildid(), processInfo.getDriverInformation().getName() , processInfo.getReleaseInformation().getLibraryName(), 
                                   "USERMOD", getTimeOfLastRun(), hasCompletedSuccessfully(), processInfo.get_descr());
    }
}
