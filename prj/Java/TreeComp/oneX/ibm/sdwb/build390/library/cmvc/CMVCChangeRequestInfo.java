package com.ibm.sdwb.build390.library.cmvc;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.cmvc.client.api.*;
import com.ibm.sdwb.build390.MBBuildException;

/**
 * Interface for classes that represent build source locations.
 * Examples would be CMVC (track or level), Rational (view), PDS (PDS location)
 */
public class CMVCChangeRequestInfo extends CMVCLibraryInfo implements com.ibm.sdwb.build390.library.ChangeRequest {
	public static final String CMVCSOURCENAME = "CMVCDEFECTFEATURE";
	private String name = null;
	private Set tracksForDefectFeature = null;
    private Set interestedProjects = new HashSet();
    private Set interestedProjectsChangesetAppearsIn = null;
    private String interestedProjectWhereString = null;
	private ComponentAndPathRestrictions restrictions = null;
    private boolean feature = false;
    private boolean defect = false;
	private boolean includeCommittedBase = false;

	public CMVCChangeRequestInfo(CMVCLibraryInfo tempInfo, String tempDefectFeatureName, ComponentAndPathRestrictions tempRestrict){
		super(tempInfo);
		name = tempDefectFeatureName;
		restrictions = tempRestrict;
	}

	public com.ibm.sdwb.build390.library.ChangeRequest getClone(){
		try {
			return (com.ibm.sdwb.build390.library.ChangeRequest) clone();
		}catch (CloneNotSupportedException cnse){
			throw new RuntimeException(cnse);
		}
	}

	public Set getIndividualSourceInfos(){
        if (tracksForDefectFeature == null) {
            Set projectSet = getProjectsChangesetAppearsIn();
            tracksForDefectFeature = new HashSet();
            // we've got a list of projects now, create the source infos for each
            for (Iterator projectIterator = projectSet.iterator(); projectIterator.hasNext();) {
                String  oneProject = (String) projectIterator.next();
                CMVCTrackSourceInfo oneTrack = new CMVCTrackSourceInfo(this, oneProject, name, restrictions);
                tracksForDefectFeature.add(oneTrack);
            }                
        }
        return tracksForDefectFeature;
	}

    public Set getProjectsChangesetAppearsIn(){
        if (interestedProjectsChangesetAppearsIn == null) {
            String whereString = new String("T.defectName=\'"+name+"\' and T.releaseid=R.id");
            if (interestedProjectWhereString != null) {
                whereString += " and  R.name in "+ interestedProjectWhereString; 
            }
            try {
                Command cmd = getCommandObject("ReportGeneral" );
                cmd.getObjectSpec().setValue("TrackView T, ReleaseView R");
                cmd.addParameterValue("-select", "DISTINCT R.name");
                cmd.addParameterValue("-where", whereString);

                String commandOut = runCommand(cmd);      
                interestedProjectsChangesetAppearsIn = new HashSet(parseOutput(new BufferedReader(new StringReader(commandOut)), null));
            }catch (MBBuildException mbe){
                throw new RuntimeException(mbe);
            }
        }
        return interestedProjectsChangesetAppearsIn;
    }

    public Set getInterestedProjects(){
        return interestedProjects;
    }

    public void setInterestedProjects(Set projectSet){
        interestedProjects = projectSet;
        if (interestedProjects.size() > 0) {
            interestedProjectWhereString = "(";
            for (Iterator projectIterator = interestedProjects.iterator(); projectIterator.hasNext(); ) {
                String nextProject = (String) projectIterator.next();
                interestedProjectWhereString+= "'"+nextProject+"'";
                if (projectIterator.hasNext()) {
                    interestedProjectWhereString+=", ";
                }
            }
            interestedProjectWhereString+=")";
        }
    }

	public Set getListOfMissingRequisites() throws MBBuildException{
        throw new UnsupportedOperationException();
	}

	public boolean isValidSource() throws MBBuildException{
        determineDefectOrFeature();
        return defect | feature;
	}

    private void determineDefectOrFeature(){
        try {
            if (!defect & !feature) {// nothing set, so it's either not there, or we haven't run this yet
                Command cmd = getCommandObject("ReportGeneral" );
                cmd.getObjectSpec().setValue("DefectView ");
                cmd.addParameterValue("-where", "name=\'"+getName()+"\'");
                cmd.addParameterValue("-select", "name");
                String commandOut = runCommand(cmd);
                if (commandOut.trim().length()>0){
                    feature = false;// found this in defect list, so it's not a feature
                    defect = true;
                }
                cmd = getCommandObject("ReportGeneral" );
                cmd.getObjectSpec().setValue("FeatureView ");
                cmd.addParameterValue("-where", "name=\'"+getName()+"\'");
                cmd.addParameterValue("-select", "name");
                commandOut = runCommand(cmd);
                if (commandOut.trim().length()>0){
                    feature = true;// found this in feature list, so it's a feature
                    defect = false;
                }
            }
        }catch (MBBuildException mbe){
            throw new RuntimeException("Error getting information about "+ getName()+ " from CMVC", mbe);
        }

    }

    public boolean isReadyForPackageProcessing(){
        boolean tracksAllOutOfFix = true;
        String fixString = "fix";
        Set trackSet = getIndividualSourceInfos();
        for (Iterator trackIterator = trackSet.iterator(); trackIterator.hasNext();) {
            Changeset oneChangeset = (Changeset) trackIterator.next();
            try {
                tracksAllOutOfFix = tracksAllOutOfFix & !fixString.equalsIgnoreCase(oneChangeset.getState());
            }catch (MBBuildException mbe){
                throw new RuntimeException("Error getting state of " + oneChangeset.getName() + " from CMVC");
            }
        }
        return tracksAllOutOfFix;
    }

	public void setStateToReady() throws MBBuildException{
		throw new UnsupportedOperationException("Ready state transition not supported for tracks");
	}

	public void setStateToPreReady() throws MBBuildException{
		throw new UnsupportedOperationException("Build state transition not supported for tracks");
	}

	public void setStateToCertify() throws MBBuildException{
		throw new UnsupportedOperationException("Certify state transition not supported for tracks");
	}

	public void setStateToIntegrate() throws MBBuildException{
		throw new UnsupportedOperationException("Integrate state transition not supported for tracks");
	}

	public String getState() throws MBBuildException{
        determineDefectOrFeature();
		Command cmd = getCommandObject("ReportGeneral" );
        if (feature) {
            cmd.getObjectSpec().setValue("FeatureView");
        }else if (defect) {
            cmd.getObjectSpec().setValue("DefectView");
        }else {
            throw new RuntimeException("Unable to determine defect or feature");
        }
		cmd.addParameterValue("-where", "Name=\'"+getName()+"\'");
		cmd.addParameterValue("-select", "state");

		return runCommand(cmd);
	}

	public void create() throws MBBuildException{
		throw new UnsupportedOperationException("Create not supported for tracks");
	}

	public void delete() throws MBBuildException{
		throw new UnsupportedOperationException("Delete not supported for tracks");
	}

	public void rename(String newName) throws MBBuildException{
		throw new UnsupportedOperationException("Rename not supported for tracks");
	}

	/**
	 * For determining if this is a feature or defect track
	 * 
	 * @return 
	 */
	private String getTrackType(){
        determineDefectOrFeature();
        if (feature) {
            return "feature";
        }else if (defect) {
            return "defect";
        }else {
            throw new RuntimeException("Unable to determine defect or feature");
        }
    }

	public void setName(String newName){
		name = newName;
	}

	public String getName(){
		return name;
	}

	public ComponentAndPathRestrictions getRestrictions(){
		return restrictions;
	}

	public void setRestrictions(ComponentAndPathRestrictions tempRestrict){
		restrictions = tempRestrict;
	}

	public String toString(){
		String buf = super.toString();
		buf += "Track = " + getName();
		return buf;
	}
}
