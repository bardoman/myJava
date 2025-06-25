package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* RegularExpressionFilter      class for the Build/390 client       */
/* A  modified filter class which allows  matching to be perfomed    */
/* based on regularexpression.                                       */
/*                                                                   */
/* TO-DO: May be this is not needed.                                 */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import com.ibm.sdwb.build390.filter.criteria.*;

public class RegularExpressionFilter extends AbstractFilter {
    public RegularExpressionFilter(FilterCriteria criteria){
        super(criteria);
    }  
}



