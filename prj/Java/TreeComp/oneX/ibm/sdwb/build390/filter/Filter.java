package com.ibm.sdwb.build390.filter; 
/*********************************************************************/
/* Filter                       class for the Build/390 client       */
/* An  filter interface  which allows  matching to be perfomed       */
/* based on criteria                                                 */
/*                                                                   */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import com.ibm.sdwb.build390.filter.criteria.*;
import java.util.*;

public interface Filter {


    public FilterCriteria getFilterCriteria();

    public  void filter(Collection filterinput);
    public  FilterOutput   output();
    public  Collection matched();
    public  Collection unmatched();
    

}
