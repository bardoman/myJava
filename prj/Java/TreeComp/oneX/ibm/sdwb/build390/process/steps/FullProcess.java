package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
/*TST1797 fix for jakes bug(restart panel) */

public class FullProcess extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private AbstractProcess processToRun = null;

    public FullProcess(AbstractProcess temporaryProcessToRun, com.ibm.sdwb.build390.process.AbstractProcess parentProcess) {
        super(parentProcess,temporaryProcessToRun.getName());
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        processToRun = temporaryProcessToRun;
        currentRunning = processToRun;
        processToRun.setUserCommunicationInterface(parentProcess);
        parentProcess.getCleanableEntity().addSubCleanableEntity(processToRun.getCleanableEntity());
        processToRun.setSaveableToUse(parentProcess);
    }


    public AbstractProcess getProcessRun(){
        return processToRun;
    }

    /**
     * Determines if an action can be halted once it has begun
     *
     * @return Null if and only if the process can be halted. Otherwise, 
     * return the reason it can't be restarted.
     */
    public boolean isHaltable() {
        return processToRun.isHaltable();
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
        return processToRun.isUndoable();
    }

    /**
     * Roll things (bps, cmvc, MVS, whatever's appropriate) back
     * to the state they were in before this occurred.
     *
     * @exception com.ibm.sdwb.build390.MBBuildException
     */
    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException {
        processToRun.undoProcess();
    }

    /**
     * Determine if this action can be rerun.
     *
     * @return true if the action can be rerun
     */
    public boolean isRestartable() {
        return processToRun.isRestartable();
    }

    public boolean isAnotherIterationNecessary(){
        return processToRun.isAnotherIterationNecessary();
    }

    /*I think this method could me moved into an interface or so */
    public boolean hasCompletedSuccessfully(){
        return processToRun.hasCompletedSuccessfully();
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
        processToRun.externalRun();
    }
}
