package com.ibm.sdwb.build390;
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 09/03/98 Parse1              When parsing the output file, ignore records that do not start with a 0
// 01/22/99 #pjs1               close the result file after reading it
// 03/26/99 Defect_236          add wasFileRead method and dontthrowexception setting
// 04/27/99 errorHandling       change LogException parms & add new error types
// 07/14/99 HFS_CLEANUP         parse the workpath from the phase result file, it may be multiple lines long
// 09/29/99 pjs - read until you fond a line that does not start with *
// 02/07/00 Ken - parse out buildcc settings for a part.
// 03/07/2000 reworklog          changes to rewrite the log stuff using listeners
// 12/18/2000 sdwb1210 - add support for failed listings to a file system
// 01/12/2000 sdwb1120 - add support for build failure notification added filePath - passed it in jobinfo object
// 02/12/2000 Defect 163 - string.index exception when phaseresults file is parsed
// 02/13/2000 sdwb1120   - if the part is in root of cmvc ignore / instead take it as ""
// 02/27/2000 Defect 219 - The parser didnt get the correct filename
// 03/15/2000 Defect 298 - parser hits nullpointer
//06/22/2001 DEF.TST0420: Added method to get LISTGEN
//11/06/2001 DEF.INT0740  Check for null in getListGenSetting
//01/10/2003 DEF.INT1110: MBPhaseResultFileParser mark allocation prob.
//07/14/2004 DEF.PTM3592: MBLDOBJ completes OK, but Client shows error due to parsing of 
//11/16/2004 DEF.INT2010: Mark Invalid Error
/*********************************************************************/
import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.LogProcessorHolderInterface;

/** MBPhaseResultFileParser parses phase result files to get info messages about jobs.
*/

public class MBPhaseResultFileParser implements java.io.Serializable {
    static final long serialVersionUID = 5464352145699418240L;
    private static final String PHASE_PARSER_ALLOC_FILE = "PhaseParserAlloc";
    private int bufferSize = 2000; //default 

    Hashtable MVSMessages = new Hashtable();
    private static final String JOBLINE1 = "*INFO*JOB=";
    private static final String JOBLINE2 = "*INFO* JOB=";
    private static final String BUILDCCLINE = "BUILDCC=";
    private static final String BUILDORDERPROCESSED = "*INFO* STORED ORDER BLDORDER";
    private static final String READYLINE = "1READY";
    private static final String ERRORLINE = "*ERROR*";
    private static final String ERRORLINE_INVALID_BUILDTYPE = "*ERROR* Invalid Build Type=";
    private static final String WARNINGLINE = "*WARN";
    private static final String INFOLINE = "*INFO";
    private static final String WORKPATHLINE = "WORKPATH:";
    private static final String FAMILYRELEASELINE = "FAMILY/RELEASE";
    private static final String LISTGEN      = "LISTGEN:";
    private static final String LISTCOPY      = "LSTCOPY:";
    private static final String PHASE = "PHASE:";

    private static final int MAXLINESIZE = 2048;
    private boolean ErrorsFound = false;
    private boolean BuildTypeError = false;
    private boolean WarningsFound = false;
    private boolean buildOrderProcessed = false;
    private boolean DontThrowException = false;
    private boolean readok = true;
    private Hashtable buildccsToUse;
    String stringToParse = null;

    public MBPhaseResultFileParser(File fileToParse, boolean inDontThrowException) throws GeneralError{
        DontThrowException = inDontThrowException;
        init(fileToParse);
    }

    public MBPhaseResultFileParser(File fileToParse) throws GeneralError{
        init(fileToParse);
    }

    public MBPhaseResultFileParser(String tempStringToParse) throws GeneralError{
        stringToParse = tempStringToParse;
        if (stringToParse != null) {
            parseFile();
        } else {
            readok = false;
            throw new GeneralError("File " + stringToParse + " does not exist");
        }
    }

    private void init(File fileToParse) throws GeneralError{
        try {
            File allocFile = new File(PHASE_PARSER_ALLOC_FILE);
            if (allocFile.exists()) {
                LineNumberReader rd = new LineNumberReader(new FileReader(allocFile));

                String bufSize = rd.readLine();

                bufferSize = Integer.valueOf(bufSize).intValue();
            }
            AbendAwareBufferedReader phaseFileReader = new AbendAwareBufferedReader(new FileReader(fileToParse));
            char[] line = new char[4096];
            StringBuffer readFile = new StringBuffer();
            int numBytesRead = 0;
            while ((numBytesRead = phaseFileReader.read(line)) > -1) {
                readFile.append(line, 0, numBytesRead);
            }
            phaseFileReader.close();
            stringToParse = readFile.toString();
            if (stringToParse != null) {
                parseFile();
            } else {
                readok = false;
                throw new GeneralError("File " + stringToParse + " does not exist");
            }
        } catch (IOException ioe) {
            readok = false;
            if (!DontThrowException) {
                throw new GeneralError("File " + fileToParse + " could not be read");
            }
        }
    }


    /*
    // read file into buffer, removing line control etc.
    if a line starts with '1' isClearFile is set to true
    */
    void parseFile() throws GeneralError{
        buildccsToUse = new Hashtable();
        String currentLine = new String();
        StringTokenizer flineTokenizer;
        String ftoken=null;
        StringBuffer clearPrintFileString = new StringBuffer();
        StringBuffer ftpedResultFile = new StringBuffer();
        AbendAwareBufferedReader phaseFileReader = null;
        try {
            phaseFileReader = new AbendAwareBufferedReader(new StringReader(stringToParse));
            String tempLine = null;
            boolean clearCopyOn = false;
            boolean isClearFile = false;
            while ((tempLine = phaseFileReader.readLine()) != null) {
                if (tempLine.startsWith("1")) {
                    clearCopyOn=false;
                    isClearFile = true;
                }
                if (tempLine.startsWith("0")) {
                    clearCopyOn=true;
                    isClearFile = true;
                }
                if (!isClearFile) {
                    ftpedResultFile.append(tempLine+'\n');
                } else if (clearCopyOn) {
                    clearPrintFileString.append(tempLine.substring(1, tempLine.length()) + '\n');
                }
            }
            if (isClearFile) {
                phaseFileReader = new AbendAwareBufferedReader(new StringReader(clearPrintFileString.toString()));
            } else {
                phaseFileReader = new AbendAwareBufferedReader(new StringReader(ftpedResultFile.toString()));
            }
            clearPrintFileString = null;
            ftpedResultFile = null;
            parsePhaseResultFile(phaseFileReader);
            phaseFileReader.close();
        } catch (IOException ioe) {
            //MBUtilities.LogException("Error occurred when parsing the first line of " + stringToParse, ioe);
            throw new GeneralError("Error occurred when parsing the first line of " + stringToParse, ioe);
        }
    }

    /*
    // parse the file
    The method does getOneMessage() to get a String depending on the
    parsing done in getOneMessage() method.(refer getOneMessage Method on
    how the string is got)
    */
    void parsePhaseResultFile(BufferedReader phaseFileReader) throws GeneralError{
        String currentLine = null;
        String jobName;
        String fileName;
        String fileVersion = null;
        Hashtable currentHash;
        String firstToken = null;
        StringTokenizer lineTokenizer;

        ErrorsFound = false;
        BuildTypeError = false;
        WarningsFound = false;

        try {
            String currentMessage = null;
            while ((currentMessage = getOneMessage(phaseFileReader))!=null) {
                parseOneMessage(currentMessage);
            }
        } catch (IOException ioe) {
            //MBUtilities.LogException("Error occurred when parsing " + stringToParse, ioe);
            throw new GeneralError("Error occurred when parsing " + stringToParse, ioe);
        }
    }

    /*
    Before looking into this method refer getOneMessage()
    The message that is inputted here is got from getOneMessage basically the 
    whole file eg:
    The below illustrates a small segment if below is input to getOneMessage()
    
0        *INFO* MACRO CMCLIEN 10300357 is built.                                                                         
0         CMCLIEN.CPY                                                                                                    
0         1.1-20010130105049 CREATE                                                                                      
0         MACROS/                                                                                                        
0        *INFO* MACRO CMCOMM 10300358 is built.                                                                          
0         CMCOMM.CPY                                                                                                     
0         1.1-20010130105123 CREATE                                                                                      
0         MACROS/                                                                                                        

The output of getOneMessage for one call is
(Starts scanning from a *INFO and ends scan on next *INFO)
This is one of the conditions,there may be more criteria  it does to parse lines
        *INFO* MACRO CMCLIEN 10300357 is built.                                                                         
         CMCLIEN.CPY                                                                                                    
         1.1-20010130105049 CREATE                                                                                      
         MACROS/                         
         
    */
    private void parseOneMessage(String  message) throws IOException{
        BufferedReader messageSource = new BufferedReader(new StringReader(message));
        String currentLine = null;
        while ((currentLine =messageSource.readLine()) != null) {
            if (currentLine.indexOf(BUILDORDERPROCESSED) > -1) {
                buildOrderProcessed = true;
            }
            StringTokenizer lineTokenizer = new StringTokenizer(currentLine);
            if (lineTokenizer.hasMoreElements()) {
                String firstToken = lineTokenizer.nextToken();
                if (firstToken.endsWith(":")) {
                    currentLine = new String();
                    while (lineTokenizer.hasMoreTokens()) {
                        currentLine += lineTokenizer.nextToken();
                        if (!firstToken.equals(WORKPATHLINE)) {
                            currentLine += " " ;
                        }
                    }
                    // 07/14/99 pjs HFS_CLEANUP
                    // if the first token is workpath and the last token is a plus sign, append the next line
                    // the workpath may be multiple lines long in the file such as:
                    // WORKPATH: /u/KENT/ +
                    // SDWB21/BASE/
                    while (firstToken.equals(WORKPATHLINE) & currentLine.endsWith("+")) {
                        String saveLine = currentLine.substring(0,currentLine.indexOf("+"));
                        currentLine = messageSource.readLine();
                        if (currentLine != null) {
                            lineTokenizer = new StringTokenizer(currentLine);
                            currentLine = new String();
                            while (lineTokenizer.hasMoreTokens()) {
                                currentLine += lineTokenizer.nextToken();
                            }
                        }
                        currentLine = saveLine + currentLine;
                    }
                    // save the keyword and value to the hash
                    MVSMessages.put(firstToken, currentLine);
                } else if (currentLine.indexOf(JOBLINE1) > -1) {
                    parseJob(JOBLINE1, currentLine, messageSource);
                } else if (currentLine.indexOf(JOBLINE2) > -1) {
                    parseJob(JOBLINE2, currentLine, messageSource);
                    // Check for error lines
                } else if (currentLine.indexOf(ERRORLINE) > -1) {
                    ErrorsFound = true;
                    if (currentLine.indexOf(ERRORLINE_INVALID_BUILDTYPE) > -1) {
                        BuildTypeError = true;
                    }
                    // Check for warning lines
                } else if (currentLine.indexOf(WARNINGLINE) > -1) {
                    WarningsFound = true;
                    // Check for BUILDCC lines
                } else if (currentLine.indexOf(BUILDCCLINE) > -1) {
                    if (firstToken.startsWith(INFOLINE)) {
                        // eat MODULE
                        lineTokenizer.nextToken();
                        String moduleName = lineTokenizer.nextToken();
                        lineTokenizer.nextToken();
                        String buildccToken = lineTokenizer.nextToken();
                        StringTokenizer buildccTokenizer = new StringTokenizer(buildccToken, "=");
                        // eat BUILDCC"
                        buildccTokenizer.nextToken();
                        Integer buildcc = new Integer(buildccTokenizer.nextToken());
                        buildccsToUse.put(moduleName, buildcc);
                    }
                }
            }
        } // while
    }

    /*
    The input to this the actual original file . 
    Basically the currentLine is read and checked for 
    JOB= (or a job line - indicates if any job is submitted)
    a mark at 500bytes  is done (read from java's api what mark is)
    It acts as a pointer in the stream and convinient to move the pointer
    back to the initial marked position.
    Refer also getCurrentLinesOcurringNextToINFOLINE the conditions that may arise if a JOBLINE is found
    */

    private String getOneMessage(BufferedReader messageSource) throws IOException{
        StringWriter returnString = new StringWriter();
        BufferedWriter returnWriter = new BufferedWriter(returnString);
        String currentLine = messageSource.readLine();
        if (currentLine==null) {
            return null;
        }
        boolean isJobLine=false;
        if (currentLine.indexOf("JOB=") > 0) {
            isJobLine=true;
        }
        returnWriter.write(currentLine);
        returnWriter.newLine();

        //messageSource.mark(2000);//DEF.INT1110:

        //DEF.PTM3592A:
        messageSource.mark(bufferSize);

        boolean atEndOfMessage = false;
        int i =0;
        while (!atEndOfMessage) {
            currentLine = messageSource.readLine();
            i++;
            if (currentLine != null) {
                if (i==1) {
                    if (isJobLine) {
                        //skip the *INFO* line following the job line
                        if (currentLine.indexOf("*INFO*") >=0) {
                            String tempcurrentLine = getCurrentLinesOcurringNextToINFOLine(messageSource);
                            if (tempcurrentLine!=null) {
                                currentLine = tempcurrentLine;
                            }

                        }
                    }
                }

                StringTokenizer lineTokenizer = new StringTokenizer(currentLine);
                String testMessageStart = new String();
                if (lineTokenizer.hasMoreTokens()) {
                    testMessageStart = lineTokenizer.nextToken();
                }
                if ((testMessageStart.startsWith("*") && testMessageStart.endsWith("*")) | (testMessageStart.startsWith(FAMILYRELEASELINE))) {
                    atEndOfMessage = true;
                    messageSource.reset();
                } else {
                    //messageSource.mark(2000);//DEF.INT1110:

                    //DEF.PTM3592A:
                    messageSource.mark(bufferSize);

                    returnWriter.write(currentLine);
                    returnWriter.newLine();
                }
            } else {
                atEndOfMessage=true;
            }
        }
        returnWriter.close();
        returnString.close();
        return returnString.toString();
    }

    /*
    This method takes the  currentLine and scans for Jobname,job filename,job file mod etc.
    the line may look like this
        *INFO* JOB=CLRACCT(JOB06965) MODULE CLRACCT(10510129) XCOMPILE                                                  
         CLRACCT.ASM                                                                                                    
         1.6-20010202125819 DELTA                                                                                       
         SRCPATH
         In the above  case the jobname=CLRACCT(JOB06965) ,fileName=CLRACCT.ASM Version=1.6-20010202125819,filePath=SRCPATH - this
         clearly shows that the part is a MVS part. there can be part in HFS(Unix Operating system).
    
    Below code shows a 	 part existing in USS
         
         WORKPATH: /u/KENT/ +                                                                                           
        *INFO* JOB=SEV(JOB13495) MODOE SEV(10727303) XCOMPILE                                                           
         sev.c                                                                                                          
         1.1-20010130162449 LINK                                                                                        
         oesev/                                                                                                         
         In the above case a workpath would be there normally it would be 
         /u/<userid>
         and the filePath is oesev/
There is also a option to store the listings (failed job listing in a dataset )
The listing name can be given in the BLDORDER using keyword LSTCOPY
And gets sent out in the phase results so this keyword also needs to be parsed too.
         LSTCOPY: CLRTEST.FUSSCLIN.B3DRVPD2.LISTINGS                                                                    
   
    */                   
    private void parseJob(String jobConstant, String currentLine, BufferedReader messageSource) throws IOException{
        Hashtable currentHash = (Hashtable) MVSMessages.get(JOBLINE2);
        if (currentHash == null) {
            currentHash = new Hashtable();
            MVSMessages.put(JOBLINE2, currentHash);
        }
        int nameStart = currentLine.indexOf(jobConstant)+jobConstant.length();
        String jobName = currentLine.substring(nameStart, currentLine.indexOf(" ", nameStart));

        // sdwb1210 - add support for failed listings to a file system
        // need the parts class and mod
        StringTokenizer cToke = new StringTokenizer(currentLine.substring(nameStart), " ");
        String fileClass = null;
        String fileMod   = null;
        if (cToke.hasMoreElements()) {
            String tt = cToke.nextToken();
            fileClass = cToke.nextToken();
            fileMod   = cToke.nextToken();
            fileMod = fileMod.indexOf("(") > 0 ? fileMod.substring(0,fileMod.indexOf("(")) :fileMod;

        }

        String fileName = messageSource.readLine();
        String fileVersion = new String();
        String filePath = new String();
        if (fileName != null) {
            StringTokenizer tempToke = new StringTokenizer(fileName);
            if (tempToke.hasMoreElements()) {
                fileName = tempToke.nextToken();
            }
            if (fileName.trim().length() > 0) {
                fileVersion = messageSource.readLine();
                if (fileVersion != null) {
                    tempToke = new StringTokenizer(fileVersion);
                    if (tempToke.hasMoreElements()) {
                        fileVersion = tempToke.nextToken();
                    }
                }

                String readfilePathLine = messageSource.readLine();
                //if there is no file path then the part lies in the root of cmvc
                if (readfilePathLine !=null) {
                    StringTokenizer tempPathToke = new StringTokenizer(readfilePathLine);
                    if (tempPathToke.hasMoreElements()) {
                        filePath = tempPathToke.nextToken();
                    }
                }
                if (filePath.trim().length() <=0) {
                    //    filePath="/";
                    //if root cmvc then space it.
                    filePath="";

                } else {
                    filePath=readfilePathLine;
                    filePath = filePath.replaceAll(" ",""); //trim all spaces.

                }
            } else {
                fileName = "NoFileFound";
            }
        } else {
            fileName = "NoFileFound";
        }

        // sdwb1210 - add support for failed listings to a file system, add class and mod
        currentHash.put(jobName, new MBJobInfo(jobName, fileName, fileVersion, fileClass, fileMod,filePath));
    }

    ///The lines we are attempting to parse are ,
    //0        *INFO* JOB=SEVWDEL(JOB05202) MODULE SEVWDEL(10500675) XCOMPILE                                                  
    //0        *INFO* PLNK.SEV BUILD=OFF  - you can have asmany lines as below.                                                                                      
    //0        *INFO* ABCK.SEV BUILD=OFF                                                                                       
    //0        *INFO* DBEK.SEV BUILD=OFF                                                                                       
    //0         Sevwdel.c                                                                                                      
    //0         1.3-20010219132921 DELTA                                                                                       
    //SEVWDEL has an entry in the USRLST field pointing to PLINK.SEV , ABCK.SEV , DBEK.SEV.  
    //This means, that PLINK.SEV,ABCK.SEV,DBEK.SEV uses SEVWDEL.  
    //So, I guess Kent decided it would be nice to tell the user.  
    //Beware - the USRLST field can have many many entries 
    //so Kent will probably issue an INFO for each one.
    // So to accomplish that skip all the *INFO* lines occuring next to the *INFO* JOB line
    private String getCurrentLinesOcurringNextToINFOLine(BufferedReader messageSource) throws IOException {
        String currentLine = messageSource.readLine();
        boolean isCheckForNextINFOLINE= true;
        if (currentLine==null) {
            isCheckForNextINFOLINE=false;
        }
        while (isCheckForNextINFOLINE) {
            //Begin DEF.INT2010:
            if (currentLine.indexOf(JOBLINE2)!=-1 | currentLine.indexOf(JOBLINE2)!=-1) {
                return null;
            } else {
                messageSource.mark(bufferSize);//***BE
            }
            //End DEF.INT2010:

            if (currentLine.indexOf("*INFO*")>=0) {
                //skip the the currently read line
                currentLine = messageSource.readLine();
                if (currentLine==null) {
                    isCheckForNextINFOLINE=false;
                }

            } else {
                isCheckForNextINFOLINE=false;
            }
        }
        return currentLine;
    }


    public boolean getErrorInfo() {
        return(boolean) ErrorsFound;
    }

    public boolean getWarningInfo() {
        return(boolean) WarningsFound;
    }
    //returns a hastable with the job info
    public Hashtable getJobInfo() {
        return(Hashtable) MVSMessages.get(JOBLINE2) ;
    }

    public String getFileSetting(String settingToGet) {
        String tempSetting = (String) MVSMessages.get(settingToGet);
        if (tempSetting != null) {
            return tempSetting.trim();
        }
        return null;
    }

    public int getTotalNumberOfPhases() throws GeneralError{
        String phaseLine = (String) MVSMessages.get(PHASE);
        StringTokenizer tokenPhase = new StringTokenizer(phaseLine);
        String numberOfPhases = null;
        while (tokenPhase.hasMoreTokens()) {
            numberOfPhases = tokenPhase.nextToken();
        }
        if (numberOfPhases != null) {
            return Integer.parseInt(numberOfPhases);
        } else {
            throw new GeneralError("Error parsing phase result file: Could not get the total number of phases");
        }

    }

    public String getListGenSetting() {
        String s=(String) MVSMessages.get(LISTGEN);
        if (s!=null)return s.trim();
        else return s;
    }

    public String getListCopySetting() {
        String s=(String) MVSMessages.get(LISTCOPY);
        if (s!=null)return s.trim();
        else return s;
    }

    public String getworkpath() {
        return(String) MVSMessages.get(WORKPATHLINE);
    }

    public boolean wasBuildOrderProcessed() {
        return buildOrderProcessed;
    }

    /**
     * Return a map that has keys of job names, and values
     * of failure condition codes for that job, in Integer
     * objects
     * 
     * @return 
     */
    public Map getBuildCCIntegerForJobMap() {
        return buildccsToUse;
    }

    public boolean getBuildTypeError() {
        return(boolean) BuildTypeError;
    }
    public boolean wasFileRead() {
        return readok;
    }

    /*
    Used for testing only to run it as a standalone thing.since i have 25phaseresults file i used a for loop for count 25. and my 
    files reside in E:\download\phaseresults0.prt etc...till phaseresults25.prt
    */
    public static void main(String[] args) {
        try {

//			MBPhaseResultFileParser temp = new MBPhaseResultFileParser(new File("e:\\temp\\phaseresults1.prt"), new LogEventProcessor());
            for (int i=0;i<=25;i++) {
                //System.out.println("======= start parsing file ======= NO:"+i); 
                MBPhaseResultFileParser temp = new MBPhaseResultFileParser(new File("e:\\download\\phaseresults"+i+".prt"));
                //System.out.println("======= Results getErrorInfo                ="+temp.getErrorInfo()); 
                //System.out.println("======= Results getWarningInfo              ="+temp.getWarningInfo()); 
                //System.out.println("======= Results getJobInfo                  ="+temp.getJobInfo()); 
                //System.out.println("======= Results getWorkpath                 ="+temp.getworkpath()); 
                //System.out.println("======= Results wasBuildOrderProcessed      ="+temp.wasBuildOrderProcessed()); 
                //System.out.println("======= end   parsing file ======= NO:"+i); 
            }
        } catch (GeneralError ge) {
            ge.printStackTrace();
            System.exit(1);
        }
    }

}
