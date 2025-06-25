package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import java.util.*;
import java.io.*;

public class DeleteMVSDriver extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBMainframeInfo mainInfo = null;
    private DriverInformation driverInfo = null;
    private boolean shouldForce = false;

    public DeleteMVSDriver(MBMainframeInfo tempMain, String tempOutputPath, DriverInformation tempInfo, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempOutputPath+"DELETEDRIVER-"+tempInfo.getRelease().getMvsName()+"-"+tempInfo.getName(),"Delete MVS Driver", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        mainInfo = tempMain;
        driverInfo = tempInfo;
    }

    public void forceDelete() {
        shouldForce = true;
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

        String deleteDriverCommand = "DELDB DRVRDB "+driverInfo.getRelease().getMvsHighLevelQualifier()+" "+driverInfo.getRelease().getMvsName()+"."+driverInfo.getName();
        if (shouldForce) {
            deleteDriverCommand += " YES";
        }

        createMainframeCall(deleteDriverCommand, "Deleting driver "+driverInfo.getName() , mainInfo);
        setTSO();
        setSystsprt();
        dontAllowHostCallCancel();

        try {
            runMainframeCall();
            if (parseResultFile()) {
                driverInfo.getRelease().removeDriver(driverInfo);
            }
        } catch (HostError he) {
            if (parseResultFile()) {
                driverInfo.getRelease().removeDriver(driverInfo);
            }

        } finally {
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

    public boolean parseResultFile() throws com.ibm.sdwb.build390.MBBuildException {
        final String SUCCESSFUL_MESSAGE = "*INFO* DRVRDB deleted successfully";
        final String DRIVER_DATASET_DOESNOT_EXISTS_MSG = "*ERROR* NO DATA SETS FOUND FOR " +  driverInfo.getRelease().getMvsHighLevelQualifier()+"."+
                                                         driverInfo.getRelease().getMvsName()+"."+driverInfo.getName(); 
        final String DRIVER_NOT_DEFINED_MSG = "*ERROR* DRIVER "+driverInfo.getName()+" IS NOT DEFINED"; 

        final Set errorSet = new HashSet();
        errorSet.add(DRIVER_DATASET_DOESNOT_EXISTS_MSG);
        errorSet.add(DRIVER_NOT_DEFINED_MSG);


        try {
            BufferedReader ResultFileReader = new BufferedReader(new FileReader(getPrintFile()));
            String currentLine = new String();
            String alllines = new String();
            boolean isDriverExists = true;
            while (currentLine != null) {
                if ((currentLine = ResultFileReader.readLine()) != null) {

                    if (errorSet.contains(currentLine.trim())) {
                        isDriverExists=false;
                    }

                    if (isDriverExists & currentLine.trim().indexOf(SUCCESSFUL_MESSAGE)>-1) {
                        ResultFileReader.close();
                        return true;
                    } else alllines = alllines+currentLine+"\n";
                }
            }
            ResultFileReader.close();

            if (!isDriverExists) {
                throw new HostError("The following driver " + driverInfo.getName() +" is not defined in the release\n",this);
            } else {
                throw new HostError("An error occurred when processing a delete request.",this);
            }
        } catch (IOException ioe) {
            throw new GeneralError("Problem reading file " +getPrintFile().getAbsolutePath(), ioe);
        }
    }

}
