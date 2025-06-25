package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* ReplaceableFilter              class for the Build/390 client     */
/* A replaceable filter, filters are replaces the matched entries    */
/* using the replaceable handler                                     */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.util.*;

/** using decorating approach. 
 * ie any type filter could be made a replaceable filter.
 * and the filter method is overridden to do default filter stuff. and then the 
 * replace stuff is taken care.
 **/ 
public class ReplaceableFilter extends AbstractFilter {

    private ReplaceableHandler handler;
    private Object newValue;

    public ReplaceableFilter(Filter filter,ReplaceableHandler handler){
        super(filter.getFilterCriteria());
        this.handler = handler;
    }  


    public void filter(Collection collection) {
        super.filter(collection);
        for(Iterator iter=matched().iterator(); iter.hasNext();){
            handler.doReplace(iter.next(),handler.getNewValue());
        }
    }
    
    public ReplaceableHandler getReplaceableHandler(){
        return handler;
    }

}

