package com.ibm.sdwb.build390.process.management;

public interface ConditionalExecution {

    public boolean isReadyForExecution();

	public boolean canEverBeReadyForExecution(); 
}
