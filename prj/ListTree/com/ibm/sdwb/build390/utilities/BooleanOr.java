package com.ibm.sdwb.build390.utilities;

import java.util.*;


/**
 * To perform a logical OR on BooleanInferface objects
 * passed in.
 */
public class BooleanOr extends BooleanOperation {

    public boolean isSatisfied() {
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            if (oneBoolean.isSatisfied()) {
                return true;
            }
        }
        return false;
    }

    public  boolean inputAvailable() {
        boolean available = false;
        for (Iterator boolIterator = booleanInterfaceSet.iterator(); boolIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) boolIterator.next();
            available = available || oneBoolean.inputAvailable();
        }
        return available;
    }

    protected String getOperationName() {
        return "OR";
    }

    public String getReasonNotSatisfied() {
        String reasonNotSatisfied = null;
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            if (!oneBoolean.isSatisfied()) {
                if (reasonNotSatisfied==null) {
                    reasonNotSatisfied = new String();
                } else {
                    reasonNotSatisfied += "\n  (or)\n";
                }
                reasonNotSatisfied+=oneBoolean.getReasonNotSatisfied();
            }
        }
        return reasonNotSatisfied;
    }
}
