package com.ibm.sdwb.build390.metadata.utilities;
//******************************************************************************
/* MBMetadataValueGenerator parses metadata files to get values and settings.
//******************************************************************************
//07/13/99		     just get values from this
//01/07/2000 ind.build.log   changes to write the build details into a individual build log file
//03/07/2000 reworklog       changes to write the log stuff using listeners
//05/16/2000 UBUILD_METADATA new constructor  for Metadataedit
//06/04/2000 UBUILD_METADATA changes to restore old metadata
//07/16/2003 DEF.TST1319:    Metadata Editor does not allow edit of all switches fields
//03/04/2004 PTM3380         Metadata Editor problem invalid values
//02/22/2005 TST2094         When there are no OPUPD values, the hash stored "PART HAS NO UPD VALUES"
//******************************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.sdwb.build390.AlphabetizedVector;
import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;
import com.ibm.sdwb.build390.metadata.info.GeneratedMetadataInfo;
import com.ibm.sdwb.build390.metadata.info.MetadataFormatInfo;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;


public class MetadataValueGenerator {



    private Map multipleEntryKeys = new HashMap();

    private static final String OPUPDATESTART = "Metadata from OP=UPD:";
    private static final String BUILDORDERSTART = "BLDORDER local Metadata:";
    private static final String PROFILESTART = "Metadata set by profile";
    private static final String RELATEDPARTSTART = "Related Metadata Part:";
    private static final String EMBEDDEDSTART = "Embedded Metadata follows:";
    private static final String GENERALSTART = "Full Metadata report follows:";
    private static final String LIBRARY_START = "Metadata from Keywords (CMVC):";
    private static final String BUILT_LIBRARY_BASENAME = "Base Name:";
    private static final String BUILT_LIBRARY_NAME = "Path:";
    private static final String BUILT_LIBRARY_DIRECTORY = "Dir:";
    private static final String BUILT_LIBRARY_VERSION = "Version:";
    private static final String BUILT_METADATA_VERSION = "Metadata version:";


    private static final int OPUPDATE = 1;
    private static final int BUILDORDER = 2;
    private static final int PROFILE = 3;
    private static final int RELATEDPART = 4;
    private static final int EMBEDDED = 5;
    private static final int CMVCSECTION = 6;
    private static final int GENERAL = 7;


    private MetadataOperationsInterface metadataHandler;
    private GeneratedMetadataInfo generatedMetadataInfo;

    /** for a userbuild, we have to send fileInfo as null.  and  metadatahandler should be null .
     * use setLibraryMetadata(..) to setup library metadata.
    **/
    public MetadataValueGenerator(String fileName, UserCommunicationInterface tempComm) throws com.ibm.sdwb.build390.MBBuildException{
        this(fileName,null,null,tempComm);
    }

    public MetadataValueGenerator(String fileName, FileInfo fileInfo, MetadataOperationsInterface metadataHandler,UserCommunicationInterface tempComm) throws com.ibm.sdwb.build390.MBBuildException{
        try {
            generatedMetadataInfo = new GeneratedMetadataInfo(fileName,fileInfo);
            this.metadataHandler= metadataHandler;
            parse();
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred while trying to read the metadata format file.", ioe, new File(fileName));
        }
    }

    public GeneratedMetadataInfo getGeneratedMetadataInfo() {
        return generatedMetadataInfo;
    }

    /** for a userbuild, we have to send fileInfo as null.  and setLibraryMetadata(..)
    **/
    public void setLibraryMetadata(Map libraryMetadataMap) {

        if (libraryMetadataMap!=null) {
            libraryMetadataMap = formatMainframeEntries(libraryMetadataMap);


            Iterator multiKeysEnum = multipleEntryKeys.keySet().iterator();
            while (multiKeysEnum.hasNext()) {
                String currRealKey = (String) multiKeysEnum.next();
                String currShortKey = (String) multipleEntryKeys.get(currRealKey);
                fixMultipleEntry(libraryMetadataMap, currRealKey, currShortKey);
            }

        } else {
            libraryMetadataMap = new HashMap();
        }

        generatedMetadataInfo.setLibraryMetadataMap(libraryMetadataMap);

    }

    public void setFileInfo(FileInfo tempInfo) {
        generatedMetadataInfo.setFileInfo(tempInfo);
    }

/*
    parseFile - break up the file into keyword value pairs, and put them into the appropriate
    hashtable based on the field type.
*/
    private void parse() throws IOException, MBBuildException{
        BufferedReader source = new BufferedReader(new FileReader(generatedMetadataInfo.getMetadataReportName()));
        String currLine = null;
        int currentFileSection = -1;

        String builtLibraryBaseName = null;
        String builtLibraryName = null;
        String builtLibraryDirectory= null;
        String builtLibraryVersion = null;
        String profileName = null;
        String builtMetadataVersion = null;

        IgnoreOPUPDLineCriteria criteria  = new IgnoreOPUPDLineCriteria();
        while ((currLine = source.readLine()) != null) {
            currLine = currLine.trim();
            if (currLine.length() > 0) {
                int eqIndex = -1;
                if ((eqIndex = currLine.indexOf("="))>-1 && !criteria.passes(currLine)) { /*This is needed to filter this string "Part has no defined OP=UPD Metadata" */
                    StringTokenizer tempToke = new StringTokenizer(currLine, "=");
                    String key = tempToke.nextToken();
                    String value = currLine.substring(eqIndex+1).trim();
                    if (value.startsWith("\'") & value.endsWith("\'")) {
                        value = value.substring(1, value.length()-1);
                    }
                    switch (currentFileSection) {
                    case OPUPDATE:
                        generatedMetadataInfo.getOpUpdateMetadataMap().put(key, value);
                        break;

                    case BUILDORDER:
                        generatedMetadataInfo.getBuildorderMetadataMap().put(key, value);
                        break;

                    case PROFILE:
                        generatedMetadataInfo.getProfileMetadataMap().put(key, value);
                        break;
                    case RELATEDPART:
                        generatedMetadataInfo.getMVSRelatedPartMetadataMap().put(key, value);
                        break;
                    case EMBEDDED:
                        generatedMetadataInfo.getEmbeddedMetadataMap().put(key, value);
                        break;
                    case CMVCSECTION:
                        generatedMetadataInfo.getBuiltLibraryMetadataMap().put(key,value);
                        break;
                    case GENERAL:
                        generatedMetadataInfo.getGeneralMetadataMap().put(key, value);
                        break;
                    default:
                        generatedMetadataInfo.getNOTMetadataMap().put(key,value);
                        break;
                    }
                } else if (currLine.startsWith(OPUPDATESTART)) {
                    currentFileSection = OPUPDATE;
                } else if (currLine.startsWith(BUILDORDERSTART)) {
                    currentFileSection = BUILDORDER;
                } else if (currLine.startsWith(PROFILESTART)) {
                    profileName = currLine.substring(PROFILESTART.length()).trim();
                    currentFileSection = PROFILE;
                } else if (currLine.startsWith(RELATEDPARTSTART)) {
                    currentFileSection = RELATEDPART;
                } else if (currLine.startsWith(EMBEDDEDSTART)) {
                    currentFileSection = EMBEDDED;
                } else if (currLine.startsWith(LIBRARY_START)) {
                    currentFileSection = CMVCSECTION;
                } else if (currLine.startsWith(GENERALSTART)) {
                    currentFileSection = GENERAL;
                } else if (currentFileSection == RELATEDPART) {
                } else if (currLine.endsWith("):")) {
                    int closeParenIndex = currLine.lastIndexOf("):");
                    int openParenIndex = currLine.lastIndexOf("(");
                    String englishName = currLine.substring(0, openParenIndex).trim();
                    String formatParms = currLine.substring(openParenIndex+1, closeParenIndex);
                    MetadataCategoryInfo parsedFormat = getCategoryInfo(formatParms);
                    boolean continueSearch = true;
                    String keywordName = null;
                    while (continueSearch) {
                        currLine = source.readLine();
                        if (currLine != null) {
                            currLine = currLine.trim();
                            if (currLine.length() > 0) {
                                int localEqIndex = -1;
                                if ((localEqIndex = currLine.indexOf("="))>-1) {
                                    continueSearch = false;
                                    StringTokenizer tempToke = new StringTokenizer(currLine, "=");
                                    String key = tempToke.nextToken();
                                    String value = currLine.substring(localEqIndex+1).trim();
                                    if (value.startsWith("\'") & value.endsWith("\'")) {
                                        value = value.substring(1, value.length()-1);
                                    }
                                    keywordName = key;
                                    if (parsedFormat.fullKeywordName ==null ) {
                                        parsedFormat.fullKeywordName = keywordName;
                                    }
                                    generatedMetadataInfo.getGeneralMetadataMap().put(key, value);
                                }
                            }
                        } else {
                            continueSearch = false;
                        }
                    }
                    if (parsedFormat.isEntryType()) {
                        if (!parsedFormat.fullKeywordName.equals(keywordName)) {
                            keywordName = keywordName.substring(0, keywordName.length()-1);
                        }
                        if (parsedFormat.reps > 1) {
                            multipleEntryKeys.put(parsedFormat.fullKeywordName, keywordName);
                        }
                        generatedMetadataInfo.getFormatInfo().addEntryField(englishName, parsedFormat.fullKeywordName, keywordName, parsedFormat.reps, parsedFormat.length, parsedFormat.readOnly, parsedFormat.caseSensitive, parsedFormat.quoted);
                    } else if (parsedFormat.isAccumType()) {
                        generatedMetadataInfo.getFormatInfo().addAccumField(englishName, parsedFormat.fullKeywordName, keywordName, parsedFormat.readOnly, parsedFormat.caseSensitive, parsedFormat.quoted);
                    } else if (parsedFormat.isSwitchType()) {
                        generatedMetadataInfo.getFormatInfo().addSwitchField(englishName, parsedFormat.fullKeywordName, keywordName, parsedFormat.readOnly, parsedFormat.caseSensitive, parsedFormat.quoted);
                    }
                    generatedMetadataInfo.getAllKeywords().add(parsedFormat.fullKeywordName);
                } else if (currLine.startsWith(BUILT_LIBRARY_BASENAME)) {
                    builtLibraryBaseName = currLine.substring(BUILT_LIBRARY_BASENAME.length()).trim();
                } else if (currLine.startsWith(BUILT_LIBRARY_NAME)) {
                    builtLibraryName = currLine.substring(BUILT_LIBRARY_NAME.length()).trim();
                } else if (currLine.startsWith(BUILT_LIBRARY_VERSION)) {
                    builtLibraryVersion = currLine.substring(BUILT_LIBRARY_VERSION.length()).trim();
                } else if (currLine.startsWith(BUILT_METADATA_VERSION)) {
                    builtMetadataVersion = currLine.substring(BUILT_METADATA_VERSION.length()).trim();
                } else if (currLine.startsWith(BUILT_LIBRARY_DIRECTORY)) {
                    builtLibraryDirectory = currLine.substring(BUILT_LIBRARY_DIRECTORY.length()).trim();
                }
            }
        }


        FileInfo builtInfo = makeFileInfo(builtLibraryDirectory, builtLibraryName,builtLibraryBaseName);
        builtInfo.setVersion(builtLibraryVersion);
        builtInfo.setMetadata(generatedMetadataInfo.getBuiltLibraryMetadataMap());
        builtInfo.setMetadataVersion(builtMetadataVersion);
        generatedMetadataInfo.setBuiltFileInfo(builtInfo);

        if (generatedMetadataInfo.shouldPopulateLibraryMetadata()) {
            Set infosSet = new HashSet();
            infosSet.add(generatedMetadataInfo.getFileInfo());
            metadataHandler.populateMetadataMapFieldOfPassedInfos(infosSet);
            generatedMetadataInfo.setLibraryMetadataMap(((FileInfo)infosSet.iterator().next()).getMetadata());
        }


        Map libraryMetadataMap = generatedMetadataInfo.getLibraryMetadataMap();            
        if (libraryMetadataMap!=null || !libraryMetadataMap.isEmpty()) {
            libraryMetadataMap = formatMainframeEntries(libraryMetadataMap);
            generatedMetadataInfo.setLibraryMetadataMap(libraryMetadataMap);
        }


        Iterator multiKeysEnum = multipleEntryKeys.keySet().iterator();
        while (multiKeysEnum.hasNext()) {
            String currRealKey = (String) multiKeysEnum.next();
            String currShortKey = (String) multipleEntryKeys.get(currRealKey);
            fixMultipleEntry(generatedMetadataInfo.getGeneralMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getOpUpdateMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getBuildorderMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getProfileMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getMVSRelatedPartMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getEmbeddedMetadataMap(), currRealKey, currShortKey);
            fixMultipleEntry(generatedMetadataInfo.getLibraryMetadataMap(), currRealKey, currShortKey);
        }
    }

    //To-DO: later :we should look at parsing the SRCPART from the report and stick in later as the mainframname. (if one exists);
    private FileInfo makeFileInfo(String libraryDirectory, String libraryName, String libraryBaseName) {
        //happens when part is not in built, and the part doesn't contain a directory in library.
        if (libraryBaseName ==null) {
            return new FileInfo(libraryDirectory,libraryName);
        }


        //happens when part is built, and the part may or maynot contain a directory in library.
        if (libraryDirectory ==null) {
            if (libraryName==null) {
                //happens when part is built, and the part doesnot contain a directory in library.
                return new FileInfo(libraryDirectory,libraryBaseName);
            } else {
                //happens when part is built, and the part does contain a directory in library.
                return new FileInfo(libraryName,libraryBaseName);
            }
        }

        //happens when part is not built, and the part does contain a directory in library.
        if(libraryName!=null && libraryName.equals(libraryDirectory)){
            return new FileInfo(libraryName, libraryBaseName);
        }
        //happens when part is not built, and the part doesn't contain a directory in library.
        return new FileInfo(libraryBaseName, libraryName);

    }


/*
    fixMultipleEntry - keywords on mulitple entries are P1, P2, P3.  The keyword name is actaully
    PFULL (For instance)  So stick P1, P2, P3 in a vector (the values, not the keywords) and associate
    that vector.
*/
    private void fixMultipleEntry(Map hashToFix, String currRealKey, String currShortKey) {
        Vector allValues = new Vector();
        boolean wasFixed = false;
        for (int i = 1; hashToFix.containsKey(currShortKey+Integer.toString(i));i++) {
            wasFixed = true;
            allValues.addElement(hashToFix.get(currShortKey+i));
            hashToFix.remove(currShortKey+i);
        }
        if (wasFixed) {
            if (allValues.size() == 1) {
                if (allValues.elementAt(0) == null) {
                    allValues = new Vector();
                } else if (allValues.elementAt(0).toString().trim().length() < 1) {
                    allValues = new Vector();
                }
            }
            hashToFix.put(currRealKey, allValues);
        }
    }

/*
    given a string with the format information for a field, parse it.
*/
    private MetadataCategoryInfo getCategoryInfo(String unparsedFormatInfo) {
        MetadataCategoryInfo oneFormat =  new MetadataCategoryInfo();
        String dimensionString = null;
        String fullKeywordName = null;
        String flags = null;
        StringTokenizer dashToke = new StringTokenizer(unparsedFormatInfo, "-");
        String firstToke = dashToke.nextToken().trim();
        if (dashToke.hasMoreElements()) {
            String secondToke = dashToke.nextToken().trim();
            if (dashToke.hasMoreElements()) {
                String thirdToke = dashToke.nextToken().trim();
                fullKeywordName = firstToke;
                dimensionString = secondToke;
                flags = thirdToke;
            } else {
                //Begin #DEF.TST1319:
                fullKeywordName = firstToke;
                dimensionString = secondToke;
                /*
                dimensionString = firstToke;
                flags = secondToke;
                */
                //End #DEF.TST1319:
            }
        } else {
            dimensionString = firstToke;
        }

        int dimDivider = dimensionString.indexOf("*");
        if (dimDivider > -1) {
            oneFormat.setEntryType();
            oneFormat.length = Integer.decode(dimensionString.substring(0, dimDivider).trim()).intValue();
            oneFormat.reps = Integer.decode(dimensionString.substring(dimDivider+1, dimensionString.length()).trim()).intValue();
        } else if (dimensionString.equals("SW")) {
            oneFormat.setSwitchType();
        } else if (dimensionString.equals("ACCUM")) {
            oneFormat.setAccumType();
        }
        if (fullKeywordName != null) {
            oneFormat.fullKeywordName=fullKeywordName;
        }
        if (flags != null) {
            oneFormat.readOnly = (flags.indexOf("R")>=0);
            oneFormat.caseSensitive = !(flags.indexOf("F")>=0);
            oneFormat.quoted = (flags.indexOf("Q")>=0);
        }
        return oneFormat;
    }





    private Map  formatMainframeEntries(Map inputMap) {

        Map  localMap  = new HashMap();
        for (Iterator iter=inputMap.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();

            String keyword = ((String)entry.getKey()).trim();
            String value   = ((String)entry.getValue());
            if (value!=null) {
                value = stripMainframeFormat(value.trim());
                localMap.put(keyword,value);
            }

        }
        return localMap;
    }



    private static String stripMainframeFormat(String origString) {
        String returnString = origString;
        if (returnString.startsWith("\'") & returnString.endsWith("\'")) {
            returnString = returnString.substring(1, returnString.length()-1);
        }
        returnString = replaceSubstring(returnString, "\"\"", "\"");
        returnString = replaceSubstring(returnString, "\'\'", "\'");
        returnString = replaceSubstring(returnString, "&&", "&");
        return returnString;
    }

    private static String replaceSubstring(String sourceString, String toReplace, String replacement) {
        int currIndex = 0;
        int currLoc =0;
        String returnString = new String();
        while ((currLoc = sourceString.indexOf(toReplace, currIndex)) >= 0) {
            returnString += sourceString.substring(currIndex, currLoc)+replacement;
            currIndex = currLoc + toReplace.length();
        }
        if (currIndex < sourceString.length()) {
            returnString += sourceString.substring(currIndex, sourceString.length());
        }
        return returnString;
    }

    private class MetadataCategoryInfo {
        public String fullKeywordName = null;
        public int length = 0;
        public int reps = 0;
        public boolean readOnly = false;
        public boolean caseSensitive = true;
        public boolean quoted = false;

        private int dataType = -1;
        private int ENTRYTYPE = 0;
        private int SWITCHTYPE = 1;
        private int ACCUMTYPE = 2;


        public void setEntryType() {
            dataType = ENTRYTYPE;
        }

        public void setAccumType() {
            dataType = ACCUMTYPE;
        }

        public void setSwitchType() {
            dataType = SWITCHTYPE;
        }

        public boolean isEntryType() {
            return dataType == ENTRYTYPE;
        }

        public boolean isAccumType() {
            return dataType == ACCUMTYPE;
        }

        public boolean isSwitchType() {
            return dataType == SWITCHTYPE;
        }

        public String toString() {
            return "MetadataCategoryInfo=[Name="+fullKeywordName+", Length="+length+", Reps="+reps+", DataType="+dataType +", ReadOnly="+readOnly +", CaseSensitive="+ caseSensitive+", Quoted="+quoted+"]\n";
        }

    }




    private class IgnoreOPUPDLineCriteria implements FilterCriteria {

        private  final Vector ignoreList = new Vector();

        private IgnoreOPUPDLineCriteria() {
            ignoreList.addElement("Metadata from OP=UPD:");
            ignoreList.addElement("Part has no defined OP=UPD Metadata");
        }

        public boolean passes(Object o) {
            String line = (String)o;
            return ignoreList.contains(line);
        }
    }



    public static void main(String[] args) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



