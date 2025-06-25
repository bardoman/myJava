package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.management.*;
import com.ibm.sdwb.build390.*;


/**
 * Process step is the basic unit of a process.  There should be 
 * only one logical action done in a step.  The Shadow check
 * and resulting report parsing could go in the same step, but
 * that is the largest combinations of steps that should be
 * allowed. 
 * Of course, there's an exception.  There will be a type
 * of ProcessStep called SubProcess.   SubProcess will allow
 * insertion of entire other Processes in a given process.
 * For instance, running driverbuild in a Usermod process would
 * be represented by a SubProcess step.
 */
public abstract class ProcessStep implements Cleanable, Haltable, Restartable, Undoable, ConditionalExecution, java.io.Serializable{
	static final long serialVersionUID = 1111111111111111L;

	private String name = null;
	protected AbstractProcess mainProcess = null;
	private boolean visibleToUser = true;
	private boolean alwaysRun = false;
	private boolean undoBeforeRerun = false;
	protected boolean completedSuccessfully = false;
	protected transient Haltable currentRunning = null;
	protected boolean stopped = false;

	protected ProcessStep(AbstractProcess tempProcess, String stepName){
		name = stepName;
		mainProcess = tempProcess;
	}

	public final String getFullName(){
		return mainProcess.getName() + ":"+name;
	}

	public final String getName(){
		return name;
	}

	public final void setName(String tempName){
		name = tempName;
	}

	public final LogEventProcessor getLEP(){
		if (mainProcess.getLEP()==null) {
			return new LogEventProcessor();
		}
		return mainProcess.getLEP();
	}

	public final MBStatus getStatusHandler(){
		if (mainProcess.getStatusHandler()==null) {
			return new MBStatus(null);
		}
		return mainProcess.getStatusHandler();
	}

	/**
	 * Determines if the user should see this step when viewing
	 * the process, for instance during a restart.  With the increased
	 * granularity of the new process structure, if all steps were 
	 * visible driverbuild would go from roughly minimum 7 steps
	 * to roughly minimum 15 steps.  This will confuse users, they
	 * will only be allowed to chose the visible places to restart from.
	 * The only exception to this is they will always be able to restart
	 * with the last step that run, whether it's visible or not.
	 * That choice will be represented on reruns as "Begin with last step" 
	 * so users won't see different step names depending on whether
	 * they stopped on a visible or invisible step.
	 * 
	 * @return True if and only if the user should be able to see and restart
	 *         at the given step.
	 */
	public boolean isVisibleToUser(){
		return visibleToUser;
	}

	public void setVisibleToUser(boolean isVis){
		visibleToUser = isVis;
	}

	/**
	 *  Used to indicate that on a restart this step should
	 *  be rerun even it the restart is meant for a later step.
	 *  For instance, the Driver lock check performed at the 
	 *  beginning of driverbuild will be run each time, no matter
	 *  where the process is to be restarted.
	 * 
	 * @return True if and only if this step must be rerun any time
	 *         the process is restarted.
	 */
	public boolean shouldAlwaysRun(){
		return alwaysRun;
	}

	public void setAlwaysRun(boolean tempRun){
		alwaysRun = tempRun;
	}

	/**
	 * Determines if a step must be undone before it can be rerun.
	 * 
	 * @return true if this step must be undone before it's rerun
	 */
	public boolean shouldBeUndoneBeforeRerun(){
		return undoBeforeRerun;
	}
	

	/**
	 * Set whether the step should be undone before it's rerun.
	 * Protected because this setting should be the same for all
	 * instances of a step.
	 * 
	 * @param tempUndoBeforeRerun
	 */
	protected void setUndoBeforeRerun(boolean tempUndoBeforeRerun){
		undoBeforeRerun	= tempUndoBeforeRerun;
	}

	/**
	 * Determines if, based on the last run of this step,
	 * another copy of this step should be made and it rerun.
	 * 
	 * @return True if another iteration of this step is required 
	 */
	public boolean isAnotherIterationNecessary(){
		return false;
	}

	/**
	 * Determines if an action can be halted once it has begun
	 *
	 * @return Null if and only if the process can be halted. Otherwise, 
	 * return the reason it can't be restarted.
	 */
	public boolean isHaltable() {
		if (currentRunning!=null) {
			return currentRunning.isHaltable();
		}
		return true;
	}

	public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException{
		stopped = true;
		if (currentRunning!=null) {
			currentRunning.haltProcess();
		}
	}

	/**
	 * Determines if this action can be rolled back, so that
	 * systems are in the state they were in before this
	 * occurred.   For things like reports, or simple queries,
	 * this should be true because, since they don't update
	 * anything, there's no state change, so things are alreay
	 * in the state they were in previously.   CMVC level commit
	 * is the only thing I am currently SURE this will be false
	 * for.
	 *
	 * @return true if this action can be undone, or is a non-state changing
	 *         action
	 */
	public boolean isUndoable() {
		return true;
	}

	/**
	 * Roll things (bps, cmvc, MVS, whatever's appropriate) back
	 * to the state they were in before this occurred.
	 *
	 * @exception com.ibm.sdwb.build390.MBBuildException
	 */
	public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException {
		// by default, assume nothing needs to be undone
		return;
	}

	/**
	 * Determine if this action can be rerun.
	 *
	 * @return true if the action can be rerun
	 */
	public boolean isRestartable() {
		return true;
	}

	public boolean isOnlyRestartableFromBeginning(){
		return true;  // assume we must rerun from the beginning since most work that way.
	}

    public boolean isReadyForExecution(){
		return true;
	}

	public boolean canEverBeReadyForExecution(){
		return true;
	}

	public void restartFromMiddle() throws com.ibm.sdwb.build390.MBBuildException{
		throw new GeneralError("Restart from middle should not have been allowed.");
	}

	/**
	 * Determine if a process can be cleaned up.  This will vary
	 * with time.  For instance, PTF builds that are in the middle
	 * cannot be cleaned until they have been undone, but driverbuilds
	 * can be cleaned anytime.  Anything that cannot be cleaned up should
	 * return a null.
	 *
	 * @return CleanableEntity
	 */
	public CleanableEntity getCleanableEntity() {
		return mainProcess.getCleanableEntity();
	}

	public final void externalExecute() throws com.ibm.sdwb.build390.MBBuildException{
		completedSuccessfully = false;
		execute();
		completedSuccessfully = true;
	}

    public final AbstractProcess getProcess(){
        return mainProcess;
    }
	
	/**
	 * This step actually runs the process.
	 * For instance, when the file upload phase is run in
	 * driverbuild, it must have a list of files to be uploaded
	 * passed to it from the previous shadow check phase.
	 * 
	 * @return Object representing the output result of this step.
	 *         In the shadow check step, the output would be the
	 *         list of files that were missing.
	 * @exception MBBuildException
	 */
	protected abstract void execute() throws com.ibm.sdwb.build390.MBBuildException;
}
