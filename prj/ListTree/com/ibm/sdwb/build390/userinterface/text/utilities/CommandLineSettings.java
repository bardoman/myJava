package com.ibm.sdwb.build390.userinterface.text.utilities;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.user.Mode;

public class CommandLineSettings {

    private Hashtable programSettings = new Hashtable();
    private Set commandLineSwitches = new HashSet();

    private boolean isCommandLine = false;
    private String command = new String();
    private String inputString = new String();

    public static final String NOTOOLCOUNT ="NOTOOLCOUNT";
    public static final String PARMFILE ="PARMFILE";
    public static final String REMOTE ="REMOTE";
    public static final String DEBUG="DEBUG";
    public static final String DEBUG1="DEBUG1";

    public static final String NOPROMPT="NOPROMPT";//INT3368

    public static final String TEST = "TEST";
    public static final String TRACE="TRACE";
    public static final String HOLD="HOLD";
    public static final String HELP="HELP";
    public static final String HELPRC="HELPRC";
    public static final String VERSION ="VERSION";
    public static final String DEV_AND_SERVICE_INTERFACE ="DEV_AND_SERVICE_INTERFACE";

    public static final String CONFIGADMIN="CONFIGADMIN";
    public static final String NOLIB="NOLIB";
    public static final String KUNGFOOMONKEY="HYPERKUNGFOOMONKEYMODE";
    private static CommandLineSettings commandLineSettings = null;

    private CommandLineSettings(String[] args) {
        // Pat - This code handles the case when running on w95/98 and the complete input
        // string is in args0. This happens because those OS's strip = signs from
        // the commnad line input string, so build390.bat forces those users to prefix the
        // string with a double quote.
        // Check for double quotes around input, if found, parse args0 into argsn
        // otherwise copy args into args. Argsn is then used instead of args0 // w95cmdline

        boolean gotCommand = false;
        String[] argsn = null;

        if (args.length == 1) {
            if (args[0].indexOf(" ")>-1) {
                // parse args0
                int idx = 0;
                StringTokenizer st = new StringTokenizer(args[0]);
                argsn = new String[st.countTokens()];
                while (st.hasMoreTokens()) {
                    argsn[idx] = st.nextToken();
                    idx++;
                }
            } else {
                // If not all in args0, copy args into argsn
                argsn = new String[args.length];
                for (int idx=0; idx<args.length; idx++) {
                    argsn[idx]=args[idx];
                }
            }
        } else {
            // If not all in args0, copy args into argsn
            argsn = new String[args.length];
            for (int idx=0; idx<args.length; idx++) {
                argsn[idx]=args[idx];
            }
        }

        // Check input args for help request
        if (argsn.length > 0) {
            if (argsn[0].equals("?")  |  argsn[0].toUpperCase().equals(HELP)) {
                System.out.println(MBText.MBClient_Help);
                MBClient.exitApplication(MBConstants.EXITSUCCESS);
            }
        }

        // Check input args for helprc request // #322
        if (argsn.length > 0) {
            if (argsn[0].toUpperCase().endsWith(HELPRC)) {
                System.out.println(MBText.MBClient_RC_Help);
                MBClient.exitApplication(MBConstants.EXITSUCCESS);
            }
        }

        // Check input args for version request
        if (argsn.length > 0) {
            if (argsn[0].toUpperCase().equals(VERSION)) {
                System.out.println("Version="+MBConstants.getProgramVersion()+", Build Date="+MBConstants.getBuildDate());
                MBClient.exitApplication(MBConstants.EXITSUCCESS);
            }
        }

        // save the args in the args_ vector
        // check for a command, if found then set cmd line mode
        // check for password, if found set pw_ but dont save in args
        int argIdx = 0;
        String currentArg;
        while (argIdx < argsn.length) {
            currentArg = argsn[argIdx];
            if (!currentArg.startsWith("pw")) { // ignore password
                inputString = inputString + currentArg + " ";
            }
            int equalIndx = currentArg.indexOf("=");
            // save all switches that start with / or -
            if ((currentArg.startsWith("/")) || currentArg.startsWith("-")) {
                commandLineSwitches.add(currentArg.substring(1,currentArg.length()).toUpperCase());
                argIdx++;
                // save all settings, ie name value pairs
            } else if (equalIndx > -1) {
                programSettings.put(currentArg.substring(0,equalIndx).toUpperCase(), currentArg.substring(equalIndx+1, currentArg.length()));
                argIdx++;
            } else if (((argIdx + 1) < argsn.length) && (argsn[argIdx+1].equals("="))) {
                programSettings.put(currentArg.toUpperCase(), argsn[argIdx+2]);
                argIdx = argIdx + 3;
            } else {
                // 09/10/2006 the first update after several years. great solid code.
                if (currentArg.trim().toUpperCase().length() > 0) {
                    isCommandLine = true;
                    if (gotCommand) {
                        System.out.println("Too many commands.");
                        System.out.println("The syntax is <command> <keyword>=<value> <switches>.");
                        MBClient.exitApplication(MBConstants.SYNTAXERROR);        // terminate the application
                    } else {
                        gotCommand = true;
                        command = argsn[argIdx].toUpperCase();      // get command from args
                    }
                }
                argIdx++;
            }
        }

    }

    public static void newInstance(String[] args){
        if(commandLineSettings ==null){
            commandLineSettings = new CommandLineSettings(args);
        }

    }

    public static CommandLineSettings getInstance(){
        return commandLineSettings;
    }


    public boolean isCommandLine() {
        return isCommandLine;
    }

    public void setCommandLine(boolean isCommandLine) {
        this.isCommandLine = isCommandLine;
    }


    public String getCommand() {
        return command;
    }

    public String getInputString() {
        return inputString;
    }

    public Hashtable  getSettings() {
        return programSettings;
    }

    public String getSetting(String settingName) {
        return(String) programSettings.get(settingName);
    }

    public boolean containsSetting(String settingName) {
        return getSettings().containsKey(settingName);
    }

    public boolean containsPARMFILE() {
        return containsSetting(PARMFILE);
    }

    public boolean hasUserATServerKey(String name){
        //line starts with PW or pw, followed by any thing, follwed by @ , followed by anything, followed by =, followed by anything.
        String regex = "^[PWpw].*[@].*[=].*$";
        return name.trim().matches(regex);
    }

    public Set getSwitches() {
        return commandLineSwitches;
    }

    public boolean isSwitchSet(String switchName) {
        return(getSwitches().contains(switchName.toUpperCase()));
    }

    public Mode getMode() {
        if (isSwitchSet(CONFIGADMIN)) {
            return new com.ibm.sdwb.build390.configuration.ConfigAdminMode();
        }

        if (isSwitchSet(NOLIB)) {
            return new com.ibm.sdwb.build390.library.FakeLibraryMode();
        }

        if (isSwitchSet(DEV_AND_SERVICE_INTERFACE)) {
        	System.out.println("got deve and service");
            return new com.ibm.sdwb.build390.library.DevelopmentAndServiceMode();
        }

        return new com.ibm.sdwb.build390.library.DevelopmentMode(); //by default send a development mode


    }

    public void logParmfileContents() throws MBBuildException {
        // if the cmd line contains a parmfile, dump it to the log file.
        if (containsSetting(CommandLineSettings.PARMFILE)) {
            String fileToBuildFrom = getSetting(CommandLineSettings.PARMFILE);
            if(!(new File(fileToBuildFrom)).exists()){
                throw new SyntaxError("Parmfile " + fileToBuildFrom + " not found. Please enter a valid parmfile.");
            }
            try {
                BufferedReader paramFileReader = new BufferedReader(new FileReader(fileToBuildFrom));
                String currentLine;
                while ((currentLine = paramFileReader.readLine()) != null) {
                    currentLine = currentLine.trim();
                    if (currentLine.length() > 0) {
                        if (!currentLine.toUpperCase().startsWith("PW")) {
                            // skip the password line
                            MBClient.lep.LogPrimaryInfo("INFORMATION", "Parm file => "+currentLine,false);
                        }
                    }
                }
                paramFileReader.close();
            } catch (IOException ioe) {
                throw new GeneralError("There was an error loading parmfile " + (new File(fileToBuildFrom)).getAbsolutePath(), ioe);
            }
        }
    }
}
