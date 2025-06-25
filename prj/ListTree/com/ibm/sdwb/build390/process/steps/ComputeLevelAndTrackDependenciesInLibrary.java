package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.info.*;
import java.util.*;

public class ComputeLevelAndTrackDependenciesInLibrary extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private LibraryInfo reqInfoQuerier = null;
    private Map releaseToTracksMap = null;
    private boolean includeStatesListed = true;
    private Set statesToHandle = null;
    private Map computedDependencies = null;

    public ComputeLevelAndTrackDependenciesInLibrary(LibraryInfo tempInfo, Map tempReleaseToTracksMap, Set tempStatesToHandle, boolean tempIncludeStatesListed, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Compute Level And Track Dependencies in Library");
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        reqInfoQuerier = tempInfo;
        statesToHandle = tempStatesToHandle;
        includeStatesListed = tempIncludeStatesListed;
        releaseToTracksMap = tempReleaseToTracksMap;
    }

    public Map getComputedDependencies(){
        return computedDependencies;
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
        getStatusHandler().updateStatus("Starting requisite check in library.", false);

        computedDependencies = new HashMap();

        for (Iterator releaseIterator = releaseToTracksMap.keySet().iterator(); releaseIterator.hasNext(); ) {
            String oneRelease = (String) releaseIterator.next();
            Set trackSet = (Set) releaseToTracksMap.get(oneRelease);
// this returns a hash of co & pre req relationships in RequisiteGraphNodes, one entry for each node.
            Map oneReleaseDependencyMap = reqInfoQuerier.getPrereqsAndCoreqs(trackSet, includeStatesListed, statesToHandle);
// here we repeatedly collapse cycles of prereq relationships in the dependancy graph.
// by that, I mean if A is a prereq of B, B is a prereq of C, and C is a prereq of A, all need to be
// built at once, so they are collapsed into a single level.
            Set handledTracks = new HashSet();
            for (Iterator nodeIterator = oneReleaseDependencyMap.keySet().iterator(); nodeIterator.hasNext();) {
                String currTrack = (String) nodeIterator.next();
                if (!handledTracks.contains(currTrack)) {
                    RequisiteGraphNode currNode = (RequisiteGraphNode) oneReleaseDependencyMap.get(currTrack);
                    handledTracks.addAll(currNode.getMembers());
                    if (!currNode.getMembers().isEmpty()) {
                        RequisiteGraphNode mainNode = (RequisiteGraphNode) computedDependencies.get(currTrack);
                        if (mainNode != null) {
                            currNode.mergeNode(mainNode);
                        }
                    }
                    for (Iterator memberIterator = currNode.getMembers().iterator();memberIterator.hasNext();) {
                        computedDependencies.put(memberIterator.next(), currNode);
                    }

                }
            }
        }
        while (shrinkGraph(computedDependencies)) {
        }
    }

    private boolean shrinkGraph(Map nodeSource) {
        boolean graphChanged = false;
        Iterator trackIterator = nodeSource.keySet().iterator();
        Set nodeVect = new HashSet();
// we go through each of the tracks in the level, and get the approriate node for each.
        while (trackIterator.hasNext()) {
            String tempTrack = (String) trackIterator.next();
            RequisiteGraphNode tempNode = (RequisiteGraphNode) nodeSource.get(tempTrack);
            // this method attempts to find a path from a node back to itself.  prereqs represent a directed
            // relationship, so this is not guaranteed
            LinkedList testPath = new LinkedList();
            testPath.add(tempNode);
            LinkedList cycle = checkForCycle(testPath, nodeSource);
            if (cycle != null) {
                // if a path is found, merge all the nodes on the path into one, and set the tracks in this node to point
                // to the new node.
                tempNode = (RequisiteGraphNode) cycle.getFirst();
                for (int i = 1; i < cycle.size(); i++) {
                    tempNode.mergeNode((RequisiteGraphNode) cycle.get(i));
                }
                for (Iterator memberIterator = tempNode.getMembers().iterator();memberIterator.hasNext();) {
                    String tempTrack2 = (String) memberIterator.next();
                    nodeSource.put(tempTrack2, tempNode);
                }
                graphChanged = true;
            }
        }
        return graphChanged;
    }

    private LinkedList checkForCycle(LinkedList nodePath, Map nodeSource) {

        if (nodePath != null & nodeSource != null) {
            RequisiteGraphNode lastNode = (RequisiteGraphNode) nodePath.getLast();
            for (Iterator prereqIterator = lastNode.getPrereqs().iterator();prereqIterator.hasNext();) {
                String currentPrereq = (String) prereqIterator.next();
                RequisiteGraphNode currentNode = (RequisiteGraphNode) nodeSource.get(currentPrereq);
                if (currentNode != null) {
                    if (nodePath.contains(currentNode)) {
                        LinkedList nodeCycle = new LinkedList();
                        int cycleStart = nodePath.indexOf(currentNode);
                        for (int i2 = cycleStart; i2 < nodePath.size(); i2++) {
                            nodeCycle.add(nodePath.get(i2));
                        }
                        return nodeCycle;
                    } else {
                        LinkedList newPath = (LinkedList) nodePath.clone();
                        newPath.add(currentNode);
                        LinkedList tempVect = checkForCycle(newPath, nodeSource);
                        if (tempVect != null) {
                            return tempVect;
                        }
                    }
                }
            }
            return null;
        }
        return null;
    }
}
