package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBUBuild class for the Build390 java client                  */
/*********************************************************************/
/* Updates:                                                          */
//Date           Key                 Reason
//11/08/99     pjs Maclibs	add user maclib support
//01/07/2000   ind.build.log    changes for logging into a individual build log file
//02/15/2000 	       		throw excpetion if not serializable
//03/07/2000   reworklog        changes to implement the log stuff using listeners
//05/16/2000   UBUILD_METADATA  some set and get methods for Metadataedit
//09/28/2000   VHJN changes     addition of set and get methods to put the vhjn hash
//Thulasi:11/15/00:Feature	For an option to save listings to a dataset , provided set and get methods
//                              For saving and retrieving the text entry from the ubuild object when fasttrack option is selected.
//10/11/2002 SDWB1777           Added a getSourceType/setSourceType method, to determine LocalParts or PDS 
//11/12/2004 PTM3767            Backup build.ser files
/*********************************************************************/
import java.io.*;
import java.util.*;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.info.BuildOptionsLocal;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.InternalFrameBuildPanelHolder;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.UserBuildPanel;

/** <br>MBUBuild provides support for User Build.
* <br>It is extends the MBBuild class and adds support unique to user builds
*/
public class MBUBuild extends MBBuild implements Serializable {

    static final long serialVersionUID = 1111111111111111L;
    private String[]  localParts = new String[0];           // part list
    private String[]  PDSMemberVersions = new String[0];    // versions of parts in pds
    private String[]  PartModels_mod_part = new String[0];    // models for parts
    private String[]  PartModels_class_path = new String[0];  // models for parts
    private String[]  PartModelTypes = new String[0];         // types of models for parts, mod.name or library
    private String    PDSMemberClass = new String (" ");
    private Map metadataMap = null;
    private int sourceType=-1;
    public static  final int LOCAL_SOURCE_TYPE=0;
    public static final int PDS_SOURCE_TYPE= LOCAL_SOURCE_TYPE+1;

    private Hashtable VHJNhash = new Hashtable();

    private BuildOptionsLocal localOptions = new BuildOptionsLocal();

    public MBUBuild(LogEventProcessor lep) throws com.ibm.sdwb.build390.MBBuildException{
        super("U",lep);
    }


    public void copyBuildSettings(MBBuild oldBuild) {
        super.copyBuildSettings(oldBuild);
        MBUBuild oldUserBuild = (MBUBuild)oldBuild;
        setLocalParts(oldUserBuild.getLocalParts());
        setPDSMemberVersions(oldUserBuild.getPDSMemberVersions());
        setPartModels(oldUserBuild.getPartModels_mod_part(),oldUserBuild.getPartModels_class_path());
        setPartModelTypes(oldUserBuild.getPartModelTypes());
        setPDSMemberClass(oldUserBuild.getPDSMemberClass());
        setMetadataMap(oldUserBuild.getMetadataMap());
        setSourceType(oldUserBuild.getSourceType());
        setVHJNHash(oldUserBuild.getVHJNHash());
        localOptions.setOptions(oldBuild.getOptions());
    }


    public String  getGeneralDirectoryHeader() {
        return(MBConstants.USERBUILDDIRECTORY);
    }

    /** <br>getLocalParts returns the localParts setting.
    * @return String[] localParts setting */
    public String[]  getLocalParts() {
        return(localParts);
    }

    /** <br>getPartModels_mod_part returns the PartModels_mod_part setting.
    * @return String[] PartModels_mod_part setting */
    public String[]  getPartModels_mod_part() {
        return(PartModels_mod_part);
    }

    /** <br>getPartModels_class_path returns the PartModels_class_path setting.
    * @return String[] PartModels_class_path setting */
    public String[]  getPartModels_class_path() {
        return(PartModels_class_path);
    }

    /** <br>getPartModelTypes returns the PartModelTypes setting.
    * @return String[] PartModelTypes setting */
    public String[]  getPartModelTypes() {
        return(PartModelTypes);
    }

    /** <br>getPDSMemberVersions returns the PDSMemberVersions setting.
    * @return String[] PDSMemberVersions setting */
    public String[]  getPDSMemberVersions() {
        return(PDSMemberVersions);
    }

    /** <br>getPDSMemberClass returns the PDSMemberClass setting.
    * @return String PDSMemberClass setting */
    public String    getPDSMemberClass() {
        return(PDSMemberClass);
    }

    /** <br>getFastTrack returns the fastTrack setting.
    * @return boolean fastTrack setting */
    public boolean  getFastTrack() {
        return localOptions.isFastTrack();
    }

    public int getSourceType() {
        return sourceType;
    } 

    public Map getMetadataMap(){
        return metadataMap;
    }

    /** <br>set the localParts setting.
    * @param String[] localParts setting */
    public void    setLocalParts(String[] tempLocalParts) {
        localParts = tempLocalParts;
    }

    /** <br>set the PartModels settings.
    * @param String[] PartModels_mod_part setting
    * @param String[] PartModels_class_path setting */
    public void    setPartModels(String[] tempPartModels_mod_part, String[] tempPartModels_class_path) {
        PartModels_mod_part = tempPartModels_mod_part;
        PartModels_class_path = tempPartModels_class_path;
    }

    /** <br>set the PartModels setting.
    * @param String[] PartModels setting */
    public void    setPartModelTypes(String[] tempPartModelTypes) {
        PartModelTypes = tempPartModelTypes;
    }

    /** <br>set the PDSMemberVersions setting.
    * @param String[] PDSMemberVersions setting */
    public void    setPDSMemberVersions(String[] tempPDSMemberVersions) {
        PDSMemberVersions = tempPDSMemberVersions;
    }

    /** <br>set the PDSMemberClass setting to macro or module etc.
    * @param String PDSMemberClass setting */
    public void    setPDSMemberClass(String tempClass) {
        PDSMemberClass = tempClass;
    }

    /** <br>set the fastTrack setting.
    * @param boolean  fastTrack setting */
    public void    setFastTrack(boolean tempFastTrack) {
        localOptions.setFastTrack(tempFastTrack);
    }

    public void setSourceType(int sourceType) {
        this.sourceType=sourceType;
    }

    public void setSource(SourceInfo newSource){
        super.setSource(newSource);
        ((LocalSourceInfo)newSource).setBuild(this);
    }

    public void setMetadataMap(Map tempMeta){
        metadataMap = tempMeta;
    }

    public void setOptions(BuildOptions  tempLocalOptions){
        this.localOptions = (BuildOptionsLocal)tempLocalOptions;
    }


    /** save serializes this object
    * @param spath Path to serialize to */
    public void save(String spath) throws com.ibm.sdwb.build390.MBBuildException {
        try {
            com.ibm.sdwb.build390.utilities.BackupBuilds.saveObject(this, new File(spath),MBBuildLoader.NUM_OF_BACKUP_BUILDS);
        }
        catch(IOException ioe) {
            throw new GeneralError("error saving " + spath, ioe);
        }
    }

    /* method to create a build edit window for all builds.  This is to be
    overridden in all subclasses
    */
    public void viewBuild(JInternalFrame tempFrame) throws com.ibm.sdwb.build390.MBBuildException {
        InternalFrameBuildPanelHolder frameToRestart = UserBuildPanel.getUserBuildFrame(this, true);
        frameToRestart.getBuildPanel().setAllowEditing(false);
    }

    /** show displays the fields of this object */
    public String toString() {
        String ts = new String();
        ts = ts + super.toString();
        ts = ts + "fastTrack = " + localOptions.isFastTrack() + "\n";
        if(localParts != null) {
            if(localParts[0] != null) {
                ts = ts + "localParts = \n";
                for(int i = 0; i < localParts.length ; i++) {
                    ts = ts + "   " + localParts[i].trim();
                    if(i!=0) {
                        if(PartModels_mod_part.length-1>=i) {
                            if(PartModels_mod_part[i]!=null) {
                                ts = ts + ", Model = " +PartModels_mod_part[i];
                                if(PartModels_class_path[i]!=null) {
                                    ts = ts + " " + PartModels_class_path[i];
                                }
                                if(PartModelTypes.length-1>=i) {
                                    if(PartModelTypes[i]!=null) {
                                        ts             =             ts             +             ", Model Type = "             +PartModelTypes[            i];
                                    }
                                }
                            }
                        }
                        if(sourceType==PDS_SOURCE_TYPE) {
                            if(PDSMemberVersions.length-1>=i) {
                                if(PDSMemberVersions[i]!=null) {
                                    ts = ts + ", PDS Version = " +PDSMemberVersions[i];
                                }
                            }
                        }
                    }
                    ts = ts +"\n";
                }
            }
        }
        if(sourceType==PDS_SOURCE_TYPE) {
            if(PDSMemberClass!=null) {
                ts = ts + "PDS Member Class = "+PDSMemberClass+"\n";
            }
        }
        return ts;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        //if the sourceType is default value , ie -1(it might be an old build), then check the PDSMemberVersion
        if(getSourceType() ==-1) {
            if(getPDSMemberVersions()==null) {
                setSourceType(LOCAL_SOURCE_TYPE);
            }
            else {
                setSourceType(PDS_SOURCE_TYPE);
            }
        }
        in.defaultReadObject();
    }

    /** save serializes this object in it's home directory
    */
    public void save() throws com.ibm.sdwb.build390.MBBuildException {
        save(getBuildPath()+MBConstants.BUILDSAVEFILE);
    }

    /**
     * this method returns the hashtable that contains the 
     * VHJN no 
     * @return returns a Hashtable contains keys like
     *         *INFO* PART LOADED .....(LEVEL)0909090 where 0909090 is the VHJN No
     */
    public Hashtable getVHJNHash() {
        return VHJNhash;
    }
    /**
     * set method for setting the VHJN stuff got from parts.out file
     * 
     * @param tempVhjnhash
     *               A hashtable containing the VHJN details
     */
    public void    setVHJNHash(Hashtable tempVHJNhash) {
        VHJNhash = tempVHJNhash;
    }

    public BuildOptions getOptions() {
        return localOptions;
    }
}
