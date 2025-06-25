package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java smartHashtable class for Build390 client                     */
/* This class extends java.util.Hashtable so putting a null doesn't  */
/* Cause a crash.                                                    */
/*********************************************************************/
/* Updates:                                                          */
/*********************************************************************/


public class smartHashtable extends java.util.Hashtable {
    
    /** put if value is not null, puts the value in the Hashtable. 
    *   if value is null, remove the given key from the Hashtable.
    *   @param Object key
    *   @param Object value */
    public synchronized Object put(Object key, Object value) {
        Object oldValue = get(key);
        if (value == null) {
            remove(key);
        } else {
            super.put(key, value);
        }
        return oldValue;
    }
}
