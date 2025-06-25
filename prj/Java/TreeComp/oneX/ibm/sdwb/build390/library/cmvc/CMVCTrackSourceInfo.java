package com.ibm.sdwb.build390.library.cmvc;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.cmvc.client.api.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
public class CMVCTrackSourceInfo extends CMVCSourceInfo implements Changeset {

    static final long serialVersionUID = -1317845123178601970L;
    public static final String CMVCSOURCENAME = "CMVCTRACK";
    private String name = null;
    private ChangesetGroup levelContainingThis = null;
    private String lastState = null;
    private boolean feature = false;
    private boolean defect = false;

    public CMVCTrackSourceInfo(CMVCLibraryInfo tempInfo, String tempRelease, String tempTrack, ComponentAndPathRestrictions tempRestrict) {
        super(tempInfo,tempRelease,tempRestrict);
        name = tempTrack;
    }

    public com.ibm.sdwb.build390.library.SourceInfo getClone() {
        try {
            return(com.ibm.sdwb.build390.library.SourceInfo) clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }

    public Set getSetOfParts() throws MBBuildException{
        try {
            return getCMVCProcessServer().getPartlist(this);
        } catch (java.rmi.RemoteException re) {
            throw new com.ibm.sdwb.build390.LibraryError("Error getting list of parts", (Exception) re.detail);
        }
    }

    public Set getChangesetsInSource() throws MBBuildException{
        Set results = new HashSet();
        results.add(getTrackType()+"-"+name);
        return results;
    }

    public Set getListOfMissingRequisites() throws MBBuildException{
        Command cmd = getCommandObject("TrackCheck" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        String checkResult = runCommand(cmd);
        Set returnSet = new HashSet();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(checkResult));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.trim().length() > 0) {
                    String neededTrack = com.ibm.sdwb.build390.MBUtilities.getNthToken(tempString, 2);
                    neededTrack = neededTrack.substring(0, neededTrack.length()-1);
                    returnSet.add(neededTrack);
                }
            }
            reader.close();
        } catch (IOException ioe) {
            throw new com.ibm.sdwb.build390.LibraryError("An error occurred while attempting to read the results of level -check command", ioe);
        }
        return returnSet;
    }

    public boolean isValidSource() throws MBBuildException{
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("TrackView t");
        cmd.addParameterValue("-where", "t.releaseName=\'"+getRelease()+"\' and t.defectname=\'"+name+"\'");
        cmd.addParameterValue("-select", "t.releaseName, t.defectname");

        String commandOut = runCommand(cmd);
        return commandOut.trim().length()>0;
    }

    public void refreshState() throws MBBuildException{
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("TrackView t");
        cmd.addParameterValue("-where", "t.releaseName=\'"+getRelease()+"\' and t.defectname=\'"+name+"\'");
        cmd.addParameterValue("-select", "t.state");
        lastState = runCommand(cmd);
    }

    public String getState() throws MBBuildException{
        if (lastState ==null) {
            refreshState();
            if (lastState!=null) {
                lastState=lastState.trim();
            }
        }
        return lastState;
    }

    /**
     * For determining if this is a feature or defect track
     * 
     * @return 
     */
    private String getTrackType() {
        determineDefectOrFeature();
        if (feature) {
            return "FEATURE";
        } else if (defect) {
            return "DEFECT";
        } else {
            throw new RuntimeException("Unable to determine defect or feature");
        }
    }

    public String getSourceIdentifyingStringForMVS() {
        try {
            if (lastState == null) {
                getState();
            }
            return " TRACK " + lastState + " " + name;
        } catch (MBBuildException mbe) {
            throw new RuntimeException("Error getting state", mbe);
        }
    }


    public void setName(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    public void setChangesetGroupContainingChangeset(ChangesetGroup levelName) {
        levelContainingThis = levelName;
    }

    public ChangesetGroup getChangesetGroupContainingChangeset() {
        return levelContainingThis;
    }

    private void determineDefectOrFeature() {
        try {
            if (!defect & !feature) {// nothing set, so it's either not there, or we haven't run this yet
                Command cmd = getCommandObject("ReportGeneral" );
                cmd.getObjectSpec().setValue("DefectView ");
                cmd.addParameterValue("-where", "name=\'"+getName()+"\'");
                cmd.addParameterValue("-select", "name");
                String commandOut = runCommand(cmd);
                if (commandOut.trim().length()>0) {
                    feature = false;// found this in defect list, so it's not a feature
                    defect = true;
                }
                cmd = getCommandObject("ReportGeneral" );
                cmd.getObjectSpec().setValue("FeatureView ");
                cmd.addParameterValue("-where", "name=\'"+getName()+"\'");
                cmd.addParameterValue("-select", "name");
                commandOut = runCommand(cmd);
                if (commandOut.trim().length()>0) {
                    feature = true;// found this in feature list, so it's a feature
                    defect = false;
                }
            }
        } catch (MBBuildException mbe) {
            throw new RuntimeException("Error getting information about "+ getName()+ " from CMVC", mbe);
        }

    }

    public String toString() {
        String buf = super.toString();
        buf += "Track = " + name;
        return buf;
    }
}
