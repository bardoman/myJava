package com.ibm.sdwb.build390.utilities;

import java.util.*;


/**
 * To perform a logical AND on BooleanInferface objects
 * passed in.
 */
public class BooleanAnd extends BooleanOperation {

    public boolean isSatisfied() {

        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            if (!oneBoolean.isSatisfied()) {
                return false;
            }
        }
        return true;
    }

    protected String getOperationName() {
        return "AND";
    }
    public final   boolean inputAvailable() {
        boolean available = true;
        for (Iterator boolIterator = booleanInterfaceSet.iterator(); boolIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) boolIterator.next();
            boolean temp = oneBoolean.inputAvailable();
            if (temp) {
                available=true;
                break;
            }
            available = available && temp; 
        }
        return available;
    }


    public String getReasonNotSatisfied() {
        String reasonNotSatisfied = null;
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            boolean notSatisfied =false;
            if (!oneBoolean.isSatisfied()) {
                notSatisfied = true;
            }
            if (notSatisfied) {
                if (reasonNotSatisfied==null) {
                    reasonNotSatisfied = new String();
                } else {
                    reasonNotSatisfied +="\n"+"  (and)\n";
                }
                reasonNotSatisfied+=oneBoolean.getReasonNotSatisfied();
            }
        }
        return reasonNotSatisfied;
    }
}
