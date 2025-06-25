package com.ibm.sdwb.build390.filter.criteria;


/**
 * The criteria compares cmvcpathnames (if its not null)
 * else compares mvcpartname & mvspart class (if both are not null.)
 */

import java.util.*;

import com.ibm.sdwb.build390.info.FileInfo;

public abstract class EliminateDuplicatesCriteria implements FilterCriteria {

    private List second =null;

    public EliminateDuplicatesCriteria() { 
    }

    public void setSearchList(List tempSecond) {
        this.second = new ArrayList(tempSecond);
        Collections.sort(second,FileInfo.BASIC_FILENAME_COMPARATOR);
    }

    public List getSearchList() {
        return this.second;
    }


    public boolean contains(Object toSearchInfo,List toSearchList){
        int ind = index(toSearchInfo,toSearchList);
        return(ind >= 0) ;
    }

    //cleanup later. so the subclasses know how to implement this guy. right now the comparator is static. so the subclasses can't change it. they'll have to override it to change it though.
    public int  index(Object toSearchInfo,List toSearchList){
        return Collections.binarySearch(toSearchList,toSearchInfo, FileInfo.BASIC_FILENAME_COMPARATOR);
    }

}
