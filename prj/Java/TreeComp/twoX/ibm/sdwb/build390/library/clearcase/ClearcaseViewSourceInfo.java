package com.ibm.sdwb.build390.library.clearcase;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.rational.clearcase.*;
import com.ibm.sdwb.build390.*;


public class ClearcaseViewSourceInfo extends ClearcaseLibraryInfo implements com.ibm.sdwb.build390.library.ChangesetGroup {
    public static final String CLEARCASESOURCENAME = "CLEARCASEVIEW";
    private String project = null;
    private String name = null;
    private File mountPoint = null;
    private String lastState = null;
    private static Random randomSource = new Random();

    public ClearcaseViewSourceInfo(ClearcaseLibraryInfo tempInfo, String tempView, File tempMount) {
        super(tempInfo);
        String tempProject = null;
        try {
            tempProject = ClearToolAPI.getProjectInfoForView(tempView).getName();
        }
        catch(CTAPIException ce) {
            throw new RuntimeException(ce);
        }
        initialize(tempView, tempProject, tempMount);
    }

    public ClearcaseViewSourceInfo(ClearcaseLibraryInfo tempInfo, String tempView, String tempProject, File tempMount) {
        super(tempInfo);
        initialize(tempView, tempProject, tempMount);
    }

    private void initialize(String tempView, String tempProject, File tempMount) {
        name = tempView;
        mountPoint = tempMount;
        project = tempProject;
    }

    public String getProject() {
        return project;
    }


    public void addChangesetToGroup(Changeset newChangeset) {
        throw new RuntimeException("Add changeset not supported");
    }

    public void removeChangesetFromGroup(Changeset changesetToNuke) {

        throw new RuntimeException("Remove changeset not supported");
    }


    public File getMountPoint() {
        return mountPoint;
    }

    public com.ibm.sdwb.build390.library.SourceInfo getClone() {
        try {
            return(com.ibm.sdwb.build390.library.SourceInfo) clone();
        }
        catch(CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }

    public void markProcessingComplete() throws MBBuildException{
        // Ken6.0
    }

    public Set getSetOfParts() throws MBBuildException{
        Set partSet = new HashSet();
        try {
//			Date startTime = new Date();
            List partList = ClearToolAPI.getFilesInView(mountPoint.getAbsolutePath()+getName(), false);
//			Date finishTime = new Date();
//			long duration = finishTime.getTime() - startTime.getTime();
//			long minuteDuration = duration / 1000 /*now it's seconds*/ / 60 ; /*now it's minutes*/

//System.out.println("time to run in minutes " + minuteDuration +   "\n for " + partList.size() + " parts");
            Set usedMainframeNames = new HashSet();
            for(Iterator partIter = partList.iterator(); partIter.hasNext();) {
                ClearcaseFileInfo oneInfo = new ClearcaseFileInfo((DescriptionInfo) partIter.next(), mountPoint);
                String tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
                while(usedMainframeNames.contains(tempMainframeName)) {
                    tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
                };
                oneInfo.setMainframeFilename(tempMainframeName);
                usedMainframeNames.add(tempMainframeName);
                partSet.add(oneInfo);
            }
        }
        catch(CTAPIException ctae) {
            throw new LibraryError("Error getting list of parts to build", ctae);
        }
        return partSet;

    }

    public Set getChangesetsInSource() throws MBBuildException{
        Set results = new HashSet();
        // Ken6.0
        return results;
    }

    public Set getListOfMissingRequisites() throws MBBuildException{
        Set returnSet = new HashSet();
        // Ken6.0
        return returnSet;
    }

    public boolean isValidSource() throws MBBuildException{
        return true; // Ken6.0
    }

    public void setStateToReady() throws MBBuildException{
        // Ken6.0
    }

    public void setStateToPreReady() throws MBBuildException{
        // Ken6.0
    }

    public void setStateToCertify() throws MBBuildException{
        // Ken6.0
    }

    public void setStateToIntegrate() throws MBBuildException{
        // Ken6.0
    }

    public String getState() throws MBBuildException{
        // Ken6.0
        return null;
    }

    public void create() throws MBBuildException{
        // Ken6.0
    }

    public void delete() throws MBBuildException{
        // Ken6.0
    }

    public void rename(String newName) throws MBBuildException{
        // Ken6.0
    }

    public String getSourceIdentifyingStringForMVS() {
        return " VIEW " + name;
    }

    public void setIncludingCommittedBase(boolean including){
        // nothing to do
    }

    public boolean isIncludingCommittedBase(){
        return true;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String buf = super.toString();
        buf += "View = " + name;
        return buf;
    }

    public static void main(String[] args) throws Exception{
        System.out.println("RIGHT ONE");
    }
}
