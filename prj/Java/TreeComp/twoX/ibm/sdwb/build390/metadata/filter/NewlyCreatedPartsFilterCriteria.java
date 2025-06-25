package com.ibm.sdwb.build390.metadata.filter;
/**
 * The criteria compares cmvcpathnames (if its not null)
 * else compares mvcpartname & mvspart class (if both are not null.)
 */
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import java.util.*;

public class NewlyCreatedPartsFilterCriteria extends com.ibm.sdwb.build390.filter.criteria.EliminateDuplicatesCriteria {


    public NewlyCreatedPartsFilterCriteria(){

    }


    public  boolean passes(Object obj) {
        FileInfo info = (FileInfo)obj;
        int index = index(info,getSearchList());
        boolean isMatch = index >= 0; /* part doesnot exist in searchList (A U B)  */


        if (index>=0) {
            FileInfo compareInfo = (FileInfo)getSearchList().get(index);
            return true;
        } else {
            return false; /*if not a match then the part is not in the driver and is a new part */
        }
    }
}
