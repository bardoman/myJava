package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.process.steps.DriverPartListFilteredByMetadata;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

//***************************************************************************
//11/13/2003 #DEF.TST1705: USS CMD line METAEDFT   fails with classcast exception
//***************************************************************************

public class MainframeDriverPartlistFilteredByMetadata extends CommandLineProcess {

    public static final String PROCESSNAME = "METAEDFT";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private MultipleAssociatedCommandLineArgument criteriaArguments = new MultipleAssociatedCommandLineArgument();


    public MainframeDriverPartlistFilteredByMetadata(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a report\n"+
        "containing parts that meet the criteria specified by the selected metadata.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n" +
        "CRITERIA1=\"DESC EQ DESCRIPTION\", CRITERIA2=\"CLASS EQ MAC\"," ;
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libRelease);
        baseAnd.addBooleanInterface(driver);

        criteriaArguments.addCommandLineArgument(new MetadataCriteria());
        baseAnd.addBooleanInterface(criteriaArguments);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild build = new MBBuild(getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        Vector criteria = new Vector();
        for (Iterator argIterator = criteriaArguments.getIndexToArgumentsMap().values().iterator(); argIterator.hasNext();) {
            //#DEF.TST1705:
            CommandLineArgument oneArg = (CommandLineArgument)  ((Set)argIterator.next()).iterator().next();

            criteria.add(oneArg.getValue());
        }
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.DriverPartListFilteredByMetadata partListCreator = new com.ibm.sdwb.build390.process.steps.DriverPartListFilteredByMetadata(criteria,null, build.getMainframeInfo(), build.getLibraryInfo(),
                                                                                                                                                                        build.getReleaseInformation(),
                                                                                                                                                                        build.getDriverInformation(),stepWrapper);
        stepWrapper.setStep(partListCreator);

        setCancelableProcess(stepWrapper);

        stepWrapper.externalRun();
        for (String outputReport : partListCreator.getOutputReport()) {
            new MBEdit(outputReport,getLEP());
        }
    }
}
