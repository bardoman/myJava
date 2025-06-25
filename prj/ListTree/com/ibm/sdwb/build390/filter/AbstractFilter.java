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

abstract class AbstractFilter implements Filter {

    private FilterCriteria criteria = null;
    private FilterOutput output = null;


    private AbstractFilter(){
    }

    AbstractFilter(FilterCriteria criteria){
        this.criteria = criteria;
        output = new FilterOutput();
    }


    public void setFilterCriteria(FilterCriteria criteria){
        this.criteria = criteria;
    }

    public FilterCriteria getFilterCriteria(){
        return criteria;
    }

    public void filter(Collection collection){
        if (collection != null) {
            output.clearAll();
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (getFilterCriteria().passes(obj)) {
                    output.addMatchedEntry(obj);
                } else {
                    output.addUnMatchedEntry(obj);

                }
            }
        }
    }

    public FilterOutput output(){
        return output;
    }

    public Collection matched(){
        return output().getMatched();
    }

    public Collection unmatched(){
        return output().getUnMatched();
    }


}
