package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

public class MainframeDriverList extends CommandLineProcess {

    public static final String PROCESSNAME = "DRIVERLIST";

    private LibraryRelease libRelease = new LibraryRelease();

    public MainframeDriverList(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a list\n"+
        "of drivers defined for the Release specified.\n";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<cmvcrelease>";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        argumentStructure.setRequiredPart(libRelease);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild tempBuild = new MBBuild(getLEP());
        tempBuild.setReleaseInformation(tempBuild.getMainframeInfo().getReleaseByLibraryName(libRelease.getValue(), tempBuild.getLibraryInfo()));
        com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseDriverList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(tempBuild.getSetup().getMainframeInfo(), tempBuild.getSetup().getLibraryInfo(), MBClient.getCacheDirectory(), this);

        setCancelableProcess(releaseDriverList);

        releaseDriverList.externalRun();

        ReleaseInformation releaseInfo = tempBuild.getReleaseInformation();
        if (releaseInfo!=null) {
            for (Iterator driverIterator = releaseInfo.getDrivers().iterator(); driverIterator.hasNext(); ) {
                DriverInformation oneDriver = (DriverInformation) driverIterator.next();
                System.out.println(oneDriver.toString());
            }

            if (releaseInfo.getDrivers().isEmpty()) {
                System.out.println("There are no drivers present for " + releaseInfo.getLibraryName());
            } else {
                getStatusHandler().updateStatus("The output report is saved in "+releaseDriverList.getOutputResultsFile().getAbsolutePath(),false);
            }
        } else {
            throw new SyntaxError("\nLibrary release " + libRelease.getValue() + " not found.\nNote:\nThe library release name is case sensitive.");
        }

    }
}

