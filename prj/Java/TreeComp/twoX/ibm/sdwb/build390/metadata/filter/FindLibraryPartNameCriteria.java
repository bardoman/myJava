package com.ibm.sdwb.build390.metadata.filter;

import com.ibm.sdwb.build390.info.*;

/*********************************************************************/
/* FindCmvcPartNameCriteria     class for the Build/390 client       */
/* A regular expression criteria to filter on cmvc partnames         */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/

public class FindLibraryPartNameCriteria  extends com.ibm.sdwb.build390.filter.criteria.RegularExpressionCriteria {


    public FindLibraryPartNameCriteria(String libraryPartNameToFind){
        super(libraryPartNameToFind);
    }

    public boolean passes(Object obj) {
        FileInfo info = (FileInfo)obj;
        return(hasMatch(info.getName()));
    }
}




