package com.ibm.sdwb.build390;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.library.LibraryInfo;

/** <br>The MBSmodDrvrReport class provides parsing for an SMOD report.*/
public class MBSmodDrvrReport implements MBStop, java.io.Serializable {

    private MBBuild build;
    private boolean stopped = false;
    private static final String DATATYPEDEFINITION = new String("Data key:");
    private static final String INFOHEADER1 = new String("Built ++USERMODs:");
    private static final String INFOHEADER2 = new String("Built ++APARs:");
    private static final String INFOHEADER3 = new String("Built ++PTFs:");
    private static final String DATADEFHASH = new String("DATADEFINITIONHASH");
    private static final String GROUPIDENTIFIER = new String("GROUP:");
    private static final String LOGICIDENTIFIER = new String("Logic:");
    private static final String PARTSIDENTIFIER = new String("Parts:");
    private Hashtable driverReportHash = new Hashtable();
    private Vector typesVector = new Vector();
    private Hashtable dataTypesHash = new Hashtable();
    private String reportType = new String();
    transient MBSocket mySock;
    transient MBStatus status;
    private LogEventProcessor lep=null;
    private boolean includePathname = false;
    private boolean justGetReport = false;


    /** @param path to the driver report */
    public MBSmodDrvrReport (MBBuild tempBuild, String tempReportType, MBStatus tempStatus,LogEventProcessor lep) {
        build = tempBuild;
        status = tempStatus;
        reportType = tempReportType;
        this.lep=lep;
    }

    public void initializeReport() throws com.ibm.sdwb.build390.MBBuildException {
        refreshReport();
    }

    public void setIncludePathname(boolean tempInclude) {
        includePathname = tempInclude;
    }

    public void setJustGetReport(boolean tempGetReport) {
        justGetReport = tempGetReport;
    }

    private void refreshReport() throws com.ibm.sdwb.build390.MBBuildException{
        LibraryInfo libInfo = build.getLibraryInfo();
        String smodrptOutputFile = MBGlobals.Build390_path+"misc"+File.separator+"SMODRPT-"+reportType+"-"+build.getReleaseInformation().getLibraryName()+"-"+build.getDriverInformation().getName();
        String cmd = "SMODRPT TYPE="+reportType+", "+libInfo.getDescriptiveStringForMVS()+", CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\',DRIVER=\'"+build.getDriverInformation().getName()+"\'";
        if (includePathname) {
            cmd+=", PATHNAME=YES";
        }
        mySock = new MBSocket(cmd, smodrptOutputFile, "Requesting a "+reportType+" report", status, build.getSetup().getMainframeInfo(),lep);
        mySock.run();
        if (!stopped & !justGetReport) {
            parseReportFile(new File(smodrptOutputFile+MBConstants.CLEARFILEEXTENTION));
        }
    }

    private void parseReportFile(File reportOutputFile) throws com.ibm.sdwb.build390.MBBuildException{
        String currentLine = null;
        String name;
        String value;
        String majorName;
        boolean continueFirstLoop = true;
        StringTokenizer reportLineParser;
        Vector tempVect = new Vector();
        Hashtable tempHash = new Hashtable();
        try {
            BufferedReader driverReportFileReader = new BufferedReader(new FileReader(reportOutputFile.getAbsolutePath()));
            while (continueFirstLoop & !stopped) {
                continueFirstLoop = false;
                if ((currentLine = driverReportFileReader.readLine()) != null) {
                    if (!currentLine.trim().startsWith(DATATYPEDEFINITION)) {
                        continueFirstLoop = true;
                        int colonIndex = currentLine.indexOf(":");
                        name = currentLine.substring(0, colonIndex+1).trim();
                        value = currentLine.substring(colonIndex +1).trim();
                        if (value.length() > 0) {
                            driverReportHash.put(name, value);
                        }
                    }
                }
            }
            Hashtable dataDefinitionHash = new Hashtable();
            driverReportHash.put(DATADEFHASH, dataDefinitionHash);
            if ((currentLine != null)&!stopped) {
                StringTokenizer parseDataDefinition = new StringTokenizer(currentLine);
                parseDataDefinition.nextToken();
                while (parseDataDefinition.hasMoreTokens()) {
                    String currentDef = parseDataDefinition.nextToken();
                    StringTokenizer parseDef = new StringTokenizer(currentDef, "=");
                    if (parseDef.countTokens() > 1) {
                        String sym = parseDef.nextToken();
                        String type = parseDef.nextToken();
                        dataDefinitionHash.put(sym, type);
                        dataTypesHash.put(type, sym);
                        typesVector.addElement(type);
                    }
                }
            }
            // 4/28/99, Chris, case#274
            while (!stopped & (!currentLine.startsWith(INFOHEADER1))&
                   (!currentLine.startsWith(INFOHEADER2))& (!currentLine.startsWith(INFOHEADER3)) ) {
                currentLine = driverReportFileReader.readLine().trim();
            }
            Hashtable allGroups = new Hashtable();
            driverReportHash.put("GROUPS", allGroups);
            while (((currentLine = driverReportFileReader.readLine()) != null)&!stopped) {
                currentLine = currentLine.trim();
                if (currentLine.length() > 0) {
                    if (currentLine.startsWith(GROUPIDENTIFIER)) {
// parse groupwide information
                        Hashtable groupHash = new Hashtable();
                        String groupIdentifier = currentLine.substring(currentLine.indexOf(":")+1).trim();
                        allGroups.put(groupIdentifier, groupHash);
                        driverReportFileReader.readLine();
                        driverReportFileReader.readLine();
                        boolean inUsermodSection = true;
                        while (inUsermodSection) {
                            StringWriter stringUsermod = new StringWriter();
                            BufferedWriter currentUsermod = new BufferedWriter(stringUsermod);
                            String tempLine = null;
                            boolean usermodNotDone = true;
                            driverReportFileReader.mark(1024);
                            String testLine = driverReportFileReader.readLine();
                            if (testLine != null) {
                                testLine = testLine.trim();
                                driverReportFileReader.reset();
                                if (!testLine.startsWith(GROUPIDENTIFIER)) {
                                    while (usermodNotDone) {
                                        tempLine = driverReportFileReader.readLine();
                                        if (tempLine != null) {
                                            if ((tempLine = tempLine.trim()).length() > 0) {
                                                currentUsermod.write(tempLine);
                                                currentUsermod.newLine();
                                            } else {
                                                usermodNotDone = false;
                                            }
                                        } else {
                                            usermodNotDone = false;
                                        }
                                    }
                                    currentUsermod.close();
                                    String usermodName = (new StringTokenizer(testLine)).nextToken();
                                    Hashtable subGroupHash = parseUsermod(stringUsermod.toString());
                                    Vector subVect = (Vector) groupHash.get(usermodName);
                                    if (subVect == null) {
                                        subVect = new Vector();
                                        groupHash.put(usermodName, subVect);
                                    }
                                    subVect.addElement(subGroupHash);
                                } else {
                                    inUsermodSection = false;
                                }
                            } else {
                                inUsermodSection = false;
                            }
                        }
                    }
                }
            }
            driverReportFileReader.close();
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred reading file "+reportOutputFile, ioe);
        }
    }

    /** getDriver searches the file object for the driver setting
    * @return driver string */
    // DRIVER:  GA
    public String getDriver() {
        String drv = (String) driverReportHash.get("DRIVER:");
        return drv;
    }

    /** getGroups searches the file object for the parsed groups
    * @return groups string */
    public Hashtable getGroups() {
        return(Hashtable) driverReportHash.get("GROUPS");
    }

    /** getDataDefinitions searches the file object for the data definitions
    * @return dataDefinitions Hashtable */
    public Hashtable getDataDefinitions() {
        return(Hashtable) driverReportHash.get(DATADEFHASH);
    }

    /** getDataTypes searches the file object for the part information
    * @return typeVector Vector */
    public Vector getDataTypes() {
        return typesVector;
    }

    /** getHlq searches the file object for the mvs hlq setting
    * @return hlq string */
    //MVS HLQ: CLRTEST
    public String getHlq() {
        String hlq = (String)driverReportHash.get("MVS HLQ:");
        return(hlq);
    }

    /** getRel searches the file object for the mvs release setting
    * @return mvs release string */
    // MVS REL: ADSM31
    public String getRel() {
        String rel = (String) driverReportHash.get("MVS REL:");
        return(rel);
    }

    public Hashtable parseUsermod(String usermodToParse) throws IOException{
        BufferedReader usermodReader = new BufferedReader(new StringReader(usermodToParse));
        Hashtable subGroup = new Hashtable();
        String tempString = usermodReader.readLine();
        String currentLine=null;
        StringTokenizer tempToke = new StringTokenizer(tempString);
        String usermodId = tempToke.nextToken();
        String date = tempToke.nextToken();
        subGroup.put("DATE", date);
        String rework = tempToke.nextToken();
        subGroup.put("REWORK", rework.substring(rework.indexOf("=")+1).trim());
        String usermodLevel = tempToke.nextToken();
        subGroup.put("USERMODLEVEL", usermodLevel.substring(usermodLevel.indexOf("=")+1).trim());
        String saveLevel = tempToke.nextToken();
        subGroup.put("SAVELEVEL", saveLevel.substring(saveLevel.indexOf("=")+1).trim());
        usermodReader.readLine();

// this part parses the Logic section
        boolean endSection = false;
        // 4/28/99, Chris, case#274, added ptf parts, no logic for PTF parts
        if (! reportType.equals("PTF")) {
            Vector usermodLogic = new Vector();
            subGroup.put("LOGIC", usermodLogic);
            while (!stopped&!endSection) {
                currentLine=usermodReader.readLine();
                endSection = true;
                if (currentLine != null) {
                    String logicLine = currentLine.trim();
                    endSection = logicLine.equals(PARTSIDENTIFIER);
                    if (!endSection) {
                        usermodLogic.addElement(logicLine);
                    }
                }
            }
        } // if none PTF parts

        currentLine = usermodReader.readLine();
        tempToke = new StringTokenizer(currentLine);
        Vector partColumns = new Vector();
        subGroup.put("PARTCOLUMNS", partColumns);
        int columnCount = tempToke.countTokens();
        for (int i = 0; i < columnCount; i++) {
            partColumns.addElement(tempToke.nextToken());
        }
        endSection = false;

        /*The first mod class line is the header (modLineCount = 0), we just grab its position and store 
                       it in an array.  */
        String[] temp = currentLine.trim().split("\\s+");
        int[] positionArray = new int[temp.length +1];
        int j=0;
        for (j=0;j<temp.length;j++) {
            positionArray[j] = currentLine.indexOf(temp[j]);
        }

        positionArray[j] = currentLine.length();
        Vector partVect = new Vector();
        subGroup.put("PARTS", partVect);
        while (!stopped&!endSection) {
            currentLine=usermodReader.readLine();
            endSection=true;
            if (currentLine!= null) {
                String partLine = currentLine.trim();
                endSection = partLine.length() < 1;
                if (!endSection && !partLine.contains("=")) {
                    Vector onePart = new Vector();
                    partVect.addElement(onePart);
                    tempToke = new StringTokenizer(partLine);
                    onePart.addElement(tempToke.nextToken());
                    onePart.addElement(tempToke.nextToken());
                    onePart.addElement(new Boolean(true));
                    tempToke.nextToken();
                    String dataTypeString = partLine.substring(positionArray[3],positionArray[3+1]).trim();
                    for (int i2 = 0; i2 < typesVector.size(); i2++) {
                        String currentSymbol = (String) dataTypesHash.get((String) typesVector.elementAt(i2));
                        if (dataTypeString.indexOf(currentSymbol)>-1) {
                            onePart.addElement(new Boolean(true));
                        } else {
                            onePart.addElement(new Boolean(false));
                        }
                    }
                }
            }
        }
        return subGroup;
    }

    public String toString() {
        return driverReportHash.toString();
    }

    public void stop() throws com.ibm.sdwb.build390.MBBuildException{
        stopped = true;
        if (mySock != null) mySock.stop();
    }
/*
    public static void main(String[] args) {
        MBSmodDrvrReport smodRep = new MBSmodDrvrReport();
        smodRep.setDR("aparRPT");

        try {
            smodRep.parseReportFile();
            Hashtable groups = smodRep.getGroups();
            Enumeration keys = groups.keys();
            while (keys.hasMoreElements()) {
                String currKey = (String) keys.nextElement();
                Hashtable subGroup = (Hashtable) groups.get(currKey);
                Enumeration keys2 = subGroup.keys();
                while (keys2.hasMoreElements()) {
                    String currKey2 = (String) keys2.nextElement();
                    System.out.println("\n\ngroup = " + currKey2 + "  " + subGroup.get(currKey2));
                }
            }
        }catch (MBBuildException mbe) {
//            System.out.println(mbe.getOriginalException().toString());
            mbe.getOriginalException().printStackTrace();
        }
    }
*/
}
