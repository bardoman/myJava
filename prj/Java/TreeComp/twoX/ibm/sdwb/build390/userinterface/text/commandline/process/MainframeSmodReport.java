package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.IncludePathname;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.LibraryRelease;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeDriver;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MainframeSmodReportType;
import com.ibm.sdwb.build390.utilities.BooleanAnd;

public class MainframeSmodReport extends CommandLineProcess {

    public static final String PROCESSNAME = "SMODRPT";

    private LibraryRelease libraryRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private IncludePathname includePathname = new IncludePathname();
    private MainframeSmodReportType type = new MainframeSmodReportType();

    public MainframeSmodReport(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a report on\n"+
        "USERMODS built in a driver.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver> TYPE=<type> PATHNAME=YES";
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libraryRelease);
        baseAnd.addBooleanInterface(driver);
        baseAnd.addBooleanInterface(type);
        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(includePathname);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild tempBuild = new MBBuild(getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(libraryRelease.getValue(),tempBuild.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,tempBuild.getSetup());

        tempBuild.setReleaseInformation(releaseInfo);
        tempBuild.setDriverInformation(driverInfo);


        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.SMODReport smodReport = new com.ibm.sdwb.build390.process.steps.SMODReport(tempBuild,type.getValue(), stepWrapper);
        smodReport.setShowFilesAfterRun(true, false);
        stepWrapper.setStep(smodReport);
        if (includePathname.isSatisfied()) {
            smodReport.setIncludePathname(includePathname.getBooleanValue());
        }

        setCancelableProcess(stepWrapper);

        stepWrapper.externalRun();
    }
}
