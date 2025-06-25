package com.ibm.sdwb.build390.library.cmvc;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.configuration.ConfigurationAccess;
import com.ibm.sdwb.build390.help.HelpLoaderInterface;
import com.ibm.sdwb.build390.info.RequisiteGraphNode;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.MetadataServerOperationsInterface;
import com.ibm.sdwb.build390.library.cmvc.server.CMVCAuthorizationVerifier;
import com.ibm.sdwb.build390.library.cmvc.server.CMVCLibraryServerInterface;
import com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUserinterfaceWidgetFactory;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;
import com.ibm.sdwb.build390.security.PasswordManager;
import com.ibm.sdwb.build390.user.authorization.AuthorizationCheck;
import com.ibm.sdwb.build390.userinterface.text.utilities.CommandLineSettings;
import com.ibm.sdwb.build390.utilities.BinarySettingUtilities;
import com.ibm.sdwb.cmvc.client.api.*;

/** <br>LibraryInfo holds a single set of information relating to a library.
*   <br>This is information tells what type of library to use, it's location,
*   <br>and the username to connect as.
*/
public class CMVCLibraryInfo extends AbstractLibraryInfo implements Serializable, Cloneable {


    static final long serialVersionUID = 1111111111111111L;

    private Map projectConfigurationMap = new HashMap();
    private int cmvcPort = -1;
    private String userName = null;
    private boolean passwordAuthentication = false;
    private CMVCUserinterfaceWidgetFactory widgetFactory = null;
    protected String cmvcProcessingType = null;
    public static final String LEVEL = "level";
    public static final String LEVELMEMBER = "levelmember";
    public static final String TRACK = "track";

    //ken's hack writer (oh!  a literary pun. yummy)
    private static PrintWriter dumpBaby = null;



    private transient MetadataOperationsInterface metadataOps;
    private transient MetadataServerOperationsInterface metadataServerHolder;
    private transient HelpLoaderInterface helpLoader;

    private static  int         LIBRARYTHREADLIMIT = 5;



    /*  Constructor which initializes all the member variable settings
    */
    public CMVCLibraryInfo() {
        widgetFactory = new CMVCUserinterfaceWidgetFactory(this);
    }

    public CMVCLibraryInfo(CMVCLibraryInfo oldInfo) {
        super(oldInfo);
        cmvcPort = oldInfo.cmvcPort;
        userName = oldInfo.userName;
        passwordAuthentication = oldInfo.passwordAuthentication; 
        cmvcProcessingType = oldInfo.cmvcProcessingType;
        widgetFactory = new CMVCUserinterfaceWidgetFactory(this);
        if (MBClient.getCommandLineSettings().isSwitchSet(CommandLineSettings.REMOTE)) {
            LIBRARYTHREADLIMIT =1;
        }
    }

    public void setProcessingType(String newType) {
        cmvcProcessingType = newType;
    }

    public int getCMVCPort() {
        return cmvcPort;
    }

    public String getCMVCPortAsString() {
        return Integer.toString(cmvcPort);
    }

    public void setCMVCPort(int tempPort) {
        cmvcPort = tempPort;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String tempUser) {
        userName = tempUser;
    }

    public boolean isUsingPasswordAuthentication() {
        return passwordAuthentication;
    }

    public void setUsingPasswordAuthentication(boolean tempPassword) {
        passwordAuthentication = tempPassword;
    }

    public String getAuthenticationKey() {
        return getUsername()+"@"+getProcessServerName()+"@"+getProcessServerAddress();
    }

    public String getDescriptiveString() {
        return super.getDescriptiveString()+" "+userName;
    }

    public String getAddressStringForMVS() {
        return getProcessServerAddress()+"@"+Integer.toString(cmvcPort);
    }

    public com.ibm.sdwb.build390.library.userinterface.UserinterfaceWidgetFactory getUserinterfaceFactory() {
        return widgetFactory;
    }

    public Map getPrereqsAndCoreqs(Set changesetsToCheck, boolean includeStates, Set stateList) {

        Map nodeTracker = new HashMap();
        Set allTracks = new HashSet();
        Map coreqTracker = new HashMap();
        // build the cmvc query
        try {
            Command cmd = getPrereqsAndCoreqsCommand1(changesetsToCheck, includeStates, stateList);
            String checkResult = runCommand(cmd);
            BufferedReader reader = new BufferedReader(new StringReader(checkResult));
            String tempString;
// this code parses the return table & puts the info into a map.
            while ((tempString = reader.readLine()) != null) {
                if (tempString.trim().length() > 0) {
                    StringTokenizer tempToke  = new StringTokenizer(tempString, "|");
                    String type = tempToke.nextToken();
                    // here we get or create the track we're talking about, and add prereq info to it.
                    if (type.equals("prereq")) {
                        tempToke.nextToken(); // eat the next token
                        String track = tempToke.nextToken();
                        String prereq = tempToke.nextToken();
                        RequisiteGraphNode tempNode = (RequisiteGraphNode) nodeTracker.get(track);
                        if (tempNode == null) {
                            tempNode = new RequisiteGraphNode();
                            tempNode.addMember(track);
                            nodeTracker.put(track, tempNode);
                        }
                        tempNode.addPrereq(prereq);
                        tempNode.addExplicitPrereq(prereq);
                    } else if (type.equals("coreq")) {
                        // here we get or create the node & add a track coreq to it.
                        String coreqGroup = tempToke.nextToken();
                        String track = tempToke.nextToken();
                        RequisiteGraphNode tempNode = (RequisiteGraphNode) coreqTracker.get(coreqGroup);
                        if (tempNode == null) {
                            tempNode = (RequisiteGraphNode) nodeTracker.get(track);
                            if (tempNode == null) {
                                tempNode = new RequisiteGraphNode();
                                nodeTracker.put(track, tempNode);
                            }
                            coreqTracker.put(coreqGroup, tempNode);
                        }
                        // this is necessary, because if a & b are coreqs & b has been created, then a could be added to
                        // it's node, without a track entry for a being made.
                        if (nodeTracker.get(track) == null) {
                            nodeTracker.put(track, tempNode);
                        }
                        tempNode.addMember(track);
                    } else if (type.equals("track")) {
                        String track = tempToke.nextToken();
                        track = tempToke.nextToken();
                        allTracks.add(track);
                    }
                }
            }
            reader.close();
/*  This query gets implicit prereqs for a level,
*/
            cmd = getPrereqsAndCoreqsCommand2(changesetsToCheck,includeStates, stateList);
            checkResult = runCommand(cmd);
            reader = new BufferedReader(new StringReader(checkResult));
// this code parses the return table & puts the info into a hashtable.
            while ((tempString = reader.readLine()) != null) {
                if (tempString.trim().length() > 0) {
                    StringTokenizer tempToke  = new StringTokenizer(tempString, "|");
                    String type = tempToke.nextToken();
                    // here we get or create the track we're talking about, and add prereq info to it.
                    if (type.equals("prereq")) {
                        String track = tempToke.nextToken();
                        track = tempToke.nextToken();
                        String prereq = tempToke.nextToken();
                        RequisiteGraphNode tempNode = (RequisiteGraphNode) nodeTracker.get(track);
                        if (tempNode == null) {
                            tempNode = new RequisiteGraphNode();
                            tempNode.addMember(track);
                            nodeTracker.put(track, tempNode);
                        }
                        tempNode.addPrereq(prereq);
                    }
                }
            }
            reader.close();
            Iterator trackIterator = allTracks.iterator();
            while (trackIterator.hasNext()) {
                String testTrack = (String) trackIterator.next();
                if (nodeTracker.get(testTrack) == null) {
                    RequisiteGraphNode tempNode = new RequisiteGraphNode();
                    tempNode.addMember(testTrack);
                    nodeTracker.put(testTrack, tempNode);
                }
            }
        } catch (MBBuildException mbe) {
            throw new RuntimeException("An error occurred while reading the list of prereqs and coreqs" , mbe);
        } catch (IOException ioe) {
            throw new RuntimeException("An error occurred while reading the list of prereqs and coreqs" , ioe);
        }
        return nodeTracker;
    }


    private Command getPrereqsAndCoreqsCommand1(Set changesetSet, boolean includeStatesListed, Set states) throws LibraryError{
/*  This query unions 2 separate queries.  coreq info returns with 'coreq' in front of it,
    and prereq info has 'prereq'.  There are string & numerical constants to make the fields equal
    out so the 2 queries can be unioned.  The first half returns all coreq info about a given level.
    The second returns all prereq info about the same level.
*/
        String fromString = null;
        String tracks = new String();
        String project = null;
        for (Iterator trackIterator = changesetSet.iterator(); trackIterator.hasNext();) {
            Changeset oneChangeset = (Changeset) trackIterator.next();
            project = oneChangeset.getProject();// yes, we'll do this multiple times when we only need to do it once. But they should all be the same project, so it doesn't matter.
            tracks +="'"+oneChangeset.getName()+"'";
            if (trackIterator.hasNext()) {
                tracks += ", ";
            }
        }

        //getStatusHandler().updateStatus("Getting requisite for " + tracks + " in " + project + " from library.", false);//TST3447

        fromString="(SELECT DISTINCT 'coreq', C1.groupId, D.name, 'dummy' "+
                   "FROM Tracks T1, Coreqs C1, Defects D "+
                   "WHERE C1.groupId in "+
                   "(SELECT C2.groupId FROM TrackView T2, Coreqs C2 "+
                   "WHERE T2.releaseName='"+project+"' and T2.defectName in ("+tracks+") "+
                   "and C2.trackId=T2.id) " +
                   "and C1.trackId=T1.id and T1.defectId=D.id "+
                   "UNION "+
                   "SELECT 'track', 0000, T1.defectname, 'dummy' "+
                   "FROM TrackView T1 "+
                   "WHERE T1.releaseName='"+project+"' and T1.defectName in ("+tracks+")  "+
                   "UNION "+
                   "SELECT 'prereq', 0000, T1.defectName, T2.defectName "+
                   "FROM TrackView T1, TrackView T2, Prereqs P "+
                   "WHERE T1.releaseName='"+project+"' and T2.releaseName='"+project+"' and "+
                   "P.trackId=T1.id and P.id=T2.id and T1.defectName in ("+tracks+") ";

        String stateList = new String();
        for (Iterator stateIterator = states.iterator(); stateIterator.hasNext();) {
            stateList +="'"+(String) stateIterator.next()+"'";
            if (stateIterator.hasNext()) {
                stateList += ", ";
            }
        }
        String inclusionVariable = null;
        if (includeStatesListed) {
            inclusionVariable = " in ";
        } else {
            inclusionVariable = " not in ";
        }
        fromString += "and   ((T2.id not in (SELECT DISTINCT trackid FROM LevelMembers) "+
                      "AND T2.state "+inclusionVariable+" ("+stateList +"))"+
                      "or "+
                      "(EXISTS    (SELECT * FROM LevelMemberView lm, Levels l "+
                      "WHERE lm.trackid=T2.id and l.id=lm.levelid and "+
                      "l.state "+inclusionVariable+" ("+stateList +"))))) AS result" ;

        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue(fromString);
        return cmd;
    }


/*  This query gets implicit prereqs for a level,
*/
    private Command getPrereqsAndCoreqsCommand2(Set changesetSet, boolean includeStatesListed, Set states) throws LibraryError{
        String fromString = null;
        String selectString = null;
        String whereString = null;
        String project = null;
        String tracks = new String();
        for (Iterator trackIterator = changesetSet.iterator(); trackIterator.hasNext();) {
            Changeset oneChangeset = (Changeset) trackIterator.next();
            project = oneChangeset.getProject();// yes, we'll do this multiple times when we only need to do it once. But they should all be the same project, so it doesn't matter.
            tracks +="'"+ oneChangeset.getName()+"'";
            if (trackIterator.hasNext()) {
                tracks += ", ";
            }
        }
        fromString="TrackView T1, TrackView T2 ";
        selectString="DISTINCT 'prereq', 0000, T1.defectName, T2.defectName";
        whereString="T1.releaseName='"+project+"' and T2.releaseName='"+project+"'  "+
                    "and T1.defectName in ("+tracks+") and T2.defectId!=T1.defectId and "+
                    "T2.defectId in (SELECT T3.defectid FROM Changes C1, Changes C2, Versions V1, Versions V2, Tracks T3 "+
                    "WHERE C1.fileId=C2.fileId and C2.trackId=T1.id and V2.id=C2.versionId and C1.versionId=V1.id "+
// Ken 4/23/01 we don't need to convert this to verDate because we are ignoring any prereqs that haven't had conflicts resolved, 
// and the change dates will have the same ordering the verdates would for our purposes.
                    "and V1.SID NOT LIKE 'v.%' and V1.changeDate<=V2.changeDate and C1.trackId=T3.id ";

        String stateList = new String();
        for (Iterator stateIterator = states.iterator(); stateIterator.hasNext();) {
            stateList +="'"+(String) stateIterator.next()+"'";
            if (stateIterator.hasNext()) {
                stateList += ", ";
            }
        }
        String inclusionVariable = null;
        if (includeStatesListed) {
            inclusionVariable = " in ";
        } else {
            inclusionVariable = " not in ";
        }
        whereString += "and   ((T3.id not in (SELECT DISTINCT trackid FROM LevelMembers) "+
                       "AND T3.state "+inclusionVariable+" ("+stateList +"))"+
                       "or "+
                       "(EXISTS    (SELECT * FROM LevelMemberView lm, Levels l "+
                       "WHERE lm.trackid=T3.id and l.id=lm.levelid and "+
                       "l.state "+inclusionVariable+" ("+stateList +")))))" ;
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue(fromString);
        cmd.addParameterValue("-select", selectString);
        cmd.addParameterValue("-where", whereString);
        return cmd;
    }



    public HelpLoaderInterface getHelpLoaderInterface() {
        try {
            if (helpLoader==null) {
                helpLoader = new CMVCHelpLoader();
            }
        } catch (com.ibm.sdwb.build390.help.HelpException hpe) {
            getLEP().LogException(hpe);
        }
        return helpLoader;
    }

    public ConfigurationAccess getConfigurationAccess(String project, boolean cached) {
        if (cached) {
            return new com.ibm.sdwb.build390.configuration.ConfigurationAccessImplementation(this, project);
        }
        if (projectConfigurationMap == null) {
            projectConfigurationMap = new HashMap();
        }
        if (!projectConfigurationMap.containsKey(project)) {
            projectConfigurationMap.put(project, new com.ibm.sdwb.build390.configuration.ConfigurationAccessImplementation(this, project));
        }
        return(ConfigurationAccess) projectConfigurationMap.get(project);

    }

    public com.ibm.sdwb.build390.metadata.MetadataOperationsInterface getMetadataOperationsHandler() {
        try {
            if (metadataOps==null) {
                metadataOps =  new com.ibm.sdwb.build390.library.cmvc.metadata.CMVCMetadataOperationsHandler((com.ibm.sdwb.build390.library.cmvc.metadata.server.MetadataServerOperationsInterface) getLibraryServer() );
            }
        } catch (Exception ex) {
            throw new RuntimeException("An error occurred while trying to create metadata operations handler.",ex);
        }
        return metadataOps;
    }

    public boolean isLibraryConnectionValid() {
        try {
            Command cmd = getCommandObject("ReportView" );
            cmd.getObjectSpec().setValue("fileview");
            cmd.addParameterValue( "-where", "pathname='-'");
            runCommand(cmd);

        } catch (MBBuildException e) {
            getLEP().LogException(e);
            return false;
        }
        return true;
    }

    public boolean isValidLibraryProject(String project) throws MBBuildException{
        String result = new String();
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("releaseview");
        cmd.addParameterValue("-where", "name='"+project+"'");
        result = runCommand(cmd);

        if (result.length() <= 0) {
            throw new LibraryError("Please enter a valid library release.  " + project + " is invalid."); 
        }
        return true;
    }


    public Set getProjectsInServiceMode() throws MBBuildException{
        String result = new String();
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("releaseview");
        cmd.addParameterValue("-where", "relProcess='service'");
        result = runCommand(cmd);
        return new HashSet(parseOutput(new BufferedReader(new StringReader(result)), null));
    }

    public Set getProjectsNotInServiceMode() throws MBBuildException{
        String result = new String();
        Command cmd = getCommandObject("ReportGeneral" );
        cmd.getObjectSpec().setValue("releaseview");
        cmd.addParameterValue("-where", "relProcess not in ('service')");
        result = runCommand(cmd);
        return new HashSet(parseOutput(new BufferedReader(new StringReader(result)), null));
    }

    public ChangesetGroup getChangesetGroup(String groupName, String project) {
        CMVCLevelSourceInfo newGroup = new CMVCLevelSourceInfo(this, project, groupName, null);
        return newGroup;
    }

    public String getBuildableObjects(String release, Set allowableLevelStates, Set allowableTrackStates) throws MBBuildException{
        Command cmd = getCommandObject("ReportGeneral" );
        StringBuffer constructTableOfBuildables = new StringBuffer("SELECT DISTINCT '"+LEVEL+"', l.state, l.name "+
                                                                   "FROM LevelView l "+
                                                                   "WHERE l.releaseName='"+release+"'");
        if (allowableLevelStates!=null) {
            constructTableOfBuildables.append(" and l.state in (");
            for (Iterator levelStateIterator = allowableLevelStates.iterator(); levelStateIterator.hasNext();) {
                constructTableOfBuildables.append("'"+(String)levelStateIterator.next()+"'");
                if (levelStateIterator.hasNext()) {
                    constructTableOfBuildables.append(", ");
                }
            }
            constructTableOfBuildables.append(")");
        }
        constructTableOfBuildables.append(" UNION ");
        constructTableOfBuildables.append(
                                         "SELECT DISTINCT '"+TRACK+"', t.state, t.defectName "+
                                         "FROM TrackView t "+
                                         "WHERE t.releaseName='"+release+"'");
        if (allowableTrackStates!=null) {
            constructTableOfBuildables.append(" and t.state in (");
            for (Iterator trackStateIterator = allowableTrackStates.iterator(); trackStateIterator.hasNext();) {
                constructTableOfBuildables.append("'"+(String)trackStateIterator.next()+"'");
                if (trackStateIterator.hasNext()) {
                    constructTableOfBuildables.append(", ");
                }
            }
            constructTableOfBuildables.append(")");
        }
        constructTableOfBuildables.append(" UNION ");
        constructTableOfBuildables.append("SELECT DISTINCT '"+LEVELMEMBER+"', l.name, lm.defectName "+
                                          "FROM LevelView l, LevelMemberView lm "+
                                          "WHERE l.releaseName='"+release+"' and lm.levelid=l.id");
        if (allowableLevelStates!=null) {
            constructTableOfBuildables.append(" and l.state in (");
            for (Iterator levelStateIterator = allowableLevelStates.iterator(); levelStateIterator.hasNext();) {
                constructTableOfBuildables.append("'"+(String)levelStateIterator.next()+"'");
                if (levelStateIterator.hasNext()) {
                    constructTableOfBuildables.append(", ");
                }
            }
            constructTableOfBuildables.append(")");
        }

        cmd.getObjectSpec().setValue("("+constructTableOfBuildables.toString()+") as r");
        String commandOut = runCommand(cmd);
        return commandOut;
    }

    public void doCopy(com.ibm.sdwb.build390.info.FileInfo fileInfo, MBMainframeInfo mainInfo, String destinationName) throws LibraryError{
        try {
            Command cmd = getCommandObject("FileExtract" );

            cmd.getObjectSpec().setValue(fileInfo.getDirectory()+fileInfo.getName());

            String mainframePassword = PasswordManager.getManager().getPassword(mainInfo.getMainframeUsername()+"@"+mainInfo.getMainframeAddress());
            cmd.addParameterValue("-root","/");
            String fileType = "A";
            if (fileInfo.isBinary()) {
                fileType = "I";
            }
// ken's hack
            cmd.addParameterValue("-verbose");

            cmd.addParameterValue("-node", "ftp://"+mainInfo.getMainframeUsername()+":"+mainframePassword+"@"+mainInfo.getMainframeAddress()+":21/'"+destinationName+"';type="+fileType);
            cmd.addParameterValue("-release", fileInfo.getProject());
            cmd.addParameterValue("-retry", "4*");
            if (fileInfo.getFTPFileType().equals("ASCII")) {
                cmd.addParameterValue("-crlf");
            }
            String ftpArguments = fileInfo.getFTPArguments();

            String ebcdicType = "TYPE E";
            int index = ftpArguments.indexOf(ebcdicType);
            if (index != -1) {
                String startStr = ftpArguments.substring(0,index);
                String endStr =  ftpArguments.substring(index+ebcdicType.length());
                ftpArguments = startStr+"TYPE A"+endStr;
            }

            cmd.addParameterValue("-nodeargs", ftpArguments);
            cmd.addParameterValue("-using", cmvcProcessingType);

            if (fileInfo.getVersion() != null) {
                cmd.addParameterValue( "-version", fileInfo.getVersion());
            }
// hacks back
            try {
                runCommand(cmd);
            } catch (LibraryError le) {
                le.printStackTrace();
                le.printStackTrace(getDumpWriter());
                runCommand(cmd);
            } catch (MBBuildException mbe) {
                mbe.printStackTrace();
                mbe.printStackTrace(getDumpWriter());
                runCommand(cmd);
            }
        } catch (LibraryError le) {
            le.printStackTrace();
            throw le;
        } catch (MBBuildException mbe) {
            mbe.printStackTrace();
            throw new LibraryError("Error extracting part to MVS", mbe);
        }
    }

//ken's hack method
    private PrintWriter getDumpWriter() {
        try {
            synchronized (MBClient.client) {
                if (dumpBaby ==null) {
                    // yeah, I have an immature sense of humor
                    dumpBaby = new PrintWriter(new FileWriter(new File(MBClient.getCacheDirectory(), "kensDumpHolder.log"), true));
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return dumpBaby;
    }

    protected CMVCLibraryServerInterface getCMVCProcessServer() throws LibraryError{
        return(CMVCLibraryServerInterface) getLibraryServer();
    }

    protected Command getCommandObject(String commandSpec) throws LibraryError{
        try {
            CommandFactory theFactory = CommandFactory.getInstance();
            synchronized (theFactory) {
                Command cmd = theFactory.getCommand(commandSpec);
                return cmd;
            }
        } catch (com.ibm.sdwb.cmvc.util.DataSourceException e) {
            Throwable emb = e.getDeepestThrowable(e);
            if (emb == null) {
                throw new LibraryError("An error occurred creating command "+commandSpec, e);
            } else {
                throw new LibraryError("An error occurred creating command "+commandSpec, (Exception) emb);
            }
        }
    }

    protected   String runCommand(Command cmvcCommand) throws MBBuildException{
        return runCommand(cmvcCommand,null);
    }

    /** This method is used to run the library commands, and return a string
    * with the command output, or an exception if there is an error
    */
    protected String runCommand(Command cmvcCommand, String workPath)  throws MBBuildException{
        String methodName = new String("CMVCLibraryInfo:runCommand");
        FamilyInfo familyInfo = JavaAPIInterface.getFamilyInfoObject(getProcessServerName(), getProcessServerAddress(), getCMVCPortAsString());
        ClientDefaults clientDefaults = JavaAPIInterface.getClientDefaults(getUsername(), isUsingPasswordAuthentication(), workPath);
        SessionData sessionData = new SessionData(); 
        sessionData.setClientDefaults(clientDefaults);
        if (getProcessServerName().toUpperCase().equals(System.getProperty("user.name").toUpperCase())) {
            JavaAPIInterface.handleAuthentication(System.getProperty("user.name"), getProcessServerName(), null, familyInfo, sessionData);
        } else {
            if (isUsingPasswordAuthentication()) {
                String pw = PasswordManager.getManager().getPassword(getAuthenticationKey(), false);
                JavaAPIInterface.handleAuthentication(System.getProperty("user.name"), getUsername(), pw, familyInfo, sessionData);
            } else {
                JavaAPIInterface.handleAuthentication(System.getProperty("user.name"), getUsername(), null, familyInfo, sessionData);
            }
        }
        cmvcCommand.setFamilyInfo( familyInfo );
        cmvcCommand.setSessionData( sessionData );
        String obSpecValue = null;
        if (cmvcCommand.getObjectSpec()!=null) {
            obSpecValue = cmvcCommand.getObjectSpec().getValue();
        }

        String commandInfo = "CMVC command: " + cmvcCommand.getName() +" "+ obSpecValue+ "  Family Info: " + familyInfo + "  Client Defaults: "+clientDefaults;
        getLEP().LogPrimaryInfo("Debug", commandInfo, true);
        MBThreadLimit libLimit;
        synchronized(MBClient.lockCache) {
            libLimit = (MBThreadLimit) MBClient.lockCache.get(getProcessServerName());
            if (libLimit == null) {
                libLimit = new MBThreadLimit(LIBRARYTHREADLIMIT,getLEP());
                MBClient.lockCache.put(getProcessServerName(), libLimit);
            }
        }
        libLimit.waitCounter();
        String outputString = null;
        try {
            CommandResults results = cmvcCommand.exec();

            outputString = JavaAPIInterface.checkCMVCResults(results, cmvcCommand, null);
        } catch (CommandConstraintException cce) {
            throw new LibraryError("The command was misformed: "+cmvcCommand.toString(), cce);
        } catch (FamilyNotFoundException fnfe) {
            throw new LibraryError("The family was not found: "+getProcessServerName()+"@"+getProcessServerAddress()+"@"+getCMVCPortAsString(), fnfe);
        } catch (Exception ioe) {
            throw new LibraryError("An error occurred communicating with the family", ioe);
        } finally {
            libLimit.notifyCounter();
        }
        return outputString;
    }

    //The super.clone() doesn't work, since the userinterfaceFactory retains an old reference to the libraryOInfobject. 
    //other classes should possibly use clone(), for instance SourceInfo when clone, needs the SourceInfo type as well. when a new instance is created, its lost.
    public LibraryInfo cloneLibraryInfo() {
        CMVCLibraryInfo info = new CMVCLibraryInfo();
        info.setProcessServerName(getProcessServerName());
        info.setProcessServerAddress(getProcessServerAddress());
        info.setProcessServerPort(getProcessServerPort());
        info.setUsername(getUsername());
        info.setUsingPasswordAuthentication(isUsingPasswordAuthentication());
        info.setCMVCPort(getCMVCPort());
        return info;
    }

    //SourceInfo objects should use this clone. 
    protected Object clone() throws CloneNotSupportedException {
        try {
            CMVCLibraryInfo info = (CMVCLibraryInfo)super.clone();
            info.setProcessServerName(getProcessServerName());
            info.setProcessServerAddress(getProcessServerAddress());
            info.setProcessServerPort(getProcessServerPort());
            info.setUsername(getUsername());
            info.setUsingPasswordAuthentication(isUsingPasswordAuthentication());
            info.setCMVCPort(getCMVCPort());
            return info;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Problem making clone of "+getClass().getName(), cnse);
        }
    }


    /** show displays the fields of this object */
    public String toCompleteString() {
        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);
        formatter.format("%s",super.toCompleteString());
        formatter.format("%-29s=%s%n","CMVCPORT",cmvcPort);
        formatter.format("%-29s=%s%n","LIBRARY","CMVC");
        formatter.format("%-29s=%s%n","LIBRARYUSER",userName);
        formatter.format("%-29s=%s%n","LIBRARYPASSWORDAUTHENTICATION",BinarySettingUtilities.convertToMainframeSetting(passwordAuthentication));
        formatter.format("%s%s=%n","pw",getAuthenticationKey());
        return buf.toString();
    }



    public com.ibm.sdwb.build390.user.authorization.AuthorizationCheck getAuthorizationChecker() {
        try {
            String password = PasswordManager.getManager().getPassword(getAuthenticationKey(), false);
            String address = java.net.InetAddress.getLocalHost().getHostAddress();
            AuthorizationCheck authChecker = new CMVCAuthorizationVerifier(getUsername(), address, password, isUsingPasswordAuthentication());
            return authChecker;
        } catch (MBBuildException mbe) {
            throw new RuntimeException(mbe);
        } catch (java.net.UnknownHostException uhe) {
            throw new RuntimeException("Information about the local network connection could not be retrieved", uhe);
        }
    }

    public static void main(String[] args) throws Exception {
        CMVCLibraryInfo tempInfo = new CMVCLibraryInfo();
        tempInfo.setCMVCPort(1200);
        tempInfo.setProcessServerAddress("colonel.storage.tucson.ibm.com");
        tempInfo.setProcessServerName("corndog");
        tempInfo.setProcessServerPort(2102);
        tempInfo.setUsername("khorne");
        tempInfo.setUsingPasswordAuthentication(true);
        PasswordManager.getManager().setPassword("khorne@colonel.storage.tucson.ibm.com@1200", "temp4now");

        //  System.out.println(tempInfo.getCMVCProcessServer().getConfigurationPreferences().toString());
    }

    protected List parseOutput(BufferedReader reader, LineParser parser) {
        List outputList = new ArrayList();
        try {
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                if (tempString.trim().length() > 0) {
                    if (parser!=null) {
                        outputList.add(parser.parseLine(tempString));
                    } else {
                        outputList.add(tempString);
                    }
                }
            }
            reader.close();
        } catch (IOException ioe) {
            throw new RuntimeException("An error occurred while attempting to read the results of a CMVC command", ioe);
        }
        return outputList;
    }

    protected interface LineParser {
        public Object parseLine(String line);
    }
}

