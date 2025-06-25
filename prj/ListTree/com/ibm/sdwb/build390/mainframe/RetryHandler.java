package com.ibm.sdwb.build390.mainframe;

/*********************************************************************/
/* RetryHandler : This should be implemented by mainframecommunication  
/* steps which needs to retry and submit the call again.
/*********************************************************************/
//09/19/2005 PTM4094 autoretry connection.
/*********************************************************************/


public interface RetryHandler extends java.io.Serializable {
    static final long serialVersionUID = 1111111111111111L;

    /** The number of times retry should be performed 
     */ 
    public int getRetryCount();

    /** sleep count before a retry  is performed  in ms.
     * so if 1  minute  of  sleep time is needed, send back 60 * 1000 
     */ 
    public int getSleepCountAfterRetry();

    /** if a password auth failed, then a retry should be attempted.
     */ 
    public boolean isRetryAppropriate();

}

