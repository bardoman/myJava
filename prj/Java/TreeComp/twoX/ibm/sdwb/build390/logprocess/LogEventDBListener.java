package com.ibm.sdwb.build390.logprocess;

import java.io.*;
import java.util.*;
import java.net.*;
import com.ibm.sdwb.build390.*;
/*****************************************************************************************************/
/* Java DBListener for Build/390 client                                                                */
/* This is the LogEventDBListener class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEventDBListener class Listens the LogEvents invoked and does the log it into the DB
/* - has abstract method HandleLogEvent which is overrided to Log stuff into a DB - exception DB
/* - isInteresting() is true if
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                  Birth of the class
// 03/27/2000 exception        send exception mail / changes to use dblistener in config process/take out the System.out.println.
// 10/20/2004 PTM3735          Getter methods to access PROGRAMVERSION/BUILDDATE.
/****************************************************************************************************/


public class LogEventDBListener extends LogEventListener implements Serializable {

    static final long serialVersionUID = 1111111111111111L;
    String dbServerAddress = null;
    int dbServerPort = -1;
    boolean sendExceptions=true;

    private  String sHostName = null;  // fixmail
    private  Vector sendtoVector = new Vector();
    private  Vector sendTestDatatoVector = new Vector();
    private  Vector sendRegistrationDatatoVector = new Vector(); //registration
    private  boolean isConfigProcess=false;

    public LogEventDBListener(String dbServerAddress,int dbServerPort,boolean isConfigProcess){
        this.dbServerAddress = dbServerAddress;
        this.dbServerPort = dbServerPort;
        this.isConfigProcess=isConfigProcess;

    }




    public synchronized void handleLogEvent(LogEvent l) throws IOException{
/* Fix this if / when we start using it again
        if (isInterestingEvent(l)) {
            sendInfoToDBServer(((LogExceptionEvent)l).getStackTrace(),l.getEventInfo());
        }
*/		
    }
	
    public boolean isInterestingEvent(LogEvent l){
/* Fix this if / when we start using it again
        if (l.getEventLevel()==LogEvent.LOGEXCEPTION_EVT_LEVEL) {
            boolean interesting =!(l.getExtraInformation() instanceof SyntaxError);
            return interesting;
        }
*/		
        return false;
    }
    public  void sendInfoToDBServer(String lstack,String exmessage) throws IOException{
/*
        if (lstack==null) {//return if stacktrace string is null. if proceeded would result in
            return;        //java.io.stringreader exception while trying to write to socket.
        }
        if (dbServerAddress==null) {
            InitMailToLists();
        }
        if (sendExceptions) {
            String formattedInfo = buildExceptionStringToSend(lstack,exmessage);
           if (dbServerAddress != null) {
                try {
                    Socket sock = new Socket(dbServerAddress,dbServerPort);
                    BufferedWriter output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
                    output.write(formattedInfo);
                    output.close();
                    sock.close();
                } catch (java.io.IOException ioe) {
                    MBUtilities.SendJustMail("!!!!Ooops...DBSERVER down...Exception not Written ..!!!!",lstack);
                   // MBClient.lep.LogException("!!!!Ooops...DBSERVER down...Exception not Written ..!!!!",ioe);
                }
            }
        }
*/				
    }

    private  String buildExceptionStringToSend(String  exceptionDump,String exmessage) throws IOException {
        final String build390Identifier = "Build390";
        final String fieldIdentifier = "PARSERFIELDNAME";
        final String fieldStart = "PARSERBEGINFIELD";
        final String fieldEnd = "PARSERENDFIELD";

        BufferedReader readString = new BufferedReader(new StringReader(exceptionDump));
        String currLine = null;
        boolean done = false;
        String localOccuranceLine = null;
        while ((currLine = readString.readLine()) != null & !done) {
            if (currLine.indexOf(build390Identifier) > -1) {
                localOccuranceLine = currLine;
                done = true;
            }
        }
        String fileLoc = null;
        String fileLine = null;
        if (localOccuranceLine != null) {
            int firstParen = localOccuranceLine.indexOf("(");
            int lastParen = localOccuranceLine.indexOf(")");
            if (firstParen > -1 & lastParen > -1) {
                localOccuranceLine = localOccuranceLine.substring(firstParen+1, lastParen);
                int separatorIndex = localOccuranceLine.indexOf(":");
                if (separatorIndex > -1) {
                    fileLoc = localOccuranceLine.substring(0,separatorIndex);
                    fileLine = localOccuranceLine.substring(separatorIndex +1, localOccuranceLine.length());
                }
            }
        }
        String stringToSend = new String();

        stringToSend += fieldIdentifier+ " " + "table_name\n";
        stringToSend += fieldStart + "\n";
        stringToSend += "exceptions" + "\n";
        stringToSend += fieldEnd + "\n";




        stringToSend += fieldIdentifier+ " " + "user_name\n";
        stringToSend += fieldStart + "\n";
       /*if (!isConfigProcess) {
            stringToSend += SetupManager.getSetupManager().getCurrentMainframeInfo().getMainframeUsername() + "\n";
        } else {
            stringToSend += System.getProperty("user.name") + "\n";
        }
        */
        stringToSend += fieldEnd + "\n";

        stringToSend += fieldIdentifier+ " " + "exception_type\n";
        stringToSend += fieldStart + "\n";
        stringToSend += exmessage + "\n";
        stringToSend += fieldEnd + "\n";

        if (fileLoc != null) {
            stringToSend += fieldIdentifier+ " " + "source_file\n";
            stringToSend += fieldStart + "\n";
            stringToSend += fileLoc + "\n";
            stringToSend += fieldEnd + "\n";
        }

        if (fileLine != null) {
            stringToSend += fieldIdentifier+ " " + "source_line_number\n";
            stringToSend += fieldStart + "\n";
            stringToSend += fileLine + "\n";
            stringToSend += fieldEnd + "\n";
        }

        stringToSend += fieldIdentifier+ " " + "stack_dump\n";
        stringToSend += fieldStart + "\n";
        stringToSend += exceptionDump + "\n";
        stringToSend += fieldEnd + "\n";

        stringToSend += fieldIdentifier+ " " + "client_version\n";
        stringToSend += fieldStart + "\n";
        stringToSend += MBConstants.getProgramVersion() + "\n";
        stringToSend += fieldEnd + "\n";

        stringToSend += fieldIdentifier+ " " + "client_build_date\n";
        stringToSend += fieldStart + "\n";
        stringToSend += MBConstants.getBuildDate() + "\n";
        stringToSend += fieldEnd + "\n";

        return stringToSend;
    }

    // Init the mail sendto lists //FixMail
    private  void InitMailToLists() {
        if (sendtoVector.isEmpty() & sendTestDatatoVector.isEmpty() & sendRegistrationDatatoVector.isEmpty()) {
            try {
                String stfn = new String(MBGlobals.Build390_path+"misc"+java.io.File.separator+"sendto.lst");
                String line = new String();
                File stf = new File(stfn);
                if (stf.exists()) {
                    BufferedReader resultReader = new BufferedReader(new FileReader(stfn));
                    while ((line = resultReader.readLine()) != null) {
                        if (line.toUpperCase().startsWith("REGISTER:")) {
                            sendRegistrationDatatoVector.addElement(line.substring(9));
                        } else if (line.toUpperCase().startsWith("TEST:")) {
                            sendTestDatatoVector.addElement(line.substring(5));  // add to test mail list
                        } else if (line.toUpperCase().startsWith("DATABASESERVER:")) {
                            dbServerAddress =  line.substring(15);    // add to test mail list
                        } else if (line.toUpperCase().startsWith("MAILSERVER:")) {
                            sHostName = line.substring(11);              // set mail server
                        } else if (line.toUpperCase().startsWith("EMAIL:")) {
                            sendtoVector.addElement(line.substring(6));  // add to normal mail list
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (sendtoVector.isEmpty()) {
            sendtoVector.addElement("none");
        }
        if (sendTestDatatoVector.isEmpty()) {
            sendTestDatatoVector.addElement("none");
        }
        if (sendRegistrationDatatoVector.isEmpty()) {
            sendRegistrationDatatoVector.addElement("none");
        }
    }

}
