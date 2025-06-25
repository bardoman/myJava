package com.ibm.sdwb.build390.process.management;

public interface Haltable {

	public boolean isHaltable();

    public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException;
}
