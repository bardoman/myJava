package com.ibm.sdwb.build390;
/*********************************************************************/
/* It parses the CLRPRINT output with SYSTSPRT file format           */
/*********************************************************************/
// Changes
// 06/03/99   feature        add getPDT method
// 11/01/99                  extract out shippable parts, add getPartsList()
// 06/09/00 Defect 35 -      No other go..The Clrout file returns the word SUCESSFULY-  Hence did alter that word to SUCESSFULLY.
//02/21/2001 #HoldParse:  add parse and access functions for HOLD info
//07/30/2001 #DEF:TST0653: date and time displayed incorrectly
//09/10/2003 #DEF:TST1538  Nullpointer during usermod.
/*********************************************************************/
/*
1                 CLR         SDWB21 05/18/98                                                                 PAGE    1

0            $ A815418B ORDER=SMODBLD,OP=CHECK,DRIVER=PQ10079,TYPE=APAR
0        ++APAR CHECK DATA ON 6/03/98 AT 11:37:57
0         FAMILY: tstfam3
0         FAMADR: ATLAS.SANJOSE.IBM.COM@1205
0         CMVCREL: hir2101.test
0         MVS HLQ: CLRTEST
0         MVS REL: HIR2101
0         DRIVER: PQ10079
0        Drivers in base:
0         BASE
0         FUNCTION: HIR2101
0         PREVIOUS: BQ10079 19980603113402 BUILD=ON FAIL=OFF
//
0         PDT: CLRTEST.IMSI
0        HIR2101 HOLDS:    // deleted
0        COMMON HOLDS:     // deleted
0         COMMENTS: YES
0         SAVELVL: AAC
// 2/23/99, new updated
0         PDT: CLRTEST.IMSI
0         LOGICTYP: PTF     // added
0         HOLD: NO          // changed
0         COMMENTS: NO
0         APAR: AW00701
0         SMPLOGIC: INPUT   // added
0         REBUILD: NO
0         SAVELVL: AAA
//
0        Previous logic:
0         PRE UQ14449
0         PRE UQ12604
0         SUP BQ10079
0         SUP AQ10079
0        Parts Checked:
0         dxrrl120.plx370
0         1.4 DELTA
0         CLASS=MODULE MOD=DXRRL120 VHJN=81531323
0         dxrrl2t0.plx370
0         1.4 DELTA
0         CLASS=MODULE MOD=DXRRL2T0 VHJN=81531325
1READY
 CLR CLRIN('DD:CLRIN') CLRPRINT('DD:CLRPRINT') CLROUT('DD:CLROUX')
 SUPFMID AQ10079
 SUPFMID BQ10079
 LASTFRZ UQ12604
 LASTFRZ UQ14449
 READY
 END

*/

import java.io.*;
import java.util.*;
import java.text.*;

/** <br>The MBClearReport class provides parsing for a track view report.*/
public class MBClearReport {
    private File clrprint;
    private String lastBuilt;
    private String processingModeInformation = null;
    private Vector reqListVector = new Vector();
    private Vector lastLogicVector = new Vector();
    private boolean isComments = false;
    private static final String LASTBUILT  = new String("PREVIOUS:");
    private static final String CLRLINE    = new String("CLRPRINT");
    private static final String READYLINE  = new String("READY");
    private static final String LASTLOGIC  = new String("Previous logic:");
    private static final String PARTSLINE  = new String("Parts Checked:");
    private static final String COMMLINE   = new String("COMMENTS:");
    private static final String HLQLINE    = new String("MVS HLQ:");
    private static final String RELLINE    = new String("MVS REL:");
    private static final String COMMNAME   = new String("APARDATA");
    private static final String DRIVERNAME = new String("DRIVER:");
    private static final String BUILDDATE  = new String("BUILD");
    private static final String CMVCREL    = new String("CMVCREL:");
    private static final String FUNCTION   = new String("FUNCTION:");
    private static final String HOLD       = new String("HOLD:");  // add function
    private static final String LOGICTYPE  = new String("LOGICTYP:");
    private static final String USERMOD  = new String("USERMOD:");
    private static final String USERMODNOSHIPPABLESUPPORT = "*INFO* No shippable parts found for function";

    private static final String PDTLINE    = new String("PDT:");
    //
    private Vector holdVector=null;//#HoldParse
    //
    private Hashtable clearReportHash = new Hashtable();
    private String buildStatus = null;
    private String packageName = null; // successful APAR build
    private Vector partsVector = new Vector(); // shippable part list from server

    /** @param client The MBclient object
    * @param path to the clrprint view report */
    public MBClearReport (String clrprintFile) {
        clrprint = new File(clrprintFile);
        if (clrprint.exists()) {
            try {
                parseReportFile();
            } catch (MBBuildException e) {
                System.out.println(e);
            }
        }
    }

    /** @param client The MBclient object
    * @param path to the clrprint view report */
    public MBClearReport (String clrprintFile, boolean status) {
        clrprint = new File(clrprintFile);
        if (clrprint.exists() & (clrprint.length() > 0)) {
            try {
                if (status)
                    lookForStatusLine();
                else
                    readInfoLines();
            } catch (MBBuildException e) {
                System.out.println(e);
            } // try-catch
        }
    }

    private void lookForStatusLine() throws com.ibm.sdwb.build390.MBBuildException {
        String methodName = new String("MBClearReport:lookForStatusLine");
        String currentLine;
        int index;
        boolean flag = false;

        try {
            BufferedReader clrReportFileReader = new BufferedReader(new FileReader(clrprint.getAbsolutePath()));
            // looking for the previous built information line
            while ((currentLine = clrReportFileReader.readLine()) != null)
                if (currentLine.indexOf("successfully built") > 0) {
                    flag = true;
                    //Defect 35 - No other go..The Clrout file returns the word SUCESSFULY-  Hence did alter that word to SUCESSFULLY.
                    String tempLine = currentLine;
                    currentLine=null;
                    currentLine = tempLine.trim().substring(0,tempLine.indexOf("successfully")) + "successfully " + tempLine.substring(tempLine.indexOf("successfully") +12);
                    break;
                } // found the succsfuly built line
                // found the status line or the end of file
                // && force not to evaluate the right-hand operand if flag is false
            if (flag && ((index = currentLine.indexOf("++")) > 0)) {
                buildStatus = new String(currentLine.substring(index).trim());
                packageName = new String(MBUtilities.getNthToken(currentLine,3));
            } else
                buildStatus = null;
            clrReportFileReader.close();
        } catch (IOException ioe) {
            throw new GeneralError("There was an error reading the clear report file " + clrprint, ioe);
        }
    }

    private void readInfoLines() throws com.ibm.sdwb.build390.MBBuildException {
        String methodName = new String("MBClearReport:readInfoLines");
        String currentLine;
        String name, value;

        try {
            BufferedReader clrReportFileReader = new BufferedReader(new FileReader(clrprint.getAbsolutePath()));
            // looking for the previous built information line
            while (((currentLine = clrReportFileReader.readLine()) != null) & (!currentLine.trim().endsWith(":"))) {
                // read all xxxx: yyyy
                int colonIndex = currentLine.indexOf(":");
                if (currentLine.indexOf("++APAR BUILD")>0) {
                    name = MBUtilities.getNthToken(currentLine, 2);
                    value = MBUtilities.getNthToken(currentLine, 5);
                    clearReportHash.put(name, value);
                } else if (colonIndex > 0) {
                    name = currentLine.substring(10, colonIndex+1).trim();
                    value = currentLine.substring(colonIndex +1).trim();
                    clearReportHash.put(name, value);
                }
            } // while
            clrReportFileReader.close();
        } catch (IOException ioe) {
            throw new GeneralError("There was an error reading "+clrprint, ioe);
        } catch (NullPointerException npe) {
            throw new GeneralError("NullPointer Exception in reading file", npe);
        }
    }

    /** look for any error string
    *@return flag boolean true if there was error
    */
    public boolean isThereError() throws com.ibm.sdwb.build390.MBBuildException {
        String methodName = new String("MBClearReport:isThereError");
        String currentLine;
        int index;
        boolean flag = false;

        try {
            BufferedReader clrReportFileReader = new BufferedReader(new FileReader(clrprint.getAbsolutePath()));
            // looking for the previous built information line
            while ((currentLine = clrReportFileReader.readLine()) != null)
                if (currentLine.indexOf("*ERROR*") > 0) {
                    flag = true;
                    break;
                } // found the succsfuly built line
            clrReportFileReader.close();
        } catch (IOException ioe) {
            throw new GeneralError("There was an error reading the clear report file "+clrprint, ioe);
        }
        return flag;
    }

    /** Get the status of last build
    *@return status string
    */
    public String getBuildStatus() {
        return buildStatus;
    }

    /** Get the Apar Package name
    *@return name string
    */
    public String getPackageName() {
        try {
            lookForStatusLine();
        } catch (MBBuildException e) {
            System.out.println(e);
        } // try-catch
        return packageName;
    }

    private void parseReportFile() throws com.ibm.sdwb.build390.MBBuildException
    {
        String methodName = new String("MBClearReport:parseReportFile");
        String currentLine;
        String name;
        String value;
        String majorName, lastType, lastReq, type, req, item;
        StringTokenizer reportLineParser;
        String temp;

        try {
            BufferedReader clrReportFileReader = new BufferedReader(new FileReader(clrprint.getAbsolutePath()));
            // looking for the previous built information line
            while (((currentLine = clrReportFileReader.readLine()) != null) &
                   (currentLine == null ? false :  (currentLine.indexOf(LASTLOGIC) <0)) & 
                   (currentLine == null ? false : currentLine.indexOf(PARTSLINE)<0)) {
                //Begin=>#HoldParse:  add parse and access functions for HOLD info
                if (currentLine.indexOf("HOLDS")!=-1) {//if HOLDS appears in line enter logic 
                    if (holdVector==null)holdVector=new Vector();

                    if (currentLine.indexOf("0")==0)currentLine=currentLine.substring(1).trim();//strip off 0

                    temp=(String)clearReportHash.get(RELLINE);

                    if (currentLine.startsWith(temp.trim()+" HOLDS")) {
                        while ((currentLine = clrReportFileReader.readLine()) != null) {
                            if (currentLine.indexOf(":")!=-1)break;
                            if (currentLine.indexOf("0")==0)currentLine=currentLine.substring(1).trim();//strip off 0
                            holdVector.add("F"+currentLine.substring(0,1));
                        }  
                    }
                    if (currentLine.indexOf("0")==0)currentLine=currentLine.substring(1).trim();//strip off 0

                    if (currentLine.startsWith("COMMON HOLDS:")) {
                        while ((currentLine = clrReportFileReader.readLine()) != null) {
                            if (currentLine.indexOf(":")!=-1)break;
                            if (currentLine.indexOf("0")==0)currentLine=currentLine.substring(1).trim();//strip off 0
                            holdVector.add("C"+currentLine.substring(0,1));
                        }  
                    }
                }
                //End=>#HoldParse:  add parse and access functions for HOLD info

                //(!currentLine.trim().endsWith(":"))){
                // read all xxxx: yyyy
                int colonIndex = currentLine.indexOf(":");
                if (colonIndex > 0) {
                    name = currentLine.substring(10, colonIndex+1).trim();
                    value = currentLine.substring(colonIndex +1).trim();
                    if (value.length() > 0)
                        clearReportHash.put(name, value);
                }
                String statusTestString = currentLine.substring(1).trim();
                if (statusTestString.startsWith(USERMODNOSHIPPABLESUPPORT)) {
                    processingModeInformation = statusTestString;
                }

            } // while
            if (currentLine != null) {
                if (getLastBuildDate() != null) {
                    majorName = currentLine.trim();
                    while (majorName.indexOf(LASTLOGIC) < 1) {
                        currentLine = clrReportFileReader.readLine();
                        majorName = currentLine.trim();
                    } // while not the previous logic line
                    // found the Previous logic line
                    while (((currentLine = clrReportFileReader.readLine()) != null) & (!currentLine.trim().endsWith(":"))) {
                        if (currentLine.startsWith("0"))
                            // leading 0 indicates a data line
                            lastLogicVector.addElement(currentLine.substring(10).trim());
                    } // reading previous logic lines
                } // if there was a previous built

                // 11/01/99, chris, find the Part Checked: line
                // get part list from Build/390 server
                // looking for Parts Checked:
                // read until CLRLINE
                // search for MOD= and then put it a vector
                while ((currentLine != null) & ((currentLine.indexOf(PARTSLINE))<0)){
                    currentLine = clrReportFileReader.readLine();
                    if (currentLine.indexOf(PARTSLINE)<0) {
                        processingModeInformation = currentLine;
                    }
                }

                while (((currentLine = clrReportFileReader.readLine())!=null) & ((currentLine.indexOf(CLRLINE))<1)) {
                    if ((currentLine.indexOf("MOD=")) > 0) {
                        int pos = currentLine.indexOf("MOD=");
                        String partline = new String(currentLine.substring(pos+4));
                        int post = partline.indexOf(" ");
                        partsVector.addElement(partline.substring(0, post));
                    } // it was a part line
                } // read part checked lines

                // looking for SYSTSPRT output
                // 11/01/99, chris, already at the SYSPRINT output line
                //while (((currentLine = clrReportFileReader.readLine())!=null) & ((currentLine.indexOf(CLRLINE))<1));

                // locate the REQ lines
                String lastLine=null;
                // read req line
                //#TST1538
                while (((currentLine = clrReportFileReader.readLine())!=null)) {
                    if ((currentLine.indexOf(READYLINE,1))<1) {
                        lastLine = currentLine;
                        if (!currentLine.startsWith(" *")) {
                            reqListVector.addElement(currentLine);
                        }
                        currentLine = clrReportFileReader.readLine();
                    }
                } // while
                // try to make the last line as an error message if there is Error in that line
                if (lastLine!=null) {
                    if (lastLine.indexOf("ERROR")>0) {
// Ken, 4/27/99 This is handled by the error checking code    	
//                        new MBMsgBox("Error",lastLine.trim());
                        throw new GeneralError("An error occurred while getting clear report: "+MBConstants.NEWLINE+ lastLine.trim());
                    }
                }
                // }
            } // if the current line is not null
            // done reading
            clrReportFileReader.close();

        } catch (IOException ioe) {
            throw new GeneralError("There was an error parsing the report file "+clrprint, ioe);
        }
    }

    /** getHlq searches the file object for the mvs hlq setting
    * @return hlq string */
//0         MVS HLQ: CLRTEST
    public String getHlq() {
        String hlq = (String)clearReportHash.get(HLQLINE);
        return(hlq);
    }

    /** getRel searches the file object for the mvs release setting
    * @return mvs release string */
//0         MVS REL: HIR2101
    public String getRel() {
        String rel = (String) clearReportHash.get(RELLINE);
        return(rel);
    }

    //BEGIN: #DEF:TST0653: date and time displayed incorrectly
    public String getLastBuildPrefix() {
        String lastPrefix=null;
        String infoLine = (String) clearReportHash.get(LASTBUILT);

        if (infoLine==null)return infoLine;

        if ((infoLine.indexOf("No") < 0) & (infoLine != null)) {
            lastPrefix = new String(MBUtilities.getNthToken(infoLine,0));
        }
        return(lastPrefix);
    }

    public String getLastBuildTime() {
        String bTime=null;
        String infoLine = (String) clearReportHash.get(LASTBUILT);

        if (infoLine==null)return infoLine;

        if ((infoLine.indexOf("No") < 0) & (infoLine != null)) {
            String dateString = new String(MBUtilities.getNthToken(infoLine,1));
            // make yyyy/mm/dd horizontal tab hh:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            ParsePosition pos = new ParsePosition(0);
            Date cDate = sdf.parse(dateString, pos); // convert to Date format
            bTime = DateFormat.getTimeInstance().format(cDate);
        }
        return(bTime);
    }

    public String getLastBuildDate() {
        String bDate=null;
        String infoLine = (String) clearReportHash.get(LASTBUILT);

        if (infoLine==null)return infoLine;

        if ((infoLine.indexOf("No") < 0) & (infoLine != null)) {
            String dateString = new String(MBUtilities.getNthToken(infoLine,1));
            // make yyyy/mm/dd horizontal tab hh:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            ParsePosition pos = new ParsePosition(0);
            Date cDate = sdf.parse(dateString, pos); // convert to Date format
            bDate = DateFormat.getDateInstance().format(cDate);
        }
        return(bDate);
    }
    //END: #DEF:TST0653: date and time displayed incorrectly

    /** return Apar name which is the same as Driver
    *@retun mvs driver string which is Apar name
    */
//0         DRIVER: PQ10079
    public String getDriver() {
        String name = (String) clearReportHash.get(DRIVERNAME);
        return(name);
    }

    /**
    *@retun cmvc Release name
    */
//0         DRIVER: PQ10079
    public String getLibRel() {
        String name = (String) clearReportHash.get(CMVCREL);
        return(name);
    }

    /** return ++Apar Build
    *@retun build_date of this ++ Apar
    */
//0        ++APAR BUILD DATA ON 4/03/98 AT 13:29:05
    public String getDate() {
        String bDate = (String) clearReportHash.get(BUILDDATE);
        if (bDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
            String dateString = bDate; // build date
            ParsePosition pos = new ParsePosition(0);
            Date cDate = sdf.parse(dateString, pos); // convert to Date format
            bDate = DateFormat.getDateInstance().format(cDate);
        }
        return(bDate);
    }

    /** Was there a comments file for the previous built
    *@ return flag boolean
    */
    public boolean isComments() {
        String value = (String) clearReportHash.get(COMMLINE);
        if (value.trim().equals("YES"))
            return true;
        else
            return false;
    }

    /** Find out logic type either APAR or PTF
    *@ return type string
    */
    public String getLogicType() {
        return((String) clearReportHash.get(LOGICTYPE)).trim();
    }

    public String getHold() {
        return((String) clearReportHash.get(HOLD)).trim();
    }

    public String getUsermod() {
        return((String) clearReportHash.get(USERMOD)).trim();
    }

//Begin=>#HoldParse:  add parse and access functions for HOLD info
    public String[] getHolds() {
        String sAry[];

        if (holdVector==null)return new String[0];
        else {
            sAry=new String[holdVector.size()];

            for (int i=0;i!=holdVector.size();i++) {
                sAry[i]=(String)holdVector.get(i);
            }
            return sAry;
        }
    }
    //End=>#HoldParse:  add parse and access functions for HOLD info



    /** get PDT name
    *@ return type string
    */
    public String getPdt() {
        return((String) clearReportHash.get(PDTLINE)).trim();
    }

    /**
    * Get REQ List
    *@return array of REQ list
    */
/*  Contents of SYSTSPRT
1READY
 CLR CLRIN('DD:CLRIN') CLRPRINT('DD:CLRPRINT') CLROUT('DD:CLROUX')
 SUPFMID AQ10079
 SUPFMID BQ10079
 LASTFRZ UQ12604
 LASTFRZ UQ14449
 READY
 END
*/
    public Vector getReqList() {
        return reqListVector;
    }

    public void showLogics() {
        String line, chgLine, type, newType;
        Vector lineList = new Vector();
        StringTokenizer tknline;

        // standard out logics
        if (lastLogicVector.size() > 0) {
            System.out.println("Previous Build Logics:");
            for (int i=0; i < lastLogicVector.size(); i++)
                System.out.println(lastLogicVector.elementAt(i).toString());
        }

        if (reqListVector.size() > 0) {
            System.out.println("PDT Recommended Logics:");
            for (int i=0; i < reqListVector.size(); i++) {
                line = reqListVector.elementAt(i).toString().trim();
                tknline = new StringTokenizer(line);
                lineList = new Vector();
                // may more than 2 tokens for APAR number
                while (tknline.hasMoreTokens())
                    lineList.addElement(tknline.nextToken());

                type = lineList.elementAt(0).toString();
                // convert PDT to logic term
                if (type.equals("LASTFRZ"))
                    newType = new String("PRE");
                else if (type.equals("LASTPTF"))
                    newType = new String("PRE");
                else if (type.equals("UNFRPTF"))
                    newType = new String("PRE");
                else if (type.equals("UNPTFED"))
                    newType = new String("REQ");
                else if (type.equals("SUPFMID"))
                    newType = new String("SUP");
                else
                    newType = new String(type);
                // write with new logic term
                chgLine = new String(newType);
                for (int k=1; k < lineList.size(); k++)
                    chgLine = new String(chgLine + " " + lineList.elementAt(k));
                System.out.println(chgLine);
            } // outer for
        }
    }

    public boolean createLogicFile(String target) {
        int listSize = reqListVector.size();
        BufferedWriter tgt;
        String line, chgLine, type, newType;
        Vector lineList = new Vector();
        StringTokenizer tknline;

        File logicFile = new File(target);
        if (logicFile.exists())
            System.out.println("overwrite the current file"+target);

        try {
            tgt = new BufferedWriter(new FileWriter(target));
            for (int i=0; i < listSize; i++) {
                line = reqListVector.elementAt(i).toString().trim();
                tknline = new StringTokenizer(line);
                lineList = new Vector();
                // may more than 2 tokens for APAR number
                while (tknline.hasMoreTokens())
                    //String token = new String(tknline.nextToken());
                    lineList.addElement(tknline.nextToken());
                //lineList.addElement(token);

                type = lineList.elementAt(0).toString();
                // convert PDT to logic term
                if (type.equals("LASTFRZ"))
                    newType = new String("PRE");
                else if (type.equals("LASTPTF"))
                    newType = new String("PRE");
                else if (type.equals("UNFRPTF"))
                    newType = new String("PRE");
                else if (type.equals("UNPTFED"))
                    newType = new String("REQ");
                else if (type.equals("SUPFMID"))
                    newType = new String("SUP");
                else
                    newType = new String(type);
                // write with new logic term
                chgLine = new String(newType);
                for (int k=1; k < lineList.size(); k++)
                    chgLine = new String(chgLine + " " + lineList.elementAt(k));
                tgt.write(chgLine, 0, chgLine.length());
                tgt.newLine();
            }
            tgt.close();
        } catch (IOException e) {
        }

        return true;
    }


    /** return previous built logic */
    public Vector getLastLogic() {
        return lastLogicVector;
    }

    public boolean isPartsToProcess() {
        return !partsVector.isEmpty();
    }

    public boolean isUsermodProcessingWithNoShippablesEnabled(){
        if (processingModeInformation!=null) {
            return (processingModeInformation.indexOf(USERMODNOSHIPPABLESUPPORT)>=0);
        }
        return false;
    }


    public String toString() {
        return reqListVector.toString();
    }

/*
    public static void main(String[] args) throws Exception{
            MBClearReport clrp = new MBClearReport("C:\\temp\\SMOD1.prt");
            System.out.println("test "+clrp.getPackageName());
            System.out.println(clrp.processingModeInformation);
            System.out.println(clrp.isUsermodProcessingWithNoShippablesEnabled());
            if (clrp.isThereError()) {
                throw new GeneralError("An error occurred during SMODBLD OP=CHECK call" + MBConstants.NEWLINE +
                                       "Please refer the screwy file");
                                       
            }

    }
*/    

}
