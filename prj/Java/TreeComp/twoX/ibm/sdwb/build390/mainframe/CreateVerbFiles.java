package com.ibm.sdwb.build390.mainframe;

import java.text.*;
import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;

//********************************************************************
//07/12/2003 #DEF.TST1307: Build does not store metadata set by MDE
//07/15/2003 #DEF.TST1323: Incorrect metadata update due to format of parlist.verb file
//08/19/2003 #DEF.TST1380: MDE changes not stored in unimodc for user build
//09/02/2003 #DEF.TST1463: Arry outof bounds on Fastrack using PDS mods
//09/08/2003 #DEF.TST1492: Fastrack fails with PDS
//********************************************************************

public class CreateVerbFiles implements java.io.Serializable {
    static final long serialVersionUID = -2656152589578266198L;


    private static final String LOADORDERFILE   = "LODORDER.ORD";

    private static final String PDSLOADORDERFILE   = "LODORDER";//INT3097C

    private static final String FILEWASMISSING = "*ERROR* PART NOT LOADED";
    private transient LogEventProcessor lep=null;
    private boolean partlistEmpty = true;
    private boolean buildOrderUpdated = false;
    private boolean buildUpToDate = true;
    private boolean loadOrderUpdated = false;
    private String pdsFileVersion=null;

    public CreateVerbFiles(LogEventProcessor lep) {
        this.lep=lep;
    }


    public void makeLoadOrderAndPartlistShadowCheckVerbFile(MBBuild build, Writer loadOrderWriter, Writer partlistCheckWriter) throws com.ibm.sdwb.build390.MBBuildException{
        try {
            partlistEmpty = true;
            loadOrderUpdated = false;
            String fileHeader = createVerbFileHeader(build);
            partlistCheckWriter.write(fileHeader);
            int partIndex  = 1;   // used for fasttrack
            for(Iterator partIterator = build.getPartInfoSet().iterator();partIterator.hasNext(); partIndex++) {
                partlistEmpty = false;
                FileInfo partData = (FileInfo) partIterator.next();

                String onePartStanza = null;

                //Begin INT3097C                     
                boolean isPDSBuild = false;

                boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

                if(build instanceof MBUBuild) {

                    isPDSBuild = ((MBUBuild) build).getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
                }

                if((partData.getName().equalsIgnoreCase(LOADORDERFILE)|(partData.getName().equalsIgnoreCase(PDSLOADORDERFILE) & isPDSBuild & isFakeLib)) & !loadOrderUpdated) {
                    //End INT3097C

                    // For the load order check file we put in the location as well so we don't have to upload another file if the loadorder is missing.
                    // Since we are only checking one file, we know if it isn't there (in the shadow) we'll want to load all the files in the query file
                    if(loadOrderWriter !=null) { /** ken-verify FastTrack sends it as null  **/ 
                        loadOrderUpdated=true;
                        onePartStanza = createPartStanzaForVerbFile(partData, build.get_buildid()+"."+partData.getMainframeFilename(),true, !isFasttrack(build),isPDS(build), 0);
                        loadOrderWriter.write(fileHeader);
                        loadOrderWriter.write(onePartStanza);

                        // Since we have many files, and some may be loaded an some not, we don't put in the LOC.  If we try to reuse this file to actually load files into the shadow
                        // we would get erros for files specified here that were already loaded.                        
                        String loadOrderPartStanza = createPartStanzaForVerbFile(partData, null, true, !isFasttrack(build),isPDS(build), partIndex);
                        partlistCheckWriter.write(loadOrderPartStanza);

                    }
                    else {   /** ken-verify , for fastrack, donot send Loadorder.ord file, if one is there reduce partIndex by 1 **/
                        partIndex--;  /** ken-verify **/
                    }/** ken-verify **/
                }
                else {
                    // Since we have many files, and some may be loaded an some not, we don't put in the LOC.  If we try to reuse this file to actually load files into the shadow
                    // we would get erros for files specified here that were already loaded.
                    onePartStanza = createPartStanzaForVerbFile(partData, null, true, !isFasttrack(build),isPDS(build), partIndex);
                    partlistCheckWriter.write(onePartStanza);
                }
            }
            if(isFasttrack(build)) {
                partlistCheckWriter.write("TOTPARTS="+(partIndex-1));
            }
        }
        catch(IOException ioe) {
            throw new GeneralError("An error occurred while writing the loadorder and partlist check verb files", ioe);
        }
    }

    public void makePartlistShadowLoadFile(MBBuild build, Set filesToLoad, Writer shadowLoadWriter)throws com.ibm.sdwb.build390.MBBuildException {
        try {
            if(!filesToLoad.isEmpty()) {
                String fileHeader = createVerbFileHeader(build);
                shadowLoadWriter.write(fileHeader);
                for(Iterator fileIterator = filesToLoad.iterator(); fileIterator.hasNext();) {
                    FileInfo tempInfo = (FileInfo) fileIterator.next();
                    String partLocation = null;
                    boolean skipPartLocation = tempInfo.getTypeOfChange().equalsIgnoreCase("DELETE");
                    if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                        skipPartLocation = skipPartLocation |((com.ibm.sdwb.build390.process.UserBuildProcess)build.getProcessForThisBuild()).isPDSBuild();
                    }
                    if(!skipPartLocation) {
                        partLocation = build.get_buildid()+"."+tempInfo.getMainframeFilename();
                    }
                    String oneCheckString = createPartStanzaForVerbFile(tempInfo, partLocation, true, !isFasttrack(build),isPDS(build), 0);
                    shadowLoadWriter.write(oneCheckString);
                }
            }
        }
        catch(IOException ioe) {
            throw new GeneralError("An error occurred while writing the partlist load and check built status verb files", ioe);
        }
    }

    public void makePartlistBuiltStatusCheckFile(MBBuild build, Set filesToCheck, Writer checkFileWriter)throws com.ibm.sdwb.build390.MBBuildException {
        try {
            if(!filesToCheck.isEmpty()) {
                if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                    checkFileWriter.write(createVerbFileHeader(build));
                }
                for(Iterator fileIterator = filesToCheck.iterator(); fileIterator.hasNext();) {
                    FileInfo tempFile = (FileInfo) fileIterator.next();

                    String oneCheckString = createPartStanzaForVerbFile(tempFile, null, true, !isFasttrack(build),isPDS(build),0);
                    checkFileWriter.write(oneCheckString);
                }
            }
        }
        catch(IOException ioe) {
            throw new GeneralError("An error occurred while writing the partlist load and check built status verb files", ioe);
        }
    }

    public void makeMVSBuildVerbFile(MBBuild build, Set unbuiltFiles, Set rebuildFiles, Writer buildVerbWriter) throws GeneralError{
        try {
            buildOrderUpdated = false;
            buildUpToDate = true;

            if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                buildVerbWriter.write(createVerbFileHeader(build));
            }

            Set filesToBuild = new HashSet();
            filesToBuild.addAll(unbuiltFiles);
            filesToBuild.addAll(rebuildFiles);
            int partIndex  = 1;   // used for fasttrack
            for(Iterator fileIterator = filesToBuild.iterator(); fileIterator.hasNext();partIndex++) {
                buildUpToDate = false;
                FileInfo tempInfo =(FileInfo) fileIterator.next();

                if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                    if(((com.ibm.sdwb.build390.process.UserBuildProcess) build.getProcessForThisBuild()).isPDSBuild()) {
                        pdsFileVersion = tempInfo.getVersion();
                    }
                }

                if(tempInfo.getName().equalsIgnoreCase(MBConstants.BUILDORDERFILE)) {
                    buildOrderUpdated = true;
                }

                String location = null;
                if(isFasttrack(build)) {
                    // if we got here, the build has to be a user build
                    if(isPDS(build)) {
                        location = "\'\'";
                    }
                    else {
                        location = build.getSetup().getMainframeInfo().getMainframeUsername().toUpperCase()+"."+build.get_buildid()+"."+tempInfo.getMainframeFilename();
                    }
                }


                String onePartStanza = createPartStanzaForVerbFile(tempInfo, location, false, !isFasttrack(build),isPDS(build), partIndex);

                buildVerbWriter.write(onePartStanza);
            }

            if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
                if(((com.ibm.sdwb.build390.MBUBuild)build).getFastTrack()) {
                    buildVerbWriter.write("TOTPARTS="+(partIndex-1));
                }
            }
        }
        catch(IOException ioe) {
            throw new GeneralError("An error occurred while writing the MVS build order verb file", ioe);
        }
    }

    private String createPartStanzaForVerbFile(FileInfo fileInfo, String location, boolean justAddTheVersion, boolean isNotFasttrackFile, boolean isPDSBuild, int partIndex) throws IOException {
        String partIndexToWrite = new String();
        if(!isNotFasttrackFile) {
            partIndexToWrite = Integer.toString(partIndex);
        }
        String name= fileInfo.getName();
        String directory = fileInfo.getDirectory();
        String realName = null;
        String realDirectory = null;

        if(fileInfo instanceof ModeledFileInfo) {
            realDirectory = fileInfo.getDirectory();
            realName = fileInfo.getName();
            name = ((ModeledFileInfo)fileInfo).getModelAfterName();
            directory = ((ModeledFileInfo)fileInfo).getModelAfterDirectory();
        }
        String fileVersion = fileInfo.getVersion();
        if(isPDSBuild) {
            fileVersion = fileInfo.getVHJN();
            if(fileVersion==null) {
                fileVersion=new String();
            }
        }

        String partStanza = null;
        partStanza  = "PHA"+partIndexToWrite+"=\'"+name+"\',"+MBConstants.NEWLINE;
        partStanza += "PHB"+partIndexToWrite+"=\'"+directory+"\',"+MBConstants.NEWLINE;
        partStanza += "VER"+partIndexToWrite+"=\'"+fileVersion+"\',TYP"+partIndexToWrite+"="+fileInfo.getTypeOfChange().toUpperCase()+",DAT"+partIndexToWrite+"="+stripNonNumeric(fileInfo.getDate());

        if(location != null) {
            partStanza += ", "+MBConstants.NEWLINE+"LOC"+partIndexToWrite+"=" + location;
        }
        String mdVersion = (String) fileInfo.getMetadataVersion();
        if(mdVersion!=null) {
            if(!justAddTheVersion) {
                AlphabetizedVector aVector = new AlphabetizedVector();  // Not sure why we alphabetize these, but I'm leaving it in.
                for(Iterator keyIterator = fileInfo.getMetadata().keySet().iterator(); keyIterator.hasNext();) {
                    aVector.addElement((String) keyIterator.next());
                }

                int keywordIndex = 1;
                for(Iterator orderedMetadataKeys = aVector.iterator(); orderedMetadataKeys.hasNext();) {
                    String key = (String) orderedMetadataKeys.next();

                    if(!key.equals(MBConstants.METADATAVERSIONKEYWORD)) {
                        String value = (String) fileInfo.getMetadata().get(key);
                        partStanza += ","+MBConstants.NEWLINE+"MKN"+keywordIndex+"="+key+",MKV"+keywordIndex+"="+value;
                        keywordIndex++;
                    }
                }

                partStanza +=","+MBConstants.NEWLINE+"#MK="+(keywordIndex-1);
            }
            partStanza += ","+MBConstants.NEWLINE+"MDV=\'"+mdVersion+"\'";
        }
        if(fileInfo.getCodePage() != null) {
            partStanza += ","+MBConstants.NEWLINE+"PAG"+partIndexToWrite+""+"=\'"+fileInfo.getCodePage()+"\'";
        }
        if(fileInfo.getScode() != null) {
            partStanza += ",CMVCSCOD"+"=\'"+fileInfo.getScode()+"\'";
        }
        if(realName != null) {
            partStanza += ",RHA"+partIndexToWrite+""+"=\'"+realName+"\'";
        }
        if(realDirectory != null) {
            partStanza += ",RHB"+partIndexToWrite+""+"=\'"+realDirectory+"\'";
        }
        if(fileInfo.isBinary()) {
            partStanza +=",BIN"+partIndexToWrite+"=YES";
        }
        else {
            partStanza +=",BIN"+partIndexToWrite+"=NO";
        }
        return formatForMainframe(new java.io.BufferedReader(new java.io.StringReader(partStanza)), isNotFasttrackFile);
    }

    private String formatForMainframe(BufferedReader partStanza, boolean isNotFasttrackFile) throws IOException{
        String returnStanza = new String();
        String stanzaLine = null;
        if(isNotFasttrackFile) {
            returnStanza += "&  &ORDER "+partStanza.readLine()+MBConstants.NEWLINE;
        }
        else {
            returnStanza+=", ";
        }
        while((stanzaLine = partStanza.readLine())!=null) {
            if(isNotFasttrackFile) {
                returnStanza += "&  ";
            }
            else {
                returnStanza+=" ";
            }
            returnStanza += stanzaLine;
            if(isNotFasttrackFile) {
                returnStanza += MBConstants.NEWLINE;
            }
        }
        if(!isNotFasttrackFile) {
            returnStanza = makeSpaceCommaCombinations(returnStanza);
        }
        return returnStanza;
    }

    private String createVerbFileHeader(MBBuild build) {
        String returnString = new String();
        if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
            com.ibm.sdwb.build390.MBUBuild ubuild = (com.ibm.sdwb.build390.MBUBuild) build;
            if(isPDS(ubuild)) {
                if(!isFasttrack(build)) {
                    returnString = "&  SET U,PDS="+ubuild.getLocalParts()[0]+","+MBConstants.NEWLINE+"&  HOSTDATA=YES"+MBConstants.NEWLINE;
                }
                else {
                    //#DEF.TST1492:
                    returnString = ", PDS="+ubuild.getLocalParts()[0]+", HOSTDATA=YES,";
                }
            }
        }
        else {
            returnString = "&  SET U,QUERY=\'";
            returnString += build.getSource().getSourceIdentifyingStringForMVS();
            if(!build.getSource().isIncludingCommittedBase()) {
                returnString+=" DELTA";
            }
            else {
                returnString+=" FULL";
            }
            returnString +="\'"+MBConstants.NEWLINE;
        }
        return returnString;
    }

    private String stripNonNumeric(String origString) {
        String returnString = new String();
        for(int i = 0; i < origString.length();i++) {
            if(Character.isDigit(origString.charAt(i))) {
                returnString += (new Character(origString.charAt(i))).toString();
            }
        }
        return returnString;
    }

    private boolean isFasttrack(MBBuild build) {
        if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
            return((com.ibm.sdwb.build390.MBUBuild) build).getFastTrack();
        }
        return false;
    }

    private boolean isPDS(MBBuild build) {
        if(build instanceof com.ibm.sdwb.build390.MBUBuild) {
            return((com.ibm.sdwb.build390.MBUBuild) build).getSourceType()==MBUBuild.PDS_SOURCE_TYPE ;
        }
        return false;
    }

    private String makeSpaceCommaCombinations(String inputString)throws IOException{
        String returnString = new String();
        StringTokenizer commaTokenizer = new StringTokenizer(inputString, ",");
        while(commaTokenizer.hasMoreTokens()) {
            returnString+= commaTokenizer.nextToken().trim() +", ";
        }
        return returnString;
    }

    public boolean isPartlistEmpty() {
        return partlistEmpty;
    }

    public boolean isBuildOrderUpdated() {
        return buildOrderUpdated;
    }

    public boolean isLoadOrderUpdated() {
        return loadOrderUpdated;
    }

    public boolean isBuildUpToDate() {
        return buildUpToDate;
    }
}
