package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class GetMVSSiteDefaults extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private Setup setup = null;
    private com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults getMVSSiteDefaultsStep = null;

    public GetMVSSiteDefaults(Setup tempSetup, UserCommunicationInterface userComm) {
        super("Get MVS Site Defaults",1, userComm); 
        setup = tempSetup;
    }

    public com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters getDefaultSettingsForMVS() {
        return 	getMVSSiteDefaultsStep.getDefaultSettingsForMVS();
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            getMVSSiteDefaultsStep = new com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults(setup, this);
            return getMVSSiteDefaultsStep;
        }
        return null;
    }
}
