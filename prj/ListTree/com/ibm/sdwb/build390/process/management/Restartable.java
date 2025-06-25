package com.ibm.sdwb.build390.process.management;

public interface Restartable {

	public boolean isRestartable();

	/**
	 * This determines how restarts are handled.
	 * If true, restart has to be from the beginning.
	 * If false, restart can be picked up in the middle.
	 * 
	 * @return true if must be restarted from beginning
	 *         false if can be resumed in the middle
	 */
	public boolean isOnlyRestartableFromBeginning();

	public void restartFromMiddle() throws com.ibm.sdwb.build390.MBBuildException;
}
