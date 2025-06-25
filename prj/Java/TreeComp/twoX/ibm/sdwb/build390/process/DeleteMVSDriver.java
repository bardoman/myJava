package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import java.util.*;

public class DeleteMVSDriver extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.DeleteMVSDriver deleteMVSDriver = null;

    public DeleteMVSDriver(MBMainframeInfo tempMain, String outPath, DriverInformation tempInfo, UserCommunicationInterface userComm){
        super("Delete MVS Driver",1, userComm); 
        deleteMVSDriver = new com.ibm.sdwb.build390.process.steps.DeleteMVSDriver(tempMain, outPath, tempInfo, this);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) throws com.ibm.sdwb.build390.MBBuildException {
        switch (stepToGet) {
        case 0:
            return deleteMVSDriver;
        }
        return null;
    }
}
