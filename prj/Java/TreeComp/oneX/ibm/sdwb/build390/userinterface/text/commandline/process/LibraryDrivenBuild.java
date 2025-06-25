package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.library.cmvc.CMVCLevelSourceInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCTrackSourceInfo;
import com.ibm.sdwb.build390.library.cmvc.ComponentAndPathRestrictions;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.DriverInformation;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.BooleanAnd;
import com.ibm.sdwb.build390.utilities.BooleanExclusiveOr;
import com.ibm.sdwb.build390.utilities.FileSystem;


//******************************************************************
//11/10/2003 #DEF.INT1084: cmd line KEYWORD VALUE pairs don't work
//******************************************************************

public class LibraryDrivenBuild extends CommandLineProcess {

    public static final String PROCESSNAME = "DRIVERBUILD";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver mainframeDriver = new MainframeDriver();
    private BuildType buildtype = new BuildType();
    private Description description = new Description();
    private LibraryLevel level = new LibraryLevel();
    private LibraryTrack track = new LibraryTrack();
    private ComponentListFile componentList = new ComponentListFile();
    private DirectoryListFile directoryList = new DirectoryListFile();
    private ComponentExclude excludeComponent = new ComponentExclude();
    private DirectoryExclude excludeDirectory = new DirectoryExclude();
    private ListingsGenerate listGen = new ListingsGenerate();
    private MainframeOutputScannersExecute runScanners = new MainframeOutputScannersExecute();
    private DryRun dryRun = new DryRun();
    private MainframeReturnCode buildCC = new MainframeReturnCode();
    private AutomaticDependencyChecking autoBuild = new AutomaticDependencyChecking();
    private ForceRebuild rebuild = new ForceRebuild();
    private PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion purge = new PurgeJobOutputAfterSuccessfulMainframeBuildPhaseCompletion();
    private SkipFileBuiltStatusCheck skipBuiltCheck = new SkipFileBuiltStatusCheck();
    private MainframeSynchronizeDeltaDriverWithBase sync = new MainframeSynchronizeDeltaDriverWithBase();
    private HaltOnShadowCheckWarnings haltShadCheck = new HaltOnShadowCheckWarnings();
    private DeltaBuild deltaBuild = new DeltaBuild();
    private TransmitOutputToUserID xmitTo = new TransmitOutputToUserID();
    private TransmitOutputType xmitType = new TransmitOutputType();

    private MBBuild build = null;

    private KeyWord keyword = new KeyWord();
    private Value value = new Value();
    private AssociativeBooleanOperation    keyWordAndValuePair = null;

    private AutoPurge autoPurge = new AutoPurge();//INT3107                                                 
    private ExtendedCheck extendedCheck = new ExtendedCheck();//INT3108

    public LibraryDrivenBuild(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return "The "+getProcessTypeHandled() + " command builds a driver.";
    }

    public String getHelpExamples() {
        return "1.To perform "+getProcessTypeHandled()+" on a level.\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "       BUILDTYPE=<buildtype> LEVEL=<level>\n\n"+
        "2.To perform "+getProcessTypeHandled()+" on a track.\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "       BUILDTYPE=<buildtype> TRACK=<track>\n\n"+
        "3.To perform "+getProcessTypeHandled()+" on a delta driver.\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "       BUILDTYPE=<buildtype> LEVEL=<level>\n"+
        "       DELTABUILD=YES\n";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd basicAnd = new BooleanAnd();
        argumentStructure.setRequiredPart(basicAnd);
        basicAnd.addBooleanInterface(libRelease);
        basicAnd.addBooleanInterface(mainframeDriver);
        basicAnd.addBooleanInterface(buildtype);
        BooleanExclusiveOr levelOrTrack = new BooleanExclusiveOr();
        levelOrTrack.addBooleanInterface(level);
        levelOrTrack.addBooleanInterface(track);
        basicAnd.addBooleanInterface(levelOrTrack);


        argumentStructure.addOption(description);
        argumentStructure.addOption(componentList);
        argumentStructure.addOption(directoryList);
        argumentStructure.addOption(excludeComponent);
        argumentStructure.addOption(excludeDirectory);
        argumentStructure.addOption(listGen);
        argumentStructure.addOption(runScanners);
        argumentStructure.addOption(dryRun);
        argumentStructure.addOption(buildCC);
        argumentStructure.addOption(autoBuild);
        argumentStructure.addOption(rebuild);
        argumentStructure.addOption(purge);
        argumentStructure.addOption(sync);
        argumentStructure.addOption(haltShadCheck);
        argumentStructure.addOption(deltaBuild);
        argumentStructure.addOption(xmitTo);
        argumentStructure.addOption(xmitType);
        argumentStructure.addOption(autoPurge);//INT3107


        BooleanAnd kgroupedAnd = new BooleanAnd();
        AssociatedArgument keyWordValueGroup  = new AssociatedArgument(kgroupedAnd);
        keyWordValueGroup.addIndexedArgument(keyword);
        keyWordValueGroup.addIndexedArgument(value);
        kgroupedAnd.addBooleanInterface(keyWordValueGroup);
        keyWordAndValuePair = new AssociativeBooleanOperation(kgroupedAnd);
        keyWordAndValuePair.setIgnoreInCompleteGroup();
        argumentStructure.addOption(keyWordAndValuePair);

        //Begin INT3108
        BooleanExclusiveOr checkGroup = new BooleanExclusiveOr();
        checkGroup.addBooleanInterface(skipBuiltCheck);
        checkGroup.addBooleanInterface(extendedCheck);
        argumentStructure.addOption(checkGroup);
        //End INT3108

    }

    public void runProcess()throws com.ibm.sdwb.build390.MBBuildException{

        build = new MBBuild("D",MBConstants.DRIVERBUILDDIRECTORY,getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo = getDriverInformation(mainframeDriver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        build.set_buildtype(buildtype.getValue());
        String descriptionValue = "";
        if (description.getValue()!=null) {
            descriptionValue = description.getValue();
        }
        build.set_descr(descriptionValue);

        ComponentAndPathRestrictions  tempRestrictions = new ComponentAndPathRestrictions();

        if (componentList.getValue()!=null) {
            tempRestrictions.setIncludeComponents(!excludeComponent.getBooleanValue());

            try {
            	tempRestrictions.setComponentsPath(componentList.getValue());
                tempRestrictions.setComponentList(FileSystem.createListFromFile(new File(componentList.getValue())));
            } catch (IOException ioe) {
                throw new com.ibm.sdwb.build390.GeneralError("Error reading the components file " + componentList.getValue());
            }
        }

        if (directoryList.getValue()!=null) {
            tempRestrictions.setIncludePaths(!excludeDirectory.getBooleanValue());
            try {
            	tempRestrictions.setDirectoryPath(directoryList.getValue());
                tempRestrictions.setPathList(FileSystem.createListFromFile(new File(directoryList.getValue())));
            } catch (IOException ioe) {
                throw new com.ibm.sdwb.build390.GeneralError("Error reading the directories file " + directoryList.getValue());
            }
        }

        if (track != null && track.getValue()!=null) {
            CMVCTrackSourceInfo trackInfo = new CMVCTrackSourceInfo((CMVCLibraryInfo)build.getLibraryInfo(), libRelease.getValue(), track.getValue(),  tempRestrictions);
            trackInfo.setIncludingCommittedBase(!deltaBuild.getBooleanValue());
            build.setSource(trackInfo);
        } else if (level != null) {
            CMVCLevelSourceInfo levelInfo = new CMVCLevelSourceInfo((CMVCLibraryInfo)build.getLibraryInfo(), libRelease.getValue(),  level.getValue(),  tempRestrictions);
            levelInfo.setIncludingCommittedBase(!deltaBuild.getBooleanValue());
            build.setSource(levelInfo);
        }

        if (!build.getSource().isValidSource()) {
            throw new SyntaxError(build.getSource().getSourceIdentifyingStringForMVS() + " not found in release " + libRelease.getValue() + ".");
        }

        build.getOptions().setListGen(listGen.getValue());
        build.getOptions().setRunScanners(runScanners.getBooleanValue());
        build.getOptions().setDryRun(dryRun.getBooleanValue());
        build.getOptions().setBuildCC(buildCC.getValueInteger());
        build.getOptions().setAutoBuild(autoBuild.getValue());
        build.getOptions().setForce(rebuild.getValue());
        build.getOptions().setPurgeJobsAfterCompletion(purge.getBooleanValue());
        build.getOptions().setSkipDriverCheck(skipBuiltCheck.getBooleanValue());
        build.getOptions().setSynchronizeDriver(sync.getBooleanValue());
        build.getOptions().setHaltOnShadowCheckWarnings(haltShadCheck.getBooleanValue());

        build.getSource().setIncludingCommittedBase(!deltaBuild.getBooleanValue());

        build.getOptions().setXmitTo(xmitTo.getValue());
        build.getOptions().setXmitType(xmitType.getValue());
        build.getOptions().setAutoPurgeSuccessfulJobs(autoPurge.getBooleanValue());//INT3107

        //Begin INT3108
        String checkValue = extendedCheck.getValue();
        if (checkValue.equals("FAIL")) {
            build.getOptions().setExtraDriverCheck("YES");
        } else {
            build.getOptions().setExtraDriverCheck(extendedCheck.getValue());
        }

        //End INT3108
        build.getBuildSettings().putAll(getAdditionalBuildSettingsMap());
        //Begin #DEF.INT1084:

        com.ibm.sdwb.build390.MBClient.lep.LogSecondaryInfo("Debug","Command line =>"+getProcessTypeHandled() + "\n"+ build);
        com.ibm.sdwb.build390.process.DriverBuildProcess libraryDrivenBuild = new com.ibm.sdwb.build390.process.DriverBuildProcess(build,this);

        setCancelableProcess(libraryDrivenBuild);

        libraryDrivenBuild.externalRun();
        if (libraryDrivenBuild.hasCompletedSuccessfully()) {
            if (libraryDrivenBuild.isPartsInDriverUpToDate()) {
                getStatusHandler().updateStatus("Driver " + build.getDriverInformation().getName() + " is up to date.",false);
            } else {
                getStatusHandler().updateStatus(libraryDrivenBuild.getName() + " - [driver " + build.getDriverInformation().getName() + "] successful.",false);

            }

        } else {
            getStatusHandler().updateStatus(libraryDrivenBuild.getName() + " - [driver " + build.getDriverInformation().getName() + "] failed.",true);
        }
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
}
