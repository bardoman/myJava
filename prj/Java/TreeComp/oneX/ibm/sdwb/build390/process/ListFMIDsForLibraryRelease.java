package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class ListFMIDsForLibraryRelease extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease getFMIDStep = null;

    public ListFMIDsForLibraryRelease(String tempRelease, Setup tempSetup, java.io.File tempOutputDirectory, UserCommunicationInterface userComm) {
        super("List FMIDs for Library Release",1, userComm); 
        getFMIDStep = new com.ibm.sdwb.build390.process.steps.mainframe.ListFMIDsForLibraryRelease(tempRelease, tempSetup, tempOutputDirectory, this);
    }

    public void setRetainReleaseAndCompID(String tempRelease, String tempCompID) {
        getFMIDStep.setRetainReleaseAndCompID(tempRelease,tempCompID);
    }

    public java.util.Map getFMIDMap() {
        return getFMIDStep.getFMIDMap();
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return getFMIDStep;
        }
        return null;
    }
}
