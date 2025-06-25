package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* AbstractFilter                class for the Build/390 client      */
/* An  filter abstract implementation of Filter interface            */
/* subclasses override this class to perform specific stuff.         */
/*                                                                   */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/

import java.util.*;
import com.ibm.sdwb.build390.filter.criteria.*;


public class EliminateDuplicatesFilter extends AbstractFilter {


    public EliminateDuplicatesFilter(EliminateDuplicatesCriteria criteria){
        super(criteria);
    }

    public void mergeSearchList(){
        matched().clear();
        matched().addAll(((EliminateDuplicatesCriteria)getFilterCriteria()).getSearchList());

    }

    public void filter(Collection  list) {
        for (Iterator iter=list.iterator();iter.hasNext();) {
            Object obj = iter.next();
            if (((EliminateDuplicatesCriteria)getFilterCriteria()).passes(obj)) {
                ((LinkedList)matched()).addFirst(obj);
            } else {
                unmatched().add(obj);

            }
        }
    }


}
