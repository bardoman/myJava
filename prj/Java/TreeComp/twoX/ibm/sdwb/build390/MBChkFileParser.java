package com.ibm.sdwb.build390;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.info.FileInfo;

/** MBChkFileParser parses chk files to get warning messages, and errors.
*  This looks for messages in this format:
*  *message type* word word word    // the words are anything.  like, part not loaded, part is loaded, etc.
*    filename
*    version number  type  formatInfo
*       the format must have 3 fields separated by hyphens, ascii/bin, recordType,  recordLength
*  other things can come after these, as long as they are the first on the
*  line and follow each other (line 1, line 2, line 3)
*/
//*****************************************************************************************

public class MBChkFileParser implements com.ibm.sdwb.build390.MBStop, java.io.Serializable {
    static final long serialVersionUID = 6659112669390946445L;

    Hashtable MVSMessages = new Hashtable();
    private static final String PARTNOTLOADED = "*ERROR* PART NOT LOADED:";
    private static final String PARTNOTDEFINED = "*ERROR* PART NOT DEFINED IN:";
    private static final String PARTINCOM = "*ERROR* PART NOT LOADED. - FOUND IN HFS COM SHADOW:";
    private static final String PARTINITEM = "*ERROR* PART NOT LOADED. - FOUND IN HFS ITEM SHADOW:";
    private static final String DELETEDPART = "*INFO* DELETED PART SKIPPED:";
    private static final String RECALLINGPART = "*WARN* RECALL";
    private static final String FILEISLOADED = "*INFO* PART IS LOADED AS";
    private static final String WARN = "*WARN*";
    private static final String ISNOTBUILT = "IS NOT BUILT.";

    private transient LogEventProcessor lep=null;
    private int lineCount = 1;
    private Map infoMap = null;
    private boolean stopped = false;
    private String partClass=null;
    private File fileToParse=null;


    public MBChkFileParser(File fileToParse, Map tempInfoMap, LogEventProcessor lep) throws GeneralError {
        this.lep=lep;
        infoMap = tempInfoMap;
        this.fileToParse=fileToParse;

        init();
    }

    public MBChkFileParser(File fileToParse, Map tempInfoMap, String partClass, LogEventProcessor lep) throws GeneralError {
        this.lep=lep;
        infoMap = tempInfoMap;
        this.partClass = partClass;
        this.fileToParse=fileToParse;

        init();
    }

    private void init()throws GeneralError
    {        
        if (fileToParse.exists()) {
            try {
                BufferedReader chkFileReader = new BufferedReader(new FileReader(fileToParse));
                String currentMessage = null;
                while ((currentMessage = getOneMessage(chkFileReader))!=null & !stopped) {
                    parseOneMessage(currentMessage);

                }
                chkFileReader.close();
            } catch (IOException ioe) {
                throw new GeneralError("An error occurred while parsing " + fileToParse + " on line " + lineCount, ioe);
            }
        } else {
            throw new GeneralError("File " + fileToParse + " does not exist");
        }
    }

    private String getOneMessage(BufferedReader messageSource) throws IOException{
        StringWriter returnString = new StringWriter();
        BufferedWriter returnWriter = new BufferedWriter(returnString);
        String currentLine = messageSource.readLine();
        if (currentLine==null) {
            return null;
        }
        lineCount++;
        returnWriter.write(currentLine);
        returnWriter.newLine();
        StringTokenizer lineTokenizer = new StringTokenizer(currentLine);
        String testMessageStart = new String();
        if (lineTokenizer.hasMoreTokens()) {
            testMessageStart = lineTokenizer.nextToken();
        }
        if (testMessageStart.startsWith("*") && testMessageStart.endsWith("*")) {
            messageSource.mark(500);
            boolean atEndOfMessage = false;
            while (!atEndOfMessage & !stopped) {
                currentLine = messageSource.readLine();
                if (currentLine != null) {
                    lineTokenizer = new StringTokenizer(currentLine);
                    testMessageStart = new String();
                    if (lineTokenizer.hasMoreTokens()) {
                        testMessageStart = lineTokenizer.nextToken();
                    }
                    if (testMessageStart.startsWith("*") && testMessageStart.endsWith("*")) {
                        atEndOfMessage = true;
                        messageSource.reset();
                    } else {
                        lineCount++;
                        messageSource.mark(500);
                        returnWriter.write(currentLine);
                        returnWriter.newLine();
                    }
                } else {
                    atEndOfMessage=true;
                }
            }
        } else {
            return currentLine;
        }
        returnWriter.close();
        returnString.close();
        return returnString.toString();
    }

    private void parseOneMessage(String message) throws IOException{
        String name = new String();
        String version = new String();
        String date = new String();
        String saveType = new String();
        String fileType = new String();
        String recordType = new String();
        String directory = new String();
        String recordLength = new String();
        String codePage = null;
        BufferedReader messageReader = new BufferedReader(new StringReader(message));
        String currentLine = null;
        while ((currentLine=messageReader.readLine())!=null & !stopped) {
            StringTokenizer lineTokenizer = new StringTokenizer(currentLine);
            if (lineTokenizer.hasMoreTokens()) {
                String currentMessageType = lineTokenizer.nextToken();
                if (currentMessageType.startsWith("*") &
                    currentMessageType.endsWith("*")) {
                    if (currentMessageType.equals("*WARN*")) {
                        while (lineTokenizer.hasMoreTokens()) {
                            currentMessageType = currentMessageType+" " +lineTokenizer.nextToken();
                        }
                    } else if (currentMessageType.equals("*ERROR*")) {
                        while (lineTokenizer.hasMoreTokens()) {
                            currentMessageType = currentMessageType+" " +lineTokenizer.nextToken();
                        }
                    } else {
                        for (int i = 0; i < 4 & lineTokenizer.hasMoreTokens(); i++) {
                            currentMessageType = currentMessageType+" " +lineTokenizer.nextToken();
                        }
                    }
                    currentMessageType = currentMessageType.toUpperCase();
                    if (currentMessageType.startsWith(RECALLINGPART)) {
                        Vector currentVector =  (Vector) MVSMessages.get(RECALLINGPART);
                        if (currentVector == null) {
                            currentVector = new Vector();
                            MVSMessages.put(RECALLINGPART, currentVector);
                        }
                        currentVector.addElement(currentMessageType.substring(RECALLINGPART.length()).trim());
                    } else {
                        //Begin #Feat.SDWB2058:
                        String mvsPartName = null;

                        String driverCheckVHJN = null;//UserBldUpdate0

                        if (currentMessageType.startsWith(WARN) & currentMessageType.endsWith(ISNOTBUILT)) {
                            StringTokenizer strTok = new StringTokenizer(currentMessageType);

                            if (strTok.countTokens()>=3) {
                                for (int i=0;i!=3;i++) {
                                    mvsPartName = strTok.nextToken();
                                }
                                driverCheckVHJN = strTok.nextToken();//UserBldUpdate0
                            }
                        }
                        //End #Feat.SDWB2058:

                        Vector currentVector =  (Vector) MVSMessages.get(currentMessageType);
                        if (currentVector == null) {
                            currentVector = new Vector();
                            MVSMessages.put(currentMessageType, currentVector);
                        }

                        // Get the token with the VHJN
                        String VHJN = null;
                        String tempVHJN = null;
                        StringTokenizer vhjnToke = new StringTokenizer(currentLine);
                        while (vhjnToke.hasMoreElements()) {
                            tempVHJN = vhjnToke.nextToken();
                        }
                        // Now just grab the VHJN, throw out the rest

                        int firstParen = tempVHJN.indexOf("(");
                        int lastParen = tempVHJN.indexOf(")");
                        if (firstParen > 0  & lastParen > 0) {
                            VHJN = tempVHJN.substring(firstParen+1, lastParen);
                        } else {
                            VHJN = driverCheckVHJN;
                        }


                        currentLine = messageReader.readLine();
                        if (currentLine != null) {
                            lineTokenizer = new StringTokenizer(currentLine);
                            if (lineTokenizer.hasMoreTokens()) {
                                name = lineTokenizer.nextToken();
                            }

                            String lineToCheck = messageReader.readLine().trim();
                            if (lineToCheck.endsWith("+")) {
                                codePage = messageReader.readLine().trim();
                                lineToCheck =  lineToCheck.substring(0, lineToCheck.length()-1);
                            }
                            lineTokenizer = new StringTokenizer(lineToCheck);
                            if (lineTokenizer.hasMoreTokens()) {
                                version = lineTokenizer.nextToken();
                                StringTokenizer versionParser = new StringTokenizer(version,"-");
                                if (versionParser.countTokens() > 1) {
                                    version = versionParser.nextToken();
                                    date = versionParser.nextToken();
                                }
                            }
                            if (lineTokenizer.hasMoreTokens())
                                saveType = lineTokenizer.nextToken();
                            if (lineTokenizer.hasMoreTokens()) {
                                currentLine = lineTokenizer.nextToken();
                                if (lineTokenizer.hasMoreTokens()) {
                                    version = lineTokenizer.nextToken();
                                    StringTokenizer versionParser = new StringTokenizer(version,"-");
                                    if (versionParser.countTokens() > 1) {
                                        version = versionParser.nextToken();
                                        date = versionParser.nextToken();
                                    }
                                }
                                lineTokenizer = new StringTokenizer(currentLine, "-");
                            }
                            boolean structR = false;
                            if (lineTokenizer.hasMoreTokens()) {
                                fileType = lineTokenizer.nextToken();
                                // Ken 12/03/99 add support for struct R FTP arguement taken from loadorder								
                                if (fileType.toLowerCase().endsWith("r")) {
                                    structR = true;
                                    fileType = fileType.substring(0,fileType.length()-1);
                                }
                            }
                            if (lineTokenizer.hasMoreTokens())
                                recordType = lineTokenizer.nextToken();
                            if (lineTokenizer.hasMoreTokens())
                                recordLength = lineTokenizer.nextToken();
                            lineTokenizer = new StringTokenizer(messageReader.readLine());
                            if (lineTokenizer.hasMoreTokens()) {
                                directory = lineTokenizer.nextToken();
                            }

                            /*Begin UserBldUpdate0
                            this is not an ideal fix. 
                            we have to get it from the *INFO* line. But the problem is the lines aren't the same. 
                            Its hard to figure out the <partname>.<partclass> token.
                            eg:
                            *INFO* PART is loaded as MODULE CLRACCT Level(62650046):
                            *INFO* Part loaded as MODULE CLRALIAS Level(62750434):
                            *INFO* Part already loaded: ORDER LODORDER Level(62650043)
                            *INFO* ORDER BLDORDER 60340008 is built.                                        
                            *INFO* STORED ORDER LODORDER(00225295) XORDER                                   
                            *ERROR* Part not loaded
                            */

                            if (partClass!=null) {
                                directory = partClass;
                                int index=-1;

                                if ((index = name.indexOf("."))!=-1) {
                                    name = name.substring(0,index).toUpperCase();
                                }
                            }
                            //End UserBldUpdate0

                            FileInfo newFile = (FileInfo) infoMap.get(directory+"|"+name);
                            newFile.setMainframeRecordType(recordType);
                            if (recordLength.trim().length() > 0) {
                                newFile.setMainframeRecordLength(Integer.parseInt(recordLength));
                            }
                            if (fileType!=null) {
                                // even though we think a part is text, MVS might think it's binary, so we have to do what it thinks.
                                newFile.setFileType(fileType);
                            }
                            newFile.setMainframeCodepage(codePage);
                            newFile.setMainframeStructR(structR);
                            if (VHJN !=null) {
                                newFile.setVHJN(VHJN);
                            }
                            newFile.setTypeOfChange(saveType);
                            currentVector.add(newFile);

                        }
                    }
                } else {
                    lineTokenizer = new StringTokenizer(currentLine);
                    if (lineTokenizer.countTokens() > 1) {
                        name = lineTokenizer.nextToken();
                        String value = new String();
                        while (lineTokenizer.hasMoreTokens()) {
                            value = value + lineTokenizer.nextToken()+" ";
                        }
                        value = value.trim();
                        MVSMessages.put(name, value);
                    }
                }
            }
        }
    }

    public Vector getRecalledDatasets() {
        return insureNonNullReturn(RECALLINGPART);
    }

    public Vector getMissingFiles() {
        return insureNonNullReturn(PARTNOTLOADED);
    }

    public Vector getUndefinedFiles() {
        return insureNonNullReturn(PARTNOTDEFINED);
    }

    public Vector getDeletedFiles() {
        return insureNonNullReturn(DELETEDPART);
    }

    public Vector getLoadedFiles() {
        return insureNonNullReturn(FILEISLOADED);
    }

    public Vector getHFSComFiles() {
        return insureNonNullReturn(PARTINCOM);
    }

    public Vector getHFSItemFiles() {
        return insureNonNullReturn(PARTINITEM);
    }

    public Object getFileSetting(String settingType) {
        return MVSMessages.get(settingType);
    }

    private Vector insureNonNullReturn(String keyString) {
        Vector returnVector = (Vector) MVSMessages.get(keyString);
        if (returnVector == null) {
            returnVector = new Vector();
        }
        return returnVector;
    }

    public Set getTableEntriesContaining(Set stringsToFind) {
        Set returnSet = new HashSet();
        for (Iterator entryTypeIndicator = stringsToFind.iterator();entryTypeIndicator.hasNext();) {
            returnSet.addAll(getTableEntriesContaining((String) entryTypeIndicator.next()));
        }
        return returnSet;
    }

    public Set getTableEntriesContaining(String stringToFind) {
        Set entrySet = new HashSet();
        for (Iterator keys = MVSMessages.keySet().iterator(); keys.hasNext();) {
            String currentKey = (String) keys.next();
            if (currentKey.indexOf(stringToFind.toUpperCase()) > -1) {
                entrySet.addAll((Vector) MVSMessages.get(currentKey));
            }
        }
        return entrySet;
    }

    public void stop() {
        stopped = true;
    }

    public String toString() {
        return MVSMessages.toString();
    }

    public static void main(String[] args) throws Exception {
        new MBChkFileParser(new File("ShadowLoad.out"),null,null);
    }
}
