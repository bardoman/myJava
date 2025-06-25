package com.ibm.sdwb.build390.userinterface.graphic.panels.metadata;

/*********************************************************************/
/* CloseableTab                  class for the Build/390 client      */
/* This interface is implemented by class OneTabWithTable            */
/* has operations to says if a tab is closeable or not.              */
/*********************************************************************/
//03/04/2005 TST2113 updated as part of this defect.(Req:SDWB2393 multi tab filter display 
/*********************************************************************/


public interface CloseableTabHandler {
    public  void setCloseable(boolean isCloseTab);
    public boolean isCloseable();
}
