package com.ibm.sdwb.build390.process.management;

public interface Undoable {

	public boolean isUndoable();

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException;
}
