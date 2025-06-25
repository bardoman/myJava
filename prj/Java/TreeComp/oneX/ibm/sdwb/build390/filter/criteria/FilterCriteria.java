package com.ibm.sdwb.build390.filter.criteria;
/*********************************************************************/
/* FilterCriteria                 class for the Build/390 client     */
/* An interface which would be implemented by classes to write their */
/* own custom filter criteria                                        */
/* example : filter by distname="abc"                                */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/

public interface FilterCriteria {
    public boolean passes(Object o);
}