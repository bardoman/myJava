package com.ibm.sdwb.build390;

import java.util.*;
import java.io.Serializable;

public class MBDependencyNode implements Serializable {
    Vector predecessors = null;
    Vector successors = null;

    public MBDependencyNode(){
        predecessors = new Vector();
        successors = new Vector();
    }

    public MBDependencyNode(Vector tempPredecessors, Vector tempSuccessors) {
        predecessors = tempPredecessors;
        successors = tempSuccessors;
    }

    public void addPredecessor(String tempPredecessors) {
        if (!predecessors.contains(tempPredecessors)) {
            predecessors.addElement(tempPredecessors);
        }
    }

    public void addSuccessor(String tempSuccessors) {
        if (!successors.contains(tempSuccessors)) {
            successors.addElement(tempSuccessors);
        }
    }

    public Vector getPredecessors() {
        return predecessors;
    }

    public Vector getSuccessors() {
        return successors;
    }

    public String toString() {
        String returnString = new String();
        returnString += "Predecessors-"+predecessors.toString()+" Successors-"+successors.toString();
        return returnString;
    }
}
