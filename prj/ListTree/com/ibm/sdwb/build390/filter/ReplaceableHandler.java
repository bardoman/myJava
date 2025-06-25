package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* ReplaceableHandler             class for the Build/390 client     */
/* A replaceable handler to replace matching values based on         */
/* doReplace implementation(overriden in subclassed)                 */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/

public abstract class   ReplaceableHandler  {

    private Object newValue;

    public ReplaceableHandler(Object newValue){
        this.newValue = newValue;
    }

    public abstract void doReplace(Object oldValue,Object newValue);

    public Object getNewValue(){
        return this.newValue;
    }
}
