package com.ibm.sdwb.build390;

/* this interface defines a window that has a status line, a cancel button,
and an animation block to indicate activity.  MBInternalFrame & MBModalStatusFrame
both implement it.
*/

public interface MBAnimationStatusWindow extends com.ibm.sdwb.build390.userinterface.UserCommunicationInterface{

	/**
	 * The method to return the status object for the window
	 * 
	 * @return 
	 * @deprecated 
	 */
	public MBStatus getStatus();

	/* the method to set the MBStop object currently executing */
    public void setRunningItem(MBStop tempRunningItem);

    /* the method to clear the running item, and return the window
        to a usable state.
    */
    public void clearRunningItem();

    /* the method to set the cursor to a wait cursor, inside the window
    */
    public void setWaitCursor();

    /* the method to restore the original cursor */
    public void clearWaitCursor();
    
    /* the method to tell if cancel was pressed */
    public boolean wasCancelPressed();

    /* the method to get the enable status of the cancel button */
    public boolean getCancelButtonStatus();

    /* the method to set the enable status of the cancel button */
    public void setCancelButtonStatus(boolean enable);
   
}
