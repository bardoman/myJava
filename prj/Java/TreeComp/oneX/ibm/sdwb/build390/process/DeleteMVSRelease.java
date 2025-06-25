package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class DeleteMVSRelease extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.DeleteMVSRelease deleteMVSRelease = null;

    public DeleteMVSRelease(Setup tempSetup, ReleaseInformation tempInfo,UserCommunicationInterface userComm) {
        super("Delete MVS Release",1, userComm); 
        deleteMVSRelease = new com.ibm.sdwb.build390.process.steps.DeleteMVSRelease(tempSetup.getMainframeInfo(), tempSetup.getLibraryInfo(), tempInfo, this);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return deleteMVSRelease;
        }
        return null;
    }
}
