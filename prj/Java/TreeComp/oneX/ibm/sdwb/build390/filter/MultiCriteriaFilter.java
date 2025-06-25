package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* MultiCriteriaFilter vvvv     class for the Build/390 client       */
/* A  modified filter class which allows  matching to be perfomed    */
/* based on multicriteria    .                                       */
/*                                                                   */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import com.ibm.sdwb.build390.filter.criteria.*;

public class MultiCriteriaFilter extends AbstractFilter {

    public MultiCriteriaFilter(FilterCriteria criteria){
        super(criteria);
    }


    private  void createFilterCriteria(){
        if (getFilterCriteria() ==null) {
            setFilterCriteria(new MultiFilterCriteria());
        }   
    }

    public void addFilterCriteria(FilterCriteria filterCriteria){
        createFilterCriteria();
        ((MultiFilterCriteria)getFilterCriteria()).addFilterCriteria(filterCriteria);
    }

    


}
