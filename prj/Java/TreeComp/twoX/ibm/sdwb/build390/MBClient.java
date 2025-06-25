package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java Client for MVS Build server                                  */
/* This is the main class for the build/390 client                   */
/* Input: password command args - for command line mode              */
/*        password              - for gui mode                       */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 10/28/98 exit_1              Exit with the correct rc
// 12/14/98                     Show cmd line in log file
// 12/14/98 Defect_163          Uppercase key when getting pw
// 12/23/98 #164                Password prompting in commandline mode
// 12/31/98 #164                OS Specific code for password prompting.
// 01/13/99                     Add System.err trapping code
// 01/13/99                     Log contents of parmfile
// 01/19/99                     Cause I'm a moron (windowSizes hash was only loaded in GUI mode, but saved always.
//                              So it was erased when commandline mode was run.
// 01/21/99                     Support version command
// 03/30/99 adddate             Get timestamp of when the client was started, used for file compares /pj
// 04/09/99 search_the_jar      class files are now in a jar
// 04/23/99 errorMessage        fix threadgroup for errorhandling.
// 04/27/99 registration        add registration function
// 05/05/99 feature             service process, reset on exit
// 05/05/99 defect              only write system error stuff to log IF IT EXISTS
// 05/05/99 defect              try to break Chris's exitApplication closing stuff
//                              Um, I mean, that is, check that a service client exists before closing it.
// 05/06/99 #322                Handle no setup info better
// 05/07/99 #322                Pat, Add helprc command to show return code help
// 05/07/99 fixexit             Pat, Fix exit code
// 05/07/99 addHelpRC           Pat, Append info about rc's to help output to keep Leon happy
// 05/20/99 #342                Ken, in submit requests, swallow errors if process was cancelled.
// 05/20/99 Ken_you_bad_boy     Ken, you broke cmd line processing
// 06/10/99 defect_387          Pat, show cmd line in log when more than one cmd entered
// 06/11/99 w95cmdline          Fix windows 95 cmd line processing
// 08/12/99                     remove beep method
// 12/09/99                     trace all parm file records if this is a user exit invocation
// 01/14/00 build.log.1m      	pops up with a dialog to inform the user of the build log >1M and the user has options of archiving the old one or leave it as it is.
// 01/24/00 entry log detail  	changed the entry log detail when a client is invoked.
// 02/02/00 dbConfig			move configuration storage into the database
// 02/25/00  					added support for the config switch
// 03/07/00 reworklog         	changed the log processing to handle using listeners
// 03/28/00 debugchanges      	changes to correct the debug problems after adding log processor
// 03/30/00 223fixMove			move 223 fixes to 23
// 05/02/00 eventDisplay      	Additon of boolean switch to LogPrimaryIfno to  control just traceit or traceit + display
// 05/15/00 archivebuildlog   	Invoke a modal frame with a progress bar with status information of the file copied.
// 05/22/00 reworklog         	changes for to pass lep object in the method WriteLogs.
// 06/05/00 pjs					move support for version command ahead of setup check.
// 07/27/00 pjs					Fix start and stop log msgs
// 08/11/00 oeclient		put the OSNAME in the  static variable  and added the stuff did in BPS Fix pack
// 09/20/00 oeclient & SDWB1251 add a  warning message string to be sent to the perl program, and changes for commandline CREATESETUP
//                              and addition of /ODE switch to bypass library stuff.    
// 12/14/00                     remove System.out.println
// 01/16/01                     comment RMISecurityManager , and make the display of /helprc as 'Build390 /helprc'
// 02/09/01                     added a static call in isUSSClient in MBClient for OS/390 or z/390 checkings
// 03/23/01   defect 205 -      if no client.ser in uss return RC=4
// 03/23/01   defec             The uss client shouldnt go into gui mode when no arguments are entered.
//06/08/2001 #DEF.TST0497:      Needed to add password routines for cmvc that were not forced to upper case
//03/29/2002 #Def.INT0754:      Add new verb trace option
//05/08/2002 #Def.TST0914:      command line throws java ioexception when prompt for pw
//06/18/2002 #Def.TST0967:      Host error Getfmid routine during APAR close in ODE process
//06/18/2002 #Def.TST0970:      Error in getfmid verb when Accepting Review in BPS
//03/14/2003 #Def.PTM2590:      JPN Regional settings (Default), doesnt let the file 2mb archive to proceed(due the weird java.io.File.separator used by Japanese font).
//05/14/2003 #DEF.RemRegFeat:   Remove registration feature
//06/04/2003 #DEF.RemUssLocks:  Remove USS locks
//08/29/2003 #DEF.TST1451:      build390.bat /printhelp is broke.
//09/03/2003 #DEF.TST1467:      DEFREQ command line not implemented
/*02/12/2004 INT1757 mixed-case support */
/*03/16/2004 TST1780            fix reenter pw. bug */
//04/08/2004 #DEF.TST1866:      logretrieve needs to allow /odebuild parm
//10/20/2004 PTM3735           Getter methods to access PROGRAMVERSION/BUILDDATE.
/*********************************************************************/
import java.io.*;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import com.ibm.sdwb.build390.logprocess.LogEventCommandLineListener;
import com.ibm.sdwb.build390.logprocess.LogEventFileListener;
import com.ibm.sdwb.build390.logprocess.LogEventGUIListener;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Mode;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.text.commandline.CommandLineHandler;
import com.ibm.sdwb.build390.userinterface.text.commandline.process.SetupCreate;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSupport;
import com.ibm.sdwb.build390.userinterface.text.utilities.ConsoleListener;


/** <br>MBClient is the main class for the Build/390 application.
* <br>It instanciates other needed classes,
* sets either command line or gui mode,
* and manages the commands and command arguments to be submitted to the server
*/
public class MBClient implements Serializable {

    // Global fields -start
    static final long serialVersionUID = 1460042637212531544L;

    private static  boolean     clientMode = false; // are we running as a client or RMI op, assume RMI mode, set to client if the client starts

    public  static  MBStatus    status = null;
    public  static  Hashtable   lockCache = new Hashtable();
    public  static  MBClient    client = null;
    public  static  MBErrorOutputStream stdErr = null;          // the replacement output stream for System.err

    private  static  String      OSNAME= new String();

    public  static   transient   LogEventProcessor lep=null;     //rework log
    public  static   transient   LogEventFileListener LogFileListenerObject = null;
    private static   transient   LogEventGUIListener LogGUIListenerObject = null;
    private static   transient   LogEventCommandLineListener logCommandListener =null; 

    private  static  boolean     isDetailDebug=false;
    private  static  boolean     isVisualDebug=false;

    private  static  boolean     isNoPromptMode=false;//INT3368

    private static File         cacheFile  = null;

    private static CommandLineHandler processCommand = null;
    public static String CANCELSTRING = "CANCEL";


    /** Create the new MBClient object and go run it
    * @param args the command line input string
    */
    public static void main (final String[] args) throws com.ibm.sdwb.build390.MBBuildException, ClassNotFoundException {
// Ken 4/23/99   Run this in our own threadgroup so we can handle uncaught/unexpected errors

        clientMode = true;

        setHomeDirectory();
        loadStoredReleasesAndDrivers();

        OSNAME = System.getProperty("os.name");

        Locale.setDefault(Locale.ENGLISH);  //PTM2590 

        ErrorThreadGroup tempGroup = new ErrorThreadGroup("Error Handling Threadgroup");
        new Thread(tempGroup, new Runnable() {
                       public void run() {
                           try {
                               client = new MBClient();
                               client.runme(args);
                           }
                           catch(MBBuildException mbe) {
                               System.err.println(mbe);
                           }
                           //Any uncaught exceptions are caught by the ErrorThreadGroup
                       }
                   }).start();
    }



    /** Initialize variables for client execution
    */
    public MBClient() {
        // create status object
        status = new MBStatus(null);
        stdErr = new MBErrorOutputStream(System.err);
        System.setErr(new PrintStream(stdErr));

        /*RMISecurityManager security =  new RMISecurityManager();
        System.setSecurityManager(security);
        */

    }

    /** The MBClient has been created, handle it.
    * @param args the command line input string
    */
    public void runme(String[] args) throws com.ibm.sdwb.build390.MBBuildException {

        MBUtilities.initTrace();

        lep.LogPrimaryInfo("INFORMATION","----Client Started in OS " + OSNAME+" ----Release:"+MBConstants.getProgramVersion()+"----Build Date:"+MBConstants.getBuildDate(),false);

        CommandLineSettings.newInstance(args);

        setSwitches();

        //Begin #DEF.TST1451:
        // check for a command help request
        if(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.HELP)) {
            CommandLineSettings.getInstance().setCommandLine(true);
            HelpRequest(CommandLineSettings.getInstance().getCommand());
            return;
        }
        //End #DEF.TST1451:

        // handle GUI processing
        if(!CommandLineSettings.getInstance().isCommandLine()) {

            MainInterface.initializeInterface(); // create GUI object
            MainInterface.getInterfaceSingleton().setVisible(true);
            createLogGUIListener();
            lep.addEventListener(getGlobalLogGUIListener());
            MBBasicInternalFrame.loadSizes();
            // Read settings from setup file
            if(!SetupManager.getSetupManager().hasSetup()) {
                System.out.println(MBText.MBClient_Setup);
                com.ibm.sdwb.build390.userinterface.graphic.panels.setup.SetupInformation mbg = new com.ibm.sdwb.build390.userinterface.graphic.panels.setup.SetupInformation(null);
                if(!SetupManager.getSetupManager().hasSetup()) {
                    lep.LogPrimaryInfo("ERROR:",MBText.MBClient_Log2,false);
                    MBClient.exitApplication(MBConstants.SETUPERROR);       // terminate the application
                }
            }

            MainInterface.getInterfaceSingleton().run();

            incrementToolCount();
            archiveLogFile();

        } /* endif for gui mode */
        // Handle command line processing
        else {
            // log input string
            lep.LogPrimaryInfo("INFORMATION:", "Command Line => "+CommandLineSettings.getInstance().getInputString(),false);
            archiveLogFile();

            try {
                CommandLineSettings.getInstance().logParmfileContents();
                //TO-DO : I think this is in the CommandLineHandler setup.setValuesFromHash(CommandLineSettings.getInstance().getSettings());

                Mode  mode = CommandLineSettings.getInstance().getMode();

                if(mode instanceof CommandLineSupport) {
                    if(((CommandLineSupport)mode).getSupportedCommands().contains(CommandLineSettings.getInstance().getCommand())) {
                        processCommand = new CommandLineHandler(CommandLineSettings.getInstance().getCommand(), CommandLineSettings.getInstance().getSettings(), CommandLineSettings.getInstance().getSwitches());

                        ConsoleListener.getInstance(processCommand).start();

                        processCommand.run();
                        // Process ToolCount
                        incrementToolCount();
                    }
                    else {
                        System.out.println("--------------------------------------------------------");
                        System.out.println("The command " + CommandLineSettings.getInstance().getCommand() + " is not supported in mode : " + mode);
                        System.out.println("--------------------------------------------------------");
                    }
                }
                else {
                    System.out.println("Command line is not supported in mode : " + mode);
                }

                exitApplication(MBConstants.EXITSUCCESS);
            }
            catch(MBBuildException mbe) {
                while(mbe.getOriginalException() != null & mbe.getOriginalException() instanceof MBBuildException) {
                    mbe = (MBBuildException) mbe.getOriginalException();
                }
                lep.LogException(mbe);
                if(mbe.getReturnCode() > -1) {
                    exitApplication(mbe.getReturnCode()); // exit_1
                }
                else exitApplication(MBConstants.GENERALERROR);
            }
        }
    }

    private static void setHomeDirectory() {
        if((MBGlobals.Build390_path = System.getProperty("MBGlobals.Build390_path")) == null) {
            MBGlobals.Build390_path = System.getProperty("user.dir");
        }
        if(!MBGlobals.Build390_path.endsWith(File.separator)) {
            MBGlobals.Build390_path = MBGlobals.Build390_path+File.separator;
        }

    }

    private static void loadStoredReleasesAndDrivers() {
        try {
            File fileToRead = new File(MBGlobals.Build390_path, "releasesAndDrivers.ser");
            if(fileToRead.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileToRead));
                MBMainframeInfo.readStaticInfoMap(ois);
                ois.close();
            }
        }
        catch(IOException ioe) {
            System.out.println("error reading release and driver info from  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            System.out.println("error reading release and driver info from  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
            cnfe.printStackTrace();
        }

        createLogListeners();
        lep = new LogEventProcessor();
    }


    private static void incrementToolCount() {
        // Process ToolCount
        if(!CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.NOTOOLCOUNT)) {
            ToolCount.toolcount(MBConstants.Toolset, MBConstants.Toolname);
        }
    }

// 01/14/2000 build.log.1M changes to popup with >1m dialog - runs on a  separate thread
    private static void archiveLogFile() {
        int LOG_FILE_MAX_ALLOWED_SIZE = 1000000;
        File lf = new File(MBGlobals.Build390_path+MBConstants.LOGFILEPATH);

        if(lf.length() > LOG_FILE_MAX_ALLOWED_SIZE) {
            File lfth = new File(MBGlobals.Build390_path+MBConstants.LOGFILEPATH);
            if(!CommandLineSettings.getInstance().isCommandLine()) {
                MBMsgBox confirmArchive = new MBMsgBox("Log file greater than 1M","Your Build390 log file, " +lfth.getPath()+", is greater than 1M. Press yes to archive the current log file and start a new one. Press no to continue with the current log file",null,true);
                if(confirmArchive.isAnswerYes()) {
                    //pops up with a progress bar with the status of the completion of task.
                    MBLogFileArchiverPage lfap = new MBLogFileArchiverPage(lep);
                }
            }
            else {
                new MBMsgBox("Warning", "Your Build390 log file "+lf.getPath()+", is larger than 1M, you may want to delete it.");
            }
        }

    }


    //this is for other classes. Will have to delete it, so other classes also get decoupled from MBClient.
    public static CommandLineSettings getCommandLineSettings() {
        return CommandLineSettings.getInstance();
    }

    public static CommandLineHandler getCommandLineHandler() {
        return processCommand;
    }

    public static void setSwitches() {
        set_debug(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.DEBUG));
        set_debug1(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.DEBUG1));

        setNoPromptMode(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.NOPROMPT));//INT3368

        com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setHoldSubServerJobOutput(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.HOLD));
        com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(CommandLineSettings.getInstance().isSwitchSet(CommandLineSettings.TRACE));
    }

    public static File getCacheDirectory() {
        if(cacheFile == null) {
            cacheFile = new File(MBGlobals.Build390_path, "cache");
            cacheFile.mkdir();
        }
        return cacheFile;
    }

    //returns true if the in the command prompt its typed as build390 /debug1 ;
    public static boolean getDetailDebug() {
        return isDetailDebug;
    }

    //returns true if the in the command prompt its typed as build390 /debug ;
    public static boolean getVisualDebug() {
        return isVisualDebug;
    }


    /** Set debug setting.
   * @param dbg boolean value to set debug to */
    public static void set_debug(boolean dbg) {
        isVisualDebug=dbg;
    }


    /** Set debug1 setting.
    * @param dbg boolean value to set debug to */
    public static void set_debug1(boolean dbg) {
        isDetailDebug=dbg;
    }

    public static void setNoPromptMode(boolean mode) {
        isNoPromptMode=mode;
    }

    public static boolean getNoPromptMode() {
        return isNoPromptMode;
    }

    //reworklog
    private static void createLogListeners() {
        String LogFileStr = MBGlobals.Build390_path+MBConstants.LOGFILEPATH;
        LogFileListenerObject = new LogEventFileListener(LogFileStr);
        logCommandListener = new LogEventCommandLineListener();

    }
    private static void createLogGUIListener() {
        MBBasicInternalFrame tempGUIbasicFrame =  new MBBasicInternalFrame("Basic-Frame created new",null,true,true, null);
        LogGUIListenerObject  = new LogEventGUIListener(tempGUIbasicFrame);
        MainInterface.getInterfaceSingleton().clearWaitCursor();
    }

    public static LogEventCommandLineListener getGlobalLogCommandLineListener() {
        return logCommandListener;
    }

    public static LogEventFileListener getGlobalLogFileListener() {
        return LogFileListenerObject;
    }

    public static LogEventGUIListener getGlobalLogGUIListener() {
        return LogGUIListenerObject;
    }


    /*
    The methods is used to determine if you are running in a USS environment in mvs.
    We have to keep track of IBMs recent releases in USS so that they dont change the 
    OSNAME value like OS/390 to something else.
    in raljes3c it was OS/390 , in snjeds3 it was z/OS
    */
    public static boolean isUSSClient() {
        if(OSNAME.equals("OS/390") | OSNAME.equals("z/OS")) {
            return true; 
        }
        else {
            return false;
        }
    }

    /** The HelpRequest method process help reuests */
    public void HelpRequest(String command) throws com.ibm.sdwb.build390.MBBuildException {
        Mode  mode = CommandLineSettings.getInstance().getMode();

        if(mode instanceof CommandLineSupport) {
            if(command.length() < 1) {
                Vector commandsToPrintHelpFor = new Vector();//INT2382
                commandsToPrintHelpFor.addAll(((CommandLineSupport)mode).getSupportedCommands());

                Collections.sort(commandsToPrintHelpFor);//INT2382
                System.out.println("--------------------------------------------------------");
                System.out.println("The following commands are supported in mode : " + mode);
                System.out.println("--------------------------------------------------------");
                int i=0;
                for(Iterator commandIterator = commandsToPrintHelpFor.iterator(); commandIterator.hasNext();) {
                    String oneCommand = (String) commandIterator.next();
                    System.out.println(i++ +"."+oneCommand);
                }
                System.out.println("--------------------------------------------------------");
                System.out.println("Detail help instructions are as follows :               ");
                System.out.println("--------------------------------------------------------");
                for(Iterator commandIterator = commandsToPrintHelpFor.iterator(); commandIterator.hasNext();) {
                    String oneCommand = (String) commandIterator.next();
                    new CommandLineHandler(oneCommand, CommandLineSettings.getInstance().getSettings(), CommandLineSettings.getInstance().getSwitches());
                    System.out.println();
                    System.out.println();
                    System.out.println();
                }
            }
            else {
                if(((CommandLineSupport)mode).getSupportedCommands().contains(command)) {
                    System.out.println("print help");
                    new CommandLineHandler(command, CommandLineSettings.getInstance().getSettings(), CommandLineSettings.getInstance().getSwitches());
                }
                else {
                    System.out.println("--------------------------------------------------------");
                    System.out.println("The command " + command + " is not supported in mode : " + mode);
                    System.out.println("--------------------------------------------------------");
                }
            }
        }
        else {
            System.out.println("Command line is not supported in mode : " + mode);
        }
        System.out.println("\nNote:");
        System.out.println("1.Use  the   option  /debug1  to  turn on extensive  information"); 
        System.out.println("  that is useful to Build/390 development in problem resolution.");
        System.out.println("2.For information about Return Codes, run: Build390 /helprc\n\n");
        MBClient.exitApplication(MBConstants.EXITSUCCESS);
    }


    /**
     * for telling if we are running from the client, or as an RMI
     * operation
     * 
     * @return true if this is client based operation
     */
    public static boolean isClientMode() {
        return clientMode;
    }


    public static void exitApplication(int returnCode) {
        // put stuff into log file so we know what happened
        lep.LogPrimaryInfo("INFORMATION","----Client Exit-----Return Code:"+returnCode,false);
        // save the System.err messages into the log file when stdErr.toString().length() > 0
        if(stdErr.toString().trim().length() > 0) {
            lep.LogPrimaryInfo("INFORMATION","System.err messages received this invocation:\n"+stdErr.toString()+"\n***End System Error***", false);
        }
        cleanOrphans();
        //only save the hash if in gui mode, so we don't kill the hashtable.
        if(MainInterface.getInterfaceSingleton() != null) {
            MBBasicInternalFrame.saveSizes();
        }
        try {
            com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.saveRememberedSettings();
        }
        catch(java.io.IOException ioe) {
            System.out.println("error saving defaults info to  " + com.ibm.sdwb.build390.userinterface.RememberedSettingsHandler.getSaveFile().getAbsolutePath());
            ioe.printStackTrace();
        }
        //only when it exists we need to save one.
        if((new File(MBGlobals.Build390_path+"releasesAndDrivers.ser")).exists()) {
            try {

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MBGlobals.Build390_path+"releasesAndDrivers.ser"));
                MBMainframeInfo.saveStaticInfoMap(oos);
                oos.close();
            }
            catch(IOException ioe) {
                System.out.println("error saving release and driver info to  " + MBGlobals.Build390_path+"releasesAndDrivers.ser");
                ioe.printStackTrace();
            }
        }
        System.out.println(MBText.MBClient_rc+returnCode);

        if(CommandLineSettings.getInstance()!=null && CommandLineSettings.getInstance().isCommandLine() && processCommand!=null) {
            if(ConsoleListener.getInstance(processCommand).isListening()) {
                ConsoleListener.getInstance(processCommand).stopListening();
            }
        }

        System.exit(returnCode);
    }

    private static void cleanOrphans() {
        for(int i = 0; i < MBConstants.BuildDirectories.size(); i++) {
            File generalDir = new File(MBGlobals.Build390_path+(String)MBConstants.BuildDirectories.elementAt(i));
            if(generalDir.exists()) {
                String[] filesInDir = generalDir.list();
                for(int i2 = 0; i2 < filesInDir.length; i2++) {
                    File buildDir = new File(generalDir, filesInDir[i2]);
                    if(buildDir.isDirectory()) {
                        if(!(new File(buildDir, MBConstants.BUILDSAVEFILE)).exists()) {
                            MBUtilities.deleteDirectory(buildDir);
                        }
                    }
                }
            }
        }
        File genericDirectory = new File(MBGlobals.Build390_path+(String)MBConstants.GENERICBUILDDIRECTORY);
        if(genericDirectory.exists()) {
            String[] filesInDir = genericDirectory.list();
            for(int i2 = 0; i2 < filesInDir.length; i2++) {
                File buildDir = new File(genericDirectory, filesInDir[i2]);
                buildDir.delete();
            }
        }
    }

}

