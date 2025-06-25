
package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;

//***************************************************************************
//INT1732    new command.
//***************************************************************************

public class MainframeMetadataValidation extends CommandLineProcess
{

    public static final String PROCESSNAME = "METADATAVALIDATION";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private MultipleAssociatedCommandLineArgument criteriaArguments = new MultipleAssociatedCommandLineArgument();


    public MainframeMetadataValidation(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus)
    {
        super(PROCESSNAME, tempLep, tempStatus);
    }


    public String getHelpDescription()
    {
        return getProcessTypeHandled()+ " command queries the Build/390 server for a report\n"+
        "on validity of the selected metadata.";
    }

    public String getHelpExamples()
    {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver> CRITERIA1=\"DESC EQ DESCRIPTION\"";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure)
    {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libRelease);
        baseAnd.addBooleanInterface(driver);

        criteriaArguments.addCommandLineArgument(new MetadataCriteria());
        baseAnd.addBooleanInterface(criteriaArguments);
        argumentStructure.setRequiredPart(baseAnd);
    }

    public void runProcess() throws MBBuildException{
        MBBuild build = new MBBuild(getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);
        Vector criteria = new Vector();
        for(Iterator argIterator = criteriaArguments.getIndexToArgumentsMap().values().iterator(); argIterator.hasNext();)
        {
            CommandLineArgument oneArg = (CommandLineArgument)  ((Set)argIterator.next()).iterator().next();
            criteria.add(oneArg.getValue());
        }
        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep stepWrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        com.ibm.sdwb.build390.process.steps.CheckMVSMetadataValidity validateMetadata = new com.ibm.sdwb.build390.process.steps.CheckMVSMetadataValidity(build,criteria,stepWrapper);
        validateMetadata.setShowFilesAfterRun(false,false);
        stepWrapper.setStep(validateMetadata);

        setCancelableProcess(stepWrapper);

        stepWrapper.externalRun();

    }
}
