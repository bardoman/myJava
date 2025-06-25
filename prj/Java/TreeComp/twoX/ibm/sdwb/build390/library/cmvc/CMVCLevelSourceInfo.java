package com.ibm.sdwb.build390.library.cmvc;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.cmvc.client.api.*;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.library.*;


/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
public class CMVCLevelSourceInfo extends CMVCSourceInfo implements com.ibm.sdwb.build390.library.ChangesetGroup {
    static final long serialVersionUID = -4455146854626151615L;
    public static final String CMVCSOURCENAME = "CMVCLEVEL";
    private String name = null;
    private String lastState = null;

    public CMVCLevelSourceInfo(CMVCLibraryInfo tempInfo, String tempRelease, String tempLevel, ComponentAndPathRestrictions tempRestrictions) {
        super(tempInfo, tempRelease, tempRestrictions);
        name = tempLevel;
    }

    public com.ibm.sdwb.build390.library.SourceInfo getClone() {
        try {
            return(com.ibm.sdwb.build390.library.SourceInfo) clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }

    public void markProcessingComplete() throws MBBuildException{
        Command cmd = getCommandObject("LevelCommit" );
        cmd.getObjectSpec().setValue(getName());
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public Set getSetOfParts() throws MBBuildException{
        try {
            return getCMVCProcessServer().getPartlist(this);
        } catch (java.rmi.RemoteException re) {
            throw new com.ibm.sdwb.build390.LibraryError("Error getting list of parts", re);
        }
    }

    public Set getChangesetsInSource() throws MBBuildException{
        Set results = new HashSet();
        // cmvc command to get a list of track in a level
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("LevelMemberView l");
        cmd.addParameterValue("-where", "l.releaseName=\'"+getRelease()+"\' and l.levelName=\'"+name+"\'");
        cmd.addParameterValue("-select", "l.defectType, l.defectName");

        String commandOut = runCommand(cmd);

        try {
            BufferedReader in = new BufferedReader(new StringReader(commandOut));
            String line = null;
            while ((line = in.readLine()) != null) {
                StringTokenizer toke = new StringTokenizer(line, "|");
                String type = toke.nextToken();
                String name = toke.nextToken();
                results.add(type.toUpperCase()+"-"+name);
            }
            in.close();
        } catch (IOException ioe) {
            throw new com.ibm.sdwb.build390.LibraryError("I/O Error occurred when trying to read results.", ioe);
        }
        return results;
    }

    public Set getListOfMissingRequisites() throws MBBuildException{
        Command cmd = getCommandObject("LevelCheck" );
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
        cmd.getObjectSpec().setValue("LevelView l");
        cmd.addParameterValue("-where", "l.releaseName=\'"+getRelease()+"\' and l.name=\'"+name+"\'");
        cmd.addParameterValue("-select", "l.releaseName, l.name");

        String commandOut = runCommand(cmd);
        return commandOut.trim().length()>0;
    }

    public void setStateToReady() throws MBBuildException{
        Command cmd = getCommandObject("LevelReady" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public void setStateToPreReady() throws MBBuildException{
        Command cmd = getCommandObject("LevelBuild" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public void setStateToCertify() throws MBBuildException{
        Command cmd = getCommandObject("LevelCertify" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public void setStateToIntegrate() throws MBBuildException{
        Command cmd = getCommandObject("LevelIntegrate" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }


    public void refreshState() throws MBBuildException{
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("LevelView l");
        cmd.addParameterValue("-where", "l.releaseName=\'"+getRelease()+"\' and l.name=\'"+name+"\'");
        cmd.addParameterValue("-select", "l.state");
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

    public void setState(String state) throws MBBuildException{
        lastState = state;
    }

    public void create() throws MBBuildException{
        Command cmd = getCommandObject("LevelCreate" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-type", "other");
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public void delete() throws MBBuildException{
        Command cmd = getCommandObject("LevelDelete" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        runCommand(cmd);
    }

    public void rename(String newName) throws MBBuildException{
        Command cmd = getCommandObject("LevelModify" );
        cmd.getObjectSpec().setValue(name);
        cmd.addParameterValue("-release", getRelease());
        cmd.addParameterValue("-using", cmvcProcessingType);
        cmd.addParameterValue("-name", newName);
        runCommand(cmd);
        name = newName;
    }

    public void addChangesetToGroup(Changeset newChangeset) {
        try {
            Command cmd = getCommandObject("LevelMemberCreate" );
            cmd.addParameterValue("-level", getName());
            cmd.addParameterValue("-release", getRelease());
            cmd.addParameterValue("-using", cmvcProcessingType);
            cmd.addParameterValue("-defect", newChangeset.getName());
            runCommand(cmd);
            CMVCTrackSourceInfo newTrack = (CMVCTrackSourceInfo) newChangeset;
            if (newTrack.getRestrictions()!=null) {
                setRestrictions(newTrack.getRestrictions());
            }
        } catch (MBBuildException mbe) {
            throw new RuntimeException(mbe);
        }
    }

    public void removeChangesetFromGroup(Changeset changesetToNuke) {
        try {
            Command cmd = getCommandObject("LevelMemberDelete" );
            cmd.addParameterValue("-level", getName());
            cmd.addParameterValue("-release", getRelease());
            cmd.addParameterValue("-using", cmvcProcessingType);
            cmd.addParameterValue("-defect", changesetToNuke.getName());
            runCommand(cmd);
        } catch (MBBuildException mbe) {
            throw new RuntimeException(mbe);
        }
    }


    public String getSourceIdentifyingStringForMVS() {
        try {
            if (lastState == null) {
                getState();
            }
            return " LEVEL " + lastState + " " + name;
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

    public String toString() {
        String buf = super.toString();
        buf += "Level = " + name;
        return buf;
    }
}
