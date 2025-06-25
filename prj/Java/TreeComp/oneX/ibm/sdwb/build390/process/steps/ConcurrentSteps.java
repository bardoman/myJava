package com.ibm.sdwb.build390.process.steps;

import java.util.*;

/*09/15/2003 TST1558 - show hosterrors, when multipleconcurrentexception occurs. **/

public class ConcurrentSteps extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private transient Set stepThreadSet = null;
    private transient com.ibm.sdwb.build390.utilities.MultipleConcurrentException multiException = null;

    private Set stepsToRun = null;
    private boolean forceSerialExecution = false;
    private boolean swallowException = false;

    public ConcurrentSteps(String stepName, com.ibm.sdwb.build390.process.AbstractProcess tempProcess) {
        super(tempProcess,stepName);
        setVisibleToUser(true);
        stepsToRun = new HashSet();
        stepThreadSet = new HashSet();
    }

    public void addStepToRun(ProcessStep newStep) {
        stepsToRun.add(newStep);
    }

    public Set getStepSetToRun(){
        return new HashSet(stepsToRun);
    }

    public void forceStepsToExecuteSerially() {
        forceSerialExecution = true;
    }

    public void setSwallowException() {
        swallowException = true;
    }

    /**
     * Determines if an action can be halted once it has begun
     *
     * @return Null if and only if the process can be halted. Otherwise, 
     * return the reason it can't be restarted.
     */
    public boolean isHaltable() {
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            if (!oneStep.isHaltable()) {
                return false;
            }
        }
        return true;
    }

    public void haltProcess()throws com.ibm.sdwb.build390.MBBuildException{
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            oneStep.haltProcess();
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
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            if (!oneStep.isUndoable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Roll things (bps, cmvc, MVS, whatever's appropriate) back
     * to the state they were in before this occurred.
     *
     * @exception com.ibm.sdwb.build390.MBBuildException
     */
    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException {
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            oneStep.undoProcess();
        }
    }

    /**
     * Determine if this action can be rerun.
     *
     * @return true if the action can be rerun
     */
    public boolean isRestartable() {
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            if (!oneStep.isRestartable()) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnotherIterationNecessary() {
        for (Iterator stepIterator = stepsToRun.iterator(); stepIterator.hasNext();) {
            ProcessStep oneStep = (ProcessStep) stepIterator.next();
            if (oneStep.isAnotherIterationNecessary()) {
                return true;
            }
        }
        return false;
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
        multiException = null;
        try {
            Set stepHolder = new HashSet(stepsToRun);
            while (!stepHolder.isEmpty()) {
                for (Iterator stepIterator = stepHolder.iterator(); stepIterator.hasNext();) {
                    ProcessStep oneStep = (ProcessStep) stepIterator.next();
                    if (oneStep.isReadyForExecution()) {
                        stepIterator.remove();
                        OneStepRunner stepRunner = new OneStepRunner(oneStep);
                        stepThreadSet.add(stepRunner);
                        stepRunner.start();
                        if (forceSerialExecution) {
                            stepRunner.join();
                        }
                    } else if (!oneStep.canEverBeReadyForExecution()) {
                        stepIterator.remove();
                    }
                }
                if (!forceSerialExecution) {
                    // put in a pause so we don't spin our wheels
                    Thread.currentThread().sleep(5000);
                }
            }
            for (Iterator threadIterator = stepThreadSet.iterator(); threadIterator.hasNext();) {
                OneStepRunner oneStep = (OneStepRunner) threadIterator.next();
                oneStep.join();
            }
            for (Iterator threadIterator = stepThreadSet.iterator(); threadIterator.hasNext();) {
                OneStepRunner oneStep = (OneStepRunner) threadIterator.next();

                if (oneStep.getExceptionEncountered()!=null) {
                    if (multiException==null) {
                        multiException = new com.ibm.sdwb.build390.utilities.MultipleConcurrentException("Problems running concurrent step " + getFullName());
                    }
                    /**TST1558. */
                    if (oneStep.getExceptionEncountered() instanceof com.ibm.sdwb.build390.utilities.MultipleConcurrentException) {
                        multiException.addMultipleConcurrentException(oneStep.getExceptionEncountered());
                    } else {
                        multiException.addException(oneStep.getExceptionEncountered());
                    }
                }

            }
            /**TST1558. */
            if (multiException!=null & !swallowException) {
                throw multiException;
            }
        } catch (InterruptedException ie) {
            //shouldn't happen
            throw new com.ibm.sdwb.build390.GeneralError("Join interrupted, shouldn't have happened.", ie);
        }
    }

    public com.ibm.sdwb.build390.utilities.MultipleConcurrentException getException() {
        return multiException;
    }

    private class OneStepRunner extends Thread {
        ProcessStep stepToRun = null;
        boolean wasExecuted = false;
        com.ibm.sdwb.build390.MBBuildException exceptionEncountered = null;

        private OneStepRunner(ProcessStep tempStep) {
            stepToRun = tempStep;
        }

        public void run() {
            try {

                getStatusHandler().clearStatus();
                getStatusHandler().updateStatus(stepToRun.getName() + " execution started.",false);    
                stepToRun.externalExecute();
            } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                exceptionEncountered = mbe;
            } finally {
                wasExecuted = true;
                getStatusHandler().clearStatus();
                getStatusHandler().updateStatus(stepToRun.getName() + " execution ended.",false);    
            }
        }

        public com.ibm.sdwb.build390.MBBuildException getExceptionEncountered() {
            return exceptionEncountered;
        }


    }
}
