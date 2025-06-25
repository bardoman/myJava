package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.sql.Driver;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

public class GetBuildTypeList extends CommandLineProcess {

    public static final String PROCESSNAME = "BUILDTYPELIST";

    private LibraryRelease libraryRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();

    public GetBuildTypeList(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return "Query the "+MBConstants.productName+" server for a list of build types defined for the Release and Driver specified.";

    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libraryRelease);
        baseAnd.addBooleanInterface(driver);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild tempBuild = new MBBuild(getLEP());
        Setup setup = tempBuild.getSetup();

        ReleaseInformation releaseInfo = getReleaseInformation(libraryRelease.getValue(),setup, true);
        DriverInformation driverInfo = getDriverInformation(driver.getValue(),releaseInfo,setup);

        tempBuild.setReleaseInformation(releaseInfo);
        tempBuild.setDriverInformation(driverInfo);

        com.ibm.sdwb.build390.process.GetBuildTypeList buildTypeRetrieval = new com.ibm.sdwb.build390.process.GetBuildTypeList(tempBuild.getDriverInformation(), tempBuild.getMainframeInfo(), tempBuild.getLibraryInfo(), tempBuild.getBuildPathAsFile(), this);

        setCancelableProcess(buildTypeRetrieval);

        buildTypeRetrieval.externalRun();
        System.out.println("\nBuildtypes:");
        com.ibm.sdwb.build390.userinterface.text.utilities.OutputFormattingMethods.formatList(buildTypeRetrieval.getListOfBuildTypes(), System.out);

    }
}
