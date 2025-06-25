package com.ibm.sdwb.build390.info;

import java.util.*;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.library.*;

public class ChangesetGroupInfo extends com.ibm.sdwb.build390.MBBuild {

    static final long serialVersionUID = 5288795939001507230L;
    private ChangeRequestPartitionedInfo parentInfo = null;
    private String name = null;
    private Set setOfChangesets = null;
    private Map FMIDToSMODMap = null;

    public ChangesetGroupInfo(LogEventProcessor lep, ChangeRequestPartitionedInfo tempParent) {
        super("O",tempParent, lep);
        parentInfo = tempParent;
        FMIDToSMODMap = new HashMap();
        setOptions(parentInfo.getOptions());
        copyAddtionalBuildSettings(parentInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String temp) {
        name = temp;
    }

    public Set getSetOfChangesets() {
        return(setOfChangesets);
    }

    public ChangesetGroup getChangesetGroup() {
        return(ChangesetGroup) getSource();
    }

    public void setSetOfChangesets(Set set) {
        setOfChangesets = set;
    }

    public Map getFMIDToSMODMap() {
        return FMIDToSMODMap;
    }

    public ChangeRequestPartitionedInfo getParentInfo() {
        return parentInfo;
    }

    public String createLogicString() {
        RequisiteGraphNode nodeToHandle = getParentInfo().getRequisiteNode();
        String logicString = new String();
        List<String> ifReqLogic = new ArrayList<String>();
        List supLogic = new ArrayList();
        Set preLogic = nodeToHandle.getExplicitPrereqs();
        Set currentTracks = nodeToHandle.getMembers();
        for (Iterator trackIterator = currentTracks.iterator(); trackIterator.hasNext();) {
            String currentTrack = (String) trackIterator.next();
            supLogic.add(currentTrack);
            List<String> ifReqsForTrack = null;
            if (parentInfo.getParent() instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
                ifReqsForTrack =  ((com.ibm.sdwb.build390.info.UsermodGeneralInfo) parentInfo.getParent()).getIfReqList(currentTrack);
            }
            if (ifReqsForTrack != null) {
                for (String currentReq: ifReqsForTrack) {
                    if (!ifReqLogic.contains(currentReq)) {
                        ifReqLogic.add(currentReq);
                    }
                }
            }
        }

        for (int i = 0; i < ifReqLogic.size(); i++) {
            logicString += ", IFR"+(i+1)+"="+ ifReqLogic.get(i);
        }
        for (int i = 0; i < supLogic.size(); i++) {
            logicString += ", SUP"+(i+1)+"="+(String) supLogic.get(i);
        }
        if (preLogic != null) {
            int i = 1; 
            for (Iterator preIterator = preLogic.iterator();preIterator.hasNext();i++) {
                logicString += ", PRE"+i+"="+(String) preIterator.next();
            }
        }
        return logicString;
    }

    public String get_buildtype() {
        return parentInfo.get_buildtype();
    }

    public String toString() {
        String buf = new String();
        buf += "ChangesetGroupInfo Object\n";
        buf += super.toString();
        if (setOfChangesets == null) {
            buf += "selected setOfChangesets =" + setOfChangesets + "\n";
        } else {
            buf += "selected setOfChangesets =" + setOfChangesets.toString() + "\n";
        }
        return buf;
    }
}
