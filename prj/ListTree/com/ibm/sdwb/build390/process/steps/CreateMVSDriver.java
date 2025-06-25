package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import java.util.*;
import java.io.*;

//****************************************************
//05/13/2003 #DEF.INT1186: Debug Mult Apar Build
//10/27/2003 #DEF.INT1667:  Cannot create full delta driver
//02/23/2004 #DEF.INT1765:  release parameter changes 
//****************************************************

public class CreateMVSDriver extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private DriverInformation destinationDriver = null;
    private DriverInformation sourceDriver = null;
    private String driverSize = null;
    private String numberOfParts = null;
    private ReleaseAndDriverParameters driverParameters = null;
    private boolean overrideDefaultSettings = false;
    private boolean deltaDriver = false;
    private boolean linkToOtherDriver = true;   // delta drivers, and base drivers copied from other drivers link to other drivers.
    private boolean thinDelta = true;
    private boolean includeSysmods = false;
    private static final String COPYSENT = new String("COPYSENT");

    public CreateMVSDriver(MBBuild tempBuild, DriverInformation tempSource, DriverInformation tempDestination, boolean tempDelta, ReleaseAndDriverParameters tempDriverParameters, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath()+"CREATEDRIVER-"+tempDestination.getRelease().getLibraryName()+"-"+tempDestination.getName(),"Create MVS Driver", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        build = tempBuild;
        driverParameters = tempDriverParameters;
        deltaDriver = tempDelta;
        sourceDriver = tempSource;
        destinationDriver = tempDestination;
        linkToOtherDriver = (tempSource!=null);   // if something is in the base driver field, this must either be a delta or
        // a base with all parts copied
    }

    public void setOverrideDefaultSettings(boolean tempOverride) {
        overrideDefaultSettings = tempOverride;
    }

    public void setDriverSize(String tempSize) {
        driverSize = tempSize;
    }

    public void setIncludeSysMods(boolean tempInclude) {
        includeSysmods = tempInclude;
    }

    public void setNumberOfParts(String tempNumber) {
        numberOfParts = tempNumber;
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
        String createDriverCommand = null;
        if (linkToOtherDriver) {
            createDriverCommand = "CPYDB DRVRDB OP=COPY"+
                                  //#DEF.INT1186:
                                  " CHILVL="+sourceDriver.getRelease().getMvsHighLevelQualifier()+
                                  // " CHILVL="+driverParameters.getHighLevelQualifier()+
                                  " CRELEASE="+sourceDriver.getRelease().getMvsName()+
                                  " CDRIVER="+sourceDriver.getName()+
                                  " CNEWDRVR="+destinationDriver.getName();
            if (deltaDriver) {
                createDriverCommand += " CDRVRTYP=DELTA";

                //Begin #DEF.INT1667:
                if (destinationDriver.isFullDriver()) {
                    //if(!thinDelta) {
                    //End #DEF.INT1667:
                    createDriverCommand += " CFULLDEL=YES";
                } else {
                    createDriverCommand += " CFULLDEL=NO";
                }
            } else {
                createDriverCommand += " CDRVRTYP=FULL";
            }
            createDriverCommand += addIfSettingNotNull(" CNEWREL=", destinationDriver.getRelease().getMvsName());
            createDriverCommand += addIfSettingNotNull(" CDVRSIZE=", driverSize);
            if (includeSysmods) {
                createDriverCommand += " CSYSMODS=YES";
            } else {
                createDriverCommand += " CSYSMODS=NO";

            }
            if (numberOfParts==null & overrideDefaultSettings ) {
                createDriverCommand += addIfSettingNotNull(" CBLKP=", driverParameters.getDriverBulkDatasetPrimarySpaceInCylinders());
                createDriverCommand += addIfSettingNotNull(" CBLKS=", driverParameters.getDriverBulkDatasetSecondarySpaceInCylinders());
                createDriverCommand += addIfSettingNotNull(" CUBKP=", driverParameters.getDriverUnibankDatasetPrimarySpaceInCylinders());
            }
        } else {
            createDriverCommand = "INITDB DRVRDB NO"+
                                  " CHILVL="+destinationDriver.getRelease().getMvsHighLevelQualifier()+
                                  " CLNAME="+destinationDriver.getRelease().getMvsName()+ "." + destinationDriver.getName();


            if (numberOfParts==null) {
                createDriverCommand += addIfSettingNotNull(" CBLKP=", driverParameters.getDriverBulkDatasetPrimarySpaceInCylinders());
                createDriverCommand += addIfSettingNotNull(" CBLKS=", driverParameters.getDriverBulkDatasetSecondarySpaceInCylinders());
                createDriverCommand += addIfSettingNotNull(" CUBKP=", driverParameters.getDriverUnibankDatasetPrimarySpaceInCylinders());
            }
        }

        createDriverCommand += addIfSettingNotNull(" CMAXCYL=", driverParameters.getBulkDatasetMaximumSizeInCylinders());
        createDriverCommand += addIfSettingNotNull(" CMAXEXT=", driverParameters.getBulkDatasetMaximumExtentsInCylinders());
        createDriverCommand += addIfSettingNotNull(" NUMPARTS=",numberOfParts);
        // if (driverSize != null |  !linkToOtherDriver) {
        createDriverCommand += addIfSettingNotNull(" CSTGCLS=",driverParameters.getSMSStorageClass());
        createDriverCommand += addIfSettingNotNull(" CMGTCLS=",driverParameters.getSMSManagementClass());

        createDriverCommand += addIfSettingNotNull(" CVOLID=",driverParameters.getDASDVolumeIdentifier());
        // }


        createMainframeCall(createDriverCommand, "Creating driver " +  destinationDriver.getName(), build.getSetup().getMainframeInfo());
        setTSO();
        setSystsprt();
        dontAllowHostCallCancel();
        runMainframeCall();

        MBMainframeInfo mainInfo = build.getSetup().getMainframeInfo();
        if (deltaDriver) {
            destinationDriver.setBaseDriver(sourceDriver);
        }

        //#DEF.INT1667:
        //destinationDriver.setFull(!thinDelta);

        destinationDriver.getRelease().addDriver(destinationDriver);


        try {  /*TST1921 */
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MBGlobals.Build390_path+"releasesAndDrivers.ser"));
            MBMainframeInfo.saveStaticInfoMap(oos);
            oos.close();
        } catch (IOException ioe) {
            System.out.println("error saving release and driver info to  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
            ioe.printStackTrace();
        }


    }

    public void undoProcess() throws com.ibm.sdwb.build390.MBBuildException{
        if (completedSuccessfully) {
            DeleteMVSDriver doDelete = new DeleteMVSDriver(build.getSetup().getMainframeInfo(), build.getBuildPath(), destinationDriver, mainProcess);
            doDelete.externalExecute();
        }
    }

    private String addIfSettingNotNull(String header, String setting) {
        if (setting!=null) {
            return header+setting;
        }
        return new String();
    }
}
