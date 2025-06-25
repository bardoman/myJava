package com.ibm.sdwb.build390.process;

import java.util.*;
import com.ibm.sdwb.build390.userinterface.*;
import com.ibm.sdwb.build390.userinterface.event.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.MBStatus;
import com.ibm.sdwb.build390.process.management.*;
import com.ibm.sdwb.build390.info.*;

//*************************************************************************
//05/30/2003 #Feat.INT1178:  Enhance /test parm for improved tracking
//10/20/2004 PTM3695         USS performance issue during the processing of phase results
//*************************************************************************

public abstract class AbstractProcess extends Thread implements Cleanable, Haltable, Restartable, Undoable, UserCommunicationInterface, java.io.Serializable, Saveable {
    static final long serialVersionUID = 1111111111111111L;
    protected transient UserCommunicationInterface uiCommunication = null;
    protected transient ProcessStep currentStep = null;

    private LinkedList stepsThatHaveBeenRun = null;
    private ProcessStep lastStepRun = null;
    private int stepToStartWith = 0;
    private int iterationToStartWith = 0;
    private String lastRun = null;
    protected CleanableEntity thingsToClean = null;
    protected String name = null;
    private boolean activeProcess = true;
    protected boolean successfull = false;
    protected boolean stopped = false;
    protected int totalNumberOfSteps = -1;
    protected transient Map stepAndIterationToExceptionMap = null;
    private   transient Exception exceptionEncountered = null; /* do we need this one */
    private Vector processActionListenerVect = new Vector();
    private   transient Saveable saveableToUse = null; /*do we need this one too */

    protected AbstractProcess(String processType, int tempNumberOfSteps, UserCommunicationInterface tempInterface) {
        super(processType);
        name = processType;
        uiCommunication = tempInterface;
        totalNumberOfSteps = tempNumberOfSteps;
        thingsToClean = new CleanableEntity();
        stepsThatHaveBeenRun = new LinkedList();
    }

    public void prepareRestart(int tempStepNumberToStartWith, int tempIterationToStartWith, UserCommunicationInterface tempComm) {
        setName(name);
        stepToStartWith = tempStepNumberToStartWith;
        iterationToStartWith = tempIterationToStartWith;
        uiCommunication = tempComm;
        stopped = false;
    }

    public UserCommunicationInterface getUserCommunicationInterface(){
        return uiCommunication;
    }

    public void setUserCommunicationInterface(UserCommunicationInterface tempInterface) {
        uiCommunication = tempInterface;
    }

    public void handleUIEvent(UserInterfaceEvent event){
        if (uiCommunication!= null) {
            uiCommunication.handleUIEvent(event);
        }
    }

    public List getStepsThatHaveRun() {
        return stepsThatHaveBeenRun;
    }

    public boolean isRestartable() {
        for (ListIterator runStepIterator = stepsThatHaveBeenRun.listIterator(); runStepIterator.hasNext();) {
            RepeatedProcessStep tempStep = (RepeatedProcessStep) runStepIterator.next();
            if (!tempStep.getStep().isRestartable()) {
                return false;
            }
        }
        return true;
    }


    /**
     * This will be used to implement restart of builds that have
     * completed.
     * 
     * @return 
     */
    public boolean isRestartableAfterCompletion() {
        return(false);
    }

    public boolean isOnlyRestartableFromBeginning() {
        return true;
    }

    public void restartFromMiddle() {
    }

    /**
     * Determines if, based on the last run of this process,
     * another copy of this process should be made and it rerun.
     * Usually only used when this process will run as a step in another process.
     * 
     * @return True if another iteration of this step is required 
     */
    public boolean isAnotherIterationNecessary() {
        return false;
    }

    public boolean isHaltable() {
        if (currentStep!=null) {
            return currentStep.isHaltable();
        } else {
            return true; // nothing is always haltable
        }
    }

    public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException{
        stopped = true;
        if (currentStep!=null) {
            if (currentStep.isHaltable()) {
                save(); //before we halt, do a save
                currentStep.haltProcess();
            }
        }
    }

    public CleanableEntity getCleanableEntity() {
        return thingsToClean;
    }

    public boolean isUndoable() {
        for (ListIterator runStepIterator = stepsThatHaveBeenRun.listIterator(); runStepIterator.hasNext();) {
            RepeatedProcessStep tempStep = (RepeatedProcessStep) runStepIterator.next();
            if (!tempStep.getStep().isUndoable()) {
                return false;
            }
        }
        return true;
    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        while (!stepsThatHaveBeenRun.isEmpty()) {
            RepeatedProcessStep tempStep = (RepeatedProcessStep) stepsThatHaveBeenRun.getLast();
            if (tempStep.getStep().isUndoable()) { /** undo till the last step that isUndoable **/
                tempStep.getStep().undoProcess();
                stepsThatHaveBeenRun.removeLast();
            } else {
                break;
            }
        }
    }

    public LogEventProcessor getLEP() {
        return uiCommunication.getLEP();
    }

    public MBStatus getStatusHandler() {
        return uiCommunication.getStatusHandler();
    }

    /**
     * run before process execution. Override if you want to do 
     * something before the process steps have run.
     * 
     * @exception com.ibm.sdwb.build390.MBBuildException
     */
    protected void preExecution() throws com.ibm.sdwb.build390.MBBuildException {
    }

    public final void externalRun()throws com.ibm.sdwb.build390.MBBuildException{
        //#Feat.INT1178:
        lastRun = (new Date()).toString();
        successfull = false;
        activeProcess = true;
        stepAndIterationToExceptionMap = new HashMap();
        save();
        try {
            preExecution();
            // this is a bit of a hack.  We should probably be undoing stuff first, however we need to make sure it's locked before we do anything,and so far the only things that 
            // have shouldAlwaysRun set are the driver report steps.  If we start using other steps with should always run set, this may screw us if they need to be undone first.
            // check to see if any of the prior steps must be rerun each time
            for (int forwardSteppingIndex = 0; forwardSteppingIndex < stepsThatHaveBeenRun.size() ; forwardSteppingIndex++) {
                RepeatedProcessStep tempStep = (RepeatedProcessStep) stepsThatHaveBeenRun.get(forwardSteppingIndex);
                if (tempStep.getStepNumber() < stepToStartWith | (tempStep.getStepNumber() == stepToStartWith & tempStep.getRepeptition() < iterationToStartWith)) {
                    if (tempStep.getStep().shouldAlwaysRun()) {
                        tempStep.getStep().externalExecute();
                    }
                }
            }

            // if rerun back out any steps after the step to restart with
            // that need to be backed out before they are rerun
            for (boolean continueBacktracking = true; continueBacktracking & !stepsThatHaveBeenRun.isEmpty();) {
                RepeatedProcessStep tempStep = (RepeatedProcessStep) stepsThatHaveBeenRun.getLast();
                if (tempStep.getStepNumber() > stepToStartWith | (tempStep.getStepNumber() == stepToStartWith & tempStep.getRepeptition() >= iterationToStartWith)) {
                    if (tempStep.getStep().shouldBeUndoneBeforeRerun()) {
                        tempStep.getStep().undoProcess();
                    }
                    stepsThatHaveBeenRun.removeLast();
                } else {
                    continueBacktracking = false;
                }
            }

            for (int currentStepNumber = stepToStartWith; currentStepNumber < totalNumberOfSteps & !stopped; currentStepNumber++) {
                int currentIteration = iterationToStartWith;
                do {
                    try {
                        preStep(currentStepNumber, currentIteration);
                        if (stopped) {
                            break;
                        }
                        currentStep = getProcessStep(currentStepNumber, currentIteration);
                        lastStepRun=currentStep;
                        stepsThatHaveBeenRun.add(new RepeatedProcessStep(currentStep, currentStepNumber, currentIteration));

                        save(); // to make sure we remember this step
                        currentStep.externalExecute();
                        if (stopped) {
                            break;
                        }
                        postStep(currentStepNumber, currentIteration);
                    } catch (Exception except) {
                        MBBuildException exceptionToThrow = null;
                        if (except instanceof MBBuildException) {
                            exceptionToThrow = (MBBuildException) except;
                        } else {
                            exceptionToThrow = new com.ibm.sdwb.build390.GeneralError("Unexpected problem executing "+getName(), except);
                        }
                        addExceptionToBeThrown(currentStepNumber, currentIteration, exceptionToThrow);
                    } finally {    /*PTM3611 */
                        save(); 
                    }
                    throwAppropriateException(currentStepNumber, currentIteration);
                    currentIteration++;
                } while (currentStep.isAnotherIterationNecessary() & !stopped);
                iterationToStartWith = 0;   // when going to a new step number, you always want to start at iteration 0
            }
            if (!stopped) {
                successfull = true;
            }
        } finally {
            try {
                notifyProcessActionListeners();
            } finally {
                activeProcess = false;
                save();
                if (!stopped) {
                    if (successfull) {
                        getStatusHandler().updateStatus(getName()+" completed successfully", false);
                    } else {
                        getStatusHandler().updateStatus(getName() +" did not complete successfully", false);
                    }
                } else {
                    getStatusHandler().updateStatus(getName()+" cancelled on user request.", false);

                }
            }
        }
    }

    public final void run() {
        try {
            exceptionEncountered = null;
            externalRun();
        } catch (MBBuildException mbe) {
            exceptionEncountered = mbe;
            getLEP().LogException(mbe);
        } catch (Exception ee) {
            exceptionEncountered = ee;
            getLEP().LogException("Problem running " + getName(), ee);
        }
    }

    /**
     * this methods indicates if the process has completed successfully.
     */
    public boolean hasCompletedSuccessfully() {
        return successfull;
    }

    public boolean isActiveNow(){
        return activeProcess;
    }

    public Exception getExceptionEncountered() {
        return exceptionEncountered;
    }

    public ProcessStep getLastStepRun(){
        return lastStepRun;
    }

    protected void setNumberOfSteps(int tempSteps) {
        totalNumberOfSteps = tempSteps;
    }


    /**
     * This is used to add exceptions to throw 
     * 
     * @param stepNumber when it's encountered
     * @param iterationNumber when it's encountered
     * 
     */
    protected void addExceptionToBeThrown(int stepNumber, int iterationNumber, MBBuildException exception) {
        StepIterationPair testPair = new StepIterationPair(stepNumber, iterationNumber);
        // the step iteration pair tells when this exception should be thrown. By default, it is thrown on the step and iteration
        // it's encounted in.
        Set exceptionsToThrow = (Set) stepAndIterationToExceptionMap.get(testPair);
        if (exceptionsToThrow == null) {
            exceptionsToThrow = new HashSet();
            stepAndIterationToExceptionMap.put(testPair, exceptionsToThrow);
        }
        exceptionsToThrow.add(exception);
    }

    protected final void throwAppropriateException(int stepNumber, int iterationNumber) throws com.ibm.sdwb.build390.MBBuildException {
        StepIterationPair testPair = new StepIterationPair(stepNumber, iterationNumber);
        if (stepAndIterationToExceptionMap.containsKey(testPair)) {
            Set exceptionsToThrow = new HashSet();
            // if we have to throw an exception, we throw all so we don't loose error info
            for (Iterator exceptionSetIterator = stepAndIterationToExceptionMap.values().iterator(); exceptionSetIterator.hasNext();) {
                exceptionsToThrow.addAll((Collection) exceptionSetIterator.next());
            }
            MBBuildException theException = null;
            Iterator exceptionIterator = exceptionsToThrow.iterator();
            if (exceptionsToThrow.size() == 1) {
                theException = (MBBuildException) exceptionIterator.next();// there's only one, so just grab it
            } else {
                theException = new com.ibm.sdwb.build390.utilities.MultipleConcurrentException("Exceptions from several stages");
                while (exceptionIterator.hasNext()) {
                    MBBuildException oneException = (MBBuildException) exceptionIterator.next();
                    ((com.ibm.sdwb.build390.utilities.MultipleConcurrentException)theException).addException(oneException);
                }
            }
            throw theException;
        }
    }



    /**
     * Put anything in here you want to run before a given step
     * 
     * @param stepRun
     * @param stepIteration
     */
    protected void preStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        // no op by default
    }

    /**
     * This method is used to return the steps to run to accomplish
     * a process.   The step to run first, then the next step to run
     * and so on.  If you need to have a step repeated, that should be
     * handled in the step, not here.                   	 * @param stepToGet
     * 
     * @return The step to run, null if there are no more steps
     */
    protected abstract ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException;

    /**
     * Put anything in here you want to run after a given step
     * 
     * @param stepRun
     * @param stepIteration
     */
    protected void postStep(int stepRun, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException{
        // no op by default
    }

    public final void setSaveableToUse(Saveable tempSaveable) {
        saveableToUse = tempSaveable;
    }

    public void childSave() throws com.ibm.sdwb.build390.MBBuildException{
        // no op by default
    }

    public synchronized final void save() throws com.ibm.sdwb.build390.MBBuildException{
        if (saveableToUse!=null) {
            saveableToUse.save();
        } else {
            childSave();
        }
    }

    public class RepeatedProcessStep implements java.io.Serializable {
        static final long serialVersionUID = 4244810755681645504L;

        private ProcessStep myStep = null;
        private int repetition = 0;
        private int stepNumber = 0;

        RepeatedProcessStep(ProcessStep tempStep, int tempStepNumber, int tempRepetition) {
            myStep = tempStep;
            stepNumber = tempStepNumber;
            repetition = tempRepetition;
        }

        public ProcessStep getStep() {
            return myStep;
        }

        public int getRepeptition() {
            return repetition;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String toString() {
            return stepNumber + " " + repetition+ " " + myStep.getName();
        }
    }

    class StepIterationPair {
        int stepNumber = -1;
        int iteration = -1;

        StepIterationPair(int tempStep, int tempIteration) {
            stepNumber = tempStep;
            iteration = tempIteration;
        }

        public boolean equals(Object test) {
            if (test!=null) {
                if (test instanceof StepIterationPair) {
                    StepIterationPair testCast = (StepIterationPair) test;
                    return stepNumber==testCast.stepNumber & iteration==testCast.iteration;
                }
            }
            return false;
        }         

        public int hashCode() {
            return stepNumber*10000+iteration;
        }

        public String toString() {
            return "Step " + stepNumber + " Iteration " + iteration;
        }

    }

    //Begin #Feat.INT1178:
    public void addProcessActionListener(ProcessActionListener pal) {
        processActionListenerVect.add(pal);
    }

    private void notifyProcessActionListeners() {
        Iterator iter = processActionListenerVect.iterator(); 
        RuntimeException oneHit = null;
        while (iter.hasNext()) {
            try {
                ((ProcessActionListener)iter.next()).handleProcessCompletion(this);
            } catch (RuntimeException re) {
                oneHit=re;
                getLEP().LogException("Exception running process completion listeners", re);
            }
        }
        if (oneHit!=null) {
            throw oneHit;
        }
    }

    public String getTimeOfLastRun() {
        return lastRun;
    }
    //End #Feat.INT1178:
}
