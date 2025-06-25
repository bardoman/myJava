package com.ibm.sdwb.build390.metadata.filter;
/**
 * The criteria compares cmvcpathnames (if its not null)
 * else compares mvcpartname & mvspart class (if both are not null.)
 */
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import java.util.*;

public class RebuildNeededPartsFilterCriteria extends com.ibm.sdwb.build390.filter.criteria.EliminateDuplicatesCriteria {


    public RebuildNeededPartsFilterCriteria(){
    }

    public  boolean passes(Object obj) {
        FileInfo info = (FileInfo)obj;
        int index = index(info,getSearchList());
        boolean isMatch = index >= 0; /* part doesnot exist in searchList (A U B)  */


        if (index>=0) {
            FileInfo compareInfo = (FileInfo)getSearchList().get(index);

     /*       if (info.isBuildOFF()) {
                return true;
            }
            */

            return !(info.getVersion().trim().equalsIgnoreCase(compareInfo.getVersion()));
        } else {
            return false; /*if not a match then the part is not in the driver and is a new part */
        }
    }
}
