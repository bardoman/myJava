package com.ibm.sdwb.build390.metadata.info;
//******************************************************************************
/* GeneratedMetadataInfo new class to store the metadata parsed.
//******************************************************************************
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
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;
import com.ibm.sdwb.build390.metadata.info.MetadataFormatInfo;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;


public class GeneratedMetadataInfo {

    private Map opUpdateValueHash = new HashMap();
    private Map buildOrderValueHash = new HashMap();
    private Map profileValueHash = new HashMap();
    private Map relatedPartValueHash = new HashMap();
    private Map embeddedValueHash = new HashMap();
    private Map libraryMetadataMap = new HashMap();
    private Map generalValueHash = new HashMap();
    private Map hostLibraryValueHash = new HashMap();
    private Map notMetadataHash   = new HashMap();

    private String builtLibraryBaseName = null;
    private String profileName = null;

    private MetadataFormatInfo formatInfo = new MetadataFormatInfo();
    private AlphabetizedVector allKeywords = new AlphabetizedVector();

    private String metadataReportName ;
    private FileInfo fileInfo;
    private FileInfo builtFileInfo;

    private ReleaseInformation releaseInformation;
    private DriverInformation driverInformation;

    private boolean dontSaveInLibrary = false; //default save it.

    public GeneratedMetadataInfo(String tempFileName, FileInfo tempFileInfo) {
        this.metadataReportName = tempFileName;
        this.fileInfo = tempFileInfo;

    }

    public void setDontSaveMetadataInLibrary(boolean tempDontSaveInLibary) {
        this.dontSaveInLibrary= tempDontSaveInLibary;
    }

    public boolean  dontSaveMetadataInLibrary() {
        return dontSaveInLibrary;
    }

    public boolean shouldPopulateLibraryMetadata() {
        return((libraryMetadataMap==null || libraryMetadataMap.isEmpty()) && fileInfo!=null);
    }

    public void setReleaseAndDriverInformation(ReleaseInformation tempRelease, DriverInformation tempDriver){
        this.releaseInformation = tempRelease;
        this.driverInformation = tempDriver;
    }

    public ReleaseInformation getReleaseInformation(){
        return releaseInformation;
    }

    public DriverInformation getDriverInformation(){
        return driverInformation;
    }


    public Map getGeneralMetadataMap() {
        return generalValueHash;
    }

    public Map getOpUpdateMetadataMap() {
        return opUpdateValueHash;
    }

    public Map getProfileMetadataMap() {
        return profileValueHash;
    }

    public Map getEmbeddedMetadataMap() {
        return embeddedValueHash;
    }

    public Map getMVSRelatedPartMetadataMap() {
        return relatedPartValueHash;
    }

    public void setLibraryMetadataMap(Map tempLibraryMetadataMap) {
        this.libraryMetadataMap = tempLibraryMetadataMap;
    }

    public Map getLibraryMetadataMap() {
        return libraryMetadataMap;
    }

    public Map getBuiltLibraryMetadataMap() {
        return hostLibraryValueHash;
    }

    public Map getBuildorderMetadataMap() {
        return buildOrderValueHash;
    }

    public Map getNOTMetadataMap() {
        return notMetadataHash;
    }

    public MetadataFormatInfo getFormatInfo() {
        return formatInfo;
    }

    public String getProfileName() {
        return profileName;
    }

    //i think this is only being used by userbuild. check it out later.
    public void setFileInfo(FileInfo tempInfo){
        this.fileInfo = tempInfo;
    }

    //this is the input fileInfo for which we wanted to populate the metadata values.
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setBuiltFileInfo(FileInfo tempBuiltFileInfo) {
        this.builtFileInfo = tempBuiltFileInfo;
    }

    /**
     * This is the built file information populated from the stuff from the host.
     * The built file info contains only the library metadata  and its version that was built on the host.
     */
    public FileInfo getBuiltFileInfo() {
        return builtFileInfo;
    }

    public String getMetadataReportName() {
        return metadataReportName;
    }


    public String getBuiltLibraryBaseName() {
        return builtLibraryBaseName;
    }

    public java.util.List getAllKeywords() {
        return(Vector) allKeywords;
    }

//this applies all the library. version specific compares are done in the metadata handler.
    public String anyWarningsInMetadataVersionComparison() {

        String  builtLibraryPartName = "";
        String  builtLibraryVersion = getBuiltFileInfo().getVersion();
        String  libraryVersion      = getFileInfo().getVersion();

        if (getBuiltFileInfo().getDirectory() !=null & getBuiltFileInfo().getName()!=null) {
            builtLibraryPartName = getBuiltFileInfo().getDirectory()+getBuiltFileInfo().getName();
        } else if (getBuiltFileInfo().getName()!=null) {
            builtLibraryPartName = getBuiltFileInfo().getName();
        }

        
        String builtMetadataVersion = getBuiltFileInfo().getMetadataVersion();
        if (builtMetadataVersion!=null) {
            if (builtMetadataVersion.trim().length() < 1) {
                builtMetadataVersion = null;
            }
        }
        String libraryMetadataVersion = getFileInfo().getMetadataVersion();

        if (libraryMetadataVersion != null & builtMetadataVersion == null) {
            return ("Library version conflict:\n" + "The part " + builtLibraryPartName + " has not been built with metadata, but metadata is defined for it in the library.");
        } else if (builtMetadataVersion!= null & libraryMetadataVersion!=null) {
            if (!builtMetadataVersion.trim().equals(libraryMetadataVersion.trim())) {
                return ("Driver and Library Metadata Version conflict:\n" + "The metadata version of the current build of this part in the driver is not " +
                           "the same as the current metadata version in the library.  Version " + builtMetadataVersion + " is the built version, "+
                           "version "+libraryMetadataVersion+" is the library version.");
            }
        }

        return null;

    }



    public String toString() {
        String returnString = new String();
        returnString += "General hash         :" + getGeneralMetadataMap().toString() + "\n\n";
        returnString += "Op Updates hash      :" + getOpUpdateMetadataMap().toString() + "\n\n";
        returnString += "Buildorder hash      :" + getBuildorderMetadataMap().toString() + "\n\n";
        returnString += "Profile hash         :" + getProfileMetadataMap().toString() + "\n\n";
        returnString += "Related Part hash    :" + getMVSRelatedPartMetadataMap().toString() + "\n\n";
        returnString += "Embedded hash        :" + getEmbeddedMetadataMap().toString() + "\n\n";
        returnString += "Host Cmvc  hash      :" + getBuiltLibraryMetadataMap().toString() + "\n\n";
        returnString += "Not Metadata     hash:" + getNOTMetadataMap().toString() + "\n\n";
        returnString += "Library Input File   :" + getFileInfo().toString() + "\n\n";
        returnString += "Built Library File   :" + getBuiltFileInfo().toString() + "\n\n";
        returnString += "Profile name         :" + getProfileName() + "\n\n";
        returnString += "Format info          :" + getFormatInfo().toString() + "\n\n";
        return returnString;
    }

}


