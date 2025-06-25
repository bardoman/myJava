package com.ibm.sdwb.build390;

/* this interface defines a window that has a save method
*/

public interface MBSaveableFrame {

	/* the method saves the information in the window.*/
	public boolean save() throws com.ibm.sdwb.build390.MBBuildException;

	/* Check if save needs to be done */
	public boolean saveNeeded();
}
