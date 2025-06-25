package com.ibm.sdwb.build390;

import java.util.Hashtable;
/*********************************************************************/
/* Java Stop interface for Build/390                              */
/*  Defines the stop method required for many command classes    */
/*********************************************************************/

/** <br>The MBStop interface defines the stop method required
* to be implemented by many classes.
*/
public interface MBStop{

    /** The stop method stops command execution.
    */
    public void stop() throws com.ibm.sdwb.build390.MBBuildException;
}
