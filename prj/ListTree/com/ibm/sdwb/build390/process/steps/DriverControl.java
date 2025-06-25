package com.ibm.sdwb.build390.process.steps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.sdwb.build390.HostError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.process.management.CleanableEntity;
import com.ibm.sdwb.build390.user.Setup;

public class DriverControl extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;
    private String buildId = null;
    private DriverInformation driverInfo = null;
    private Set cleanables = null;
    //private CleanableEntity oneCleanable = null;
    private String driverOpToPerform = null;
    public static final String LOCKDRIVER = "INUSE";
    public static final String UNLOCKDRIVER = "FREE";
    private boolean ignoreHostErrors = false;


    public DriverControl(String tempBuildID, DriverInformation tempInfo, CleanableEntity tempCleanable, String outputLocation, String tempOpToPerform, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(outputLocation + tempOpToPerform,"Driver Control - "+tempOpToPerform, tempProc);
        setUndoBeforeRerun(false);
        buildId = tempBuildID;
        driverInfo = tempInfo;
        driverOpToPerform = tempOpToPerform;
        cleanables = new HashSet();
        cleanables.add(tempCleanable);
    }

    public DriverControl(Set tempCleanablesToHandle, String outputLocation, String tempOpToPerform, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(outputLocation + tempOpToPerform,"Driver Control - "+tempOpToPerform, tempProc);
        setUndoBeforeRerun(false);
        cleanables = tempCleanablesToHandle;
        driverOpToPerform = tempOpToPerform;
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

        for (Iterator cleanableIterator = cleanables.iterator(); cleanableIterator.hasNext(); ) {
            CleanableEntity oneCleanable =  (CleanableEntity) cleanableIterator.next();
            getStatusHandler().updateStatus("Setting driver  status to "+driverOpToPerform, false);
            if (driverOpToPerform.equals(LOCKDRIVER)) {
                handleOneLockOperation(buildId,oneCleanable.getSetup(),driverInfo );
                oneCleanable.addDriverLock(buildId,driverInfo);
            } else {
                Set lockToBeRemovedFromCleanableEntity =  new HashSet();
                try {
                    if (oneCleanable.getDriverLocks().isEmpty()) {
                        getStatusHandler().updateStatus("No driver lock exists.",false);
                        getLEP().LogPrimaryInfo("Information:","No driver lock exists.",false);
                    }
                    for (Iterator locksInCleanables = oneCleanable.getDriverLocks().keySet().iterator(); locksInCleanables.hasNext();) {
                        String buildId = (String) locksInCleanables.next();
                        handleOneLockOperation(buildId, oneCleanable.getSetup(),(DriverInformation) oneCleanable.getDriverLocks().get(buildId));
                        lockToBeRemovedFromCleanableEntity.add(buildId);
                    }
                } catch (MBBuildException mbe) {
                    if (mbe instanceof HostError) {
                        if (!ignoreHostErrors) {
                            throw mbe;
                        }
                    } else {
                        throw mbe;
                    }
                } finally {
                    for (Iterator removeLockIterator = lockToBeRemovedFromCleanableEntity.iterator(); removeLockIterator.hasNext();) {
                        oneCleanable.removeDriverLock((String)removeLockIterator.next());
                    }
                }
            }

        }
    }

    public void ignoreHostErrors(boolean tempignoreErrors){
        this.ignoreHostErrors = tempignoreErrors;
    }

    private void handleOneLockOperation(String buildid, Setup setup, DriverInformation driverInfo) throws com.ibm.sdwb.build390.MBBuildException{
        LibraryInfo libInfo = setup.getLibraryInfo();
        String driverControlCommand =  "DRVRCTL OP="+driverOpToPerform+", DRIVER="+driverInfo.getName()+", BUILDID="+buildid+", CMVCREL="+driverInfo.getRelease().getLibraryName()+
                                       ", "+libInfo.getDescriptiveStringForMVS();

        String statusMessage = null;
        if (driverOpToPerform==LOCKDRIVER) {
            statusMessage = "Locking the driver "+driverInfo.getName() + ", buildid("+buildid+").";
        } else if (driverOpToPerform==UNLOCKDRIVER) {
            statusMessage =  "Unlocking the driver "+driverInfo.getName()+ ", buildid("+buildid+").";
        }
        createMainframeCall(driverControlCommand, statusMessage, setup.getMainframeInfo());
        runMainframeCall();
    }
}
