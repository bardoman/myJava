package com.ibm.sdwb.build390;
/********************************************************************/
/* Java Ftp Client class provides a ftp session with MVS host       */
/********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 01/19/99 defect_220          read and replace ASCII uploaded files cr/lf combination.
// 05/14/99 feature             add a put method can leave the ftp session open
// 07/21/99 usingWrongAddr      in a system with several ipaddresses, this used the wrong one sometimes.
// 01/17/2000	       		let serversocket choose the port, don't suggest one.  ALso, simplify getHighPart and getLowPart
// 05/10/00 ken(pjs)		always do another type i for binary parts after a codepage set
//08/11/2000                    changes for the OECLient - if Ascii - then make it TYPE E and shouldnt use the textLineconverterStream
//03/28/2001                    changes for delete method - should detect hfs files and delete accordingly and not use the DSName class
/*********************************************************************/

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.process.steps.mainframe.RetrievePassticket;
import com.ibm.sdwb.build390.security.PasswordManager;

/**
* Create a FTP client
* May make it runs on thread if needs later
*/
public class MBFtp {
    static final int CMDPORT = 21; // ftp command port
    static final int BUFFSIZE = 2048; // buffer size
    static final int BYTESIZE = 8; // byte size
    static final int READY = 220; // ftp service ready
    static final int USEROK = 331; // user name ok
    static final int USERLOGGEDIN = 230; // user loggged in
    static final int CLOSEDATACON = 226; // closing data connection
    static final int ACTOK = 250; // dele successful
    static final int CMDOK = 200; // port command ok
    static final int DATACONOK = 125; // data connection open, transfer starting
    static final int MAXBLOCKSIZE = 10796;
    static final int SIZETEST   = 800000;
    public static final String CODEPAGENOTFOUND   = "Codepage was not found ";

    private static boolean usePassiveConnection = true; // part of the workaround for TST1996, if false active dataconnections are used


    private String host; // host name
    private String userid; // user id
    private String passwd; // password
    private BufferedReader fromServer;  // read stream from the host
    private PrintWriter serverCommand;  // write steam to the host
    private Socket sock; // ftp session socket
    private InetAddress addr; // ftp server internet address
    private String messageLine; // ftp server reply message
    private String previousLine = null;
    private String currentDirectory;
    private boolean stopped;
    private ServerSocket servSock;
    private String connectionLog = new String();

    private String localCurrentFile = new String();
    private LogEventProcessor lep=null;
    private boolean isLogObjPassed = false;
    private boolean fakeIt = false;
    private String argString = null;


    /*new constructors - build log file stuff  start/
   /**
  * Create a FTP client
  */
    public MBFtp(String ihost, String iuid, String ipw,LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        this.lep=lep;
        host = ihost;
        userid = iuid;
        passwd = ipw;

        login();
    }

    public MBFtp(MBMainframeInfo mainInfo,final LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        this.lep=lep;
        host = mainInfo.getMainframeAddress();
        userid = mainInfo.getMainframeUsername();
        if (MBClient.isClientMode()) {
            passwd = PasswordManager.getManager().getPassword(userid+"@"+host);
        } else {
            // this is  a hack , should be handled above the MBFtp level
            com.ibm.sdwb.build390.userinterface.UserCommunicationInterface userComm = new com.ibm.sdwb.build390.userinterface.UserCommunicationInterface() {
                public MBStatus getStatusHandler() {
                    return new MBStatus(null);
                }
                public LogEventProcessor getLEP() {
                    return lep;
                }

                public void handleUIEvent(com.ibm.sdwb.build390.userinterface.event.UserInterfaceEvent event) {
                }
            };
            ProcessWrapperForSingleStep wrapper = new ProcessWrapperForSingleStep(userComm);
            RetrievePassticket passticketGenerator = new RetrievePassticket(".",mainInfo, wrapper);
            passticketGenerator.externalExecute();
            passwd = passticketGenerator.getPassticket();
        }
        login();
    }

    public MBFtp(MBMainframeInfo mainInfo,LogEventProcessor lep, boolean tempFakeIt) throws com.ibm.sdwb.build390.MBBuildException{
        this.lep=lep;
        fakeIt=tempFakeIt;
        host = mainInfo.getMainframeAddress();
        userid = mainInfo.getMainframeUsername();
        passwd = PasswordManager.getManager().getPassword(userid+"@"+host);
        if (!fakeIt) {
            login();
        }
    }

    /**
    * Create a FTP client, for using pass ticket
    */
    public MBFtp(MBMainframeInfo mainInfo, String pw,LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        this.lep=lep;
        host = mainInfo.getMainframeAddress();
        userid = mainInfo.getMainframeUsername();
        passwd = pw; // pass ticket
        lep.LogSecondaryInfo("Debug :","HOST = " + host + "USERID ="+ userid + "PASSWORD ="+ passwd);
        login();
    }


    /**
    * read reply from ftp server
    * retrun ftp code
    */
    public int readReply() throws com.ibm.sdwb.build390.MBBuildException{
        if (!fakeIt) {
            String methodName = new String("MBFtp:"+localCurrentFile+":readReply");

            do {
                try {
                    previousLine = messageLine;
                    messageLine = fromServer.readLine();

                    //Begin TST2268
                    if (messageLine.length()==0) {
                        messageLine = fromServer.readLine();
                    }
                    //End TST2268

                    lep.LogSecondaryInfo(methodName,messageLine);
                    connectionLog += messageLine;
                } catch (IOException e) {
                    if (isLogObjPassed) {
                        lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                    }
                    throw new FtpError("An error occurred reading the FTP servers response for "+localCurrentFile, e);
                }
            } while (!stopped & !(Character.isDigit(messageLine.charAt(0)) &&
                                  Character.isDigit(messageLine.charAt(1)) &&
                                  Character.isDigit(messageLine.charAt(2)) &&
                                  messageLine.charAt(3) == ' '));
            connectionLog+=MBConstants.NEWLINE;

            return(Integer.parseInt(messageLine.substring(0, 3)));
        } else {
            return CMDOK;
        }
    }

    /**
    * read reply message from ftp server
    */
    public String readMessage() {
        return messageLine;
    }


    /**
    * send string to ftp server
    */
    public void sendAndLogCommand(String command) {
        sendAndLogCommand(command, false);
    }

    /**
    * send string to ftp server
    */
    public void sendAndLogCommand(String command, boolean addToArgString) {
        String methodName = new String("MBFtp:"+localCurrentFile+":sendAndLogCommand");
        if (!fakeIt) {
            serverCommand.print(command+"\r\n");
            serverCommand.flush();
            lep.LogSecondaryInfo(methodName,command);
            connectionLog += command+MBConstants.NEWLINE;
        } else if (addToArgString) {
            if (argString.length() > 0) {
                argString+=",";
            }
            argString += command;
        }

    }

    /**
    * Make a ftp connection to a MVS host
    */
    public void login() throws com.ibm.sdwb.build390.MBBuildException{
        String methodName = new String("MBFtp:login");
        String buff = new String();
        String str = new String();
        connectionLog=new String();

        try {
            addr = InetAddress.getByName(host);
            try {
                sock = new Socket(addr, CMDPORT);
                lep.LogSecondaryInfo(methodName, "created a socket");
            } catch (IOException e) {
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("An error occurred creating the connection to the ftp server  "+addr, e);
            }

            // obtain input and output streams
            // we had problems in extracting form CMVC to OE - 8859_1 is some codepage stuff.
            //shouldnt affect the normal evironment ie WinNT etc
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream(), "8859_1"));
            serverCommand = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "8859_1"));
            lep.LogSecondaryInfo(methodName,"created input and output streams");
        } catch (IOException io) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("An error occurred setting up communication with the FTP server", io);
        }

        if (readReply() != READY) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("The FTP server did not return the expected READY reply \n"+ readMessage());
        }

        sendAndLogCommand("USER " + userid);
        if (readReply() != USEROK) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("The FTP server did not return the expected USEROK reply \n"+ readMessage());
        }

        serverCommand.print("PASS " + passwd +"\r\n");

        serverCommand.flush();

        if (readReply() != USERLOGGEDIN) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("The FTP server did not return the expected LOGGED IN reply \n"+ readMessage());
        }
    }

    /**
    * Get a host file and store it in a local workstation
    * @param fileName Name of the host file
    * @param flag True for ASCII type, false for binary type
    * @return rc True for successful get, false for failed
    */
    public boolean get(String fileName, String localName, boolean flag) throws com.ibm.sdwb.build390.MBBuildException{
        String methodName = new String("MBFtp:get");
        lep.LogPrimaryInfo("Debug:", "MBFtp get RemotefileName="+ fileName + ", LocalFileName="+ localName, false);
        localCurrentFile = localName;
        boolean rc = false;
        servSock = null; // ftp data  Server socket for get, put
        Socket dataSock = null;       // ftp data socket
        String clientAddr = null;
        String cmd;
        byte[] buffer = new byte[BUFFSIZE];
        FileOutputStream fos=null;
        BufferedInputStream bis = null;
        int bytes;

        IOException IOEHold = null;
        for (int i = 0 ; (servSock == null) & (i<20);i++) {
            try {
                servSock = new ServerSocket(0);
            } catch (IOException io) {
                IOEHold = io;
            }
        }
        if (servSock == null) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("Data connection socket not initialized", IOEHold);
        }

        clientAddr = new String(sock.getLocalAddress().getHostAddress());
        lep.LogPrimaryInfo("Debug","CLIENT IP:" + clientAddr  + ",CLIENT IS LISTENING IN PORT="+servSock.getLocalPort(),false);
        // dotted decimal for host name separated by comma
        cmd = new String("PORT " + clientAddr.replace('.', ',')
                         + ","
                         + getHighPart(servSock.getLocalPort())
                         + ","
                         + getLowPart(servSock.getLocalPort()));
        sendAndLogCommand(cmd);

        if (readReply() != CMDOK) { // port command failed
            lep.LogPrimaryInfo(methodName, readMessage(),false);
            try {
                servSock.close();
            } catch (IOException io) {
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was an error closing the data connection to the FTP server", io);
            }
            return false;
        }

        // parse data set and member names
        DSName dsn = new DSName(fileName);

        // move to the working data set
        lep.LogSecondaryInfo("Ftp Command","CWD \'" + dsn.dataSet +"\'");
        sendAndLogCommand("CWD \'" + dsn.dataSet + "\'");
        if (readReply() != ACTOK) { // not found data set
            lep.LogSecondaryInfo(methodName,readMessage());
            // close ftp server socket
            try {
                servSock.close();
            } catch (IOException io) {
                lep.LogException("An error occurred while attempting to close the server socket", io);
            }
            return false;
        }

        if (!flag) { // binary mode
            sendAndLogCommand("TYPE I");
            readReply();

        } else {
            //changes for OECLIENT
            if (MBClient.isUSSClient()) {
                sendAndLogCommand("TYPE E");
                readReply();
            }
        }



        sendAndLogCommand("RETR " + dsn.memName);
        if (readReply() != DATACONOK) {
            lep.LogSecondaryInfo(methodName, readMessage());
            // close ftp server socket
            try {
                servSock.close();
            } catch (IOException io) {
                lep.LogException("An error occurred while attempting to close the server socket", io);
            }
            return false;
        }

        // create a fileoutputstream to store file
        try {
            fos = new FileOutputStream(localName);
        } catch (IOException io) {
            lep.LogSecondaryInfo(methodName, readMessage());
            // close ftp server socket
            try {
                servSock.close();
            } catch (IOException ioe) {
            }
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem creating the local file " + localName + "\n" +
                               "CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT="+servSock.getLocalPort(), io);
        }

        // Create a serverSocket for get and put
        try {
            dataSock = servSock.accept();
            bis = new BufferedInputStream(dataSock.getInputStream());
        } catch (IOException e) {
            lep.LogSecondaryInfo(methodName, readMessage());
            try {
                servSock.close();
            } catch (IOException io) {
            }
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem making the data connection to the FTP server", e);
        }

        try {
            while (!stopped & ((bytes = bis.read(buffer, 0, buffer.length)) != -1)) {
                fos.write(buffer, 0, bytes);
            } // while
        } catch (IOException io) {
            try {
                dataSock.close();
                servSock.close();
                fos.close();
                bis.close();
            } catch (IOException ioe) {
            }
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was an error writing data to the local file " + localName+"\n" +
                               "CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT="+servSock.getLocalPort(), io);
        }

        try {
            fos.close();
            bis.close();
            dataSock.close();
            servSock.close();
        } catch (IOException io) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem closing the communication streams", io);
        }

        if (readReply() == ACTOK) {
            rc = true;
        } else {
            lep.LogSecondaryInfo(methodName,readMessage());
        }

        logout();
        return rc;
    }

    /**
    * Copy local file to another workstation
    * @param fileName Name of a local file name to transfer
    * @param hostFilename Name of remote file
    * @param flag True for ASCII type, false for binary type
    * @return rc True for successfully store it on host or false for failure
    * 01/04/2000 pjs - add support for zipping build dir and sending it to development
    */
    public boolean zput(String localFilename, String hostFilename, boolean flag)throws com.ibm.sdwb.build390.MBBuildException {
        FileInfo tempFile = new FileInfo(null, null);
        tempFile.setLocalFile(new File(localFilename));
        if (flag) {
            tempFile.setFileType("ASCII");
        } else {
            tempFile.setFileType("BINARY");
        }
        return zput(tempFile, hostFilename);
    }

    /**
    * Put a local file from workstation to store it in a host
    * and automatically close the ftp session after the file transfer
    * @param file Object containing filename, and all info to upload it.
    * @param hostFilename The filename on the host
    * @return rc True for successfully store it on host or false for failure
    * 01/04/2000 pjs - add support for zipping build dir and sending it to development
    */
    public boolean zput(FileInfo tempFile, String hostFilename) throws com.ibm.sdwb.build390.MBBuildException{
        return zput(tempFile, hostFilename,false);
    }

    public boolean put(File localFile, String hostFilename)throws com.ibm.sdwb.build390.MBBuildException{
        FileInfo  temp = new FileInfo(null, null);
        temp.setLocalFile(localFile);
        temp.setFileType("ASCII");
        return put(temp,hostFilename, false);
    }

    /**
    * Put a local file from workstation to store it in a host
    * the caller may leave the ftp session on and the caller explicitly
    * close the ftp session after done.
    * @param file Object containing filename, and all info to upload it.
    * @param hostFilename The filename on the host
    * @return rc True for successfully store it on host or false for failure
    */
    public boolean put(FileInfo fileInfo, String hostFilename, boolean onFlag) throws com.ibm.sdwb.build390.MBBuildException{
        if (fileInfo.getLocalFile()!=null) {
            lep.LogPrimaryInfo("Debug:", "MBFtp put LocalFileName="+ fileInfo.getLocalFile().getAbsolutePath() + ", RemoteFileName=" + hostFilename, false);
        } else {
            lep.LogPrimaryInfo("Debug:", "MBFtp put " + ", RemoteFileName=" + hostFilename, false);
        }
        argString = new String();
        String methodName = new String("MBFtp:"+localCurrentFile+":put");
        boolean rc = false; // return code
        String clientAddr = null;
        servSock = null; // ftp data  Server socket for get, put
        Socket dataSock = null;       // ftp data socket
        byte[] buffer = new byte[BUFFSIZE];
        int blockSize = 0;
        int dataPort = 0;
        long fileSize =  -1;
        if (fakeIt) {
            fileSize = fileInfo.getSize();
        } else {
            fileInfo.getLocalFile().length();
        }

        // parse data set name
        DSName dsn = new DSName(hostFilename);

        if (!usePassiveConnection) {
            IOException holdIO = null;
            for (int i = 0 ; (servSock == null) & (i<20) ;i++) {
                try {
                    servSock = new ServerSocket(0);
                } catch (IOException io) {
                    holdIO = io;
                }
            }
            if (servSock == null) {
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("Server socket could not be initialized", holdIO);
            }

            clientAddr = new String(sock.getLocalAddress().getHostAddress());
            lep.LogPrimaryInfo("Debug","CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT=="+servSock.getLocalPort(),false);

            // dotted decimal for host name separated by comma
            String cmd = new String("PORT " + clientAddr.replace('.', ',')
                                    + ","
                                    + getHighPart(servSock.getLocalPort())
                                    + ","
                                    + getLowPart(servSock.getLocalPort()));


            sendAndLogCommand(cmd);

            if (readReply() != CMDOK) { // port command failed
                lep.LogSecondaryInfo(methodName, readMessage());
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                try {
                    servSock.close();
                } catch (IOException io) {
                    throw new FtpError("There was a problem closing the local FTP server socket", io);
                }
                return false;
            }
        } else {
            sendAndLogCommand("PASV");
            dataSock = connectToPassiveServer();
        }

        if (fileInfo.getMainframeRecordType()!=null) { // record type
            if (fileInfo.getMainframeRecordType().equals("F") | fileInfo.getMainframeRecordType().equals("FB")) {
                fileInfo.setMainframeRecordType("FB");
                int recs = MAXBLOCKSIZE / fileInfo.getMainframeRecordLength();
                blockSize = recs * fileInfo.getMainframeRecordLength();
                if (!fileInfo.isBinary()) {
                    if (!fakeIt) {
                        try {
                            BufferedReader recordReader = new BufferedReader(new FileReader(fileInfo.getLocalFile()));
                            long recordCount;
                            for (recordCount=0; recordReader.readLine() != null; recordCount++);
                            fileSize = fileInfo.getMainframeRecordLength() * recordCount;
                        } catch (IOException ioe) {
                            throw new GeneralError("There was a problem reading " + fileInfo.getLocalFile().getAbsolutePath(), ioe);
                        }
                    }
                }
            } else if (fileInfo.getMainframeRecordType().equals("V") | fileInfo.getMainframeRecordType().equals("VB")) {
                fileInfo.setMainframeRecordType("VB");
                blockSize = MAXBLOCKSIZE;
            }
            sendAndLogCommand("SITE RECfm="+fileInfo.getMainframeRecordType(), true);
            readReply();
        }

        if (fileSize == 0) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            if (fileInfo.getLocalFile()!=null) {
                throw new FtpError("File " + fileInfo.getLocalFile().getAbsolutePath() + " empty or missing");
            } else {
                throw new FtpError("RemoteFile " + hostFilename + " empty or missing");
            }
        }
        if (fileSize > SIZETEST) {
            sendAndLogCommand("SITE CYLINDERS", true);
            readReply();
        }

/*
ken, 01/11/99
Switched order blcksize and lrecl were given in.  blksize is cahnged to be a multiple of lrecl,
so it get's resized based on default lrecl, then as lrecl is changed, blksize is shrunk again, leading to
lost space.  So I switched the order they are given in.
*/

        if (fileInfo.getMainframeRecordLength()>=0) {
            sendAndLogCommand("SITE LRecl="+fileInfo.getMainframeRecordLength(), true);
            readReply();
        }

        sendAndLogCommand("SITE BLKSIZE=" + Integer.toString(blockSize), true);
        readReply();
        if (fileInfo.isBinary()) { // binary mode
            sendAndLogCommand("TYPE I", true); // compressed mode
            readReply();
// 12/03/99 based on load order, decide to use struct R or not			
            if (fileInfo.isUsingMainframeStructR()) {
                sendAndLogCommand("stru r", true); // compressed mode
                readReply();
            }
        } else if (fileInfo.getFTPFileType().equalsIgnoreCase("ASCII")) {
            if (MBClient.isUSSClient()) {
                sendAndLogCommand("TYPE E", true);
            } else {
                sendAndLogCommand("TYPE A", true);
            }
            readReply();

        } else {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("Unknown file type " + fileInfo.getFTPFileType().toUpperCase());
        }

        if (fileInfo.getMainframeCodepage() != null) {
            sendAndLogCommand(fileInfo.getMainframeCodepage(), true);
            readReply();
// Ken 5/10/00 check for a missing code page			
            if (previousLine!= null) {
                boolean codepageCommandFailed = (previousLine.indexOf("not found.") > -1) && (previousLine.indexOf("ignored.") > -1);
                if (codepageCommandFailed) {
                    throw new FtpError(CODEPAGENOTFOUND + fileInfo.getMainframeCodepage());
                }
            }
            // 05/10/00 (ken) pjs - always do another type i for binary parts after a codepage set
            // codepage set screws up type i
            if (fileInfo.isBinary()) { // binary mode
                sendAndLogCommand("TYPE I", true); // compressed mode
                readReply();
            }
        }

        if (!fakeIt) {
        	// move to the working data set
            lep.LogSecondaryInfo("Ftp Command","CWD \'"+dsn.dataSet+"\'");
            sendAndLogCommand("CWD \'" + dsn.dataSet +"\'");
            
            if (readReply() != ACTOK ) { // not found data set
            	
            	lep.LogSecondaryInfo(methodName,readMessage());
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);                
                
                try {
                	if(servSock!=null) {
                		servSock.close();
                	}
                } catch (IOException io) {
                    throw new FtpError("There was a problem closing the local FTP server socket", io);
                } 
                
               return false;
            }
            sendAndLogCommand("DELE "+dsn.memName);
            readReply(); // throw away rc

            // rename it as host file name and put the local file on file stream
            sendAndLogCommand("STOR " + dsn.memName);
            if (readReply() != DATACONOK) {
                lep.LogSecondaryInfo(methodName,readMessage());
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                if (!usePassiveConnection) {
                    // close ftp server socket
                    try {
                        servSock.close();
                    } catch (IOException io) {
                    }
                }
                return false;
            }
            //if we're using passive, we should already havea  dataSock connection
            if (!usePassiveConnection) {
                // Create a serverSocket for get and put
                try {
                    dataSock = servSock.accept();
                    servSock.close(); /*TST1507 a delay in servSocket close, causes problems during put (STOR)*/
                } catch (IOException e) {
                    lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                    throw new FtpError("There was a problem getting the data connection", e);
                }
            }

            int bytesRead=0, i=0;
            OutputStream bos = null;
            BufferedInputStream fis = null;
            try {
                fis = new BufferedInputStream(new FileInputStream(fileInfo.getLocalFile()));
                try {
                    // section added to translate platform linebreak string to
                    // cr/lf string, which MVS needs
                    //ken
                    //use the variable to pick up parts binary in lib (stupid)
                    // Ken actually, let's treat everything the same.   A bad idea, but what are you going to do?
                    // Ken On second though, gee, that don't work, so back to the old way.

                    if (!fileInfo.isBinary()) {
                        bos = new TextLineConverterOutputStream(new BufferedOutputStream(dataSock.getOutputStream()));
                    } else {
                        bos = new BufferedOutputStream(dataSock.getOutputStream());
                    }
                    int totalWrote = 0;
                    while (!stopped & ((bytesRead = fis.read(buffer)) > -1)) {
                        bos.write(buffer, 0, bytesRead);
                        bos.flush();
                        totalWrote += bytesRead;
                    } // for
                } catch (IOException ie) {
                    try {
                        // close data sock
                        dataSock.close();
                        // close ftp server socket
                        servSock.close();
                        // close file streams
                        fis.close();
                        bos.close();
                    } catch (IOException ioe) {
                    }
                    //MBUtilities.TraceIt(connectionLog);
                    //  build.TraceIt(connectionLog);
                    lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                    throw new FtpError("There was a problem writing the file \n" + dsn.toString() + " the FTP server."+ "\n" +
                                       "CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT="+servSock.getLocalPort(), ie);
                }
            } catch (FileNotFoundException fnote) {
                //    MBUtilities.TraceIt(connectionLog);
                //      build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("The local file " + fileInfo.getLocalFile().getAbsolutePath() + " could not be found\n"+
                                   "CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT="+servSock.getLocalPort(), fnote);
            }

            // close server socket
            try {
                dataSock.close();
            } catch (IOException io) {
                //MBUtilities.TraceIt(connectionLog);
                //build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was a problem closing the data connection to the FTP server"+ "\n" +
                                   "CLIENT IP:" + clientAddr  + ",CLIENT SERVERSOCKET PORT="+servSock.getLocalPort(), io);
            }

            /*  close ftp command socket
            try {
                servSock.close();
            } catch (IOException io) {
                //MBUtilities.TraceIt(connectionLog);
                //  build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was a problem closing the local FTP server socket", io);
            }
             */

            if (readReply() == ACTOK) {
                rc = true;
            } else {
                //MBUtilities.TraceIt(connectionLog);
                //  build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                //MBUtilities.Logit(MBConstants.DEBUG_USER, readMessage(), methodName);
                lep.LogSecondaryInfo(methodName,readMessage());
                //    build.Logit(MBConstants.DEBUG_USER, readMessage(), methodName);
                // ken 11/17/99 don't do this for now, leave it up there so we can take a look at it.
                //            delete(hostFilename); // delete incomplete file on the host
            }

            // close ftp session
            // Chris, 5/14/99, leave the ftp session on and explictly close the session
            // after done
            if (!onFlag)
                logout();

            // close file streams
            try {
                fis.close();
            } catch (IOException ioe) {
                //MBUtilities.TraceIt(connectionLog);
                //  build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was a problem closing the local file stream", ioe);
            }

            try {
                bos.close();
            } catch (IOException ioe) {
                //MBUtilities.TraceIt(connectionLog);
                //  build.TraceIt(connectionLog);
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was a problem closing the communication stream to the host", ioe);
            }
        }
        return rc;

    } // end of put

    /**
    * Put a local file from workstation to store it in a host
    * the caller may leave the ftp session on and the caller explicitly
    * close the ftp session after done.
    * @param file Object containing filename, and all info to upload it.
    * @param hostFilename The filename on the host
    * @return rc True for successfully store it on host or false for failure
    * 01/04/2000 pjs - add support for zipping build dir and sending it to development
    */
    public boolean zput(FileInfo fileInfo, String hostFilename, boolean onFlag) throws com.ibm.sdwb.build390.MBBuildException{
        lep.LogPrimaryInfo("Debug:", "MBFtp zput LocalFileName="+ fileInfo.getLocalFile().getAbsolutePath() + ", RemoteFileName=" + hostFilename, false);
        String methodName = new String("MBFtp:"+localCurrentFile+":put");
        boolean rc = false; // return code
        String cmd;
        String clientAddr = null;
        servSock = null; // ftp data  Server socket for get, put
        Socket dataSock = null;       // ftp data socket
        byte[] buffer = new byte[BUFFSIZE];
        FileInputStream fis=null;
        String justName = new String(fileInfo.getLocalFile().getName());
        OutputStream bos = null;
        int blockSize = 0;
        int dataPort = 0;
        long fileSize = fileInfo.getLocalFile().length();

        IOException holdIO = null;
        for (int i = 0 ; (servSock == null) & (i<20);i++) {
            try {
                servSock = new ServerSocket(0);
            } catch (IOException io) {
                holdIO = io;
            }
        }
        if (servSock == null) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("Server socket could not be initialized", holdIO);
        }

        clientAddr = new String(sock.getLocalAddress().getHostAddress());
        lep.LogSecondaryInfo(methodName,"client ip address:"+clientAddr);

        // dotted decimal for host name separated by comma

        //Begin #DEF.TST1569:
        cmd = "PASV";
        sendAndLogCommand(cmd);

        dataSock = connectToPassiveServer();

        if (dataSock == null) {
            // port command failed
            lep.LogSecondaryInfo(methodName,readMessage());
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            try {
                servSock.close();
            } catch (IOException io) {
                throw new FtpError("There was a problem closing the local FTP server socket", io);
            }
            return false;
        }

        if (fileSize == 0) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("File " + fileInfo.getLocalFile().getAbsolutePath() + " empty or missing");
        }

        if (fileInfo.isBinary()) {  // binary mode
            sendAndLogCommand("TYPE I"); // compressed mode
            readReply();
        } else if (fileInfo.getFTPFileType().toUpperCase().equals("ASCII")) {
            sendAndLogCommand("TYPE A");
            readReply();

        } else {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("Unknown file type " + fileInfo.getFTPFileType().toUpperCase());
        }

        sendAndLogCommand("CWD ./projects/b/build390util/ZIPS/");

        readReply(); // throw away rc 

        // rename it as host file name and put the local file on file stream
        sendAndLogCommand("STOR " + hostFilename);

        readReply(); // throw away rc 

        // Create a serverSocket for get and put
        try {
            bos = new BufferedOutputStream(dataSock.getOutputStream());
        } catch (IOException e) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem getting the data connection", e);
        }

        int bytesRead=0, i=0;
        try {
            fis = new FileInputStream(fileInfo.getLocalFile().getAbsolutePath());
            try {
                if (!fileInfo.isBinary()) {
                    BufferedReader textSource = new BufferedReader(new InputStreamReader(fis));
                    String line = null;
                    while (!stopped & ((line = textSource.readLine()) != null)) {
                        line += MBConstants.NEWLINE;
                        bos.write(line.getBytes(), 0, line.length());
                        bos.flush();
                    }
                } else {
                    int totalWrote = 0;
                    while (!stopped & ((bytesRead = fis.read(buffer)) > -1)) {
                        bos.write(buffer, 0, bytesRead);
                        bos.flush();
                        totalWrote += bytesRead;
                    } // for
                }
            } catch (IOException ie) {
                try {
                    dataSock.close();
                    servSock.close();
                    fis.close();
                    bos.close();
                } catch (IOException ioe) {
                }
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
                throw new FtpError("There was a problem writing the file to the FTP server", ie);
            }
        } catch (FileNotFoundException fnote) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("The local file " + fileInfo.getLocalFile().getAbsolutePath() + " could not be found", fnote);
        }

        try {
            dataSock.close();
        } catch (IOException io) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem closing the data connection to the FTP server", io);
        }

        try {
            servSock.close();
        } catch (IOException io) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem closing the local FTP server socket", io);
        }

        readReply(); // throw away rc

        logout();

        try {
            fis.close();
        } catch (IOException ioe) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem closing the local file stream", ioe);
        }

        try {
            bos.close();
        } catch (IOException ioe) {
            lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            throw new FtpError("There was a problem closing the communication stream to the host", ioe);
        }
        return true;

    } // end of put

    /**
* Delete a host file
* @param fileName File name that is to be delete on the host
* @return rc True for successful deletion otherwise false returns
*/
    public boolean delete(String fileName) throws com.ibm.sdwb.build390.MBBuildException{
        lep.LogPrimaryInfo("Debug:", "MBFtp delete "+ fileName, false);
        String methodName = new String("MBFtp:delete");
        boolean rc = false;

        // parse data set name
        //The delete should check for hfs files too like if the fileName is like this
        // /u/KISHORE/FW95GREL/LISTINGS/MODIFIED/CLRACCT.ASM
        //in that case the DSName class would not work.
        //so scan for starting / and then if so dont do dsnname stuff. 
        boolean isHFSFile=true;
        DSName dsn=null;
        if (!fileName.startsWith("/")) {
            dsn = new DSName(fileName);
            isHFSFile=false;
        }

        // move to the working data set
        //MBUtilities.Logit(MBConstants.DEBUG_DEV, "CWD \'" + dsn.dataSet +"\'", "Ftp Command");
        //  build.Logit(MBConstants.DEBUG_DEV, "CWD \'" + dsn.dataSet +"\'", "Ftp Command");
        if (!isHFSFile) {
            lep.LogSecondaryInfo("Ftp Command","CWD \'"+dsn.dataSet+"\'");
            sendAndLogCommand("CWD \'" + dsn.dataSet + "\'");
            if (readReply() != ACTOK) { // the data set not found
                //MBUtilities.Logit(MBConstants.DEBUG_USER, readMessage(), methodName);
                //  build.Logit(MBConstants.DEBUG_USER, readMessage(), methodName);
                lep.LogSecondaryInfo(methodName,readMessage());
                return false;
            }
        }
        if (!isHFSFile) {
            sendAndLogCommand("DELE "+dsn.memName);
        } else {
            //if its a HFS file the whole fileName is would be sent to delete.
            sendAndLogCommand("DELE "+fileName);

        }

        if (readReply()== ACTOK)
            rc = true;

        // close ftp session
        logout();
        return rc;
    }

    public String getFTPArgs() {
        return argString;
    }

    /**
    * Close the ftp session with the host
    * @param host The Host name
    */
    public void logout() throws com.ibm.sdwb.build390.MBBuildException{
        String methodName = new String("MBFtp:logout");

        // send "bye" command
        sendAndLogCommand("QUIT");
        readReply();

        // close socket
        try {
            sock.close();
        } catch (IOException io) {
            //MBUtilities.LogException("An error occurred while trying to close the socket during logout", io);
            //  build.LogException("An error occurred while trying to close the socket during logout", io);
            lep.LogException("An error occurred while trying to close the socket during logout", io);
        }
    }

    /**
    */
    private int getLowPart(int i) {
/*
        BigInteger tmp;
        BigInteger bi = new BigInteger( (new Integer(i)).toString() );

        tmp = bi.shiftRight(BYTESIZE);
        tmp = tmp.shiftLeft(BYTESIZE);
        tmp = bi.xor(tmp);
        return tmp.intValue();
*/
// ken 1/17/00
        return i % 256;
    }

    /**
    */
    private int getHighPart(int i) {
/*
        BigInteger bi = new BigInteger( (new Integer(i)).toString() );
        bi = bi.shiftRight(BYTESIZE);
        return bi.intValue();
*/
// ken 1/17/00
        return i / 256;
    }

    public void stop() throws com.ibm.sdwb.build390.MBBuildException {
        stopped = true;
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ioe) {
            }
        }
        if (servSock != null) {
            try {
                servSock.close();
            } catch (IOException ioe) {
            }
        }
    }

    //Begin #DEF.TST1569:
    /**
   * get socket based on reply from ftp server
   * retrun ftp code
   */
    public Socket connectToPassiveServer() throws com.ibm.sdwb.build390.MBBuildException{
        Socket sock=null;
        try {
            if (!fakeIt) {
                do {
                    messageLine = fromServer.readLine();
                    connectionLog += messageLine;
                } while (!stopped & !(Character.isDigit(messageLine.charAt(0)) &&
                                      Character.isDigit(messageLine.charAt(1)) &&
                                      Character.isDigit(messageLine.charAt(2)) &&
                                      messageLine.charAt(3) == ' '));
                connectionLog+=MBConstants.NEWLINE;

                if (messageLine.indexOf("Entering Passive Mode")!=-1) {
                    int index = messageLine.indexOf("(");


                    String tmpStr = messageLine.substring(index+1);


                    StringTokenizer strTok = new StringTokenizer(tmpStr,",.)");

                    String addr = null;
                    String hiPort = null;
                    String loPort = null;;

                    while (strTok.hasMoreTokens()) {
                        tmpStr = strTok.nextToken()+".";
                        tmpStr+= strTok.nextToken()+".";
                        tmpStr+= strTok.nextToken()+".";
                        tmpStr+= strTok.nextToken();

                        addr = tmpStr;

                        hiPort= strTok.nextToken();
                        loPort= strTok.nextToken();

                    }

                    int hiPortByte = Integer.valueOf(hiPort).intValue();

                    hiPortByte = hiPortByte << 8;

                    int loPortByte = Integer.valueOf(loPort).intValue();

                    int port = hiPortByte + loPortByte;

                    sock = new Socket(addr,port);
                }
            }
        } catch (IOException e) {
            if (isLogObjPassed) {
                lep.LogPrimaryInfo("ERROR:",connectionLog,false);
            }
            throw new FtpError("An error occurred reading the FTP servers response for "+localCurrentFile, e);
        }
        return sock;
    }
    //End #DEF.TST1569:

/*
    public static void main(String[] args)throws com.ibm.sdwb.build390.MBBuildException{
        
                Setup setup = SetupManager.getSetupManager().createSetupInstance();
        PasswordManager.getManager().setPassword(setup.getMainframeinfo().getMainframeUsername()+"@"+setup.getMainframeAddress(), "iwtwt1p");
        MBFtp mftp = new MBFtp(setup,new LogEventProcessor());
        String fileToUpload = "E:\\temp\\isgah000.pan";
        String whereToUpload = "KHORNE.TEST";
        MBVersionedFile tempFile = new MBVersionedFile(null, null, null, null, "ASCII", null, null, null, "SITE SBD=CLRPROD.ISO88591.EBC1047.TCPXLBIN", false);
        long startTime = (new Date()).getTime();
        mftp.put(fileToUpload, whereToUpload, tempFile);
        System.out.println((new Date()).getTime() - startTime);
    }
*/  

}

//******************************************************************************************
// Assumption: getting files from MVS sequetial data set or partition data set (member name)
// PS (physical sequential) xxx.yyy.zzz
// PO (partition organization) xxx.yyy.zzz(abc)
//******************************************************************************************
/**
* Parse MVS Data set name
* determine PS vs. PO and separate the data set and  member name
*
*/
class DSName {
    public String dataSet; // data set name
    public String memName; // member name

    public DSName(String dsn) {

        int begPos = dsn.indexOf('(');
        if (begPos <0) {  // sequential data set or hfs
            begPos = dsn.lastIndexOf('.');
            if (begPos < 0) { /// hfs
                begPos = dsn.lastIndexOf("/");
                this.dataSet = dsn.substring(0, begPos);
                this.memName = dsn.substring(begPos+1);
            } else { //sequential dataset 
                this.dataSet = dsn.substring(0, begPos);
                this.memName = dsn.substring(begPos+1);
            }
        } else { // partitiona data set
            this.dataSet = dsn.substring(0, begPos);
            int endPos = dsn.indexOf(')');
            this.memName = dsn.substring(begPos+1, endPos);
        } // if-else
    }
}
