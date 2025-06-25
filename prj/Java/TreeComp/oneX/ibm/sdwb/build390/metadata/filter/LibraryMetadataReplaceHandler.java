package com.ibm.sdwb.build390.metadata.filter;
/*********************************************************************/
/* CmvcMetadataReplaceHandler     class for the Build/390 client     */
/* A replaceable handler to replace matching metadatavalues as       */
/* filtered by the regular expression                                */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.util.*;

import com.ibm.sdwb.build390.info.*;

public class LibraryMetadataReplaceHandler extends com.ibm.sdwb.build390.filter.ReplaceableHandler {

    public LibraryMetadataReplaceHandler(String newValue) {
        super(newValue);
    }

    public void doReplace(Object oldValue, Object newValue) {
        Map metadata = ((FileInfo)oldValue).getMetadata();
        Map newMetadata = new HashMap();
        for (Iterator iter=metadata.keySet().iterator();iter.hasNext();) {
            newMetadata.put(iter.next(),getNewValue());
        }
        //just set what we wanna replace. 
        if(!newMetadata.isEmpty()){
            ((FileInfo)oldValue).setMetadata(newMetadata);

        }
    }
}

