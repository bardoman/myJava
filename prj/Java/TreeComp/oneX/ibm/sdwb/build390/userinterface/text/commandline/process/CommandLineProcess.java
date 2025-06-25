package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

//*********************************************************************
//11/11/2003 #DEF.TST1696: USS CMD LINE "METAFLDS" doesnot work
//*********************************************************************

/**
 * This is the base class for all commandline access to processes.
 */
public abstract class CommandLineProcess implements com.ibm.sdwb.build390.userinterface.UserCommunicationInterface {

    private final String processName;
    private RequiredAndOptionalArguments argumentStructure = null;
    private LogEventProcessor logHandler = null;
    private MBStatus status = null;

    private AbstractProcess cancelableProcess=null;

    protected CommandLineProcess(String tempProcessName, LogEventProcessor tempLep, MBStatus tempStatus) {
        processName = tempProcessName;
        logHandler = tempLep;
        status = tempStatus;
    }

    public void setCancelableProcess(AbstractProcess cancelableProcess) {
        this.cancelableProcess = cancelableProcess;
    }

    public AbstractProcess getCancelableProcess() {
        return cancelableProcess;
    }

    public final RequiredAndOptionalArguments getArgumentStructure() {
        initializeArgumentStructure();
        return argumentStructure;
    }

    public final LogEventProcessor getLEP() {
        return logHandler;
    }

    public final MBStatus getStatusHandler() {
        return status;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
    }

    public String getHelpDescription() {
        return "No specific command description defined for " + getProcessTypeHandled();
    }

    public String getHelpExamples() {
        return "No command examples defined for " + getProcessTypeHandled();
    }

    private void initializeArgumentStructure() {
        if (argumentStructure==null) {
            argumentStructure = new RequiredAndOptionalArguments();
            setArgumentStructure(argumentStructure);
        }
    }

    protected abstract void setArgumentStructure(RequiredAndOptionalArguments baseStructure);

    public final String getProcessTypeHandled() {
        return processName;
    }

    public void setValuesOfArguments(Map settings, Set switches) {
        initializeArgumentStructure();
        handleSetting(argumentStructure.getRequiredPart(), settings, switches);
        for (Iterator optionIterator = argumentStructure.getOptions().iterator(); optionIterator.hasNext();) {
            handleSetting((BooleanInterface) optionIterator.next(), settings, switches);
        }
    }

    protected void handleSetting(BooleanInterface oneInterface, Map settings, Set switches) {
        //Begin #DEF.TST1696:
        if (oneInterface == null) {
            return;
        }
        //End #DEF.TST1696:

        if (oneInterface instanceof BooleanOperation) {
            BooleanOperation oneOperation = (BooleanOperation) oneInterface;
            for (Iterator interfaceIterator = oneOperation.getOperandSet().iterator(); interfaceIterator.hasNext();) {
                handleSetting((BooleanInterface)interfaceIterator.next(), settings, switches);
            }
        } else if (oneInterface instanceof RequiredAndOptionalArguments) {
            RequiredAndOptionalArguments reqAndOptionalArgs = (RequiredAndOptionalArguments) oneInterface;
            handleSetting(reqAndOptionalArgs.getRequiredPart(), settings, switches);
            for (Iterator optionIterator = reqAndOptionalArgs.getOptions().iterator(); optionIterator.hasNext();) {
                handleSetting((BooleanInterface)optionIterator.next(), settings, switches);
            }
        } else if (oneInterface instanceof AssociativeBooleanOperation) {
            handleSetting(((AssociativeBooleanOperation) oneInterface).getBooleanOperation(),settings,switches);
            ((AssociativeBooleanOperation)oneInterface).setValues();
        } else if (oneInterface instanceof MultipleAssociatedCommandLineArgument) {
            ((MultipleAssociatedCommandLineArgument) oneInterface).setValues(settings);
        } else if (oneInterface instanceof AssociatedArgument) {
            ((AssociatedArgument) oneInterface).setValues(settings);
        } else if (oneInterface instanceof IndexedArgument) {
            ((IndexedArgument) oneInterface).setValues(settings);
        } else if (oneInterface instanceof CommandLineSwitch) {
            ((CommandLineSwitch)oneInterface).setSwitchesFromSet(switches);
        } else if (oneInterface instanceof CommandLineArgument) {
            CommandLineArgument oneArgument = (CommandLineArgument) oneInterface;
            if (settings.containsKey(oneArgument.getCommandLineName())) {
                oneArgument.setValue((String) settings.get(oneArgument.getCommandLineName()));
            } else {
                oneArgument.setValueFromMap(settings);
            }
        } else {
            System.out.println(oneInterface.getClass().getName()+" is not supported.");
        }
    }

    public abstract void runProcess() throws com.ibm.sdwb.build390.MBBuildException;


    protected  ReleaseInformation getReleaseInformation(String release, Setup setup,boolean isByLibrary) throws MBBuildException {
        MBMainframeInfo mainframeInfo = setup.getMainframeInfo();
        LibraryInfo libraryInfo = setup.getLibraryInfo();
        ReleaseInformation releaseInfo = null;
        if (isByLibrary) {
            releaseInfo = mainframeInfo.getReleaseByLibraryName(release,libraryInfo);
        } else {
            releaseInfo = mainframeInfo.getReleaseByMVSName(release, libraryInfo);
        }
        if (releaseInfo == null) {
            com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseAndDriversList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(mainframeInfo, libraryInfo, MBClient.getCacheDirectory() ,this);
            setCancelableProcess(releaseAndDriversList);
            releaseAndDriversList.externalRun();
            if (isByLibrary) {
                releaseInfo = mainframeInfo.getReleaseByLibraryName(release,libraryInfo);
            } else {
                releaseInfo = mainframeInfo.getReleaseByMVSName(release,libraryInfo);
            }
        }
        //End #DEF.TST1841:
        if (releaseInfo==null) {
            if (isByLibrary) {
                throw new SyntaxError("\nLibrary release " + release + " not found.\nNote:\nThe library release name is case sensitive.");
            } else {
                throw new SyntaxError("Release "+release +" not found. Please enter a valid mainframe release.");
            }
        }
        return releaseInfo;
    }

    protected  DriverInformation getDriverInformation(String driver, ReleaseInformation releaseInfo,Setup setup)  throws MBBuildException {

        DriverInformation driverInfo = releaseInfo.getDriverByName(driver);
        MBMainframeInfo mainframeInfo = setup.getMainframeInfo();
        LibraryInfo libraryInfo = setup.getLibraryInfo();

        if (driverInfo == null) {
            com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseAndDriversList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(mainframeInfo, libraryInfo, MBClient.getCacheDirectory() ,this);
            setCancelableProcess(releaseAndDriversList);
            releaseAndDriversList.externalRun();
            releaseInfo = mainframeInfo.getReleaseByLibraryName(releaseInfo.getLibraryName(),libraryInfo);
            driverInfo = releaseInfo.getDriverByName(driver);
        }
        //End #DEF.TST1841:
        if (driverInfo==null) {
            throw new SyntaxError(driver + " not found in release " + releaseInfo.getLibraryName() + ".");
        }

        return driverInfo;
    }

    public String toString() {
        return "Commandline Process " + processName +" arguments "+argumentStructure;
    }
}
