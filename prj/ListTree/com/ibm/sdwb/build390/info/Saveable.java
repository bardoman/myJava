package com.ibm.sdwb.build390.info;

public interface Saveable{

	/**
	 * This method should save the state of the object 
	 * implementing it.
	 */
	public void save() throws com.ibm.sdwb.build390.MBBuildException;

}
