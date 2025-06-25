package com.ibm.sdwb.build390.process.steps;

public class NoOp extends ProcessStep {
	static final long serialVersionUID = 1111111111111111L;

	public NoOp(com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
		super(tempProc,"NoOp");
		setVisibleToUser(false);
		setUndoBeforeRerun(false);
	}

	/**
	 * This is the method that should be implemented to actually
	 * run the process.	Use executionArgument if you need to 
	 * access the argument from the execute method.
	 * 
	 * @return Object indicating output of the step.
	 */
	public void execute() throws com.ibm.sdwb.build390.MBBuildException{
		getLEP().LogSecondaryInfo(getFullName(),"Entry");
	}
}
