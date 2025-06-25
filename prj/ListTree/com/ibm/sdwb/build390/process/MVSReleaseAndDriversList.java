package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import java.io.File;
import java.util.*;

public class MVSReleaseAndDriversList extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private com.ibm.sdwb.build390.process.steps.MVSReleaseAndDriversList getReleaseAndDriversListStep =null;

    public MVSReleaseAndDriversList(MBMainframeInfo tempMain, LibraryInfo tempLib, File saveDirectory, UserCommunicationInterface comm){
        super("MVS Release And Drivers List",1, comm); 
        if (saveDirectory == null) {
			saveDirectory = new File(MBGlobals.Build390_path+File.separator+"misc");
		}
        getReleaseAndDriversListStep = new com.ibm.sdwb.build390.process.steps.MVSReleaseAndDriversList(tempMain, tempLib, saveDirectory, this);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            return getReleaseAndDriversListStep;
        }
        return null;
    }

    public File getOutputResultsFile(){
        return getReleaseAndDriversListStep.getOutputFile();
    }
}
