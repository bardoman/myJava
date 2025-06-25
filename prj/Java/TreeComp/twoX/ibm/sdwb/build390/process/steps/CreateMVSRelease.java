package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.library.*;
import java.util.*;
import java.io.*;

public class CreateMVSRelease extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBMainframeInfo mainInfo = null;
    private LibraryInfo libInfo = null;
    private ReleaseInformation newRelease = null;
    private ReleaseAndDriverParameters releaseParameters = null;
    public static String CREATEMVSRELEASE = "CREATERELEASE";

    public CreateMVSRelease(MBMainframeInfo tempMain, LibraryInfo tempLibInfo, ReleaseInformation tempRelease, ReleaseAndDriverParameters tempReleaseParameters, String outputPath, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(outputPath+CREATEMVSRELEASE+"-"+tempRelease.getLibraryName(),"Create MVS Release", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        mainInfo = tempMain;
        libInfo = tempLibInfo;
        releaseParameters = tempReleaseParameters;
        newRelease = tempRelease;
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
        String createShadowCommand =    "INITDB SHADDB NO"+
                                        " CMVCFAM=\'"+libInfo.getProcessServerName()+"\'"+
                                        " CMVCADR=\'"+libInfo.getAddressStringForMVS()+"\'"+
                                        " CMVCREL=\'"+newRelease.getLibraryName()+"\'"+
                                        " CHILVL="+newRelease.getMvsHighLevelQualifier()+
                                        " CLNAME="+newRelease.getMvsName()+
                                        " CCLTR="+releaseParameters.getAdditionalCollectors()+
                                        " CPRCS="+releaseParameters.getAdditionalProcessSteps()+
                                        " CBLKP="+releaseParameters.getShadowBulkDatasetPrimarySpaceInCylinders()+
                                        " CBLKS="+releaseParameters.getShadowBulkDatasetSecondarySpaceInCylinders()+
                                        " CUBKP="+releaseParameters.getShadowUnibankDatasetPrimarySpaceInCylinders()+
                                        " CMAXCYL="+releaseParameters.getBulkDatasetMaximumSizeInCylinders()+
                                        " CMAXEXT="+releaseParameters.getBulkDatasetMaximumExtentsInCylinders();
        if (releaseParameters.getDASDVolumeIdentifier()!=null) {
            createShadowCommand += " CVOLID="+releaseParameters.getDASDVolumeIdentifier();
        }
        if (releaseParameters.getSMSStorageClass()!=null) {
            createShadowCommand +=  " CSTGCLS="+releaseParameters.getSMSStorageClass();
        }

        if (releaseParameters.getSMSManagementClass()!=null) {
            createShadowCommand +=  " CMGTCLS="+releaseParameters.getSMSManagementClass();
        }
        createMainframeCall(createShadowCommand, "Creating mvs release " + newRelease.getMvsName(), mainInfo);
        setTSO();
        setSystsprt();
        dontAllowHostCallCancel();
        runMainframeCall();
        mainInfo.addRelease(newRelease, libInfo);
        try {  /*TST1921 */
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MBGlobals.Build390_path+"releasesAndDrivers.ser"));
            MBMainframeInfo.saveStaticInfoMap(oos);
            oos.close();
        } catch (IOException ioe) {
            System.out.println("error saving release and driver info to  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
            ioe.printStackTrace();
        }

    }
}
