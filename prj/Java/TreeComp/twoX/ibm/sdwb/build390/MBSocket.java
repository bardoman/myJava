package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java Socket Client for MVS Build server                           */
/*********************************************************************/
// Changes
// Date     Key                 Reason
// 01/10/2002 #Def.PTM1852 had to fix => StringIndexOutOfBoundsException
//03/29/2002 #Def.INT0754:       Add new verb trace option
//04/24/2003 #Def.AutomateRetries:   Add 10 sockect connect retrys with 30sec pause between
//08/13/2003 #TST1367:   trace popup not displayed when - "hold subserver job output is selected"
/*02/12/2004 INT1757 mixed-case support */
//09/17/04 #DEF.PTM3451: Build stops for no apparent reason at end of a phase.
/***********************************************************************/
import java.io.*;
import java.lang.Integer;
import java.lang.Runtime;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.security.PasswordManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

/** <br>The MBSocket class provides the facilities to communicate with the
* Build/390 server.
*/
public class MBSocket implements Serializable, MBStop, com.ibm.sdwb.build390.process.management.Haltable {

    private int    buflen_    = 4096;                                // command buffer length
    private final int  bufferSize = 4096;                                // command buffer length
    private ByteArrayOutputStream inputBuffer = null;
    private byte[] serverCmd_ = new byte[buflen_];                  // create command buffer
    private byte[] switches  = {0x03, 0x00};                        // server switches
    private boolean setClrout = true;
    private boolean sysout = false;
    private boolean delsysout = false;
    private boolean jobscancel = false;
    private boolean jobstatus = false;
    private boolean systsprt = true;                                // SYSTSPRT output - 5/15/98 PJ always use systsprt per kent
    private boolean tso      = false;
    private boolean scheduler = false;
    private boolean DontThrowBuildException = false;
    private boolean verbToFile = false;                             // 12/15/99 set when the verb info is to long
    private boolean savejobstatus = false;
    private boolean savedelsysout = false;
    private boolean savejobscancel = false;
    private int    TotalRecvd_= 0;                                  // define recvd count
    private int    clrprnt_   = 0;                                  // define num of clrprnts
    private int    clrout_    = 0;                                  // define num of clrouts
    private int    hostrc_    = 0;                                  // define host rc
    private int    lrecl_     = 0;                                  // define lrecl of ouput file
    private int    JobQueuePosition = 0;                            // position of subserver job on job queue
    private final byte dsysoutAndSysout = (byte)0x80 | 0x40;        // constant meaning delSysOut(0x80) and sysout set
    private boolean gui_        = false;                            // GUI flag
    private boolean stopped = false;
    private MBMainframeInfo mainInfo;                                         // setup object
    private MBMsgBox mbx;                                           // msg box for user info
    private MBStatus stat;                                          // status output object
    private String clrprintfn;                                      // clrprt file name
    private String outFile_;                                        // output file name
    private String filePrefix;
    private String command;                                           // server command
    private String verbPath = new String();
    private String outFileContents = new String();
    private String printFileContents = new String();
    private Socket s;                                               // socket object
    private DataOutputStream d;                                     // output stream to server
    private String statmsg_;
    private InetAddress server;
    private boolean doingCompatiblityCheck = false;
    private boolean cancellableHostCall = true;
    private static Hashtable clientServerCompatibilityHash = new Hashtable();
    private static String jobposition = new String(" (Job Position ");
    // Password Masking: create pw mask
    private static byte[] pwmask = {(byte)0x22,(byte)0x89,(byte)0x0F,(byte)0xE7,(byte)0x83,(byte)0x04,(byte)0x40,(byte)0x18,(byte)0x28,(byte)0xCD,(byte)0xEB,(byte)0xFF,(byte)0x00,(byte)0x3A,(byte)0xD9,(byte)0xE6};

    private LogEventProcessor lep=null;
    private MBSocket mySock;
    private MBFtp mftp;

    private String lfn;//PTM4037

    /** This constructor acccepts a flag that indicates that build exceptions should not be thrown.
       * @param cmdx A string containing the command to be sent.
       * @param outFile A string containing the path to the clrout output file.
       * @param clrprtfn A string containing the path to the clrprint output file.
       * @param caller Object that started the socket thread
       * @param statmsg A string containing the status msg
       * @param DontThrowBuildException boolean, if true, dont throw exceptions
       * @param isLogObjPassed is true if build object is passed*/

    public MBSocket(String cmdx, String outFile, String statmsg, MBStatus status, MBMainframeInfo tempMainInfo, LogEventProcessor lep)
    {
        this.lep = lep;
        mainInfo    = tempMainInfo;
        init(cmdx, outFile, statmsg, status);
    }

    /** Sets up data for the new thread.
    * @param cmdx A string containing the command to be sent.
    * @param outFile A string containing the path to the clrout output file.
    * @param clrprtfn A string containing the path to the clrprint output file.
    * @param caller Object that started the socket thread
    * @param statmsg A string containing the status msg */
    public MBSocket(String cmdx, String outFile, String statmsg, MBStatus status, MBMainframeInfo tempMainInfo, boolean tempCompatibleCheck, LogEventProcessor lep)
    {
        this.lep = lep;
        mainInfo    = tempMainInfo;
        doingCompatiblityCheck = tempCompatibleCheck;
        DontThrowBuildException = true;
        init(cmdx, outFile, statmsg, status);
    }

    /** This constructor acccepts a flag that indicates that build exceptions should not be thrown.
    * @param cmdx A string containing the command to be sent.
    * @param outFile A string containing the path to the clrout output file.
    * @param clrprtfn A string containing the path to the clrprint output file.
    * @param caller Object that started the socket thread
    * @param statmsg A string containing the status msg
    * @param DontThrowBuildException boolean, if true, dont throw exceptions
    * @param isLogObjPassed is true if build object is passed*/
    public MBSocket(String cmdx, String outFile, String statmsg, boolean DontThrowBuildException_, MBStatus status, MBMainframeInfo tempMainInfo, LogEventProcessor lep)
    {
        this.lep=lep;
        DontThrowBuildException = DontThrowBuildException_;
        mainInfo    = tempMainInfo;
        init(cmdx, outFile, statmsg, status);
    }

    public void dontAllowHostCallCancel()
    {
        cancellableHostCall = false;
    }

    private void init(String cmdx, String outFile, String statmsg, MBStatus status)
    {
        filePrefix = outFile;
        clrprintfn = outFile+MBConstants.PRINTFILEEXTENTION;
        outFile_   = outFile+MBConstants.CLEARFILEEXTENTION;
        command    = cmdx;
        statmsg_   = statmsg;

        File temp1 = new File(outFile_);
        if (temp1.exists()) {
            if (!temp1.delete()) {
                lep.LogPrimaryInfo("INFORMATION:","MBSocket was unable to delete "+outFile_,true);

            }
        }
        File temp2 = new File(clrprintfn);
        if (temp2.exists()) {
            if (!temp2.delete()) {
                lep.LogPrimaryInfo("INFORMATION:","MBSocket was unable to delete "+clrprintfn,true);
            }
        }

        if (MainInterface.getInterfaceSingleton() != null) gui_ = true;

        // get status area object
        stat = status;
        try {
            server = InetAddress.getByName(mainInfo.getMainframeAddress());
        } catch (UnknownHostException uhe) {
            lep.LogException("The host "+mainInfo.getMainframeAddress() + " could not be found", uhe);

        }
    }

    /** Set CLROUT setting.
    * This method sets bit 2 in server switch 0.*/
    public void unset_clrout()
    {
        setClrout = false;
    }

    /** Set SYSOUT setting.
    * This method sets bit 7 in server switch 0. */
    public void setSysout()
    {
        sysout = true;
    }

    /** Set DELSYSOUT setting.
    * This method sets bit 8 in server switch 0.*/
    public void setDelsysout()
    {
        delsysout = true;
    }

    /** Set scheduler setting.
    * This method sets bit  in server switch 1.*/
    public void setScheduler()
    {
        scheduler = true;
    }

    /** Set JobsCancel setting.
    * This method sets bit 4 in server switch 0.*/
    public void setJobsCancel()
    {
        jobscancel = true;
    }

    /** Set JOBSTATUS setting.
    * This method sets bit 3 in server switch 0.*/
    public void setJobstatus()
    {
        jobstatus = true;
    }

    /** Set SYSTSPRT setting.
    * This method sets bit 6 in server switch 2.*/
    public void setSystsprt()
    {
        systsprt = true;
    }

    /** Set TSO setting.
    * This method sets bit 3 in server switch 2.*/
    public void setTSO()
    {
        tso = true;
    }

    /** Set CLRTSRC for the server to find the buildid file you're calling
    * @ param pathToFile */
    public void setPathToVerbFile(String tempVerbPath)
    {
        verbPath = tempVerbPath;
    }

    /** Sends the command to the server in a new thread and manages getting the response,
    * then calls the return method so that the results get processed.
    * Also manages the various return codes from the server.
    */
    public void run() throws com.ibm.sdwb.build390.MBBuildException {
        stopped = false;
        lep.LogSecondaryInfo("Debug","MBSocket:Entry");
        // Connect and communicate with server
        // Nutsy1 - dont check compat if nutsy
        if (!doingCompatiblityCheck & !command.startsWith("NUTSY")) {
            Vector clientCompatibilityVector = null;
            synchronized (clientServerCompatibilityHash)
            {
                clientCompatibilityVector = (Vector) clientServerCompatibilityHash.get(mainInfo.getMainframeAddress()+mainInfo.getMainframePort());
                if (clientCompatibilityVector == null) {
                    clientCompatibilityVector = new Vector();
                    clientServerCompatibilityHash.put(mainInfo.getMainframeAddress()+mainInfo.getMainframePort(), clientCompatibilityVector);
                }
            }
            synchronized (clientCompatibilityVector)
            {
                if (clientCompatibilityVector.size() == 0) {
                    String cmd_ = "CLIENT VERSION="+MBConstants.getProgramVersion();
                    String clrout_ = MBGlobals.Build390_path+"misc" + java.io.File.separator + "clientCompCheck";
                    String clrprn = MBGlobals.Build390_path+"misc" + java.io.File.separator + "clientCompCheck"+MBConstants.PRINTFILEEXTENTION;

                    // Submit the command
                    mySock = new MBSocket(cmd_, clrout_, "Checking client compatibility", stat, mainInfo, true, lep);
                    mySock.run();
                    int results = mySock.getHostReturnCode();
                    boolean isClientCompatible = true;
                    String returnText = new String();
                    // if rc != 0 let user know
                    if (results > 0 & !stopped) {
                        try {
                            File cfile = new File(clrprn);
                            if (cfile.exists()) {
                                String cline = new String();
                                BufferedReader cReader = new BufferedReader(new FileReader(clrprn));
                                while (((cline = cReader.readLine()) != null) & !stopped) {
                                    if (cline.indexOf("Minimum client for") > -1  |
                                        cline.indexOf("Recommended client") > -1  |
                                        cline.indexOf("Client version") > -1) {
                                        returnText += cline.substring(2).trim()+"\n";
                                    }
                                }
                                cReader.close();
                            }
                        } catch (FileNotFoundException fnfe) {
                            MBEdit edit = new MBEdit(clrprn,lep);
                        } catch (IOException ioe) {
                            MBEdit edit = new MBEdit(clrprn,lep);
                        }
                        if (results >= 8 & !stopped) {
                            isClientCompatible = false;
                            if (returnText.trim().length() < 1) {
                                returnText = "There was an error determining the compatibility of the client & server.  A server error occurred";
                            }
                        } else if (!returnText.equals("")) {
                            MBMsgBox mb = new MBMsgBox("Warning", returnText);
                        }
                    }
                    mySock = null;
                    clientCompatibilityVector.addElement(new Boolean(isClientCompatible));
                    clientCompatibilityVector.addElement(returnText);
                    clientServerCompatibilityHash.put(mainInfo.getMainframeAddress()+mainInfo.getMainframePort(), clientCompatibilityVector);
                }
            }
            Boolean isCompatible = (Boolean) clientCompatibilityVector.elementAt(0);
            if (!isCompatible.booleanValue()) {
                String returnTextStored = (String) clientCompatibilityVector.elementAt(1); /** TST1450 **/
                clientCompatibilityVector.removeAllElements(); /** TST1450 **/
                throw new HostError(returnTextStored, MBGlobals.Build390_path+"misc" + java.io.File.separator + "clientCompCheck");
            }
        }

        // Jerk, you need to append a comma if there are any keywords in the verb
        // ie. $ VERBNOKEYWORDS TRACE=TRACE, or $ VERBWITHKEYWORDS kw1=jerk,TRACE=TRACE
        // do not trace if command=nutsy or client-version or if
        // if you change this code, also change the code below 'ShowServerCmd(serverCmd_, command)'

        DebugAndTracePopup tracePopup = new DebugAndTracePopup();
        tracePopup.show();

        // show status
        stat.updateStatus(statmsg_, false);
        BufferedInputStream i = null;

        // send command to server
        MBThreadLimit sockLimit;
        synchronized(MBClient.lockCache)
        {
            sockLimit = (MBThreadLimit) MBClient.lockCache.get(mainInfo.getMainframeAddress()+mainInfo.getMainframePort());
            if (sockLimit == null) {
                sockLimit = new MBThreadLimit(MBConstants.MAINFRAMETHREADLIMIT,lep);
                MBClient.lockCache.put(mainInfo.getMainframeAddress()+mainInfo.getMainframePort(), sockLimit);
            }
        }
        sockLimit.waitCounter();
        try {
            //Begin #Def.AutomateRetries:
            s   = null;
            for (int socketRetryCount = 0; socketRetryCount < 10 & s==null & !stopped; socketRetryCount++) {
                try {
                    s = new Socket(mainInfo.getMainframeAddress(), Integer.parseInt(mainInfo.getMainframePort()));

                    //#DEF.PTM3451:
                    s.setSoTimeout(180000);//Set timeout for 3 mins

                } catch (SocketException socketException) {
                    lep.LogPrimaryInfo("MBSocket connect error","Got error, retrying socket " + socketException.toString(), false);


                    s=null;
                    try {
                        int secCnt = 30;

                        for (int n=0;n!=secCnt;n++) {
                            stat.updateStatus("Connection Error! Retrying in " +(secCnt-n) +" secs... , Count = "+ socketRetryCount, false);
                            Thread.currentThread().sleep(1000);//1 sec

                            if (stopped) {
                                throw new IOException();
                            }
                        }
                    } catch (InterruptedException ie) {
                        /// screw it
                    }
                }
            }

            if (s==null) {
                throw new IOException();
            }
            //End #Def.AutomateRetries:
            d = new DataOutputStream(s.getOutputStream());
            d.write(serverCmd_, 0, tracePopup.getServerCommandLength());

            // Server read loop
            // If partial data recieved, loop until all read
            // If you get a time out, send a resume msg back to the server
            i = new BufferedInputStream(s.getInputStream());
            int num_read  = 0;
            int TotalData = 0;
            byte [] tempBuffer = new byte[bufferSize];
            String cin = new String();
            inputBuffer = new ByteArrayOutputStream(bufferSize);
            clrprnt_ = 0;
            clrout_ = 0;
            do {
                // pjs, 11/18 loop until at least the header has been read or you get a bounds exception
                num_read = 0;
                int readCounter = 0;
                while (num_read < 6 & readCounter++ < 10) {
                    try {
                        num_read += i.read(tempBuffer, num_read, bufferSize - num_read);
                    } catch (SocketException sei) {
                        /**  sei.printStackTrace(); **/
                    }
                }
                if (!stopped) {
                    try {
                        inputBuffer.write(tempBuffer, 6, num_read-6);
                    } catch (Exception aobe) {
                        lep.LogPrimaryInfo("INFORMATION:","ReadCounter: " + readCounter + "  Num_Read: " + num_read + "  tempBufferSize: " + tempBuffer.length,false);
                        throw new GeneralError("An incorrect number of bytes was received from the host.\nDebug Info is  ReadCounter: " + readCounter + "  Num_Read: " + num_read + "  tempBufferSize: " + tempBuffer.length, aobe);
                    }

                    /**************************************************************
                     * 3/22/98, Chris, SYSTSPRT is part of the clrprnt file if the
                     * systsprt is on and starts with the Ready in the first column.
                     * Get the contents of SYSTSPRT data from the clrprnt output file.
                     **************************************************************/

                    // if more data is needed, read it
                    int tempClrprnt_ = ByteArraytoInt(tempBuffer, 2, 2);
                    int tempClrout_  = 0;
                    clrprnt_ += tempClrprnt_;
                    lrecl_=121;
                    if ((switches[0] & dsysoutAndSysout)==dsysoutAndSysout) {
                        tempClrout_ = 0;
                        lrecl_      = ByteArraytoInt(tempBuffer, 4, 2);
                    } else tempClrout_ = ByteArraytoInt(tempBuffer, 4, 2);
                    hostrc_   = ByteArraytoInt(tempBuffer, 0, 2);

                    // if we got a time out, clrout_ contains the job queue position of the subserver job
                    JobQueuePosition = 0;
                    if (hostrc_ == -1) {
                        JobQueuePosition = tempClrout_;
                        TotalData = 6 + (tempClrprnt_ * lrecl_);
                    } else {
                        clrout_ += tempClrout_;
                        TotalData = 6 + (tempClrprnt_ * lrecl_) + (tempClrout_ * 80);
                    }
                    TotalRecvd_ = num_read;
                }
                // loop until all data received
                while ((TotalData > TotalRecvd_)&(!stopped)) {
                    num_read = i.read(tempBuffer);
                    if (num_read == -1) {
                        TotalRecvd_ = TotalData;
                    } else {
                        inputBuffer.write(tempBuffer, 0, num_read);
                        TotalRecvd_ +=num_read;
                    }
                }
                // check return code
                CheckServerRc(d);
            } while (!stopped & (hostrc_ == -1 | hostrc_ == -2));
        }

        //Begin #DEF.PTM3451:
        catch (java.io.InterruptedIOException iioe) {
            if (!stopped) {
                throw new HostError("The socket connection to the host "+mainInfo.getMainframeAddress()+" timed out", iioe);
            }
        }
        //End #DEF.PTM3451:


        catch (UnknownHostException uhe) {
            if (!stopped) {
                throw new HostError("The host "+mainInfo.getMainframeAddress()+" was not found", uhe);
            }
        } catch (IOException ioe) {
            if (!stopped) {
                //#Def.AutomateRetries:
                stat.updateStatus("Connection Error to "+mainInfo.getMainframeAddress(), false);

                throw new HostError("There was an error creating a socket connection to "+mainInfo.getMainframeAddress(), ioe);
            }
        } finally {
            sockLimit.notifyCounter();
            try {
                // close stuff and get out
                if (i != null) {
                    i.close();
                }

            } catch (IOException ioe) {
            }
            try {
                // close stuff and get out
                if (d != null) {
                    d.close();
                }
            } catch (IOException ioe) {
            }
            try {
                // close stuff and get out
                if (s != null) {
                    s.close();
                }
            } catch (IOException ioe) {
            }
        }
        if (stopped) {
            throw new StopError();
        }


        // get rid of status msg
        stat.clearStatus();

        // parse response and show user
        WritetoFile(outFile_);
        String sb = new String();
        sb += "Host RC = "+hostrc_+", ";
        // 05/29/00 pjs show rc in hex
        sb += "x\'" + intToHex(hostrc_) + "\'\n";
        sb += "# of Clrprint records = "+clrprnt_+"\n";
        sb += "# of Clrout records   = "+clrout_+"\n";
        lep.LogPrimaryInfo("Debug",sb.toString(),true);

        // if trace, show user trace data
        if ((com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly() | com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData()) & !stopped) {
            // don't show trace for nutsy and client version commands as well as server function requests
            if (command.indexOf("NUTSY") == -1 & command.indexOf("CLIENT VERSION") == -1 & !tso & !jobstatus & !sysout & !delsysout & !jobscancel & !jobstatus) {
                MBEdit edit = new MBEdit(clrprintfn,lep);
                // if gui mode and this is not a nutsy or client version check, turn off trace
                if (gui_) {
                    com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbOnly(false);
                    com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().setTraceVerbAndData(false);
                    MainInterface.getInterfaceSingleton().ResetTrace();
                }
            }
        }

        // AddHoldTrace
        verbPath = new String();

        // if rc != 0 let user know
        if (hostrc_ != 0 & !stopped) {
            // If a 546, process was canceled
            if (hostrc_ == 546) stat.updateStatus("Process canceled", false);

            else if (hostrc_ == 290) {
                PasswordManager.getManager().setPassword(mainInfo.getMainframeUsername()+"@"+mainInfo.getMainframeAddress(), ""); // If a 290, then reset the password
                // ShowFlushData - instead of showing the old msg, show what nutsy sends in the output file
                String emsg = new String(MBConstants.NEWLINE);
                String currentLine;
                boolean inValidPassword=false;
                try {
                    BufferedReader resultReader = new BufferedReader(new FileReader(clrprintfn));
                    while (((currentLine = resultReader.readLine()) != null) & !stopped) {
                        emsg = emsg + currentLine + "\n";
                        if (emsg.indexOf("CHECK USERID/PASSWORD")>-1) {
                            inValidPassword=true;
                        }
                    }
                } catch (IOException ioe) {
                    emsg = "There were errors reading "+clrprintfn;
                }
                if (inValidPassword) {
                    throw new PasswordError(emsg);
                } else {
                    throw new HostError(emsg, filePrefix);
                }
            } else if (hostrc_ == 913) { // open data set error, porbably racf failure //Error_913
                throw new HostError("Received RC="+hostrc_+", x\'"+intToHex(hostrc_)+"\' from the server which is most likely a Racf failure."+MBConstants.NEWLINE+MBConstants.NEWLINE+"Start the client with the /hold option and then review the held output on the MVS host for more information.", filePrefix);
            }

            // Otherwise tell the user and ask if he wants to see the results
            // Dont not throw nuthin unless I says not to
            else if (!DontThrowBuildException) {
                // If a 4002, show detail info
                if (hostrc_ == 4002) {
                    String currentLine;
                    String emsg = new String("Received RC="+hostrc_+", x\'"+intToHex(hostrc_)+"\' from the server with the following information\n\n");
                    try {
                        BufferedReader resultReader = new BufferedReader(new FileReader(clrprintfn));
                        while (((currentLine = resultReader.readLine()) != null) & !stopped) {
                            emsg = emsg + currentLine + "\n";
                        }
                    } catch (IOException ioe) {
                        emsg = "Error reading file "+clrprintfn;
                    }
                    emsg = emsg + "\nThe host server must have lost contact with the subserver.\n\nContact your administrator.";
                    MBMsgBox viewQuestion = new MBMsgBox("Error", emsg);
                } else {
                    if (gui_) {
                        throw new HostError("Bad return code from host: Received RC="+hostrc_+", x\'"+intToHex(hostrc_)+"\' from the server", filePrefix);
                    } else {
                        String newFilename = null;
                        Random randNum = new Random();
                        for (int i2 = 0; newFilename == null & i2 < 100; i2++) {
                            String testFilename = MBGlobals.Build390_path+"misc"+File.separator+"hosterr"+randNum.nextInt();
                            File testFile = new File(testFilename);
                            if (!testFile.exists()) {
                                newFilename = testFilename;
                            }
                        }
                        if (newFilename == null) {
                            newFilename = MBGlobals.Build390_path+"misc"+File.separator+"hosterr";
                        }
                        (new MBJavaFile(lep)).copy(clrprintfn, newFilename+MBConstants.PRINTFILEEXTENTION, true);
                        (new MBJavaFile(lep)).copy(outFile_, newFilename+MBConstants.CLEARFILEEXTENTION, true);
                        throw new HostError("Bad return code from host: Received RC="+hostrc_+", x\'"+intToHex(hostrc_)+"\' from the server.",newFilename);
                    }
                }
            }
        }
    }

    /** This method returns the return code received from the mainframe. */
    public int getHostReturnCode()
    {
        return hostrc_;
    }

    /** Stop the server action being performed.
    * Sends a CANCEL message to the server.
    */
    public void stop() throws com.ibm.sdwb.build390.MBBuildException{
        if (!stopped) {

            stopped = true;

            if (mySock != null) {
                mySock.stop();
            } else {
                stopped = true;
                lep.LogSecondaryInfo("Debug:","MBSocket:stop:Entry");
                if (cancellableHostCall) {
                    stat.updateStatus("Canceling server request", false);

                    lep.LogPrimaryInfo("INFORMATION:","Canceling server request ["+statmsg_+"] in progress...",false);
                    serverCmd_[0] = 0x00;
                    serverCmd_[1] = 0x04;
                    serverCmd_[2] = 0x11;
                    serverCmd_[3] = 0x00;
                    try {
                        d.write(serverCmd_, 0, 4);
                        d.close();
                        s.close();

                        lep.LogPrimaryInfo("INFORMATION:","Cancel server request ["+statmsg_+"] complete.",false);
                        // ignore the nullptr and socket execetions, they can occur due to timing
                    } catch (Exception e) {
                        lep.LogPrimaryInfo("INFORMATION:","Cancel server request ["+statmsg_+"] exception ignored.",false);
                    }
                }
            }
        }
    }

    public boolean isHaltable()
    {
        return true;
    }

    public void haltProcess() throws com.ibm.sdwb.build390.MBBuildException{
        stop();
    }

    /** Convert part of a byte array to an integer.
    * @param src_array byte array containing the bytes to be converted.
    * @param offset int containing the offset within the src_array to start the conversion from.
    * @param len int containing the length within the src_array to convert.
    * @return int value of contents of src_array.
    */
    public int ByteArraytoInt(byte[] src_array, int offset, int len)
    {
        byte[] ba = new byte[len];
        System.arraycopy(src_array, offset, ba, 0, len);
        BigInteger bi = new BigInteger(ba);
        return(bi.intValue());
    }


    /** Process return codes from the server.
    * @param fn String containing the output file path.
    * @param d Output stream to write output to.
    */
    public void CheckServerRc(DataOutputStream d) throws com.ibm.sdwb.build390.MBBuildException{
        lep.LogSecondaryInfo( "Debug","MBSocket:CheckServerRc:Entry");
        try {
            // check for data overload
            if (hostrc_ == -2) {
                stat.updateStatus("-", true);
                serverCmd_[0] = 0x00;
                serverCmd_[1] = 0x04;
                serverCmd_[2] = 0x20;
                serverCmd_[3] = 0x00;
                if(!stopped){
                    d.write(serverCmd_, 0, 4);
                }
            }

            // check for time out
            if (hostrc_ == -1) {
                // get current status msg
                String cstatus = stat.getStatus();
                if (!cstatus.equals(null)) {
                    // get index of previous job position msg within current status msg
                    int jp = cstatus.indexOf(jobposition);
                    String newstatus = new String();
                    // strip previous job postion msg from status
                    if (jp > -1) {
                        newstatus = cstatus.substring(0,jp);
                    } else newstatus = cstatus;
                    // append to status msg, either a timeout indicator or the new job position
                    if (JobQueuePosition < 1) {
                        newstatus+="*";
                    } else {
                        // if we found any *, strip them
                        int ap = newstatus.lastIndexOf("*");
                        if (ap > -1) {
                            newstatus = newstatus.substring(0,ap);
                        }
                        newstatus+=jobposition+JobQueuePosition+")";
                    }
                    stat.updateStatus(newstatus, false);
                }
                serverCmd_[0] = 0x00;
                serverCmd_[1] = 0x04;
                serverCmd_[2] = 0x20;
                serverCmd_[3] = 0x00;
                if(!stopped){
                    d.write(serverCmd_, 0, 4);
                }

            }
        }

        //Begin #DEF.PTM3451:
        catch (java.io.InterruptedIOException iioe) {
            if (!stopped) {
                throw new HostError("The socket connection to the host "+mainInfo.getMainframeAddress()+" timed out", iioe);
            }
        }
        //End #DEF.PTM3451:


        catch (IOException ioe) {
            stat.clearStatus();
            throw new GeneralError("There was an error creating the command to send to the server", ioe);
        }
    }


    /** Append a string to a byte array.
    * @param str A string containing the data to be appended.
    * @param bytearray byte array to append string data to.
    * @param startidx Point within bytearray to start appending data at.
    * @returns pointer to new byte array
    * Defect_211 */
    public byte[] AppendStringtoByteArray(String str, byte[] bytearray, int startidx)
    {
        // if the array is not large enough, increase it
        int slen = str.length() + startidx;
        if (slen > bytearray.length) {
            byte[] newbuf = new byte[slen + startidx + buflen_ + 10];
            System.arraycopy(bytearray, 0, newbuf, 0, bytearray.length);
            bytearray = newbuf;
        }
        // append the data
        byte[] temp = new byte[str.length()];
        temp = str.getBytes();
        for (int x=0; x<str.length(); x++) {
            bytearray[startidx+x] = temp[x];
        }
        return bytearray; // return new ptr to array
    }


    /** Display command to be sent to server.
    @param buf byte array containing the server command.
    @param command A string containing the verb portion of the command.
    */
    public void ShowServerCmd(byte[] buf, String command)
    {
        lep.LogSecondaryInfo("Debug","MBSocket:ShowServerCmd:Entry");
        int lng = ByteArraytoInt(buf, 0, 2);
        String sb = new String();

        // 09/14/99, pjs, DontShowUserid or password in debug
        sb+="\nServer IP/Port   = "+mainInfo.getMainframeAddress()+", "+mainInfo.getMainframePort()+"\n";

        int acct_leng = ByteArraytoInt(buf, 22, 1);
        String acct = new String(buf, 23, acct_leng);
        sb += "Acct Info/Length = "+acct.trim()+", "+acct_leng+"\n";

        int clrtsrc_len = ByteArraytoInt(buf, 23+acct.length(), 1);
        String clrtsrc = new String(buf, 23+acct.length()+1, clrtsrc_len);
        if (clrtsrc.trim().length()>0) {
            sb += "CLRTSRC        = "+clrtsrc.trim()+"\n";
        }

        String hx = byteToHex(switches[0]);
        sb += "Switch 0         = x\'"+hx.toUpperCase()+"\'";
        if ((switches[0] & 0x01)>0) sb += ", AsciiClient";
        if ((switches[0] & 0x02)>0) sb += ", ReturnClrout";
        if ((switches[0] & 0x04)>0) sb += ", QueryStatus";
        if ((switches[0] & 0x08)>0) sb += ", CancelJobs";
        if ((switches[0] & 0x10)>0) sb += ", Cancel";
        if ((switches[0] & 0x20)>0) sb += ", Overflow/Timeout";
        if ((switches[0] & 0x40)>0) sb += ", ReturnSysout";
        if ((switches[0] & 0x80)>0) sb += ", PurgeSysout";
        sb += "\n";

        String hx1 = byteToHex(switches[1]);
        sb += "Switch 1         = x\'"+hx1.toUpperCase()+"\'";
        if ((switches[1] & 0x01)>0) sb += ", DSN";
        if ((switches[1] & 0x02)>0) sb += ", GetSystsprt";
        if ((switches[1] & 0x04)>0) sb += ", HoldOutput";
        if ((switches[1] & 0x08)>0) sb += ", SchedulerRequest";
        if ((switches[1] & 0x10)>0) sb += ", TSOCommand";
        //if ((switches[1] & 0x20)>0) sb += ", Unused";
        if ((switches[1] & 0x40)>0) sb += ", VerbInClrtsrc";
        if ((switches[1] & 0x80)>0) sb += ", MaskUidPW";
        sb += "\n";

        int clr_leng = buf[23+acct_leng];

        lng = 23+acct_leng+clr_leng+1;

        String verb=null;

        if ((command.length()+lng) > buf.length) {
            byte tmpBuf[] = new byte[command.length()+lng];

            System.arraycopy(buf,0,tmpBuf,0,buf.length);

            verb = new String(tmpBuf, lng, command.length());
        } else {
            verb = new String(buf, lng, command.length());
        }

        sb += "Verb             = "+verb.trim()+"\n";

        // 06/02/00 pjs - add output file names to debug info
        if (outFile_ != null & clrprintfn != null) {
            sb += "Output Files     = "+outFile_+" and "+clrprintfn+"\n";
        }

        lep.LogPrimaryInfo("Debug",sb,true);
    }


    /** Write data to clrprint and clrout output files.
    * @param fn File name to write to
    */
    public void WritetoFile(String fn) throws com.ibm.sdwb.build390.MBBuildException{
        lep.LogSecondaryInfo("Debug","MBSocket:WritetoFile:Entry");
        // create output files
        int idx, idx1;
        byte [] tempBuffer = inputBuffer.toByteArray();
        BufferedWriter fout = null;
        BufferedWriter fprt = null;
        try {
            // make sure the directory to write to exists
            File outputFile = new File(outFile_);
            File printFile = new File(clrprintfn);
            if (outputFile.getParentFile()!=null) {
                outputFile.getParentFile().mkdirs();
            }
            if (printFile.getParentFile()!=null) {
                printFile.getParentFile().mkdirs();
            }
            fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile_)));
            fprt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clrprintfn)));
            outFileContents = new String();
            printFileContents = new String();
            StringWriter primaryOutStringWriter = new StringWriter();
            StringWriter primaryPrintStringWriter = new StringWriter();
            BufferedWriter outStringWriter = new BufferedWriter(primaryOutStringWriter);
            BufferedWriter printStringWriter = new BufferedWriter(primaryPrintStringWriter);
            // if getting sysout, save clrprnt in clrout
            if ((switches[0] & dsysoutAndSysout)==dsysoutAndSysout) {
                fprt = fout;
                outFileContents = printFileContents;
                clrout_ = 0;
            }

            for (idx=0; idx<clrprnt_; idx++) {
                printStringWriter.write(new String(tempBuffer, (idx*lrecl_), lrecl_));
                printStringWriter.newLine();
            }
            for (idx1=0; idx1<clrout_; idx1++) {
                outStringWriter.write(new String(tempBuffer, (idx*lrecl_)+(idx1*80), 80));
                outStringWriter.newLine();
            }
            printStringWriter.close();
            outStringWriter.close();
            printFileContents = primaryPrintStringWriter.toString();
            outFileContents = primaryOutStringWriter.toString();
            fout.write(outFileContents.toString());
            fprt.write(printFileContents.toString());
            fout.close();
            fprt.close();
        } catch (IOException ioe) {
            stat.clearStatus();
            try {
                fout.close();
                fprt.close();
            } catch (Exception ioe2) {
            }
            throw new GeneralError("Errors occurred writing the server responses to files", ioe);
        }
    }

    public String getClearOut()
    {
        return outFileContents;
    }

    public String getClearPrint()
    {
        return printFileContents;
    }

    /** Build server command.
    * @param buf byte array to contain command.
    * @param command A string containing the verb portion of the command.
    * @param switches A byte array containing the switch settings for the command.
    * @param setup The setup object that contains the userid etc.
    */
    public int BuildServerCmd(byte[] buf, String command, byte[] switches, MBMainframeInfo mainInfo) throws com.ibm.sdwb.build390.MBBuildException{
        //MBUtilities.Logit(MBConstants.DEBUG_DEV, "MBSocket:BuildServerCmd:Entry", "Debug");
        //01/07/2000 buildlog changes
        //  build.Logit(MBConstants.DEBUG_DEV, "MBSocket:BuildServerCmd:Entry", "Debug");
        lep.LogSecondaryInfo("Debug", "MBSocket:BuildServerCmd:Entry");

        // switches[0] - first byte of client switch at offset +2
        // switches[1] - second byte of client switch at offset +3
        if (MBClient.isUSSClient()) {
            switches[0] = 0x02;
        }
        if (tso)  // systsprt output
            switches[1] = (byte) (switches[1] | 0x10);
        if (systsprt)  // systsprt output
            switches[1] = (byte) (switches[1] | 0x02);
        if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput()) switches[1] = (byte)(switches[1] | 0x04);
        else     switches[1] = (byte)(switches[1] & 0xFB);
        if (scheduler)  // scheduler request
            switches[1] = (byte) (switches[1] | 0x08);

        if (sysout) {
            switches[0] = (byte) (switches[0] | 0x40);
            sysout = false;
        }
        savejobstatus = false;
        if (jobstatus) {
            switches[0] = (byte) (switches[0] | 0x04);
            jobstatus = false;
            savejobstatus = true;
        }
        savedelsysout = false;
        if (delsysout) {
            switches[0] = (byte) (switches[0] | 0x80);
            delsysout = false;
            savedelsysout = true;
        }
        savejobscancel = false;
        if (jobscancel) {
            switches[0] = (byte) (switches[0] | 0x08);
            jobscancel = false;
            savejobscancel = true;
        }
        if (!setClrout) {
            switches[0] = (byte) (switches[0] & 0xFD);
            setClrout = true;
        }

        // Password Masking: turn on the masking switch
        // If the second switch byte has x'80' on, then that means
        // the message has the userid/password encrypted.
        switches[1] = (byte) (switches[1] | 0x80);

        buf[0] = 0x00; buf[2] = switches[0]; buf[3] = switches[1];
        buf[4] = 0x00;
        buf[5] = 0x3C; // 60 seconds = 3C

        // userid
        String uid = new String(mainInfo.getMainframeUsername());
        int temp = 0;
        // Password Masking: don't append the uid yet, just pad it to 8 chars with blanks
        while (uid.length() < 8) {
            uid += " ";
        }

        // password
        String pw = PasswordManager.getManager().getPassword(mainInfo.getMainframeUsername()+"@"+mainInfo.getMainframeAddress(),false);/* INT1757 mixed-case support */
        // Password Masking: pad the pw to 8 chars with blanks
        while (pw.length() < 8) {
            pw += " ";
        }
        // Password Masking:
        // If the second switch byte has x'80' on, then the userid/password is encrypted,
        // To encrypt, take the binary value of each byte in the 16-byte Userid/Password string, and
        // ADD the corresponding value from the 16-byte MASK, disregarding any carry. The
        // resultant 16-byte string is the encrypted userid/password.
        // MASK     DC    XL16'22890FE78304401828CDEBFF003AD9E6'
        String uidpw = uid+pw;          // append the pw to the uid to from 16 byte string
        byte[] ptemp = new byte[16];
        ptemp = uidpw.getBytes();       // convert string to byte array
        for (int x=0; x<16; x++) {
            buf[x+6] = (byte)(ptemp[x] + pwmask[x]);
        }

        stat.clearStatus();
        // acct info
        String acct = new String(mainInfo.getMainframeAccountInfo());
        buf[22] = (byte)acct.length();
        buf = AppendStringtoByteArray(acct, buf, 23); // Defect_211
        buf[22+acct.length()+1] = 0x00;

        // 12/15/99 if the verb info is to long,
        // save it to a file, ftp it to the host,
        // set the CLRTSRC to the dsn for the new file,
        // set the verbToFile switch,
        // and rebuild the command buffer
        temp = 23+acct.length()+35+command.length()+1;
        if (temp > 65000 & (savejobstatus | savedelsysout | savejobscancel)) {  // check for > 65000
            // create local file
            File vfile = null;
            String vfn = null;
            Random randomSource = new Random();
            String MVSVerbFile = null;
            try {
                //Begin INT2208
                //if file exists with ".verb" signature then clean it up
                File miscDir = new File(MBGlobals.Build390_path+"misc");

                File filesInMisc[] = miscDir.listFiles();

                String fileName="";

                for (int i=0;i!=filesInMisc.length;i++) {
                    fileName = filesInMisc[i].getName();

                    if (fileName.indexOf(".verb")!=-1) {
                        filesInMisc[i].delete();
                    }
                }
                //End INT2208

                do {
                    lfn = new String("V" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7));//PTM4037

                    vfn = new String(MBGlobals.Build390_path+"misc"+File.separator+lfn+".verb");
                    vfile = new File(vfn);
                } while (vfile.exists());
                // save to file
                int lrecl = 80;
                BufferedWriter vfw = new BufferedWriter(new FileWriter(vfile));
                for (int idx=0; idx<command.length(); idx+=lrecl) {
                    int len = lrecl;
                    if (command.substring(idx).length() < lrecl) {
                        len = command.substring(idx).length();
                    }
                    vfw.write(command.substring(idx, idx+len)+MBConstants.NEWLINE);
                }
                vfw.close();


                // ftp to host
                MVSVerbFile = new String(uid.trim()+".B390.NUTSY."+lfn);//PTM4037


                com.ibm.sdwb.build390.info.FileInfo vf = new com.ibm.sdwb.build390.info.FileInfo("", MVSVerbFile);
                vf.setFileType("ASCII");
                vf.setMainframeRecordType("FB");
                vf.setMainframeRecordLength(lrecl);
                vf.setLocalFile(new File(vfn));
                mftp = new MBFtp(mainInfo,lep);

                if (!mftp.put(vf, MVSVerbFile, false)) {
                    throw new FtpError("Could not upload verb file "+vfn+" to "+MVSVerbFile+" within MBSocket class.");
                }
            } catch (IOException ioe) {
                throw new HostError("An I/O error occurred when trying to create verb file "+MVSVerbFile+" within MBSocket class."+mainInfo.getMainframeAddress(), ioe);
            }
            // reset CLRTSRC
            verbPath = MVSVerbFile;
            // set switch
            switches[1] = (byte) (switches[1] | 0x40);
            buf[3] = switches[1];
            // reset command
            command = "";
        }

        //CLRTSCR path to verb file
        buf[23+acct.length()] = (byte) verbPath.length();
        buf = AppendStringtoByteArray(verbPath, buf, 23+acct.length()+1);  // Defect_211

        // command
        buf = AppendStringtoByteArray(command, buf, 23+acct.length()+1+verbPath.length()); // Defect_211
        buf = AppendStringtoByteArray(" ", buf, 23+acct.length()+1+verbPath.length()+command.length()); // Defect_211
        temp = 23+acct.length()+1+verbPath.length()+command.length()+1;
        if (temp > 255) {
            buf[1] = (byte)(temp%256);
            buf[0] = (byte)(temp/256);
        } else buf[1] = (byte)temp;
        serverCmd_ = buf; // Defect_211
        return(temp);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        lep=new  LogEventProcessor();
    }

    // 05/29/00 pjs show rc in hex
    public String byteToHex(byte inbyte)
    {
        String hx = new String();
        try {
            String bstring = (new Byte(inbyte)).toString();
            int bint = Integer.parseInt(bstring, 10);
            hx = Integer.toHexString(bint);
            // pad
            while (hx.length() < 2) {
                hx = "0"+hx;
            }
            // truncate
            if (hx.length() > 2) {
                hx = hx.substring(hx.length()-2);
            }
        } catch (Exception e) {
            hx = "0";
        }
        return hx;

    }

    // 05/29/00 pjs show rc in hex
    public String intToHex(int bint)
    {
        String hx = new String();
        try {
            hx = Integer.toHexString(bint);
            // pad
            while (hx.length() < 4) {
                hx = "0"+hx;
            }
            // truncate
            if (hx.length() > 4) {
                hx = hx.substring(hx.length()-4);
            }
        } catch (Exception e) {
            hx = "0";
        }
        return hx;
    }



    class DebugAndTracePopup {

        private String commandKeyword = null;
        private int cmdlen =-1;
        private boolean previousTraceVerbOnlyOption = false;
        private boolean previousTraceVerbAndDataOption =false;
        private boolean previousHoldSubServerJobOutputOption =false;



        void show() throws com.ibm.sdwb.build390.MBBuildException{
            // Defect_103 do not append a comma
            // AddHoldTrace
            // Jerk, you need to append a comma if there are any keywords in the verb
            // ie. $ VERBNOKEYWORDS TRACE=TRACE, or $ VERBWITHKEYWORDS kw1=jerk,TRACE=TRACE
            // do not trace if command=nutsy or client-version or if
            // if you change this code, also change the code below 'ShowServerCmd(serverCmd_, command)'

            //Begin #Def.INT0754: 
            previousTraceVerbOnlyOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly();
            previousTraceVerbAndDataOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData();
            previousHoldSubServerJobOutputOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput();
            ServerCommand  servCommand = new ServerCommand(command);
            do {
                if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly() | 
                    com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData()|
                    com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput()) {
                    // don't append trace to nutsy and client version commands as well as server function requests
                    if (command.indexOf("NUTSY") == -1 & command.indexOf("CLIENT VERSION") == -1 & !tso & !jobstatus & !sysout & !delsysout & !jobscancel & !jobstatus) {
                        StringTokenizer cmdtmp = new StringTokenizer(command.substring(1), " ");
                        if (cmdtmp.countTokens()>1) {
                            //*blankchar check for blank at the end of the Verb command, if found back off one character
                            String blanktest = command.substring(command.length()-1);
                            if (blanktest.indexOf(" ")!=-1) {
                                command = command.substring(0,command.length()-1);
                            }
                            if (!com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput()) {
                                command += ",";
                            }
                        }

                    }
                }
                //End #Def.INT0754: 
                command = servCommand.getString();
                // build the command to go to server
                cmdlen = BuildServerCmd(serverCmd_, command, switches, mainInfo);

                // show the command if debug is on
                ShowServerCmd(serverCmd_, command);

            } while ( hasOptionsChanged(servCommand));


        }

        int getServerCommandLength()
        {
            return cmdlen;
        }



        private boolean hasOptionsChanged(ServerCommand servCommand)
        {
            boolean hasAnyOneOptionChanged = false;

            if (command.indexOf("NUTSY") == -1 & command.indexOf("CLIENT VERSION") == -1 & !tso & !jobstatus & !sysout & !delsysout & !jobscancel & !jobstatus) {
                if (hasTraceVerbOnlyOptionChanged()) {
                    hasAnyOneOptionChanged=true;
                    servCommand.setTraceVerbOnlyString();
                }

                if (hasTraceVerbAndDataOptionChanged()) {
                    hasAnyOneOptionChanged=true;
                    servCommand.setTraceVerbAndDataString();
                }

                if (hasHoldSubServerJobOutputOptionChanged()) {
                    hasAnyOneOptionChanged=true;
                }
            }

            return hasAnyOneOptionChanged;

        }

        private boolean hasTraceVerbOnlyOptionChanged()
        {
            if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly()) {
                if (previousTraceVerbOnlyOption) {
                    return false;
                }
            }
            boolean comparedSetting  = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly() | previousTraceVerbOnlyOption;
            previousTraceVerbOnlyOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly();
            return comparedSetting ;
        }

        private boolean hasTraceVerbAndDataOptionChanged()
        {
            if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData()) {
                if (previousTraceVerbAndDataOption) {
                    return false;
                }
            }
            boolean comparedSetting  = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData() | previousTraceVerbAndDataOption;
            previousTraceVerbAndDataOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData();
            return comparedSetting ;

        }

        private boolean hasHoldSubServerJobOutputOptionChanged()
        {
            if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput()) {
                if (previousHoldSubServerJobOutputOption) {
                    return false;
                }
            }
            boolean comparedSetting  = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput() | previousHoldSubServerJobOutputOption;
            previousHoldSubServerJobOutputOption = com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isHoldSubServerJobOutput();
            return comparedSetting ;

        }


        class ServerCommand {

            private String verb;
            private Set debugStringSet = new HashSet();

            ServerCommand(String verb)
            {
                this.verb = verb;
            }

            void setTraceVerbOnlyString()
            {
                final String debugString = ", TRACE=TRACE";
                if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbOnly()) {
                    debugStringSet.add(debugString);
                } else {
                    debugStringSet.remove(debugString);
                }

            }

            void setTraceVerbAndDataString()
            {
                final String debugString = ", TRACE=ALL";
                if (com.ibm.sdwb.build390.mainframe.MainframeOutputTraceOptions.getInstance().isTraceVerbAndData()) {
                    debugStringSet.add(debugString);
                } else {
                    debugStringSet.remove(debugString);
                }
            }


            String getString()
            {
                StringBuffer debugStringBuffer = new StringBuffer();
                for (Iterator debugStringIterator = debugStringSet.iterator(); debugStringIterator.hasNext();) {
                    debugStringBuffer.append((String)debugStringIterator.next());
                }
                return(verb + debugStringBuffer.toString());
            }

        }

    }




}
