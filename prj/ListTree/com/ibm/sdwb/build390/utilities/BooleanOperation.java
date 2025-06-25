package com.ibm.sdwb.build390.utilities;

import java.util.*;


/**
 * To perform a logical AND on BooleanInferface objects
 * passed in.
 */
public abstract class BooleanOperation implements BooleanInterface {
    protected Set booleanInterfaceSet = null;

    public BooleanOperation(){
        booleanInterfaceSet = new HashSet();
    }

    public void addBooleanInterface(BooleanInterface newBoolean){
        booleanInterfaceSet.add(newBoolean);
    }

    public void addAllBooleanInterfaces(Collection newCollection){
        booleanInterfaceSet.addAll(newCollection);
    }

    public Set getOperandSet(){
        return booleanInterfaceSet;
    }


   public abstract boolean inputAvailable();
     
    public final String getNameOfBoolean(){
        return getOperationName();
    }

    protected abstract String getOperationName();


    public String getDescriptionOfBoolean(){
        String description = new String();
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            description+=oneBoolean.getReasonNotSatisfied();
            if (booleanIterator.hasNext()) {
                description +="\n  ("+getOperationName().toLowerCase()+")\n";
            }
        }
        return description;
    }
}
