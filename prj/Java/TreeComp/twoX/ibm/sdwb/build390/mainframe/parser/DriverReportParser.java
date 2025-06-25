package com.ibm.sdwb.build390.mainframe.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.PhaseInformation;
/*****************************************************************/
//02/13/2006       Time to cleanup thie MBDrReport/MBLogDrvrReport class. 
/*****************************************************************/
/* This is a combination of both MBDrReport and MBLogDrReport. 
 This is something that had to cleaned up a while ago. 
/*****************************************************************/


public class DriverReportParser implements com.ibm.sdwb.build390.MBStop,java.io.Serializable {

    static final long serialVersionUID = 1111111111111111L;

    private static final String BEGINBUILDTYPEDEFINITION = new String("Defined Build Types:");
    private static final String BEGINCONTROLSETTINGS = new String("Default Control settings:");
    private static final String PHASESINBUILDTYPE = new String ("Phases in build type ");
    private static final String CMVCREL = new String ("CMVCREL:");
    private static final String DRIVERREPORTKEYWORD = "DRIVERREPORTKEYWORD";
    private static final String AUTOBLDSTRING = "AUTOBLD";
    private static final String LISTGENSTRING = "LISTGEN";
    private static final String RUNSCANSTRING = "RUNSCAN";
    private static final String FORCESTRING   = "FORCE";
    private static final String BUILDCCSTRING = "BUILDCC";
    public static final String THIN_DELTA ="THINDEL";
    private static final String NOLOCKINDICATOR = "NONE";
    private Hashtable driverReportHash = new Hashtable();
    private Vector sourceInfoVector = new Vector();   /*SDWB2406 */
    private File fileToParse;
    private boolean stopped = false;

    public DriverReportParser(File tempFile) {
        fileToParse = tempFile;
    }

    public void parseReport() throws com.ibm.sdwb.build390.GeneralError{
        try {
            BufferedReader driverReportFileReader = new BufferedReader(new FileReader(fileToParse));
            parseReportFile(driverReportFileReader);
            driverReportFileReader.close();
        } catch (IOException ioe) {
            throw new com.ibm.sdwb.build390.GeneralError("Problem reading " + fileToParse.getAbsolutePath(), ioe);
        }
    }

    public void setBuildtypesForDriver(DriverInformation driverInfo) {
        driverInfo.setBuildTypes(getBuildTypes());
    }

    private void parseReportFile(BufferedReader driverReportFileReader)  throws com.ibm.sdwb.build390.GeneralError    {
        String currentLine = null;

        try {
            boolean continueLoop = true;
            String name;
            String value;
            String base ="";
            String path="";
            int modLineCount=-1;
            int[] positionArray = null;

            while (continueLoop & !stopped) {
                continueLoop = false;
                if ((currentLine = driverReportFileReader.readLine()) != null) {
                    /* This is to parse this line 
                    Drivers in base:                                                              
                                BASE         
                    */
                    if (currentLine.trim().endsWith(":")) {
                        continueLoop=true;
                        Vector tempVect = new Vector();
                        Hashtable tempHash = new Hashtable();
                        /*the majorName in the above example will be
                        "Drivers in base:"
                        */
                        String majorName = currentLine.trim();
                        while (((currentLine=driverReportFileReader.readLine()) !=null
                                && currentLine.trim().length() > 0)&& !stopped) {
                            driverReportFileReader.mark(1000);
                            /*This helps to parse a line like this,
                            when a another line which ends with : follows the current one.
                            In the below ex:  "Driver in base" , is followed by "Additional defined .... steps:"
                            Drivers in base:                                                              
                                BASE                                                                            
                            Additional defined data collectors and process steps:                         
                                COLECTRS: 2        
                            */                           
                            if (currentLine.trim().endsWith(":")) {
                                /*Store the old majorName (Driver in base:) and initialize the majorName to point to 
                                the next one  (Additional defined data .... steps:)*/
                                if (!tempVect.isEmpty()) {
                                    driverReportHash.put(majorName, tempVect);
                                    tempVect = new Vector();
                                } else if (!tempHash.isEmpty()) {
                                    driverReportHash.put(majorName, tempHash);
                                    tempHash = new Hashtable();
                                }

                                majorName = currentLine.trim();
                            } else if (currentLine.indexOf(":") > -1) {
                                /*if the line following majorName (Additional defined ... steps:) in our example)
                                contains a : then add it to the hashtable as keyword/value pair
                                if it doesnt contain a : like the example ("Driver in base:")
                                add it to a vector.
                                */
                                int colonIndex = currentLine.indexOf(":");
                                String keyword = currentLine.substring(0, colonIndex).trim();
                                String kvalue = currentLine.substring(colonIndex +1).trim();
                                if (tempVect.isEmpty()) {
                                    tempHash.put(keyword, kvalue);
                                } else {
                                    keyword = currentLine.substring(0, colonIndex+1).trim();
                                    driverReportHash.put(keyword,kvalue);
                                }
                            } else {
                                /*if the line doesnot contain a : then store the full line. 
                                Examples are :
                                1)
                                Dependent delta drivers:                                                      
                                        EK00779                                                                     
                                        MR0153                                                                      
                                2)
                              Phases in build type STD:                                                     
                                    0       CLASS  ORDER                                                       
                                    1       CLASS  MACRO    YNNN4                                              
                                    2       CLASS  *        XXYY8                                              
                                    3       CLASS  *                                                           
                                    4       CLASS  *                                                           
                                */
                                tempVect.addElement(currentLine.trim());
                            }
                        }
                        if (!tempVect.isEmpty()) {
                            driverReportHash.put(majorName, tempVect);
                            tempVect = new Vector();
                        } else if (!tempHash.isEmpty()) {
                            driverReportHash.put(majorName, tempHash);
                            tempHash = new Hashtable();
                        }
                        /* since we have read ahead, we just reset it */
                        driverReportFileReader.reset();
                    } else if (currentLine.indexOf(":") > -1) {
                        continueLoop = true;
                        String mainKey ="";
                        /*Note: The last condition checks for a length of 9, to allow keywords with null value in nolib mode as attached below.
                         FAMILY:   fakelib                                                             
                         FAMADR:                                                                       
                         CMVCREL:  fakeklib */
                        while (currentLine.trim().length() >0 && !(currentLine.trim().endsWith(":") && currentLine.trim().length()>=9)) {
                            if (currentLine.indexOf("=") < 0) {
                                /*this is performed untill a line which ends with : is found or the line is null.
                                Examples: 
                                DRIVER: COPYSENT  */
                                int colonIndex = currentLine.indexOf(":");
                                name = currentLine.substring(0, colonIndex+1).trim();
                                value = currentLine.substring(colonIndex +1).trim();
                                driverReportHash.put(name, value);
                            } else {
                                /* if the line contains an equal examples :
                                DATA:  S=SRC  O=OBJ  M=SYM  L=LST   1=C1  2=C2                                  
                                FLAGS: S=BASE L=LOCAL I=INACTIVE F=FAIL B=BUILD O=BLDOBJ P=PACKAGE              
                                       M=Editor Metadata 
                                       */                                                       
                                String[] splitOnSpaces = splitOnSpaces = currentLine.split("\\s+");
                                Map  tempMap = null;
                                /*we split on spaces and grab the mainKey. in the above examples mainKeys are
                                DATA: and FLAG:
                                */
                                if (currentLine.indexOf(":") > -1) {
                                    mainKey = splitOnSpaces[0];
                                }
                                if (!driverReportHash.containsKey(mainKey)) {
                                    tempMap = new LinkedHashMap();
                                    driverReportHash.put(mainKey,tempMap);
                                } else {
                                    tempMap = (Map) driverReportHash.get(mainKey);
                                }
                                String subKey = "";
                                for (int i=1;i<splitOnSpaces.length;i++) {
                                    /*match for a string=string eg: S=SRC */
                                    if (splitOnSpaces[i].trim().matches("\\w=\\w+")) {
                                        String[] subSplitOnEquals = splitOnSpaces[i].split("=");
                                        /* in our example subKey=S */    
                                        subKey = subSplitOnEquals[0];
                                        String subValue = subSplitOnEquals[1];
                                        tempMap.put(subKey, subValue);
                                    } else {
                                        /* the else take care of the  case where 
                                        "M=Editor Metadata" . ie the value has a space.
                                        In such a case we grab the old subKey and update the same value */
                                        String tempVal = (String)tempMap.get(subKey);
                                        tempMap.put(subKey, tempVal + " " + splitOnSpaces[i]);
                                    }
                                }

                            }

                            driverReportFileReader.mark(1000);
                            currentLine = driverReportFileReader.readLine();
                        }
                        /*the reset/mark is done to roll back a line. Because when we loop out, we would be doing
                        a another readLine in the while loop at the top again.
                        After the DATA: and FLAG: line an empty line occurs, we exit out at that point.
                        */
                        driverReportFileReader.reset();

                    } else if (currentLine.indexOf("=") >= 0) {
                        continueLoop = true;
                        /*This is to split the line which contains library path names. eg:
                          BASE=CRC.COM                                                                  
                          PATH=MISC/ 
                          the library pathname occurs in the report if PATHNAME=YES is used.
                          */                                                                   
                        String[] temp = currentLine.split("=");
                        if (base.trim().length() > 0) {
                            path = temp[1].trim();
                        } else {
                            base=  temp[1].trim();
                        }
                    } else {
                        //should be mod class data line.
                        if (currentLine.trim().length() >0) {
                            modLineCount++;
                        }
                        /*The first mod class line is the header (modLineCount = 0), we just grab its position and store 
                        it in an array.  */
                        if (modLineCount==0) {
                            String[] temp = currentLine.trim().split("\\s+");
                            positionArray = new int[temp.length +1];
                            int j=0;
                            for (j=0;j<temp.length;j++) {
                                positionArray[j] = currentLine.indexOf(temp[j]);
                            }

                            positionArray[j] = currentLine.length();
                            driverReportHash.put("PARTS_HEADER",currentLine);

                        }
                        /* if modLineCount is greater than zero, then it should be library parts line 
                        example :
                            CRC      COMBIN   53200136 00756582 S           B    1.1                        
                        */
                        if (positionArray!=null && modLineCount > 0) {
                            Vector tempVector = new Vector();
                            /*if path/base exists or no, add it to the vector as element 0,1 */
                            tempVector.add(path);
                            tempVector.add(base);
                            /* grab the mod/class and others and store it to the vector.
                            Note: A split on spaces WILL NOT WORK ! 
                            */
                            for (int k=0;k<positionArray.length-1; k++) {
                                tempVector.add(currentLine.substring(positionArray[k],positionArray[k+1]).trim());
                            }
                            sourceInfoVector.addElement(tempVector);
                            base="";
                            path="";
                        }
                        continueLoop = true;
                    }


                }
            }
            driverReportFileReader.close();
        } catch (IOException ioe) {
            throw new com.ibm.sdwb.build390.GeneralError("There was a problem reading file "+fileToParse.getAbsolutePath(), ioe);
        }
    }


    /** getLock searches the file object for the lock setting
    * @return boolean representation of the setting */
    public boolean getLock() {
        String lock = (String)driverReportHash.get("LOCK:");
        if (lock != null)
            if (lock.indexOf("ON") > -1) return true;
        return false;
    }

    /** getMergeOnlysearches the file object for the lock setting
* @return boolean representation of the setting */
    public boolean getMergeOnly() {
        String mergonly = (String)driverReportHash.get("MERGONLY:");
        if (mergonly==null) {
            return true;
        }
        if (mergonly != null && mergonly.indexOf("ON") > -1) {
            return true;
        }
        return false;
    }

    /** getControl searches the file object for the control setting
    * @return boolean representation of the setting */
    // CONTROL: OFF
    public boolean getControl() {
        String cntl = (String) driverReportHash.get("CONTROL:");
        if (cntl != null)
            if (cntl.indexOf("ON") > -1) return true;
        return false;
    }

    /** getDriver searches the file object for the driver setting
    * @return driver string */
    // DRIVER:  GA
    public String getDriver() {
        String drv = (String) driverReportHash.get("DRIVER:");
        return drv;
    }

    /** getDriverType searches the file object for the driver type setting
    * @return driver type string */
    // DRVRTYPE:  GA
    //#Defect363:  Fix the thin delta driver check
    public String getDriverType() {
        return(String) driverReportHash.get("DRVRTYPE:");
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

    /** getLibRel searches the file object for the CMVC release setting
    * @return cmvc release string */
    //CMVCREL: adsm310s.all
    public String getLibRel() {
        String rel = (String) driverReportHash.get(CMVCREL);
        return(rel);
    }

    public Vector getDataTypes() {
        Vector tempVector = new Vector();
        for (Iterator iter=getDataDefinitions().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            tempVector.addElement((String)entry.getValue());
        }
        return tempVector;
    }

    public Vector getDataSymbols() {
        Vector tempVector = new Vector();
        for (Iterator iter=getDataDefinitions().entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            tempVector.addElement((String)entry.getKey());
        }
        return tempVector;
    }

    /** getDataDefinitions searches the file object for the data definitions
    * @return dataDefinitions Hashtable */
    public Map getDataDefinitions() {
        return(Map) driverReportHash.get("DATA:");
    }

    /** getDataDefinitions searches the file object for the data definitions
    * @return dataDefinitions Hashtable */
    public Map getFlagDefinitions() {
        return(Map) driverReportHash.get("FLAG:");
    }


    /** getBuildid searches the file object for the buildid setting
    * @return buildid string */
    // BUILDID: B80143C9
    public String getBuildid() {
        String bid = (String) driverReportHash.get("BUILDID:");
        return(bid);
    }

    /** getisData searches the file object for the 'Drivers in base:' string
    * @return boolean true if found */
    // Drivers in base:
    public boolean getisDelta() {
//		String drv = (String) driverReportHash.get("Drivers in base:");
        return(driverReportHash.get("Drivers in base:") != null);
//		return(true);
    }

    /** getBuildTypes searches the file object for the supported build types.
    * @return Vector containing the build types */
    public Vector getBuildTypes() {
        return(Vector) driverReportHash.get(BEGINBUILDTYPEDEFINITION);
    }

    /** getNextUsermod constructs the next usermod name
    * @return String containing the build types */
    public String getNextUsermod() {
        Hashtable smodStuff = (Hashtable) driverReportHash.get("SYSMOD Control settings:");
        String usermodName = (String) smodStuff.get("USERMOD");      
        return usermodName;
    }

    public String getAutobuildSetting() {
        return(String) getDefaultControlSettings().get(AUTOBLDSTRING);
    }

    public String getListingGenerateSetting() {
        return(String) getDefaultControlSettings().get(LISTGENSTRING);
    }

    public String getRunScannersSetting() {
        return(String) getDefaultControlSettings().get(RUNSCANSTRING);
    }

    public String getForceSetting() {
        return(String) getDefaultControlSettings().get(FORCESTRING);
    }

    public int getBuildCCSetting() {
        return Integer.parseInt((String) getDefaultControlSettings().get(BUILDCCSTRING));
    }

    /** Get the Default control settings
    * convert 5 lines text to compact 5 character string the same format of
    * phase override string
    * @return setting string
    */
    public Map getDefaultControlSettings() {
        /*
        * Driver report format:
        *    Default Control settings:
        *      AUTOBLD: YES
        *      LISTGEN: YES
        *      RUNSCAN: YES
        *      FORCE:   NO
        *      BUILDCC: 4
        */
        return(Map) driverReportHash.get(BEGINCONTROLSETTINGS); // concatenate default setting string
    }


    /**
    * Get phase override settings
    *@return array of phase override setting strings
    *each string has buildType, phaseNumber, phaseOverride
    */
    public List getPhaseInforamtion(String bldType) {
        /*
         The first line of each phase specifies the build type and
         the first column is the phase number and the 4th column is override for
         that phase.
         Driver report format:
         Phases in build type DEVTEST:
            0    CLASS  ORDER
            1    CLASS  MACRO
            2    CLASS  REBUILD
            3    CLASS  MODINT
            4    CLASS  BLDCMPLR
            5    CLASS  MSG
            6    CLASS  MAKEMSG
            7    CLASS  MACINT
            8    CLASS  *        XNNX8
            9    CLASS  DSMPLINK XXXX8
        */
        String methodName = new String("MBDrReport:getPhaseOverride");
        List phaseInfos = new ArrayList();
        Vector buildTypePhaseVector = (Vector) driverReportHash.get(PHASESINBUILDTYPE+bldType+":");
        if (buildTypePhaseVector != null) {
            Enumeration phaseEnum = buildTypePhaseVector.elements();
            while (phaseEnum.hasMoreElements()) {
                PhaseInformation onePhase = parsePhaseInformation((String) phaseEnum.nextElement());
                phaseInfos.add(onePhase);
            }
        }
        return  phaseInfos;
    }

    private PhaseInformation parsePhaseInformation(String phaseLine) {
        StringTokenizer phaseLineParser = new StringTokenizer(phaseLine);
        String phaseNumberString = phaseLineParser.nextToken();
        String phaseToCheckBeforeString = null;
        if (phaseNumberString.indexOf("/") > -1) {
            StringTokenizer phaseNumberParser = new StringTokenizer(phaseNumberString, "/");
            phaseNumberString = phaseNumberParser.nextToken();
            phaseToCheckBeforeString = phaseNumberParser.nextToken();
        }
        String className = phaseLineParser.nextToken();  // donot eat the class token TST3145
        String phaseName = phaseLineParser.nextToken();
        String overrideString = null;
        if (phaseLineParser.hasMoreTokens()) {
            overrideString = phaseLineParser.nextToken();
        }
        PhaseInformation phaseInfo = new PhaseInformation(phaseName, className, Integer.parseInt(phaseNumberString));
        if (phaseToCheckBeforeString !=null) {
            phaseInfo.setPhaseToHaltOnIfErrorsFound(Integer.parseInt(phaseToCheckBeforeString)-1);
        }
        if (overrideString!=null) {
            phaseInfo.setPhaseOverrideString(overrideString);
        }
        return phaseInfo;
    }



    public void setBuildOptions(com.ibm.sdwb.build390.info.BuildOptions options, String buildType) {
        setPhaseOverrides(options, buildType);
        options.setControlled(getControl());
    }

    public void setBuildInformation(com.ibm.sdwb.build390.MBBuild build) {
        build.setLocked(getBuildid());
        setBuildOptions(build.getOptions(), build.get_buildtype());
    }

    public void doDriverLockCheck(com.ibm.sdwb.build390.MBBuild build) throws GeneralError{
        if (build.getBuildIDLock() != null) {
            if (!getBuildid().equals(NOLOCKINDICATOR) & !getBuildid().equals(build.get_buildid()) & !build.getBuildIDLock().equals(NOLOCKINDICATOR)) {
                throw new GeneralError("Driver is locked by Build "+getBuildid() + "\nRun Reports - Driver Reports - Builds for more information.\n");
            }
        }
    }

    public boolean isDriverLocked(com.ibm.sdwb.build390.MBBuild build) {
        if (build.getBuildIDLock() != null) {
            if (!getBuildid().equals(NOLOCKINDICATOR) & !getBuildid().equals(build.get_buildid()) & !build.getBuildIDLock().equals(NOLOCKINDICATOR)) {
                return true;
            }
        }
        return false;
    }


    private void setPhaseOverrides(com.ibm.sdwb.build390.info.BuildOptions options, String buildtype) {
        List phases = getPhaseInforamtion(buildtype);
        if (!phases.isEmpty()) {
            int[] newOverrides = new int[phases.size()+1];
            for (Iterator phaseIterator = phases.iterator(); phaseIterator.hasNext();) {
                PhaseInformation onePhase = (PhaseInformation) phaseIterator.next();
                newOverrides[onePhase.getPhaseNumber()] = -1;
                if (onePhase.getPhaseOverrides() !=null) {
                    String tempOverride = onePhase.getPhaseOverrides().substring(onePhase.getPhaseOverrides().length()-1, onePhase.getPhaseOverrides().length());
                    try {
                        int override = Integer.parseInt(tempOverride);
                        newOverrides[onePhase.getPhaseNumber()] = override;
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
            options.setBuildCCPhaseOverrides(newOverrides);
        }
    }

    public boolean containsLibrarySourceName() {
        return driverReportHash.containsKey("PATHNAME:");
    }

    public String getPartsHeader() {
        return(String)driverReportHash.get("PARTS_HEADER");
    }


    /** getAllPartInfo searches the file object for the part information
   * @return allPartInfo Vector */
    public Vector getPartsVector() {
        return sourceInfoVector;
    }



    public Vector getPartsInfo() {
        Vector partsInfo = new Vector();
        for (Iterator iter=getPartsVector().iterator();iter.hasNext();) {
            Vector singleElement = (Vector)iter.next();
            int i=-1;
            String path ="";
            String base = "";
            if (containsLibrarySourceName()) {
                i++;
                path = (String)singleElement.elementAt(i);
                i++;
                base = (String)singleElement.elementAt(i);
            }
            i++;
            String mvsPartName  = (String)singleElement.elementAt(i);
            i++;
            String mvsPartClass = (String)singleElement.elementAt(i);
            String reportFlag ="";
            String version ="";
            if (singleElement.size() >= (i+5)) {
                reportFlag   = (String)singleElement.elementAt(i+4);
                version      = (String)singleElement.elementAt(i+5);
            }
            com.ibm.sdwb.build390.info.FileInfo info = new com.ibm.sdwb.build390.info.FileInfo(path,base);

            info.setTypeOfChange(reportFlag);
            info.setMainframeFilename(mvsPartClass + "."+ mvsPartName);
            info.setVersion(version);
            info.setProject(getLibRel());
            info.setVersion(version);
            partsInfo.addElement(info);
        }
        return partsInfo;
    }

    public void stop() throws com.ibm.sdwb.build390.MBBuildException{
        stopped = true;
    }


    public String toString() {
        StringBuffer strb = new StringBuffer();
        for (Iterator iter=driverReportHash.entrySet().iterator();iter.hasNext();) {
            strb.append((Map.Entry)iter.next());
            strb.append("\n");
        }
        return strb.toString();
    }



    public static void main(String args[]) {
        if (args.length <= 0) {
            args = new String[1];
            args[0]= "test.out";
        }
        try {

            DriverReportParser parser =new DriverReportParser(new File(args[0]));
            parser.parseReport();
            System.out.println("====================== output ========================");
            System.out.println(parser.toString());
            System.out.println("======================================================");
            for (Iterator iter=parser.getPartsVector().iterator();iter.hasNext();) {
                System.out.println((Vector)iter.next());
            }
            System.out.println("======================================================");

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }



}



