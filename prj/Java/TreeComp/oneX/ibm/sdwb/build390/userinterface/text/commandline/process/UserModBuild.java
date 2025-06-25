package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.UsermodGeneralInfo;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import static com.ibm.sdwb.build390.userinterface.text.commandline.arguments.LogicalReqType.*;
import com.ibm.sdwb.build390.utilities.*;

public class UserModBuild extends CommandLineProcess {

    public static final String PROCESSNAME = "USERMOD";

    private LibraryRelease libraryRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private Description description = new Description();
    private LibraryLevel level = new LibraryLevel();
    private LibraryTrack track = new LibraryTrack();
    private BuildType buildtype = new BuildType(); /* required during driverbuild */

    private ListingsGenerate listGen = new ListingsGenerate();
    private MainframeOutputScannersExecute runScanners = new MainframeOutputScannersExecute();
    private MainframeReturnCode buildCC = new MainframeReturnCode();
    private AutomaticDependencyChecking autoBuild = new AutomaticDependencyChecking();
    private ForceRebuild rebuild = new ForceRebuild();
    private PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion purge = new PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion();
    private SkipFileBuiltStatusCheck skipBuiltCheck = new SkipFileBuiltStatusCheck();
    private MainframeSynchronizeDeltaDriverWithBase sync = new MainframeSynchronizeDeltaDriverWithBase();
    private HaltOnShadowCheckWarnings haltShadCheck = new HaltOnShadowCheckWarnings();
    private TransmitOutputToUserID xmitTo = new TransmitOutputToUserID();
    private TransmitOutputType xmitType = new TransmitOutputType();
    private ShipBuiltPackageTo shipTo = new ShipBuiltPackageTo();
    private PDSSavePath destinationDataSet = new PDSSavePath();
    private ComponentListFile componentList = new ComponentListFile();
    private DirectoryListFile directoryList = new DirectoryListFile();
    private ComponentExclude excludeComponent = new ComponentExclude();
    private DirectoryExclude excludeDirectory = new DirectoryExclude();
    private AutoPurge autoPurge = new AutoPurge();//INT3107
    private BundleSource bundled = new BundleSource();
    private LibraryReqCheck libraryReqCheck = new LibraryReqCheck();

    private LogicalTarget logicalTarget = new LogicalTarget();
    private LogicalReqType logicalReqType = new LogicalReqType();
    private LogicalReq logicalReq = new LogicalReq();
    private KeyWord keyword = new KeyWord();
    private Value value = new Value();
    private AssociativeBooleanOperation    logicalReqTriple    = null;
    private AssociativeBooleanOperation    keyWordAndValuePair = null;


    public UserModBuild(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command creates usermods from the list of submitted tracks.";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver> BUILDTYPE=<buildtype>\n"+ 
        "Either  LEVEL=<stagingLevel>\n"+
        "   Or   TRACK=<trackToBuild>\n";
    }
    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libraryRelease);
        baseAnd.addBooleanInterface(driver);
        baseAnd.addBooleanInterface(buildtype); /*required during driverbuild of delta driver */
        BooleanExclusiveOr levelTrackOr = new BooleanExclusiveOr();
        levelTrackOr.addBooleanInterface(level);
        levelTrackOr.addBooleanInterface(track);
        baseAnd.addBooleanInterface(levelTrackOr);

        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(description);
        argumentStructure.addOption(bundled);
        argumentStructure.addOption(libraryReqCheck);
        argumentStructure.addOption(listGen);
        argumentStructure.addOption(runScanners);
        argumentStructure.addOption(buildCC);
        argumentStructure.addOption(autoBuild);
        argumentStructure.addOption(rebuild);
        argumentStructure.addOption(purge);
        argumentStructure.addOption(sync);
        argumentStructure.addOption(haltShadCheck);
        argumentStructure.addOption(xmitTo);
        argumentStructure.addOption(xmitType);
        argumentStructure.addOption(shipTo);
        argumentStructure.addOption(destinationDataSet);
        argumentStructure.addOption(componentList);
        argumentStructure.addOption(directoryList);
        argumentStructure.addOption(excludeComponent);
        argumentStructure.addOption(excludeDirectory);

        argumentStructure.addOption(autoPurge);//INT3107

        BooleanAnd kgroupedAnd = new BooleanAnd();
        AssociatedArgument keyWordValueGroup  = new AssociatedArgument(kgroupedAnd);
        keyWordValueGroup.addIndexedArgument(keyword);
        keyWordValueGroup.addIndexedArgument(value);
        kgroupedAnd.addBooleanInterface(keyWordValueGroup);
        keyWordAndValuePair = new AssociativeBooleanOperation(kgroupedAnd);
        keyWordAndValuePair.setIgnoreInCompleteGroup();
        argumentStructure.addOption(keyWordAndValuePair);

        BooleanAnd groupedAnd = new BooleanAnd();
        AssociatedArgument grouped = new AssociatedArgument(groupedAnd);
        grouped.addIndexedArgument(logicalTarget);
        grouped.addIndexedArgument(logicalReqType);
        grouped.addIndexedArgument(logicalReq);
        groupedAnd.addBooleanInterface(grouped);
        logicalReqTriple = new AssociativeBooleanOperation(groupedAnd);
        logicalReqTriple.setIgnoreInCompleteGroup();
        argumentStructure.addOption(logicalReqTriple);

        //Begin INT3108
        argumentStructure.addOption(skipBuiltCheck);
        //End INT3108

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        UsermodGeneralInfo build = new UsermodGeneralInfo(getLEP());
        ReleaseInformation releaseInfo = getReleaseInformation(libraryRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        String descriptionValue = "";
        if (description.getValue()!=null) {
            descriptionValue = description.getValue();
        }
        build.set_descr(descriptionValue);

        ComponentAndPathRestrictions  restrictions = new ComponentAndPathRestrictions();

        if (componentList.getValue()!=null) {
            restrictions.setIncludeComponents(!excludeComponent.getBooleanValue());

            try {
                restrictions.setComponentsPath(componentList.getValue());
                restrictions.setComponentList(FileSystem.createListFromFile(new File(componentList.getValue())));
            } catch (IOException ioe) {
                throw new com.ibm.sdwb.build390.GeneralError("Error reading the components file " + componentList.getValue());
            }
        }

        if (directoryList.getValue()!=null) {
            restrictions.setIncludePaths(!excludeDirectory.getBooleanValue());
            try {
                restrictions.setDirectoryPath(directoryList.getValue());
                restrictions.setPathList(FileSystem.createListFromFile(new File(directoryList.getValue())));
            } catch (IOException ioe) {
                throw new com.ibm.sdwb.build390.GeneralError("Error reading the directories file " + directoryList.getValue());
            }
        }

        if (level.isSatisfied()) {
            build.setChangeRequestSet(new HashSet((buildSourceInfoFromLevel(build, restrictions)).getChangeRequestCollection()));
        } else {
            build.setChangeRequestSet(new HashSet((buildSourceInfoFromTrack(build, restrictions)).getChangeRequestCollection()));
        }
        if (shipTo.isSatisfied()) {
            build.setMainframeUserAddressToSendOutputTo(shipTo.getValue());
        }
        if (destinationDataSet.isSatisfied()) {
            build.setMainframeDatasetToStoreOutputIn(destinationDataSet.getValue());
        }

        if (build.getChangeRequests().isEmpty()) {
            if (level.isSatisfied()) {
                throw new LibraryError("The level  " + level.getValue() +" is empty"); 
            } else {
                throw new LibraryError("The track  " + track.getValue() +" are not in proper state."); 
            }
        }

        build.getOptions().setListGen(listGen.getValue());
        build.getOptions().setRunScanners(runScanners.getBooleanValue());
        build.setDryRun(libraryReqCheck.getBooleanValue());
        build.setBundled(bundled.getBooleanValue());
        build.set_buildtype(buildtype.getValue());
        build.getOptions().setBuildCC(buildCC.getValueInteger());
        build.getOptions().setAutoBuild(autoBuild.getValue());
        build.getOptions().setForce(rebuild.getValue());
        build.getOptions().setPurgeJobsAfterCompletion(purge.getBooleanValue());
        build.getOptions().setSkipDriverCheck(skipBuiltCheck.getBooleanValue());
        build.getOptions().setSynchronizeDriver(sync.getBooleanValue());
        build.getOptions().setHaltOnShadowCheckWarnings(haltShadCheck.getBooleanValue());
        build.getOptions().setXmitTo(xmitTo.getValue());
        build.getOptions().setXmitType(xmitType.getValue());

        build.getOptions().setAutoPurgeSuccessfulJobs(autoPurge.getBooleanValue());//INT3107

        build.getBuildSettings().putAll(getAdditionalBuildSettingsMap());
        processReqArguments(build,restrictions);

        com.ibm.sdwb.build390.process.UsermodGeneral usermodProcess = new com.ibm.sdwb.build390.process.UsermodGeneral(build, this);
        build.setProcessForThisBuild(usermodProcess);

        setCancelableProcess(usermodProcess);

        usermodProcess.externalRun();

        if (build.isDryRun()) {
            if (level.isSatisfied()) {
                getStatusHandler().updateStatus("Library requisite satisfied for the library source " + level.getValue()+ ".",false);
            } else {
                getStatusHandler().updateStatus("Library requisite satisfied for the library source " + track.getValue()+ ".",false);
            }
        }
    }

    private SourceInfoCollection buildSourceInfoFromLevel(UsermodGeneralInfo build, ComponentAndPathRestrictions restrictions) throws MBBuildException{
        // create a level to represent the track set.
        Set projectSet = new HashSet();
        projectSet.add(build.getReleaseInformation().getLibraryName());
        getStatusHandler().updateStatus("Getting source " + level.getValue() +" information.",false);
        com.ibm.sdwb.build390.library.cmvc.CMVCLevelSourceInfo levelSource = new com.ibm.sdwb.build390.library.cmvc.CMVCLevelSourceInfo((CMVCLibraryInfo) build.getSetup().getLibraryInfo(), build.getReleaseInformation().getLibraryName(), level.getValue(), restrictions);
        if (!levelSource.isValidSource()) {
            throw new LibraryError("The level " + levelSource.getName() + " not found in library. Please enter a valid level.");
        }
        Set sourceInfoSet = new HashSet();
        Set trackSet = levelSource.getChangesetsInSource();
        for (Iterator trackIterator = trackSet.iterator(); trackIterator.hasNext();) {
            String oneTrack = (String) trackIterator.next();
            int breakPoint = oneTrack.indexOf("-");
            String justTrackName = oneTrack.substring(breakPoint+1);
            CMVCChangeRequestInfo changeRequestInfo = new CMVCChangeRequestInfo((CMVCLibraryInfo) build.getSetup().getLibraryInfo(),justTrackName,  restrictions);
            changeRequestInfo.setInterestedProjects(projectSet);
            // now we need to make sure we track what level this was selected from, so we'll prime source infos now
            Set infoSet = changeRequestInfo.getIndividualSourceInfos();
            for (Iterator changesetIterator = infoSet.iterator(); changesetIterator.hasNext();) {
                Changeset oneChangeset = (Changeset) changesetIterator.next();
                if (oneChangeset.getProject().equals(build.getReleaseInformation().getLibraryName())) {    // in case we enable multiple releases, make sure we only pull this from the release we got it from
                    oneChangeset.setChangesetGroupContainingChangeset(levelSource);
                }
            }
            sourceInfoSet.add(changeRequestInfo);
        }
        getStatusHandler().updateStatus("Getting source " + level.getValue() +" complete.",false);
        SourceInfoCollection sourceCollection = new SourceInfoCollection();
        sourceCollection.setChangeRequestCollection(sourceInfoSet);
        return sourceCollection;
    }

    private SourceInfoCollection buildSourceInfoFromTrack(UsermodGeneralInfo build, ComponentAndPathRestrictions restrictions) throws MBBuildException{


        Set projectSet = new HashSet();
        projectSet.add(build.getReleaseInformation().getLibraryName());
        Set sourceInfoSet = new HashSet();
        CMVCChangeRequestInfo changeRequestInfo = new CMVCChangeRequestInfo((CMVCLibraryInfo) build.getSetup().getLibraryInfo(), track.getValue(),  restrictions);
        if (!changeRequestInfo.isValidSource()) {
            throw new LibraryError("The track " + changeRequestInfo.getName() + " not found in library. Please enter a valid track.");
        }
        changeRequestInfo.setInterestedProjects(projectSet);

        if (changeRequestInfo.getIndividualSourceInfos()!=null && changeRequestInfo.getIndividualSourceInfos().isEmpty()) {
            throw new LibraryError("The track " + changeRequestInfo.getName() + " not found in library release " + projectSet + ". Please enter a valid release.");
        }

        sourceInfoSet.add(changeRequestInfo);
        SourceInfoCollection sourceCollection = new SourceInfoCollection();
        sourceCollection.setChangeRequestCollection(sourceInfoSet);
        return sourceCollection;
    }

    private Map<String,String> getAdditionalBuildSettingsMap() {
        Map<String,String> outputMap = new HashMap<String,String>();
        if (keyWordAndValuePair.inputAvailable()) {
            Map keyWordMap = keyWordAndValuePair.getIndexToArgumentsMap();

            TreeSet tempTree= new TreeSet(keyWordMap.keySet());

            for (Iterator Iterator0 = tempTree.iterator(); Iterator0.hasNext();) {
                String key = (String) Iterator0.next();

                Set oneArgumentSet = (Set) keyWordMap.get(key);
                String keywordSetting = null;
                String valueSetting   = null;

                for (Iterator argumentsWithinASetIterator = oneArgumentSet.iterator(); argumentsWithinASetIterator.hasNext();) {
                    CommandLineArgument oneArg = (CommandLineArgument) argumentsWithinASetIterator.next();
                    if (oneArg.getCommandLineName().equals(keyword.getCommandLineName())) {
                        keywordSetting = oneArg.getValue();
                    } else if (oneArg.getCommandLineName().equals(value.getCommandLineName())) {
                        valueSetting = oneArg.getValue();
                    }
                    if (keywordSetting!=null && valueSetting!=null) {
                        outputMap.put(keywordSetting,valueSetting);
                    }
                }

            }
        }
        return outputMap;
    }


    private  void processReqArguments(UsermodGeneralInfo build, ComponentAndPathRestrictions restrictions) throws MBBuildException  {
        if (logicalReqTriple.inputAvailable()) {//should be using inputAvailable (need to tweak a different argument class later.
            StringBuilder strbd = new StringBuilder();
            for (Iterator argIterator = logicalReqTriple.getIndexToArgumentsMap().entrySet().iterator(); argIterator.hasNext();) {

                Set tempLogicalReqArgSet = (Set)(((Map.Entry)argIterator.next()).getValue());
                String singleLogicalTarget = "";
                String singleLogicalReqType = "";
                String singleLogicalReq     = "";

                for (Iterator iter=tempLogicalReqArgSet.iterator();iter.hasNext();) {
                    CommandLineArgument oneArg = (CommandLineArgument) iter.next();

                    if (oneArg.getCommandLineName().equals(logicalTarget.getCommandLineName())) {
                        singleLogicalTarget = oneArg.getValue();
                    } else if (oneArg.getCommandLineName().equals(logicalReqType.getCommandLineName())) {
                        singleLogicalReqType = oneArg.getValue();
                    } else if (oneArg.getCommandLineName().equals(logicalReq.getCommandLineName())) {
                        singleLogicalReq = oneArg.getValue();
                        singleLogicalReq = singleLogicalReq.replace('\"',' ').trim();
                    }


                    if ((singleLogicalTarget!=null && singleLogicalTarget.trim().length() >0) &&
                        (singleLogicalReqType!=null && singleLogicalReqType.trim().length() >0) && 
                        (singleLogicalReq!=null && singleLogicalReq.trim().length() >0)) {
                        if (!handleReqs(build,restrictions,singleLogicalTarget,singleLogicalReqType, singleLogicalReq)) {
                            strbd.append(singleLogicalTarget);
                            strbd.append("\n");
                        }
                    }

                }


            }


            if (strbd.length() > 0) {
                throw new LibraryError("The following  logical targets are invalid, since they aren't part of the input library source (LEVEL/TRACK) argument.\n" +
                                       strbd.toString());
            }

        }



    }

    private boolean handleReqs(UsermodGeneralInfo build, ComponentAndPathRestrictions restrictions,String singleLogicalTarget, String singleLogicalReqType,String singleLogicalReq) {
        //verify if its there in the LEVEL/TRACK
        boolean proceed = false;
        if (singleLogicalReqType.equals(IFREQ)) {
            for (Iterator iter = build.getChangeRequests().iterator();(!proceed && iter.hasNext());) {
                ChangeRequest request  = (ChangeRequest)iter.next();
                if (request.getName().equals(singleLogicalTarget)) {
                    proceed = true;
                }
            }

            if (proceed) {
                Scanner  lineScanner = new Scanner(singleLogicalReq);
                List<String> ifReqList = new ArrayList<String>();
                while (lineScanner.hasNext()) {
                    ifReqList.add(lineScanner.next().toUpperCase());
                }
                build.setIfReqList(singleLogicalTarget,ifReqList);
            }

        }
        return proceed;

    }
}


