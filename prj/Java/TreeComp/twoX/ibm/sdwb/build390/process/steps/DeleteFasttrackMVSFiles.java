package com.ibm.sdwb.build390.process.steps;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import com.ibm.sdwb.build390.user.Setup;

public class DeleteFasttrackMVSFiles extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Set cleanables = null;
    private static final String FASTTRACKDELETECOMMAND = "FSTRKDEL ";

    public DeleteFasttrackMVSFiles(Set tempCleanables, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+java.io.File.separator+"cudelhost","Delete Fasttrack MVS files", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        cleanables = tempCleanables;
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
        Map setupToCleanablesMap = CleanableEntity.divideCleanablesBySetup(cleanables);
        for(Iterator setupIterator = setupToCleanablesMap.keySet().iterator();setupIterator.hasNext();) {
            Setup setup = (Setup) setupIterator.next();
            Set cleanableSetForSetup = (Set) setupToCleanablesMap.get(setup);
            handleOneSetup(setup,cleanableSetForSetup);
        }
    }

    private void handleOneSetup(Setup setup, Set cleanables) throws com.ibm.sdwb.build390.MBBuildException{
        String deleteFasttrackCommand = FASTTRACKDELETECOMMAND;
        for(Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext();) {
            CleanableEntity stuffToClean = (CleanableEntity) cleanableIterator.next();
            for(Iterator prefixIterator = stuffToClean.getMVSFileSets().iterator(); prefixIterator.hasNext();) {
                deleteFasttrackCommand += (String) prefixIterator.next();
                if(prefixIterator.hasNext()) {
                    deleteFasttrackCommand += " ";
                }
            }
        }
        //If MVS file sets were found
        if(!deleteFasttrackCommand.equals(FASTTRACKDELETECOMMAND)) {
            createMainframeCall(deleteFasttrackCommand, "Deleting fasttrack files", setup.getMainframeInfo());
            setTSO();
            setSystsprt();
            runMainframeCall();
        }
        for(Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext();) {
            CleanableEntity stuffToClean = (CleanableEntity) cleanableIterator.next();
            stuffToClean.clearMVSFileSets();
        }
    }
}
