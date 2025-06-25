package com.ibm.sdwb.build390.metadata.filter;


/**
 * The criteria compares cmvcpathnames (if its not null)
 * else compares mvcpartname & mvspart class (if both are not null.)
 */

import java.util.*;
import com.ibm.sdwb.build390.info.*;

public class EliminateDuplicatePartsCriteria extends com.ibm.sdwb.build390.filter.criteria.EliminateDuplicatesCriteria {


    public EliminateDuplicatePartsCriteria() {
        super();
    }


    public  boolean passes(Object obj) {
        FileInfo info = (FileInfo)obj;
        boolean notADup = !contains(info, getSearchList());
        /** two conditions arise.
         * either the searchlist can have driverparts info or libraryparts info.
         */

        boolean inputHasHostMetadata       = (info.getTypeOfChange()!=null && info.getTypeOfChange().indexOf("M") >= 0);
        boolean inputHasMainframeName = (info.getMainframeFilename()!=null && ((info.getMainframeFilename().split("\\.")).length >= 2));

//if its library part then inputHasHostMetadata, inputHasMainframeName will be false.
//if its driver  part then inputHasHostMetadata (probably false, depending on mde already built), inputHasMainframeName  (true).

        if (!notADup) {
            int tempIndex = index(info,getSearchList());
            FileInfo temp = (FileInfo)getSearchList().get(tempIndex);

            boolean tempHasHostMetadata  = (temp.getTypeOfChange()!=null && temp.getTypeOfChange().indexOf("M") >= 0);
            boolean tempHasMainframeName = (temp.getMainframeFilename()!=null && ((temp.getMainframeFilename().split("\\.")).length >= 2));
            //only needed when the searchList contains library parts.

            if (!tempHasHostMetadata && inputHasHostMetadata) {
                temp.setTypeOfChange(info.getTypeOfChange());
            }

            //only needed when the searchList contains library parts.
            if (!tempHasMainframeName && inputHasMainframeName) {
                temp.setMainframeFilename(info.getMainframeFilename());
            }


        }

        return notADup;
    }

}
