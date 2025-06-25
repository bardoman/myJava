package com.ibm.sdwb.build390.metadata.filter;
/*********************************************************************/
/* CmvcMetadataFilterCriteria     class for the Build/390 client     */
/* A regular expression criteria to filter on cmvc metadata          */
/* keywords/value                                                    */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.util.*;
import java.util.Map.Entry;

import com.ibm.sdwb.build390.info.*;

public  class LibraryMetadataFilterCriteria extends AbstractMetadataFilterCriteria {


    public LibraryMetadataFilterCriteria(String metadataKey, String regex){
        super(metadataKey,regex);
    }


    public boolean passes(Object obj) {
        boolean isMatch = false;
        FileInfo info = (FileInfo)obj;
        Set  entries  = info.getMetadata().entrySet();
        for (Iterator iter = entries.iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (((String)entry.getKey()).startsWith(getKeyword())) {
                if (hasMatch(entry.getValue())) {
                    isMatch = true;
                } else {
                    iter.remove();
                }
            } else{
                iter.remove();
            }

        }

        return isMatch;

    }   


}

