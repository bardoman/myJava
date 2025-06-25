package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBBuild class for the Build390 java client                   */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/12/2004 PTM3767 backup builds.
/*************************************************************************************/
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.configuration.ConfigurationAccess;
import com.ibm.sdwb.build390.info.BuildOptions;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.info.Location;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.SourceInfo;
import com.ibm.sdwb.build390.logprocess.LogEventFileListener;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.DriverBuildPanel;
import com.ibm.sdwb.build390.userinterface.graphic.panels.build.InternalFrameBuildPanelHolder;

/** MBBuild provides base support for builds.
* <br>It is extended by the MBDbuild and MBUBuild classes to provide
* support for Driver and User builds.
* It contains all fields common to both build types including build options. */
public class MBBuild implements Serializable, Cloneable, com.ibm.sdwb.build390.info.Location {

    static final long serialVersionUID = 1111111111111111L;
    private static final String AUTOBLDSTRING = "AUTOBLD";
    private static final String LISTGENSTRING = "LISTGEN";
    private static final String RUNSCANSTRING = "RUNSCAN";
    private static final String FORCESTRING   = "FORCE";
    private static final String BUILDCCSTRING = "BUILDCC";
    public static final String  BUILDSETTINGKEYKEYWORD = "BUILDSETTINGKEY";
    public static final String  BUILDSETTINGVALUEKEYWORD = "BUILDSETTINGVALUE";
    public static final String BUILDLOGFILENAME = "Build.log";
    public static final String GENERICBUILDIDSTART = "B";
    public static final String LIBRARYDRIVENFULLBUILDIDSTART = "F";
    public static final String LIBRARYDRIVENDELTABUILDIDSTART = "D";

    private String buildIDStart = null;
    private String  buildid = null;               // unique id of this build
    private String  buildtype = null;                 // type of build
    private String  description = null;           // description of build
    private Date    dateHolder = null;                      // dateHolder last submitted
    private String locked = null;
    private String generalDirectoryHeader = MBConstants.GENERICBUILDDIRECTORY;
    private ReleaseInformation releaseInfo = null;
    private DriverInformation driverInfo = null;
    public Map partInfoMap = new HashMap();
    private ConfigurationAccess configInfo = null;
    private Setup setup = null;
    private SourceInfo sourceInfo = null;
    private BuildOptions options = new BuildOptions();
    private Map<String,String> buildSettings = new HashMap<String,String>();
    private AbstractProcess myProcess = null;
    private Location locationHeaderForDirectoryStructure = null;
    private String optionalBuildPath = null;

    protected transient LogEventFileListener  LogBuildFileListener = null;
    protected transient LogEventProcessor lep = null;

    /** constructor - Sets the dateHolder field and creates the buildid */
    public MBBuild(LogEventProcessor tempLep) {
        initialize(null,tempLep);
    }

    /** constructor - Sets the dateHolder field and creates the buildid */
    public MBBuild(String idStart,LogEventProcessor tempLep) {
        initialize(idStart, tempLep);
    }

    public MBBuild(String idStart, Location baseLocation, LogEventProcessor tempLep) {
        locationHeaderForDirectoryStructure = baseLocation;
        initialize(idStart,tempLep);
    }

    public MBBuild(String idStart, String typeBuildPath, LogEventProcessor tempLep) {
        generalDirectoryHeader = typeBuildPath;
        initialize(idStart,tempLep);
    }

    /** constructor - Sets the dateHolder field and creates the buildid */
    public MBBuild(String abid, boolean aparbuild,LogEventProcessor lep) {
        if (aparbuild) buildid = abid;
        initialize(null, lep);
    }

    public void changeStartingCharacter(String newCharacter) {
        newCharacter = newCharacter.toUpperCase();
        if (!buildid.startsWith(newCharacter)) {
            File oldDirectory = getBuildPathAsFile();
            int substringStart = buildid.length() - 7;
            buildid = newCharacter + buildid.substring(substringStart);
            oldDirectory.renameTo(getBuildPathAsFile());
            lep.removeEventListener(LogBuildFileListener); /*we have to remove the old log listener and attach a new one */
            LogBuildFileListener = new LogEventFileListener(getBuildPath()+BUILDLOGFILENAME);
            lep.addEventListener(LogBuildFileListener);
        }
    }

    private void initialize(String idStart, LogEventProcessor tempLep) {
        this.lep=tempLep;

        if (dateHolder == null) {
            dateHolder = new Date();
        }
        buildIDStart = idStart;
        if (buildIDStart==null) {
            buildIDStart = GENERICBUILDIDSTART;
        }
        if (buildid == null) {
            // create the unique build directory
            buildid = new String(MBUtilities.UniqueString(getBuildPathPrefix(), buildIDStart));
            String tempFilename = new String (getBuildPathPrefix()+buildid);
            File tempFile = new File(tempFilename);

            // create the new directory
            if (!tempFile.mkdirs()) {
                buildid = null;
                throw new RuntimeException("Could not create directory "+tempFile);
            }
            LogBuildFileListener = new LogEventFileListener(getBuildPath()+BUILDLOGFILENAME);
            lep.addEventListener(LogBuildFileListener);
        }
        setup = SetupManager.getSetupManager().createSetupInstance();
    }

    public void copyBuildSettings(MBBuild oldBuild) {
        releaseInfo = oldBuild.releaseInfo;
        driverInfo = oldBuild.driverInfo;
        /** String objects are immutable. I can't find a reasoning for TST3494. 
         ** But lets just make a new String copy of buildtype here. */
        buildtype =  new String(oldBuild.buildtype); 
        description = oldBuild.description;
        locked = oldBuild.locked;
        partInfoMap = new HashMap(oldBuild.partInfoMap);
        setup = oldBuild.getSetup().getClone();
        sourceInfo = oldBuild.sourceInfo.getClone();
        options = new com.ibm.sdwb.build390.info.BuildOptions(oldBuild.options);
        copyAddtionalBuildSettings(oldBuild);
    }

    public void copyAddtionalBuildSettings(MBBuild oldBuild) {
        buildSettings = new HashMap<String,String>(oldBuild.buildSettings);
    }

    private void attachLogListeners() {
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        if (MainInterface.getInterfaceSingleton()!=null) {
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
    }

    public final String getLocation() {
        return getBuildPath();
    }

    public String getBuildIDStart() {
        return buildIDStart;
    }

    public LogEventProcessor getLEP() {
        return lep;
    }

    /** getBuildPath will the build path.
    */
    public final String  getBuildPath() {
        return(getBuildPathPrefix()+getIndividualSubBuildPath()+getBuildPathSuffix());
    }

    public final java.io.File  getBuildPathAsFile() {
        return(new java.io.File(getBuildPathPrefix()+getIndividualSubBuildPath()+getBuildPathSuffix()));
    }

    public final String getIndividualSubBuildPath() {
        return get_buildid()+File.separator;
    }

    public String getGeneralDirectoryHeader() {
        return generalDirectoryHeader;
    }


    public String getBuildPathSuffix() {
        if (optionalBuildPath!=null) {
            return optionalBuildPath+File.separator;
        } else {
            return new String();
        }
    }

    public void setBuildPathSuffix(String variablePart) {
        optionalBuildPath = variablePart;
    }


    /** getBuildPath returns the buildPath.
    * @return String buildPath*/
    public final String getBuildPathPrefix() {
        String path = null;
        if (locationHeaderForDirectoryStructure==null) {
            path = MBGlobals.Build390_path+getGeneralDirectoryHeader();
        } else {
            path = locationHeaderForDirectoryStructure.getLocation();
        }
        return(path);
    }

    public final void setLocationHeaderForDirectoryStructure(Location newLocation) {
        locationHeaderForDirectoryStructure = newLocation;
        lep.removeEventListener(LogBuildFileListener);
        LogBuildFileListener = new LogEventFileListener(getBuildPath()+BUILDLOGFILENAME);
        lep.addEventListener(LogBuildFileListener);
    }

    /** get_buildid returns the buildid_ setting.
    * @return String buildid_ setting */
    public String  get_buildid() {
        return(buildid);
    }

    /** getReleaseInformation returns the release_ setting.
    * @return String release_ setting */
    public ReleaseInformation  getReleaseInformation() {
        return(releaseInfo);
    }

    public com.ibm.sdwb.build390.mainframe.DriverInformation getDriverInformation() {
        return driverInfo;
    }

    /** get_buildtype returns the buildtype_ setting.
    * @return String buildtype_ setting */
    public String  get_buildtype() {
        return(buildtype);
    }

    /** get_descr returns the descr_ setting.
    * @return String descr_ setting */
    public String  get_descr() {
        return(description);
    }

    /** get_date returns the date_ setting.
    * @return dateHolder date_ setting */
    public Date  get_date() {
        return(dateHolder);
    }

    /** getSetup returns the setup setting.
    * @return Setup setup */
    public Setup getSetup() {
        return(setup);
    }

    public MBMainframeInfo getMainframeInfo() {
        return setup.getMainframeInfo();
    }

    public LibraryInfo getLibraryInfo() {
        return setup.getLibraryInfo();
    }

    public BuildOptions getOptions() {
        return options;
    }

    public String getBuildIDLock() {
        return locked;
    }

    /** getPartInfoSet returns the partInfoMap setting.
    * @return Set partInfoSet setting */
    public Set getPartInfoSet() {
        Set returnSet = new HashSet();
        for (Iterator infoIterator = partInfoMap.keySet().iterator(); infoIterator.hasNext(); ) {
            returnSet.add(partInfoMap.get(infoIterator.next()));
        }
        return(returnSet);
    }

    /** getPartInfoMap returns the partInfoMap setting.
    * @return Map partInfoSet setting */
    public Map getMapOfPartNameToPartInfo() {
        return(partInfoMap);
    }

    public FileInfo getFileInfo(String directory, String name) {
        return(FileInfo) partInfoMap.get(directory+"|"+name);
    }

    /** getConfigInfo returns the config setting for a given keyword.
    * @return String  configKeyword setting */
    public String getConfigInfo(String section, String keyword) throws LibraryError{
        return(configInfo.getProjectConfigurationSetting(section, keyword));
    }

    public AbstractProcess getProcessForThisBuild() {
        return myProcess;
    }

    public SourceInfo getSource() {
        return sourceInfo;
    }

    public void setSource(SourceInfo newSource) {
        sourceInfo = newSource;
    }

    //rework log - returns a listener object of the build
    public LogEventFileListener getLogListener() {
        return(LogBuildFileListener);
    }

    public void setReleaseInformation(ReleaseInformation newInfo) {
        releaseInfo = newInfo;
        if (newInfo!=null) {
            configInfo = setup.getLibraryInfo().getConfigurationAccess(newInfo.getLibraryName(), true);
        }
    }

    public void setDriverInformation(DriverInformation newInfo) {
        driverInfo = newInfo;
        if (driverInfo!=null) {
            setReleaseInformation(driverInfo.getRelease());
        }
    }

    /** set_buildtype sets the buildtype_ setting.
    * @param String buildtype_ setting */
    public void set_buildtype(String tempBuildtype) {
        if (tempBuildtype != null) {
            buildtype = tempBuildtype.trim().toUpperCase();
        }
    }

    /** set_descr sets the descr_ setting.
    * @param String descr_ setting */
    public void set_descr(String tempDescr) {
        if (tempDescr!= null) {
            description = tempDescr.trim();
        }
    }

    /** setSetup sets the setup setting.
    * @param Setup setup setting */
    public void setSetup(Setup tempSetup) {
        this.setup = tempSetup;
    }

    /** set_locked sets the locked setting.
    * @param String tempLocked */
    public void    setLocked(String tempLocked) {
        if (tempLocked != null) {
            locked = tempLocked.trim();
        }
    }

    /** setPartInfoHash sets the partInfoMap setting.
    * @param Hashtable partInfoMap setting */
    public void setPartInfo(Collection tempInfoSet) {
        partInfoMap = new HashMap();
        for (Iterator infoIterator = tempInfoSet.iterator(); infoIterator.hasNext();) {
            FileInfo oneInfo = (FileInfo) infoIterator.next();
            partInfoMap.put(oneInfo.getDirectory()+"|"+oneInfo.getName(), oneInfo);
        }
    }

    public void setOptions(BuildOptions temp) {
        options = temp;
    }

    public void setProcessForThisBuild(AbstractProcess newProcess) {
        myProcess = newProcess;
    }

    /* method to show a build page
    */
    public void showPage(boolean restart, java.awt.Point tempPoint)throws com.ibm.sdwb.build390.MBBuildException {
        throw new RuntimeException("Show page is not implemented in MBBuild");
    }

    /* method to clean up a build
    */
    public void cleanup(Hashtable parameters)throws com.ibm.sdwb.build390.MBBuildException {
    }

    /* method to create a build edit window for all builds.  This is to be
    overridden in all subclasses
    */
    public void viewBuild(JInternalFrame tempFrame) throws com.ibm.sdwb.build390.MBBuildException{
        InternalFrameBuildPanelHolder frameToRestart = DriverBuildPanel.getDriverBuildFrame(this, true);
        frameToRestart.getBuildPanel().setAllowEditing(false);
    }

    /** show displays the fields of this object
    * @param String build in string form*/
    public String toString() {
        String buf = new String();
        buf+="buildid = "+buildid+"\n";
        if (releaseInfo!=null) {
            buf+="release = "+releaseInfo.toString()+"\n";
        } else {
            buf+="release = null\n";
        }
        if (driverInfo!=null) {
            buf+="driver = "+driverInfo.toString()+"\n";
        } else {
            buf+="driver = null\n";
        }
        buf+="buildtype = "+buildtype+"\n";
        buf+="locked = "+locked+"\n";
        buf+="description = "+description+"\n";
        buf+="dateHolder = "+dateHolder+"\n";
        buf+="partInfoMap = "+partInfoMap+"\n";
        if (buildSettings!=null) {
            buf+="buildSettings = "+buildSettings.toString()+"\n";
        } else {
            buf+="buildSettings = null\n";
        }
        if (options!=null) {
            buf+="Options = "+options.toString()+"\n";
        } else {
            buf+="Options = null\n";
        }
        return buf;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        if (buildSettings == null) {
            buildSettings = new HashMap();
        }

        if (MBClient.lep!=null) {
            lep= (LogEventProcessor)MBClient.lep.clone();
        } else {
            lep = new LogEventProcessor();
            lep.addEventListener(MBClient.getGlobalLogFileListener());
        }
        LogBuildFileListener = new LogEventFileListener(getBuildPath()+"Build.log");
        lep.addEventListener(LogBuildFileListener);      
/*Ken 7/5/00 we should do this, but we'll add & test later.
        lep.addEventListener(MBClient.getGlobalLogFileListener());
*/
    }

    public MBBuild getClone() {
        try {
            return(MBBuild) clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Problem making clone of "+getClass().getName(), cnse);
        }
    }

    /** save serializes this object in it's home directory
    */
    public void save() throws com.ibm.sdwb.build390.MBBuildException {
        save(getBuildPath());
    }

    /** save serializes this object in the path directory
    */
    public synchronized void save(String path) throws com.ibm.sdwb.build390.MBBuildException {
        try {

            com.ibm.sdwb.build390.utilities.BackupBuilds.saveObject(this, new File(path+MBConstants.BUILDSAVEFILE),MBBuildLoader.NUM_OF_BACKUP_BUILDS);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
            throw new GeneralError("error saving " + path+MBConstants.BUILDSAVEFILE, ioe);
        }

    }

    public Map<String,String>  getBuildSettings() {
        return buildSettings;
    }
}
