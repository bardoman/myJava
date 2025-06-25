package com.ibm.sdwb.build390.info;

import java.util.*;
import java.io.Serializable;

public class RequisiteGraphNode implements Serializable {
    HashSet nodeMembers = null;
    HashSet prereqs = null;
    HashSet explicitPrereqs = null;

    public RequisiteGraphNode(){
        nodeMembers = new HashSet();
        prereqs = new HashSet();
        explicitPrereqs = new HashSet();
    }

    public RequisiteGraphNode(Set tempMembers, Set tempPrereqs, Set tempExplicitPrereqs) {
        nodeMembers = new HashSet(tempMembers);
        prereqs = new HashSet(tempPrereqs);
        explicitPrereqs = new HashSet(tempExplicitPrereqs);
    }

    public void mergeNode(RequisiteGraphNode tempNode) {
        addMemberSet(tempNode.getMembers());
        addPrereqSet(tempNode.getPrereqs());
        addExplicitPrereqSet(tempNode.getExplicitPrereqs());
    }

    public void addMember(String tempMember) {
        prereqs.remove(tempMember);
        nodeMembers.add(tempMember);
    }

    protected void addMemberSet(Set newMembers){
        prereqs.removeAll(newMembers);
        nodeMembers.addAll(newMembers);
    }

    public void addPrereq(String tempPrereq) {
        if (!nodeMembers.contains(tempPrereq)) {
            prereqs.add(tempPrereq);
        }
    }

    protected void addPrereqSet(Set newPrereqs){
// add all the new prereqs
        prereqs.addAll(newPrereqs);
// remove any of the prereqs we added that are already members
        prereqs.removeAll(nodeMembers);
    }

    public void addExplicitPrereq(String tempPrereq) {
        if (!nodeMembers.contains(tempPrereq)) {
            explicitPrereqs.add(tempPrereq);
        }
    }

    protected void addExplicitPrereqSet(Set newPrereqs){
// add all the new prereqs
        explicitPrereqs.addAll(newPrereqs);
// remove any of the prereqs we added that are already members
        explicitPrereqs.removeAll(nodeMembers);
    }


    public boolean containsMember(String tempMember) {
        return nodeMembers.contains(tempMember);
    }

    public boolean containsPrereq(String tempMember) {
        return prereqs.contains(tempMember);
    }

    public boolean containsExplicitPrereq(String tempMember) {
        return explicitPrereqs.contains(tempMember);
    }

    public Set getMembers() {
        return(Set) nodeMembers.clone();
    }

    public Set getPrereqs() {
        return(Set) prereqs.clone();
    }

    public Set getExplicitPrereqs() {
        return(Set) explicitPrereqs.clone();
    }

    public String toString() {
        String returnString = new String();
        returnString += "Members-"+nodeMembers.toString()+" Prereqs-"+prereqs.toString()+" Explicit Prereqs-"+explicitPrereqs.toString();
        return returnString;
    }
}
