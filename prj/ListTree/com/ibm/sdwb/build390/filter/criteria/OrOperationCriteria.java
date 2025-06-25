package com.ibm.sdwb.build390.filter.criteria;

import java.util.*;

public class OrOperationCriteria extends BooleanOperationCriteria {

    public OrOperationCriteria(){
    }

    public boolean passes(Object obj){

        for (Iterator iter= getAllFilterCriteria().iterator();iter.hasNext();) {
            FilterCriteria filterCriteria = (FilterCriteria)iter.next();
            if (filterCriteria.passes(obj)) {
                return true;
            }
        }
        return false;

    }

    public String getOperationName(){
        return "OR";
    }
}