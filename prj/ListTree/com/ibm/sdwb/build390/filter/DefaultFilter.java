package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* DefaultFilter                class for the Build/390 client       */
/* An  filter interface  which allows  matching to be perfomed       */
/* based on singlecriteria                                           */
/*                                                                   */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;


public class DefaultFilter extends AbstractFilter {
   public DefaultFilter(FilterCriteria criteria){
        super(criteria);
    } 
}
