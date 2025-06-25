package com.ibm.sdwb.build390.info;

import java.util.*;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.logprocess.*;

public class ChangeRequestPartitionedInfo extends com.ibm.sdwb.build390.MBBuild {

    private Set singleProjectBuild = null;
    private Set changeRequests = null;
    private Set prereqChangeRequestGroups = new HashSet();
    private ChangeRequestMultipleInfo parentInfo = null;
    private RequisiteGraphNode reqNode = null;

    public ChangeRequestPartitionedInfo(ChangeRequestMultipleInfo tempParent, LogEventProcessor lep) {
        super("N",tempParent,lep);
        parentInfo = tempParent;
        setOptions(parentInfo.getOptions());
        copyAddtionalBuildSettings(parentInfo);
    }

    public RequisiteGraphNode getRequisiteNode() {
        return reqNode;
    }

    public void setRequisiteNode(RequisiteGraphNode temp) {
        reqNode = temp;
    }

    public String getMainframeUserAddressToSendOutputTo() {
        return parentInfo.getMainframeUserAddressToSendOutputTo();
    }

    public String getMainframeDatasetToStoreOutputIn() {
        return parentInfo.getMainframeDatasetToStoreOutputIn();
    }

    public ChangeRequestMultipleInfo getParent() {
        return parentInfo;
    }

    public Set getSingleProjectBuildSet() {
        return(singleProjectBuild);
    }

    public Set getChangeRequestsInGroup() {
        return changeRequests;
    }

    public Set getPrereqChangeRequestGroups() {
        return prereqChangeRequestGroups;
    }

    public boolean isPrereqsSatisfied(Set changeRequestGroupsComplete) {
        return changeRequestGroupsComplete.containsAll(prereqChangeRequestGroups);
    }


    public boolean isPrereqChangeRequestGroupBroken(Set changeRequestGroupsBroken) {
        Set setCopy = new HashSet(changeRequestGroupsBroken);
        setCopy.retainAll(prereqChangeRequestGroups); // this will save any ChangeRequestGroup names that are in both the prereqs, and the broken set.
        return !setCopy.isEmpty();// if there are ChangeRequestGroup in both, then a prereq is broken, so it won't be empty
    }


    public void setSingleProjectBuildSet(Set set) {
        singleProjectBuild = set;
    }

    public void setSelectedChangeRequests(Set temp) {
        changeRequests = temp;
    }

    public void setPrereqChangeRequestGroups(Set prereqs) {
        if (prereqs != null) {
            prereqChangeRequestGroups = prereqs;
        } else {
            prereqChangeRequestGroups = new HashSet();
        }
    }

    public String get_buildtype() {
        return parentInfo.get_buildtype();
    }

    public String toString() {
        String buf = new String();
        buf += "ChangeRequestPartitionedInfo Object\n";
        buf += super.toString();
        if (singleProjectBuild == null) {
            buf += "singleProjectBuild =" + singleProjectBuild + "\n";
        } else {
            buf += "singleProjectBuild = " + singleProjectBuild.toString() + "\n";
        }
        if (changeRequests == null) {
            buf += "selected changeRequests =" + changeRequests + "\n";
        } else {
            buf += "selected changeRequests =" + changeRequests.toString() + "\n";
        }
        buf += "prereqChangeRequestGroups = " + prereqChangeRequestGroups.toString()+"\n";
        return buf;
    }
}
