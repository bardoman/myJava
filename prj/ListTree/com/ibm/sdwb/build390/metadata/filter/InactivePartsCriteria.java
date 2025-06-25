package com.ibm.sdwb.build390.metadata.filter;
/**
 * The criteria compares cmvcpathnames (if its not null)
 * else compares mvcpartname & mvspart class (if both are not null.)
 */
import com.ibm.sdwb.build390.info.*;

public class InactivePartsCriteria implements com.ibm.sdwb.build390.filter.criteria.FilterCriteria {


    public InactivePartsCriteria() {
    }


    public  boolean passes(Object obj) {
        FileInfo inputInfo = (FileInfo)obj;
        //return inputInfo.isBuildOFF();
        return false;
    }
}

