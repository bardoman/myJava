package com.ibm.sdwb.build390.filter.criteria;
/*********************************************************************/
/* MultiFilterCriteria            class for the Build/390 client     */
/* A  modified filter class which has helper method to add multiple  */
/* filtercriteria.                                                   */
/* example : filter by distname="abc" and filter on partname="tempo" */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.util.*;

public class MultiFilterCriteria implements FilterCriteria {

    private LinkedList allFilterCriteria = new LinkedList(); /*criteria are ordered */

    public void addFilterCriteria(FilterCriteria filterCriteria){
        allFilterCriteria.add(filterCriteria);
    }

    public Collection getAllFilterCriteria(){
        return allFilterCriteria;
    }

    public boolean passes(Object o){
        for (int i = 0; i < allFilterCriteria.size(); i ++) {
            FilterCriteria filterCriteria = (FilterCriteria)allFilterCriteria.get(i);
            if (!filterCriteria.passes(o)) {
                return false;
            }
        }
        return true;


    }

    public String toString(){
        StringBuffer toStr = new StringBuffer();
        for (int i = 0; i < allFilterCriteria.size(); i ++) {
            FilterCriteria filterCriteria = (FilterCriteria)allFilterCriteria.get(i);
            toStr.append(filterCriteria.toString() + "\n");
        }
        return("Multi criteria is: " + toStr.toString());
    }


}
