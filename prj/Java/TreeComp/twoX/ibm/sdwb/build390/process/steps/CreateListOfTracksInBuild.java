package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.*;
import java.io.*;
import java.util.*;

public class CreateListOfTracksInBuild extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;
    private boolean writeAndUploadFile = true;
    private Set tracksFound = null;
	private MBBuild build = null;
    private SourceInfo source = null;
	private File saveLocation = null;
    private static final String USERMODPHASEFILE = "driverMembers.list";

    public CreateListOfTracksInBuild(MBBuild tempBuild, File tempSave, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Create List Of Tracks In Build");
        build = tempBuild;
		source = build.getSource();
		saveLocation = new File(tempSave, "FixList") ;
        setUndoBeforeRerun(false);
    }

    public void setWriteAndUploadFile(boolean tempDoWrite){
        writeAndUploadFile = tempDoWrite;
    }

    public Set getTracksFound(){
        return tracksFound;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        int listSize=0;
        
		getStatusHandler().updateStatus("Getting information for " + source.getName(), false);
        Set resultSet = source.getChangesetsInSource();
        tracksFound = processResultsToSimpleTrackSet(resultSet);

        if (writeAndUploadFile) {
            getStatusHandler().updateStatus("Uploading member list file", false);
            if (!resultSet.isEmpty()) {
                try {
                    writeResultFile(resultSet, saveLocation);
                    String MVSMembersFile = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+"COMMENTS("+build.get_buildid()+")";
                    MBFtp mftp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
                    if (!mftp.put(saveLocation, MVSMembersFile)) {
                        throw new FtpError("Could not upload "+saveLocation.getAbsolutePath()+" to "+MVSMembersFile);
                    }
                } catch (IOException ioe) {
                    throw new GeneralError("Writing the track list file.", ioe);
                }
            }
        }
    }

    private void writeResultFile(Set results, File fileLocation) throws IOException{
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileLocation));
        for (Iterator resultIterator = results.iterator(); resultIterator.hasNext(); ) {
            fileWriter.write((String) resultIterator.next() + MBConstants.NEWLINE);
        }
        fileWriter.close();
    }

    private Set processResultsToSimpleTrackSet(Set results){
        Set returnSet = new HashSet();
        for (Iterator resultIterator = results.iterator(); resultIterator.hasNext(); ) {
            String oneResult = (String) resultIterator.next();
            returnSet.add(oneResult.substring(oneResult.indexOf("-")+1));
        }
        return returnSet;
    }
}
