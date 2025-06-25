package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBUtilities class for the Build390 java client               */
/*********************************************************************/
/* Updates:                                                          */
// 12/24/98 #183            changed help function format
// 3/5/99   #129            add method to allow logexception to just take an excception as an argument.
// 03/17/99 Defect_247      Add site specific help hook
// 03/23/99 LocalUpdates    Gets local updates form the web
// 04/23/99 FixMail         Fix send mail problems and add ability to send test info
// 04/26/99 errorHandling   Simplify trace it and LogException
// 04/27/99 registration    Add registration mail function
// 04/28/99 fixmail         Add mail server ip to sendto.lst file
// 04/29/99 UsersGuideName  Change users guide name for 2.2
// 05/03/99 help_setup      Make sure setup is done before requesting help
// 05/07/99 ErrorHandling   Fix msg data sent when an excpetion occurs and change Logit to Traceit
//                          in sendmail
// 05/07/99 fixmail         add platform to error mail info
// 05/10/99 CmdLineErrorMsg Show user correct msgs when cmd line error occurs
// 05/12/99 checkFileSize   Only show files larger than 0 bytes
// 05/12/99 fixmail         add error type to subject of error mail
// 06/10/99 defect_387      Dont try to send mail if setup is not inited, also dont show 'null' in log msgs
// 06/17/99 Dir_Create      get local updates was not creating directoies correctly
// 06/27/99 exceptionEmail  fix the email parsing so it requires email in front of regular email addresses
// Release 2.2 shipped
// 07/09/99 gethelp         if the usersguide does not exist, go get it
// 07/14/99                 add createParmFile method
// 08/12/99                 remove beep function
// 09/30/99 pjs - Fix help link
// 11/08/99 chris - log it rather than bring up a pop-up dialog
// 11/23/99 pjs - Use 'Error' as default exception msgbox title
// 11/23/99 pjs - Add new log methods to accept a flag that indicates that the error info should not be put into a msgbox
// 11/30/99 *registrationerror   - Send email to dev team  if err  occurs during reg. due to err in dbserver
// 12/10/99 reset password for user exit to use
// 01/04/2000 pjs - add support for zipping build dir and sending it to development
// 01/07/2000 individual build log file - adding methods traceit,logit with an additional parm of  buildpath - called from MBBuild object
// 03/07/2000 reworklog      commented the logit/logexception/traceit stuff
// 03/29/2000 223fixesMove	moved code from 223 to 23 for setting account info
// 05/02/2000 eventDisplay      Additon of boolean switch to LogPrimaryIfno to  control just traceit or traceit + display
// 03/25/2001 Defect 319 - Dont log the exception if the file cant be delete..it might be
//06/10/2002 #Def.NukLocalUpdate: Remove local update feature
//12/03/2002 SDWB-2019 Enhance the help system
//01/16/2002 //DEF.INT1078:  Removing update function due to new help system.
// 01/23/2003 #DEF.INT1114:   Send mail hangs on extra CR after ".\n"
//05/14/2003 #DEF.RemRegFeat: Remove registration feature
//05/30/2003 #Feat.INT1178:  Enhance /test parm for improved tracking
//08/19/2003 #DEF:TST1380:  MDE changes not stored in unimodc for user build
// 10/20/2004 PTM3735           Getter methods to access PROGRAMVERSION/BUILDDATE.
/*********************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;

import com.ibm.sdwb.build390.help.HelpController;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;


/** MBUtilities contains the methods that are used by many parts of the program,
* yet aren't logically associated with any particular part. */
public class MBUtilities {
    //reworklog
    // private static Object logFileLock = new Object();   //the trace file to write to
// ken  11/15/99  try opening and closing this each time we need it, and just use the above object for a lock.
//    private static BufferedWriter trcfile = null;   //the trace file to write to
    //reworklog stuff
    //private static byte debug = 0;                  //the debug setting
//    private static Hashtable buildPartsAndPaths = new Hashtable();
    private static Vector sendtoVector = new Vector();
    private static Vector sendTestDatatoVector = new Vector();
    //#DEF.RemRegFeat:
    // private static Vector sendRegistrationDatatoVector = new Vector(); //registration
    private static MBHtmlViewer helpViewer = null;
    private static String sHostName = null;  // fixmail
    private static boolean sendExceptions = true;
    private static String storageServerAddress = null;
    private static final int dbServerPort = 6996;
    private static LogEventProcessor lep = new LogEventProcessor();
    /** Create an audible warning. */
    //public static void beep() { System.out.print('\u0007');}

    //reworklog stuff
    ///** Set the debug level */
    // public static void setDebug(byte bugSetting) {
    //     debug = bugSetting;
    // }

    /** open the log file */
    public static void initTrace() {
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        if (MainInterface.getInterfaceSingleton()!=null) {
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
    }


    public static void replaceStringsInHash(Hashtable hash, String oldString, String newString) {
        String currentKey;
        Object tempVal;
        String currentVal;
        int idx;
        Enumeration keys = hash.keys();
        while (keys.hasMoreElements()) {
            currentKey = (String) keys.nextElement();
            tempVal = hash.get(currentKey);
            if (tempVal.getClass().getName().equals("java.lang.String")) {
                currentVal = (String) tempVal;
                for (idx = currentVal.indexOf(oldString);idx > -1; idx = currentVal.indexOf(oldString)) {
                    currentVal = currentVal.substring(0, idx) + newString + currentVal.substring(idx + oldString.length(), currentVal.length());
                }
                hash.put(currentKey, currentVal);
            }
        }
    }

    public static String getNthToken(String sourceString, int tokenIndex) {
        StringTokenizer sourceParser = new StringTokenizer(sourceString);
        int i = 0;
        String resultToken;
        for (i = 0; i < tokenIndex ; i++) {
            sourceParser.nextToken();
        }
        return sourceParser.nextToken();
    }

    /** Gets a token from a string.
    * @param str A String containing the data to be parsed.
    * @param delim - token delimiter
    * @param index An int indicating which token within the source string to return. */
    public static String getNthToken(String sourceString, String delim, int tokenIndex) {

        int currentIndex = 0;
        int nextIndex = 0;
        int cnt = 0;

        StringTokenizer tok = new StringTokenizer(sourceString.trim(),delim);

        if (!tok.hasMoreElements()) {
            return " ";
        }

        while (cnt < tokenIndex & currentIndex > -1) {
            cnt++;
            nextIndex = sourceString.indexOf(delim, currentIndex);
            if (nextIndex == -1) nextIndex = sourceString.length();
            if (cnt < tokenIndex) currentIndex = nextIndex + 1;
        }
        if (currentIndex > -1 & nextIndex-currentIndex > 0)
            return sourceString.substring(currentIndex, nextIndex);
        else return null;
    }


    public static java.awt.event.ActionListener getHelpListener(final String anchor, final String newAnchor) {
        java.awt.event.ActionListener listener = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MBUtilities.ShowHelp(anchor,newAnchor);
            }
        };
        return listener;

    }

    /* Shows local site help data in web browser // Defect_247
    * @param url a String containing the anchor portion of the url to be shown 
    */
    public synchronized static void ShowSiteHelp(final String anchor) {
        new Thread(new Runnable() {
                       public void run() {

                           String url = new String("file:///"+MBGlobals.Build390_path+MBConstants.SITE_HELP_FILE);
                           if (!anchor.equals("")) {
                               url = url+"#"+anchor;
                           }
                           if (helpViewer == null) {
                               helpViewer = new MBHtmlViewer(lep);
                               helpViewer.setPage(url);
                           } else {
                               helpViewer.setPage(url);
                           }
                       }
                   }).start();
    }


    /** Shows help data in web browser
    * @param url a String containing the anchor portion of the url to be shown */
    public synchronized static void ShowHelp(final String anchor,final String newAnchor) {
        ShowHelp(newAnchor);

    }



    /** Shows help data in web browser
    * @param url a String containing the anchor portion of the url to be shown */
    public synchronized static void ShowHelp(final String anchor) {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           try {
                                               HelpController.getInstance().showTopic(anchor);
                                           } catch (MBBuildException mbe) {
                                               lep.LogException(mbe);
                                           }

                                       }
                                   });

    }


    private static Map lastId = new HashMap();
    /** Create an unique string with julian date
    *@return unique string format: pljjjaaa
    * where p - prefix; b - driver build, a - ++ apar build
    *       l - last digit of year
    *       jjj - julian date
    *       aaa - unique string
    */
    public static String UniqueString(String path, String prefix) {
        String buildid = null;
        String tempFilename;
        File tempFile;

        long ms = (new Date()).getTime();       // ms since 1/1/70
        ms = ms / 100;               // reset to 1/10 secs since 1/1/90

        synchronized(MBClient.client) {
            do {
                String timeHex = "0000000"+Long.toHexString(ms);
                String bd = timeHex.substring(timeHex.length() - 7, timeHex.length()).toUpperCase().trim();
                List generatedIds = (List) lastId.get(prefix);
                if (generatedIds==null) {
                    generatedIds=new ArrayList();
                    lastId.put(prefix, generatedIds);
                }
                if (!generatedIds.contains(bd)) {
                    buildid = prefix+bd;
                    generatedIds.add(bd);
                    if (generatedIds.size()>1000) {
                        generatedIds.remove(0);
                    }
                }
                ms++;
            } while (buildid==null); /* endwhile */
        }
        return buildid.toUpperCase();  // return Uppercase String
    }



    // 01/04/2000 pjs - add support for zipping build dir and sending it to development
    // build mail to be sent to development when zipped dir is sent
    public static void SendZipMail(String Buildid, String notesid, String comments, String FtpPath) {
        InitMailToLists();
        if (!((String)sendtoVector.elementAt(0)).equals("none")) {
            if (SetupManager.getSetupManager().hasSetup() && storageServerAddress != null) { // defect_387
                // build msg
                String dt = new String("Subject: "  + "Build390 Client Mail, Build Zipped:\n"
                                       + "\n\nNotes ID: "+notesid
                                       + "\n\nThe build directory for build "+Buildid+" has been zipped and FTP'd to "+FtpPath+" on "+storageServerAddress+"."
                                       + "\n\nPlease review the subject build."
                                       + "\n\nComments: "+comments+"\n\n"
                                       + "I\'m running Release "+MBConstants.getProgramVersion()
                                       + ", Built on "+MBConstants.getBuildDate()
                                       + ", On platform "+System.getProperty("os.name")
                                       + "\n" + SetupManager.getSetupManager().toString());
                SendMail(dt, null, sendtoVector);
            }
        }
    }

    // Build mail msg for test //FixMail
    public static void SendTestMail(String driver, String release, String buildtype) {
        // only send the mail if the /test option was specified when the client was started
        if (MBClient.getCommandLineSettings().isSwitchSet(CommandLineSettings.TEST)) {
            // build msg
            InitMailToLists();
            if (!((String)sendTestDatatoVector.elementAt(0)).equals("none")) {
                String dt = new String("Subject: "  + "BLD390 COMPLETE\n"
                                       + driver+" "+release+" "+buildtype+" \'"+(new Date()).toString()+"\'\n");
                SendMail(dt, null, sendTestDatatoVector);
                // Notify user of mail sent
                new MBMsgBox("Mail", "Notification has been sent to the test coordinator");
            }
        }
    }


    // Init the mail sendto lists //FixMail
    public static void InitMailToLists() {
        //#DEF.RemRegFeat: 
        if (sendtoVector.isEmpty() & sendTestDatatoVector.isEmpty())
        // if (sendtoVector.isEmpty() & sendTestDatatoVector.isEmpty() & sendRegistrationDatatoVector.isEmpty())
        {
            try {
                String stfn = new String(MBGlobals.Build390_path+"misc"+java.io.File.separator+"sendto.lst");
                String line = new String();
                File stf = new File(stfn);
                if (stf.exists()) {
                    BufferedReader resultReader = new BufferedReader(new FileReader(stfn));
                    while ((line = resultReader.readLine()) != null) {
                        //#DEF.RemRegFeat:
                        /*
                        if (line.toUpperCase().startsWith("REGISTER:"))
                        {
                            sendRegistrationDatatoVector.addElement(line.substring(9));
                        }
                        else 
                        */
                        if (line.toUpperCase().startsWith("TEST:")) {
                            sendTestDatatoVector.addElement(line.substring(5));  // add to test mail list
                        } else if (line.toUpperCase().startsWith("DATABASESERVER:")) {
                            storageServerAddress =  line.substring(15);    // add to test mail list
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

        //#DEF.RemRegFeat:
        /*
        if (sendRegistrationDatatoVector.isEmpty())
        {
            sendRegistrationDatatoVector.addElement("none");
        }
        */
    }

    // Send mail
    // Pat - mail send does not work without the FLUSH commands added //FixMail
    public static void SendMail(String msg, Throwable ex, Vector vt) {
        if (!vt.isEmpty() & SetupManager.getSetupManager().hasSetup()) { // defect_387
            if (!((String)vt.elementAt(0)).equals("none") & sHostName != null) {  // fixmail
                if (ex != null) {
                    StringWriter exWriter = new StringWriter();
                    PrintWriter printEx = new PrintWriter(exWriter);
                    ex.printStackTrace(printEx);
                    printEx.flush();
                    msg += exWriter.toString();
                }

                SendMail(msg, vt, SetupManager.getSetupManager().getCurrentMainframeInfo().getMainframeUsername(),SetupManager.getSetupManager().getCurrentMainframeInfo().getMainframePort());
            }
        }
    }


    public static void SendMail(String msg, Vector mailToVector, String mailFromUsername, String mailFromDNS) {
        InitMailToLists();
        String mailFrom = mailFromUsername;  // 4/26/02 change this, old one not working.
//		String mailFrom = mailFromUsername + "@" + mailFromDNS;
        if (!mailToVector.isEmpty()) { // defect_387
            if (sHostName == null) {
                sHostName = "localhost";
            }
            if (!((String)mailToVector.elementAt(0)).equals("none")) {  // fixmail
                //This is the method responsible for establishing a communications
                //path to the mail server and for sending SMTP commands.
                //  Step 1:  Create a new socket object using the name of the mail
                //             server and the port number.
                //  Step 2:  Create a printstream object based on the new socket
                //  Step 3:  Send the mail server the appropriate SMTP commands
                //             using the printstream.
                //  Step 4:  This sample also captures and displays status messages
                //             return from the mail server.  See getReply() method
                //             below.
                //  Step 5:  Release the connection to the server.
                //  Step 6:  Close the printstream and the socket.
                //Insert sendmail port number on sending host machine
                //(Note:  Port will typically be port #25.)
                int portNum = 25;
                try {
                    //Step 1
                    Socket outgoing = new Socket(sHostName, portNum);
                    //Step 2
                    PrintWriter ps = new PrintWriter(new OutputStreamWriter(outgoing.getOutputStream(), "8859_1"));
                    // read responses from mail server and log them
                    BufferedReader br = new BufferedReader(new InputStreamReader(outgoing.getInputStream(), "8859_1"));
                    //reworklog
                    lep.LogSecondaryInfo("SENDMAIL","SendMail:"+br.readLine());

                    //Steps 3 & 4
                    ps.println("HELO "+outgoing.getLocalAddress().getHostName());
                    ps.flush();
                    //reworklog
                    lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    ps.println("MAIL FROM: " + mailFrom);
                    ps.flush();
                    //reworklog
                    lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    for (int idx=0; idx<mailToVector.size(); idx++) {
                        ps.println("RCPT TO: " + (String)mailToVector.elementAt(idx));
                        ps.flush();
                        //reworklog
                        lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    }
                    ps.println("DATA");
                    ps.flush();
                    //reworklog
                    lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    // Mail Message
                    ps.println(msg);
                    // dont mess with this line

                    //Begin #DEF.INT1114:
                    //ps.println("\n"+"."+"\n");

                    ps.println(".");
                    //End #DEF.INT1114:

                    ps.flush();
                    //reworklog
                    lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    //Step 5
                    ps.println("QUIT");
                    ps.flush();
                    lep.LogSecondaryInfo("SENDMAIL:","SendMail:"+br.readLine());
                    //Step 6
                    ps.close();
                    outgoing.close();
                } catch (IOException Ex) {
                    System.out.println(Ex.getMessage());
                }
            }
        }
    }


    // 01/04/2000 pjs - add support for zipping build dir and sending it to development
    // get dbserver defined in sendto.txt
    public static String getStorageServer() {
        InitMailToLists();
        return storageServerAddress;
    }

    /**
    * Scan for a string pattern in a file
    *@param str String that is being searched for
    *@param fn String that is the name of file
    *@return flag Boolean either found or not found
    */
    public static boolean scanForString(String str, String fileName) { //throw MBBuildException {
        String methodName = new String("MBUtilities:scanForString");
        String currentLine;
        int index;
        boolean flag = false;
        File fn = new File(fileName);
        try {
            BufferedReader aFileReader = new BufferedReader(new FileReader(fn.getAbsolutePath()));
            // looking for the previous built information line
            while ((currentLine = aFileReader.readLine()) != null)
                if (currentLine.indexOf(str) > -1) {
                    flag = true;
                    break;
                } // found the succsfuly built line
            aFileReader.close();
        } catch (IOException ioe) {
            ioe.getMessage();
        }
        return flag;
    }

    /** delete a directory, any files in it, and all subdirectories */
    public static void deleteDirectory(java.io.File directory) {
        File tempFile;
        String dirFiles[] = directory.list();
        if (dirFiles != null) {
            for (int idx = 0; idx < dirFiles.length; idx++) {
                tempFile = new File(directory, dirFiles[idx]);
                if (tempFile.isDirectory()) {
                    deleteDirectory(tempFile);
                } else {
                    if (!tempFile.delete()) {
                        //reworklog
                        //LogException("Could not delete file "+tempFile, new Exception());

                        /*Defect 319 - Dont log the exception if the file cant be delete..it might be
                        locked by a process exclusively so it might result in the file not being deleted.
                        so ignore it.
                        */

                        // lep.LogException("Could not delete file "+tempFile, new Exception());
                    }
                }
            }
        }
        directory.delete();
    }

    /** make sure a track name is the correct format to be part of the usermod process
    */
    public static String validateTrackForSMOD(String track) {
        String errorString = "The track name must be seven characters long, uppercase, the first character a letter, and all others letters or digits.\n"+track+" does not conform to these requirements.\n";
        if (track.length()!=7) {
            return errorString;
        }
        if (!track.toUpperCase().equals(track)) {
            return errorString;
        }
        if (!Character.isLetter(track.charAt(0))) {
            return errorString;
        }
        for (int i=1; i < track.length(); i++) {
            if (!Character.isLetterOrDigit(track.charAt(i))) {
                return errorString;
            }
        }

        return new String();
    }

    // 01/04/2000 pjs - add support for zipping build dir and sending it to development
    public static void zipBuildDirectory(String path, String bid) {
        try {
            String zipFilename = path+bid+".zip";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilename));

            // get list of files in build directory
            String chopBuildidPath = path.substring(0,(path.length() - (bid.length()+1)));
            File dir = new File(path);
            java.util.List filesList = new ArrayList();
            com.ibm.sdwb.build390.utilities.FileSystem.listFilesTree(dir,filesList,null);
            // loop through files, adding each one to the zip file
            System.out.println("Files being zipped to "+path+bid+".zip");
            for (java.util.Iterator iter=filesList.iterator();iter.hasNext();) {
                File thisfile = (File)iter.next();
                if (thisfile.getName().indexOf(".zip")<0) {
                    System.out.println(thisfile.getAbsolutePath() +" being zipped..");
                    // Add ZIP entry to output stream.
                    out.putNextEntry(new ZipEntry(thisfile.getAbsolutePath().substring(chopBuildidPath.length())));
                    FileInputStream in = new FileInputStream(thisfile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                }
            }

            // close files
            out.closeEntry();
            out.close();
            System.out.println("zip complete.");


        } catch (IOException e) {
            lep.LogException("An error occurred duing zip process.",e);
        }
    }

    //Begin #Feat.INT1178:
    public static void createTestMail(String buildID, String driver, String release,
                                      String type, String startTime, 
                                      boolean successful, String description) {

        // only send the mail if the /test option was specified when the client was started
        if (MBClient.getCommandLineSettings().isSwitchSet(CommandLineSettings.TEST)) {
            // build msg
            InitMailToLists();

            if (description!=null) {
                if (description.length()>25) {
                    description = description.substring(0,25);
                }
            }

            String sendTime = (new Date()).toString();

            String success = "Success";
            if (successful!=true) {
                success="Fail";
            }

            if (!((String)sendTestDatatoVector.elementAt(0)).equals("none")) {
                String msg = "Subject: "+buildID+" "+driver+" "+release+" "+type+" '"+startTime+"' '"+
                             sendTime+"' "+success+" "+description+"\n";

                SendMail(msg, null, sendTestDatatoVector);
            }
        }
    }
    //End #Feat.INT1178:

    //Begin #DEF:TST1380:
    public static String getLocalFileVersion(String filename, MBUBuild build) throws com.ibm.sdwb.build390.MBBuildException {
        String fileVersion = new String();
        if (((com.ibm.sdwb.build390.process.UserBuildProcess) build.getProcessForThisBuild()).isPDSBuild()) {
            for (int i = 1; i < build.getLocalParts().length; i++) {
                String fName = (new File(build.getLocalParts()[i])).getName();
                if (filename.equals(fName)) {
                    if (build.getPDSMemberVersions().length-1>=i) {
                        fileVersion = build.getPDSMemberVersions()[i];
                    } else {
                        throw new GeneralError("There was an error while trying to read the PDS member versions.");
                    }
                }
            }
        } else {
            GregorianCalendar currCal = new GregorianCalendar();
            if (build.getFastTrack() || (!new File(filename).exists())) {
                currCal.setTime(new Date());
            } else {
                currCal.setTime(new Date((new File(filename)).lastModified()));
            }
            fileVersion = Integer.toString(currCal.get(Calendar.YEAR));
// all this stuff just handles padding time, dates, etc with 0
            String tempString = "0"+Integer.toString(1+currCal.get(Calendar.MONTH));
            int strLength = tempString.length();
            fileVersion += tempString.substring(strLength - 2, strLength);
            tempString = "0"+Integer.toString(currCal.get(Calendar.DATE));
            strLength = tempString.length();
            fileVersion += tempString.substring(strLength - 2, strLength);
            tempString = "0"+Integer.toString(currCal.get(Calendar.HOUR)+12*currCal.get(Calendar.AM_PM));
            strLength = tempString.length();
            fileVersion += tempString.substring(strLength - 2, strLength);
            tempString = "0"+Integer.toString(currCal.get(Calendar.MINUTE));
            strLength = tempString.length();
            fileVersion += tempString.substring(strLength - 2, strLength);
            tempString = "0"+Integer.toString(currCal.get(Calendar.SECOND));
            strLength = tempString.length();
            fileVersion += tempString.substring(strLength - 2, strLength);
        }
        return fileVersion;
    }
    //End #DEF:TST1380:






} // MBUtilities class


// 01/04/2000 pjs - add support for zipping build dir and sending it to development
/** The CommandFilter class creates a list of files in the build directory 
class CommandFilter implements FilenameFilter {
    public boolean accept(File dir, String name)
    {
        return new File(dir, name).exists();
    }
}
*/
/** Julian date class extends Date */
class JDate {
    Date currDate = new Date();
    GregorianCalendar currCal = new GregorianCalendar();
    private final static int [] startDay =
    {0, 31,59,90,120,151,181,212,243,273,304,334};

    public JDate() {
        currCal.setTime(currDate);
    } // implicit call to super()

    /** return the number of milliseconds */
    public  long getTime() {
        return currDate.getTime();
    }

    /** compute the day in the year */
    public String julianDay() {
        // make 3 digit integers with leading zeros */
        int jday = startDay[currCal.get(Calendar.MONTH)] + currCal.get(Calendar.DATE);
        if (currCal.isLeapYear(currCal.get(Calendar.YEAR)) & (currCal.get(Calendar.MONTH) > 1)) jday++;
        String temp = "000"+new String(String.valueOf(jday));
        temp = temp.substring(temp.length()- 3, temp.length());
        return temp;
    }

    /** get the last digit of year  - one character string */
    public String lastDigitYear() {
        String temp = new String(String.valueOf(currCal.get(Calendar.YEAR)));
        char[] tempAry = temp.toCharArray();
        return String.valueOf(tempAry[tempAry.length-1]); // return the last digit
    }


}
