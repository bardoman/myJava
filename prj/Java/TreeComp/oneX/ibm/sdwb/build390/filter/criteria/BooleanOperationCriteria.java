package com.ibm.sdwb.build390.filter.criteria;

import java.util.*;


public abstract class BooleanOperationCriteria  extends MultiFilterCriteria {

    abstract String getOperationName();

    public String toString(){
        StringBuffer toStr = new StringBuffer();
        for (Iterator iter = getAllFilterCriteria().iterator(); iter.hasNext();) {
            FilterCriteria filterCriteria = (FilterCriteria)iter.next();
            toStr.append(filterCriteria.toString() + "\n");
        }
        return("{"+getOperationName() +  " is: " + toStr.toString()+"}");
    }

}


