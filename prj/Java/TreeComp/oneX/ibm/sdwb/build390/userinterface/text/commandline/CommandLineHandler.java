package com.ibm.sdwb.build390.userinterface.text.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBBuildLoader;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.security.PasswordManager;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.B390Password;
import com.ibm.sdwb.build390.userinterface.text.commandline.process.*;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;
import com.ibm.sdwb.build390.utilities.BooleanInterface;
import com.ibm.sdwb.build390.utilities.PrettyPrinter;

//******************************************************************
//09/03/2003 #DEF.TST1467:  DEFREQ command line not implemented
//09/19/2003 TST1603        parmfile <keyword=values> are ignored
//12/04/2003 TST1714        cleanup throws nullpointer when performed on a clean build/fresh b390 instalation.
//04/08/2004 #DEF.TST1866: logretrieve needs to allow /odebuild parm
//******************************************************************

public class CommandLineHandler implements com.ibm.sdwb.build390.userinterface.UserCommunicationInterface {
    private Map settings = null;
    private Set switches = null;

    private CommandLineProcess commandHandler = null;
    private static final String PARMFILE_KEY="PARMFILE";

    private static final Set buildToLoadKeys = new HashSet();
    static{
        buildToLoadKeys.add("BUILDID");
        buildToLoadKeys.add("BUILD");
    }

    private String commandName ;

    public CommandLineHandler(String command, Map settings,Set switches) throws com.ibm.sdwb.build390.MBBuildException{
        this.settings = settings;
        this.switches = switches;
        handleEnvironmentProperties();
        MBBuild loadedBuild = handleLoadOfBuild();
        if (MBClient.getCommandLineSettings().containsPARMFILE()) {
            handleParmfileInput();
        }
        if (!SetupCreate.PROCESSNAME.equalsIgnoreCase(command)) {
            //we have to handle the following conditions.
            // a)any command might contain setup keywords as arguments. eg : RELEASELIST LIBRARY=CMVC ...
            // b)any command might contain setup keywords in a parmfile. 
            // c)any command might contain setup keywords  like SELECTLIBRARY=<..> 
            // c)any command might use setup from a client.ser file 
            validateSetup();
        }

        if (command!=null) {
            this.commandName = command.toUpperCase();
            MBClient.status.updateStatus("Processing command " + command+"...", false);
            if (command.equals(SetupCreate.PROCESSNAME)) {
                commandHandler = new SetupCreate(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeShadowDelete.PROCESSNAME)) {
                commandHandler = new MainframeShadowDelete(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverDelete.PROCESSNAME)) {
                commandHandler = new MainframeDriverDelete(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverList.PROCESSNAME)) {
                commandHandler = new MainframeDriverList(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverParameterCheck.PROCESSNAME)) {
                commandHandler = new MainframeDriverParameterCheck(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverPartlistFilteredByMetadata.PROCESSNAME)) {
                commandHandler = new MainframeDriverPartlistFilteredByMetadata(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeMetadataValidation.PROCESSNAME)) {
                commandHandler = new MainframeMetadataValidation(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverReport.PROCESSNAME)) {
                commandHandler = new MainframeDriverReport(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeMetadataFieldList.PROCESSNAME)) {
                commandHandler = new MainframeMetadataFieldList(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeReleaseList.PROCESSNAME)) {
                commandHandler = new MainframeReleaseList(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeServerConnectionTest.PROCESSNAME)) {
                commandHandler = new MainframeServerConnectionTest(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeSmodReport.PROCESSNAME)) {
                commandHandler = new MainframeSmodReport(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeShadowReport.PROCESSNAME)) {
                commandHandler = new MainframeShadowReport(MBClient.lep,MBClient.status);
            } else if (command.equals(CleanupBuilds.PROCESSNAME)) {
                commandHandler = new CleanupBuilds(MBClient.lep,MBClient.status);
            }
            //End #DEF.TST1467: 
            else if (command.equals(LogRetrieve.PROCESSNAME)) {
                commandHandler = new LogRetrieve(MBClient.lep, MBClient.status);
            } else if (command.equals(LibraryDrivenBuild.PROCESSNAME)) {
                commandHandler = new LibraryDrivenBuild(MBClient.lep, MBClient.status);
            } else if (command.equals(com.ibm.sdwb.build390.userinterface.text.commandline.process.GetBuildTypeList.PROCESSNAME)) {
                commandHandler = new com.ibm.sdwb.build390.userinterface.text.commandline.process.GetBuildTypeList(MBClient.lep, MBClient.status);
            } else if (command.equals(LogRetrieve.PROCESSNAME)) {
                commandHandler = new LogRetrieve(MBClient.lep, MBClient.status);
            } else if (command.equals(UserModBuild.PROCESSNAME)) {
                commandHandler = new UserModBuild(MBClient.lep, MBClient.status);
            } else if (command.equals(UserSourceDrivenBuild.PROCESSNAME)) {
                commandHandler = new UserSourceDrivenBuild(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeDriverCreate.PROCESSNAME)) {
                commandHandler = new MainframeDriverCreate(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeShadowCreate.PROCESSNAME)) {
                commandHandler = new MainframeShadowCreate(MBClient.lep, MBClient.status);
            } else if (command.equals(ListProcesses.PROCESSNAME)) {
                commandHandler = new ListProcesses(MBClient.lep, MBClient.status);
            } else if (command.equals(ProcessInfo.PROCESSNAME)) {
                commandHandler = new ProcessInfo(MBClient.lep, MBClient.status);
            } else if (command.equals(RestartProcess.PROCESSNAME)) {
                commandHandler = new RestartProcess(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeListFMIDsForLibraryRelease.PROCESSNAME)) {
                commandHandler = new MainframeListFMIDsForLibraryRelease(MBClient.lep, MBClient.status);
            } else if (command.equals(MainframeUNCHECKEDSocketConnection.PROCESSNAME)) {
                commandHandler = new MainframeUNCHECKEDSocketConnection(MBClient.lep, MBClient.status);
            } 

            if (commandHandler == null) {
                throw new SyntaxError("No process was found named " + command + ".\nPlease check the spelling of this command.");
            }
        } else if (loadedBuild !=null) {
            MBClient.status.updateStatus("Handling restart of " + loadedBuild.get_buildid(), false);
            // do stuff for handling a restart here
        }
        if (switches.contains("HELP")) {
            handleHelp();
        }
    }

    public void run() throws com.ibm.sdwb.build390.MBBuildException{
        if (!SetupCreate.PROCESSNAME.equalsIgnoreCase(commandName)) {
            if (!SetupManager.getSetupManager().hasSetup()) {
                throw new SyntaxError("\nNo setup information found.!\n"+
                                      "One or more of the setup keywords are incomplete or not found.!\n"+
                                      "Note:\n"+
                                      "To create setup from the command line, type " + SetupCreate.PROCESSNAME + " /help\n"+ 
                                      "Refer Client Users Guide for more details.\n");
            }
        }
        commandHandler.setValuesOfArguments(settings,switches);
        if (commandHandler.getArgumentStructure().isSatisfied()) {
            if (SetupManager.getSetupManager().hasSetup()) {
                populateReleasesAndDriversFile(); /** TST1714 **/
            }
            commandHandler.runProcess();
        } else {
            String temp = "";
            String reasonNotSatisfied = commandHandler.getArgumentStructure().getReasonNotSatisfied();
            if (!reasonNotSatisfied.startsWith("==")) {
                temp += String.format("%-42s%n","===========================================");
            }
            temp += reasonNotSatisfied;
            if (!temp.trim().endsWith("==")) {
                temp += String.format("%-42s%n","===========================================");
            }
            throw new SyntaxError(String.format("%-42s%n%-42s%n%-42s%n%-42s%n%-42s%n","===========================================",
                                                "The  following  problems  were  found  with",
                                                "the command arguments:", temp,
                                                "For the correct syntax enter "+ commandName +" /help."));
        }
    }

    public void cancelProcess() throws MBBuildException {
        if (commandHandler.getCancelableProcess()!=null) {
            commandHandler.getCancelableProcess().haltProcess();
        }
    }

    private MBBuild handleLoadOfBuild() throws com.ibm.sdwb.build390.MBBuildException{
        MBBuild loadedBuild = null;
        for (Iterator buildLoadKeyIterator = buildToLoadKeys.iterator(); buildLoadKeyIterator.hasNext();) {
            String oneKey = (String) buildLoadKeyIterator.next();
            String  buildToLoad = (String) settings.get(oneKey);
            if (buildToLoad != null) {
                MBBuildLoader buildLoader = new MBBuildLoader();
                loadedBuild = buildLoader.loadBuild(buildToLoad);
                if (loadedBuild!=null) {
                    // assume there will only be one
                    return loadedBuild;
                }
            }
        }
        return loadedBuild;
    }
/**TST1603 **/
    private void handleParmfileInput() throws com.ibm.sdwb.build390.MBBuildException {
        String fileToBuildFrom =  (String)settings.get(PARMFILE_KEY);
        try {
            Scanner fileScanner = new Scanner(new BufferedReader(new FileReader(fileToBuildFrom)));
            fileScanner.useDelimiter(System.getProperty("line.separator")); 
            while (fileScanner.hasNext()) {
                String currentLine = fileScanner.next();
                if (currentLine.length() > 0) {
                    if (currentLine.contains("=")) {
                        String[] lineSplit  =  currentLine.split("=",2);
                        if (lineSplit!=null && lineSplit.length >=2) {
                            String name  = lineSplit[0];
                            name = name.trim().toUpperCase();
                            if (lineSplit[1] !=null) {
                                String value = lineSplit[1];
                                if (CommandLineSettings.getInstance().hasUserATServerKey(currentLine)) {
                                    PasswordManager.getManager().setPassword(name,value);
                                } else if (!CommandLineSettings.getInstance().containsSetting(name)) {
                                    value = value.trim();
                                    CommandLineSettings.getInstance().getSettings().put(name, value);
                                }
                            }
                        }
                    } else {
                        if (!CommandLineSettings.getInstance().isSwitchSet(currentLine.substring(1, currentLine.length()).toUpperCase())) {
                            CommandLineSettings.getInstance().getSwitches().add(currentLine.substring(1, currentLine.length()).toUpperCase());
                        }
                    }
                }
            }
            MBClient.setSwitches();
        } catch (FileNotFoundException fnfe) {
            throw new GeneralError(fileToBuildFrom + " not found!", fnfe);
        } catch (IOException ioe) {
            throw new GeneralError("IOException loading parmfile " + fileToBuildFrom, ioe);
        }
    }

    private void handleEnvironmentProperties() {
        B390Password password = new B390Password();
        TreeMap<String,String> sortedEnvMap = new TreeMap<String,String>(System.getenv());
        password.setValues(sortedEnvMap);
        for (Map.Entry<String,String> entry:password.getIndexToValuesMap().entrySet()) {
            Scanner lineScanner = new Scanner(entry.getValue());
            lineScanner.useDelimiter("=");
            String userATServerKey = lineScanner.next();
            String passwordString = "";
            if (lineScanner.hasNext()) {
                passwordString = lineScanner.next();
            }
            PasswordManager.getManager().setPassword(userATServerKey, passwordString);
        }
    }

    private void validateSetup()  throws com.ibm.sdwb.build390.MBBuildException {
        //clean it up later. 
        SetupValidator validateSetup = new SetupValidator(getLEP(),getStatusHandler());
        validateSetup.setValuesOfArguments(settings,switches);
        validateSetup.runProcess();
    }

    private void handleHelp() {
        System.out.println("\nDescription:\n");
        System.out.println(commandHandler.getHelpDescription());
        System.out.println("\nSyntax:");
        if (commandHandler.getArgumentStructure().getRequiredPart()!=null) {
            System.out.println("\nRequired argument section:\n");
            System.out.println(PrettyPrinter.handleBooleanInterface(commandHandler.getArgumentStructure().getRequiredPart()));
        } else {
            System.out.println("\nBUILD is the only required keyword.");
        }
        if (!commandHandler.getArgumentStructure().getOptions().isEmpty()) {
            System.out.println("\nOptional arguments:\n");
            for (Iterator optionIterator = commandHandler.getArgumentStructure().getOptions().iterator(); optionIterator.hasNext();) {
                System.out.println(PrettyPrinter.handleBooleanInterface((BooleanInterface) optionIterator.next()));
            }
        } else {
            System.out.println("\nThere are no options for this command.");
        }
        System.out.println("\nExamples:\n");
        System.out.println(commandHandler.getHelpExamples());
    }
/** TST1714 **/
    private void populateReleasesAndDriversFile() throws com.ibm.sdwb.build390.MBBuildException {
        File fileToRead = new File(MBGlobals.Build390_path, "releasesAndDrivers.ser");
        Setup setup = SetupManager.getSetupManager().createSetupInstance();
        if (!fileToRead.exists() && !commandName.equals(MainframeReleaseList.PROCESSNAME)) {
            com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseAndDriversList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(setup.getMainframeInfo(), setup.getLibraryInfo(), MBClient.getCacheDirectory() ,this);
            releaseAndDriversList.externalRun();
        }

    }

    public com.ibm.sdwb.build390.MBStatus getStatusHandler() {
        return MBClient.status;
    }

    public com.ibm.sdwb.build390.logprocess.LogEventProcessor getLEP() {
        return MBClient.lep;
    }

    public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
    }
}
