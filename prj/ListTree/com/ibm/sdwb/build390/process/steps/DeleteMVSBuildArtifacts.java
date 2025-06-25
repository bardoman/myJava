package com.ibm.sdwb.build390.process.steps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import com.ibm.sdwb.build390.user.Setup;

public class DeleteMVSBuildArtifacts extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private Set cleanablesToHandle = null;

    public DeleteMVSBuildArtifacts(Set tempCleanables, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+java.io.File.separator+"DeleteBuildFromMVS","Delete build from MVS", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        cleanablesToHandle = tempCleanables;
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
        Map setupToCleanableMap = CleanableEntity.divideCleanablesBySetup(cleanablesToHandle);
        for (Iterator setupIterator = setupToCleanableMap.keySet().iterator(); setupIterator.hasNext();) {
            Setup oneSetup = (Setup) setupIterator.next();
            handleOneSetup(oneSetup,(Set) setupToCleanableMap.get(oneSetup));
        }
    }

    private void handleOneSetup(Setup setup, Set cleanables) throws com.ibm.sdwb.build390.MBBuildException{
        for (Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext();) {
            CleanableEntity oneCleanable = (CleanableEntity) cleanableIterator.next();
            Set deleteMVSBuildID = new HashSet();
            try {
                for (Iterator buildIterator =oneCleanable.getAllMVSBuildIDs().keySet().iterator(); buildIterator.hasNext();) {
                    String buildId = (String) buildIterator.next();
                    DriverInformation driverInfo = (DriverInformation) oneCleanable.getAllMVSBuildIDs().get(buildId);
                    String deleteBuildFromMVS = "DELBUILD FAMHLQ="+driverInfo.getRelease().getMvsHighLevelQualifier()
                                                +",REL="+driverInfo.getRelease().getMvsName()
                                                +", DRIVER="+driverInfo.getName()+",BUILDID="+buildId;
                    createMainframeCall(deleteBuildFromMVS, "Deleting build("+buildId+")"+ " from MVS", true,oneCleanable.getSetup().getMainframeInfo());
                    runMainframeCall();
                    deleteMVSBuildID.add(buildId);
                    /* oneCleanable.removeMVSBuildID(buildId); */
                }
            } catch (MBBuildException e) {
                throw e;
            } finally {
                for (Iterator deleteIterator = deleteMVSBuildID.iterator(); deleteIterator.hasNext();) {
                    oneCleanable.removeMVSBuildID((String)deleteIterator.next());
                }
            }
        }
    }
}
