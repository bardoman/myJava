package com.ibm.sdwb.cmvc.client.api;

import com.ibm.sdwb.cmvc.client.api.*;

import java.io.*;
import java.util.*;

public class CMVCPass {

    public  static  Hashtable   lockCache = new Hashtable();

    static final long serialVersionUID = 1111111111111111L;

    private boolean passwordAuthentication = false;  
    private static PrintWriter printWr = null;
    private static  int LIBRARYTHREADLIMIT = 5;
    private static HashMap familyInfoHash = new HashMap();

    private String cmvcServerName=null;
    private String cmvcServerAddress=null;
    private String cmvcPortAsString=null;

    private String userId=null;
    private String password=null;
    private String msg=null;

    private static String COMMAND     ="COMMAND";
    private static String OBJECT_SPEC ="OBJECT_SPEC"; 
    private static String PARM_KEY    ="PARM_KEY"; 
    private static String PARM_VALUE  ="PARM_VALUE";

    private String command=null;
    private String objectSpec=null;
    private int integerCnt=0;
    private Hashtable keyValuePairs= new Hashtable();
    int returnCode=0;

    private String logPath=null;
    private File logFile=null;

    public CMVCPass(String args[]) {

        try {
            logPath = System.getenv("B390_CMVCPASS_PATH");//get path to log location

            if(logPath==null) {

                logFile = new File("CMVCPassException.log");
            }
            else {
                if(!logPath.endsWith(File.separator)) {

                    logFile = new File(logPath+File.separator+"CMVCPassException.log");
                }
                else {
                    logFile = new File(logPath+"CMVCPassException.log");
                }
            }

            printWr = new PrintWriter(new FileWriter(logFile),true);

            userId = System.getenv("B390_CMVCPASS_USERID");
            if(userId==null)badInput("The B390_CMVCPASS_USERID is misformed.");
            if(userId.length()==0)badInput("The B390_CMVCPASS_USERID is misformed.");

            password = System.getenv("B390_CMVCPASS_PASSWORD");
            if(password==null)badInput("The B390_CMVCPASS_PASSWORD is misformed.");
            if(password.length()==0)badInput("The B390_CMVCPASS_PASSWORD is misformed.");

            String tempAddress = System.getenv("B390_CMVCPASS_ADDRESS");

            parseAddress(tempAddress);

            parseArgs(args);

            Command cmd = getCommandObject(command);

            if(cmd.isObjectSpecNeeded()==true) {
                if(objectSpec!=null) {
                    cmd.getObjectSpec().setValue(objectSpec);
                }
                else {
                    badInput("OBJECT_SPEC needed for this command is not supplied");
                }
            }

            Enumeration keyEnum = keyValuePairs.keys();
            String tempKey="";

            while(keyEnum.hasMoreElements()) {
                tempKey=(String)keyEnum.nextElement();

                cmd.addParameterValue(tempKey, (String)keyValuePairs.get(tempKey));
            }

            msg=runCommand(cmd);

            System.out.println(msg);

            if( returnCode== CommandResults.SUCCESS ) {
                System.out.println( "The command completed successfully." );
            }

            System.out.println("RC="+returnCode);
        }
        catch(Exception e) {
            logException(null,e);
        }
    }

    void badInput(String msg) {
        System.out.println("Error: "+msg);
        System.out.println("RC=BADINPUT");
        System.exit(1);
    }

    void parseAddress(String tempAddress) {
        boolean malformed=false;

        if(tempAddress!=null) {
            if(tempAddress.length()!=0) {
                StringTokenizer strTok = new StringTokenizer(tempAddress,"@");

                if(strTok.countTokens()==3) {
                    while(strTok.hasMoreTokens()) {
                        cmvcServerName=strTok.nextToken();
                        cmvcServerAddress=strTok.nextToken();
                        cmvcPortAsString=strTok.nextToken();
                    }
                }
                else {
                    malformed=true; 
                }
            }
            else {
                malformed=true; 
            }
        }
        else {
            malformed=true; 
        }


        if(malformed==true) {
            badInput("The B390_CMVCPASS_ADDRESS is misformed.");
        }
    }

    void logException(String msg, Exception e) {

        System.out.println("Exception:");
        Date date = new Date();

        if(printWr==null) {
            System.out.println(date+", ");
            if(msg==null) {
                e.getMessage();
            }
            else {
                System.out.println(msg);
            }
            e.printStackTrace();
        }
        else {

            try {
                printWr.print(date+", ");
                printWr.println(msg);
                e.printStackTrace(printWr);
                printWr.flush();
                printWr.close();
            }
            catch(Exception ee) {
                System.out.println("Problem writing to log");
                ee.printStackTrace();
            }
            String logPath = logFile.getAbsolutePath();

            System.out.println("Please check "+logPath+" for details");
        }
        System.out.println("RC=EXCEPTION");
        System.exit(1);
    }

    private void parseArgs(String args[]) throws Exception {
        String temp="";
        String key="";
        String value="";

        if(args.length>=1) {
            if(args[0]!=null) {
                temp = args[0].toUpperCase();

                if(temp.startsWith(COMMAND)) {

                    temp = args[0].substring(COMMAND.length());

                    if(temp.startsWith("=")) {
                        temp = temp.substring(1);
                        command = temp.trim();
                        if(command.length()==0) {
                            badInput("The COMMAND value is missing.");
                        }
                    }
                    else {
                        badInput("The COMMAND is misformed.");
                    }
                }
                else {
                    badInput("The COMMAND parameter is missing.");
                }
            }
        }
        if(args.length>=2) {
            if(args[1]!=null) {
                temp = args[1].toUpperCase();

                if(temp.startsWith(OBJECT_SPEC)) {

                    temp = args[1].substring(OBJECT_SPEC.length());

                    if(temp.startsWith("=")) {
                        temp = temp.substring(1);
                        objectSpec = temp.trim();
                        if(objectSpec.length()==0) {
                            badInput("The OBJECT_SPEC value is missing.");
                        }
                    }
                    else {
                        badInput("The OBJECT_SPEC is misformed.");
                    }
                }
                else {
                    badInput("The OBJECT_SPEC parameter is missing.");
                }
            }
        }

        if(args.length>=4) {
            int argIndex=2;
            int parmIndex=1;
            int parmCnt = args.length - 2;
            if(isOdd(parmCnt)) {
                badInput("PARM_KEY/PARM_VALUE pair mismatch.");
            }

            for(int i=0;i!=parmCnt;i+=2) {
                if(args[argIndex]!=null) {
                    temp = args[argIndex].toUpperCase();

                    if(temp.startsWith(PARM_KEY)) {

                        temp = args[argIndex++].substring(PARM_KEY.length());

                        if(temp.startsWith(Integer.toString(parmIndex))) {
                            temp = temp.substring(1);
                            key = temp.trim();
                            if(key.startsWith("=")) {
                                key = key.substring(1);
                                if(key.length()==0) {
                                    badInput("The PARM_KEY"+parmIndex+" value is missing.");
                                }
                            }
                        }
                        else {

                            badInput("The PARM_KEY"+parmIndex+" is misformed.");
                        }
                    }
                    else {

                        badInput("The PARM_KEY"+parmIndex+" parameter is missing.");
                    }
                }

                if(args[argIndex]!=null) {
                    temp = args[argIndex].toUpperCase();

                    if(temp.startsWith(PARM_VALUE)) {

                        temp = args[argIndex++].substring(PARM_VALUE.length());

                        if(temp.startsWith(Integer.toString(parmIndex))) {
                            temp = temp.substring(1);
                            value = temp.trim();
                            if(value.startsWith("=")) {
                                value = value.substring(1);
                                if(value.length()==0) {
                                    badInput("The PARM_VALUE"+parmIndex+" value is missing.");
                                }
                            }
                        }
                        else {
                            badInput("The PARM_VALUE"+parmIndex+" is misformed.");
                        }
                    }
                    else {
                        badInput("The PARM_VALUE"+parmIndex+" parameter is missing.");
                    }
                }
                parmIndex++;
                keyValuePairs.put(key,value);
            }
        }
    }

    boolean isOdd(int n) {
        return(n&1)==1;
    }

    protected   String runCommand(Command cmvcCommand) throws Exception{
        return runCommand(cmvcCommand,null);
    }

    protected String runCommand(Command cmvcCommand, String workPath)  throws Exception{
        String methodName = new String("CMVCLibraryInfo:runCommand");
        FamilyInfo familyInfo = getFamilyInfoObject(getProcessServerName(), getProcessServerAddress(), getCMVCPortAsString());
        ClientDefaults clientDefaults = getClientDefaults(getUserId(), workPath);
        SessionData sessionData = new SessionData(); 
        sessionData.setClientDefaults(clientDefaults);

        handleAuthentication(System.getProperty("user.name"), getUserId(), getPW(), familyInfo, sessionData);

        cmvcCommand.setFamilyInfo( familyInfo );
        cmvcCommand.setSessionData( sessionData );
        String obSpecValue = null;
        if(cmvcCommand.getObjectSpec()!=null) {
            obSpecValue = cmvcCommand.getObjectSpec().getValue();
        }

        String commandInfo = "CMVC command: " + cmvcCommand.getName() +" "+ obSpecValue+ "  Family Info: " + familyInfo + "  Client Defaults: "+clientDefaults;
        System.out.println(commandInfo);
        ThreadLimit libLimit;
        synchronized(lockCache) {
            libLimit = (ThreadLimit) lockCache.get(getProcessServerName());
            if(libLimit == null) {
                libLimit = new ThreadLimit(LIBRARYTHREADLIMIT);
                lockCache.put(getProcessServerName(), libLimit);
            }
        }
        libLimit.waitCounter();
        String outputString = null;
        try {
            CommandResults results = cmvcCommand.exec();

            returnCode = results.getReturnCode();

            outputString = checkCMVCResults(results, cmvcCommand, null);
        }
        catch(CommandConstraintException cce) {
            logException("The command was misformed: "+cmvcCommand.toString(), cce);
        }
        catch(FamilyNotFoundException fnfe) {

            logException("The family was not found: "+getProcessServerName()+"@"+getProcessServerAddress()+"@"+getCMVCPortAsString(), fnfe);
        }
        catch(Exception ioe) {

            logException("An error occurred communicating with the family", ioe);
        }
        finally {
            libLimit.notifyCounter();
        }
        return outputString;
    }

    public  FamilyInfo getFamilyInfoObject(String familyName, String familyAddress, String familyPort) throws Exception{
        String infoKey = familyName+"@"+familyAddress+"@"+familyPort;
        FamilyInfo infoToGet = (FamilyInfo) familyInfoHash.get(infoKey);
        if(infoToGet == null) {
            infoToGet = new FamilyInfo();
            infoToGet.setFamilyName(familyName);
            infoToGet.setHostName(familyAddress);
            infoToGet.setPortNumber(Integer.parseInt(familyPort));
            try {
                infoToGet.retrieveServerVersion();
            }
            catch(Exception ioe) {

                logException("Error getting server version: " + ioe.getMessage(), ioe);
            }
            familyInfoHash.put(infoKey, infoToGet);
        }
        return infoToGet;
    }

    public static ClientDefaults getClientDefaults(String cmvcUsername, String extractionPath) {
        ClientDefaults clientDef = new ClientDefaults();
        clientDef.setProperty(ClientDefaults.CMVC_AUTH_METHOD, "PW");

        clientDef.setProperty(ClientDefaults.CMVC_BECOME,cmvcUsername);
        if(extractionPath != null) {
            clientDef.setProperty(ClientDefaults.CMVC_ROOT, extractionPath);
        }
        return clientDef;
    }

    public  void handleAuthentication(String localUsername, String cmvcBecomeUsername, String password, FamilyInfo familyInfo, SessionData sessionData) throws Exception{
        Authentication auth = null;
        if(password==null) {
            auth = new HostAuthentication(cmvcBecomeUsername, cmvcBecomeUsername);
        }
        else {
            auth = new PasswordAuthentication( cmvcBecomeUsername, cmvcBecomeUsername,password,"Build390");
            try {
                CommandResults authRes = ((PasswordAuthentication)auth).login(familyInfo, sessionData);
                checkCMVCResults(authRes,null, "An error occurred logging in to the server"); 
            }
            catch(CommandConstraintException cce) {

                logException("The login command was misformed.", cce);
            }
            catch(FamilyNotFoundException fnfe) {

                logException("The family could not be found.", fnfe);
            }
            catch(com.ibm.sdwb.cmvc.util.DataSourceException e) {
                Throwable emb = e.getDeepestThrowable(e);
                if(emb == null) {

                    logException("An exception occurred attempting to communicate with the family "+familyInfo.getHostName(), e);
                }
                else {
                    logException(e.getMessage(), (Exception) emb);
                }
            }
            catch(Exception ioe) {
                logException(ioe.getMessage(), ioe);
            }
        }
        sessionData.setAuthentication( auth );
    }

    public static String checkCMVCResults(CommandResults commResults, Command cmvcCommand, String errorMessage)throws Exception{
        String outputString = new String();
        for(int i = 0; i < commResults.getMessages().length; i++) {
            outputString += commResults.getMessages()[i];
        }
        if(commResults.getReturnCode() != CommandResults.SUCCESS) {
            if(errorMessage == null) {
                errorMessage = "An error occurred during a CMVC call:";
            }
            if(cmvcCommand!= null) {
                errorMessage += "CMVC Command="+cmvcCommand.toString();
            }
            int rc = commResults.getReturnCode();

            if((rc == PasswordAuthentication.PASSWORD_BAD) ||
               (rc == PasswordAuthentication.PASSWORD_EXPIRED) ||
               (rc == PasswordAuthentication.USER_PASSWORD_UNDEFINED) || 
               (rc ==PasswordAuthentication.USERNAME_BAD)) {
                throw new Exception(errorMessage+"\n"+ outputString +" Password Error");
            }
            throw new Exception(errorMessage+"\n"+ outputString);
        }
        return outputString;
    }

    String getProcessServerName() {
        return  cmvcServerName;
    }

    String getProcessServerAddress() {
        return cmvcServerAddress;
    }

    String getCMVCPortAsString() {
        return cmvcPortAsString;
    }

    String getUserId() {
        return userId;
    }

    String getPW() {
        return password;
    }

    protected Command getCommandObject(String commandSpec) throws Exception{
        Command cmd=null;

        try {
            CommandFactory theFactory = CommandFactory.getInstance();
            synchronized (theFactory) {
                cmd = theFactory.getCommand(commandSpec);
            }
        }
        catch(com.ibm.sdwb.cmvc.util.DataSourceException e) {
            Throwable emb = e.getDeepestThrowable(e);
            if(emb == null) {
                logException("An error occurred creating command "+commandSpec, e);
            }
            else {
                logException("An error occurred creating command "+commandSpec, (Exception) emb);
            }
        }
        return cmd;
    }

    public boolean isLibraryConnectionValid(String msg) {
        try {
            Command cmd = getCommandObject("ReportView" );
            cmd.getObjectSpec().setValue("fileview");
            cmd.addParameterValue( "-where", "pathname='-'");
            msg=runCommand(cmd);

        }
        catch(Exception e) {
            System.out.println("Error validating Library");
            return false;
        }
        return true;
    }

    public static void main(String args[]) {

        if(args.length<4) {
            System.out.println("CMVC_PASS: A stand alone pass-through command tool for the CMVC java390API.jar.");

            System.out.println("  This tool receives parameters from the command line.");
            System.out.println("  It passes these parameters to the execution");
            System.out.println("  function of the Command Class of the java390API.jar");
            System.out.println("USAGE=> CMVC_PASS COMMAND=<value> OBJECT_SPEC=<value> PARM_KEYn=<value> PARM_VALUEn=<value> ...");

            System.out.println("  The n subscripts are index numbers that allow multiple key/value pairs to be defined");
            System.out.println("  Values for n must be consecutive non-zero, positive integers beginning with 1.\n");
            System.out.println("  The following Environment variables must be defined for proper operation:");
            System.out.println("  B390_CMVCPASS_USERID = The cmvc userid for all operations.");
            System.out.println("  B390_CMVCPASS_PASSWORD = The cmvc password for all operations");
            System.out.println("  B390_CMVCPASS_ADDRESS = The address of the CMVC serverin the format 'name@address@port'.");
            System.out.println("  The following Environment variable usage is optional. It defaults to the local directory.");
            System.out.println("  B390_CMVCPASS_PATH = The directory path location for storage of the operation logs.");

        }
        else {

            CMVCPass pass = new CMVCPass(args);
        }
    } 

    class ThreadLimit {
        int current = 0;
        int limit = 0;

        public ThreadLimit (int tempLimit) {
            limit = tempLimit;
        }

        public synchronized void waitCounter() {
            while(current >= limit) {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                    System.out.println("An interruption occurred while waiting on the thread counter");
                }
            }
            current++;
        }

        public synchronized void notifyCounter() {
            current--;
            notifyAll();
        }
    }

    class keyValuePair {
        private String key=null;
        private String value=null;

        public keyValuePair(String key, String Value) {
            this.key=key;
            this.value=value;
        }

        void putKey(String key) {
            this.key=key;
        }

        void putValue(String value) {
            this.value=value;
        }

        String getValue() {
            return value;
        }

        String getKey() {
            return key;
        }
    }
}

