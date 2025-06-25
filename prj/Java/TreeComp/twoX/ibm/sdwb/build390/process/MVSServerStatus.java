package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class MVSServerStatus extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.MVSServerStatus checkStatus = null;

    public MVSServerStatus(MBMainframeInfo tempMainframeInfo, UserCommunicationInterface userComm) {
        super("MVS Server Status", 1, userComm); 
        checkStatus = new com.ibm.sdwb.build390.process.steps.MVSServerStatus(tempMainframeInfo, this);
        checkStatus.setShowFilesAfterRun(false, true);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return checkStatus;
        }
        return null;
    }
}
