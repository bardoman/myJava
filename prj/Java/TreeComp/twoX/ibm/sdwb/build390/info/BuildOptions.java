package com.ibm.sdwb.build390.info;

import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;

public class BuildOptions implements java.io.Serializable {
    static final long serialVersionUID = 7474383263574584292L;
    private boolean controlled = false;
    private String  listgen = "NO";                 // generate and store listings
    private boolean  runscan = false;                   // run scanners and verifiers
    private boolean  purgeJobs = true;              // purge job output after phase
    private int     buildcc = 4;                    // mainframe return code to terminate on, 4 or 8
    private String  autobuild = "YES";              // Off, On, Manual
    private String  force = "NO";
    private boolean  dryrun = false;
    private String  xmitTo = null;
    private String  xmitType = null;
    private boolean haltOnShadCheckWarnings = false;
    private boolean skipdcheck = false;
    private String  XmitDebugFilesLocation = new String("");
    private boolean GenDebugFiles  = false;
    private boolean XmitDebugFiles = false;
    private boolean SaveDebugFiles = false;
    private int[] buildccOverrides = null;
    private boolean SyncDriver = false;
    private String extraDriverCheckMode=null;
    private boolean autoPurgeSuccessfulJobs = false;
    private boolean beenSet = false;

    public BuildOptions() {
    }

    public BuildOptions(BuildOptions old) {
        setOptions(old);
    }

    public void setControlled(boolean temp) {
        controlled = temp;
    }

    public boolean isControlled() {
        return controlled;
    }

    public void setListGen(String temp) {
        listgen = temp;
    }

    public String getListGen() {
        return listgen;
    }

    public void setRunScanners(boolean temp) {
        runscan = temp;
    }

    public boolean isRunScanners() {
        return runscan;
    }

    public void setPurgeJobsAfterCompletion(boolean temp) {
        purgeJobs = temp;
    }

    public boolean isPurgeJobs() {
        return purgeJobs;
    }

    public void setBuildCC(int temp) {
        buildcc = temp;
    }

    public int getBuildCC() {
        return buildcc;
    }

    public void setAutoBuild(String tempAuto) {
        autobuild = tempAuto;
    }

    public String getAutoBuild() {
        return autobuild;
    }

    public void setForce(String temp) {
        force = temp;
    }

    public String getForce() {
        return force;
    }

    public void setDryRun(boolean temp) {
        dryrun = temp;
    }

    public boolean isDryRun() {
        return dryrun;
    }

    public void setXmitTo(String temp) {
        xmitTo = temp;
    }

    public String getXmitTo() {
        return xmitTo;
    }

    public void setXmitType(String temp) {
        xmitType = temp;
    }

    public String getXmitType() {
        return xmitType;
    }

    public void setHaltOnShadowCheckWarnings(boolean temp) {
        haltOnShadCheckWarnings = temp;
    }

    public boolean isHaltOnShadowCheckWarnings() {
        return haltOnShadCheckWarnings;
    }

    public void setSkipDriverCheck(boolean temp) {
        skipdcheck = temp;
    }

    public boolean isSkippingDriverCheck() {
        return skipdcheck;
    }

    public void setXmitDebugFileLocation(String temp) {
        XmitDebugFilesLocation = temp;
    }

    public String getXmitDebugFileLocation() {
        return XmitDebugFilesLocation;
    }

    public void setGenerateDebugFiles(boolean temp) {
        GenDebugFiles = temp;
    }

    public boolean isGeneratingDebugFiles() {
        return GenDebugFiles;
    }

    public void setXmitDebugFiles(boolean temp) {
        XmitDebugFiles = temp;
    }

    public boolean isXmitDebugFiles() {
        return XmitDebugFiles;
    }

    public void setSaveDebugFiles(boolean temp) {
        SaveDebugFiles = temp;
    }

    public boolean isSaveDebugFiles() {
        return SaveDebugFiles;
    }

    public void setSynchronizeDriver(boolean temp) {
        SyncDriver = temp;
    }

    public boolean isSynchronizingDriver() {
        return SyncDriver;
    }

    public void setExtraDriverCheck(String temp) {
        extraDriverCheckMode = temp;
    }

    public String getExtraDriverCheck() {
        return extraDriverCheckMode;
    }

    public void setAutoPurgeSuccessfulJobs(boolean temp) {
        autoPurgeSuccessfulJobs = temp;
    }

    public boolean isAutoPurgeSuccessfulJobs() {
        return autoPurgeSuccessfulJobs;
    }

    public void setOptionsBeenSet(boolean temp) {
        beenSet = temp;
    }

    public boolean isBeenSet() {
        return beenSet;
    }

    public void setBuildCCPhaseOverrides(int[] temp) {
        buildccOverrides = temp;
    }

    public int[] getBuildCCPhaseOverrides() {
        return buildccOverrides;
    }

    public BuildOptions getCopy(){
        return new BuildOptions(this);
    }

    public void setOptions(BuildOptions old) {
        controlled = old.controlled;
        listgen = old.listgen;
        runscan = old.runscan;
        purgeJobs = old.purgeJobs;
        buildcc = old.buildcc;
        autobuild = old.autobuild;
        force = old.force;
        dryrun = old.dryrun;
        xmitTo = old.xmitTo;
        xmitType = old.xmitType;
        haltOnShadCheckWarnings = old.haltOnShadCheckWarnings;
        skipdcheck = old.skipdcheck;
        XmitDebugFilesLocation = old.XmitDebugFilesLocation;
        GenDebugFiles = old.GenDebugFiles;
        XmitDebugFiles = old.XmitDebugFiles;
        SaveDebugFiles = old.SaveDebugFiles;
        if(old.buildccOverrides!=null){
        buildccOverrides = new int[old.buildccOverrides.length];  // these are arrays, so we can't just copy them.
        for (int index = 0; index < buildccOverrides.length; index++) {
            buildccOverrides[index] = old.buildccOverrides[index];
        }
        }
        SyncDriver = old.SyncDriver;
        extraDriverCheckMode = old.extraDriverCheckMode;
        autoPurgeSuccessfulJobs = old.autoPurgeSuccessfulJobs;
        beenSet = old.beenSet; // not sure about this one
    }

    public void setOptions(com.ibm.sdwb.build390.mainframe.parser.DriverReportParser dr) {
        autobuild = dr.getAutobuildSetting();
        if (autobuild == null) {
            autobuild = BinarySettingUtilities.getPreferredTrueSetting();
        }
        listgen = dr.getListingGenerateSetting();
        if (listgen == null) {
            listgen = BinarySettingUtilities.getPreferredFalseSetting();
        }
        if (dr.getRunScannersSetting()!=null) {
            runscan = BinarySettingUtilities.isTrueSetting(dr.getRunScannersSetting());
        } else {
            runscan = false;
        }
        force = dr.getForceSetting();
        if (force == null) {
            force = BinarySettingUtilities.getPreferredFalseSetting();
        }
        buildcc = dr.getBuildCCSetting();
    }
}
