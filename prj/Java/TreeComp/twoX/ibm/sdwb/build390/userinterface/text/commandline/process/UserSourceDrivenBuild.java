package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.BuildOptionsLocal;
import com.ibm.sdwb.build390.library.LocalSourceInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

//*************************************************************************
//09/22/2003  TST1614 userbuild command fails - out of bounds
//11/24/2003  #DEF.TST1716: USS CMD LINE USERBUILD still failing in PDS
//12/06/2003 #DEF.TST1727:  Command Line - FASTTRACK keyword not recognized
//12/06/2003 #DEF.TST1731: Command Line - NullPointerException USERBUILD with PDS
//12/06/2003 #DEF.TST1730: Command Line - FASTTRACK allows only 1 maclib
//12/15/2003 #DEF.TST1736: Command Line - FASTTRACK fix for indexout of bounds
//08/03/2007 #DEF.TST3304: USS: no BUILDID info in the output of a FastTrack build
//*************************************************************************

public class UserSourceDrivenBuild extends CommandLineProcess {

    public static final String PROCESSNAME = "USERBUILD";

    private LibraryRelease libraryRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private Description description = new Description();
    private BuildType buildType = new BuildType();
    private Fasttrack fastTrack = new Fasttrack();
    private RootDirectory rootDirectory = new RootDirectory();
    private PDSName pdsName = new PDSName();
    private LocalFilePath localFilePath  = new LocalFilePath();
    private AssociativeBooleanOperation localFilePaths  = null;
    private PDSClass pdsClass = new PDSClass();
    private PDSMember pdsMember  = new PDSMember();
    private AssociativeBooleanOperation pdsMembers  = null;

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
    private TransmitOutputToUserID xmitTo = new TransmitOutputToUserID();
    private TransmitOutputType xmitType = new TransmitOutputType();
    private EmbeddedMetadata embeddedMetadata = new EmbeddedMetadata();
    private MultipleAssociatedCommandLineArgument userMacs = new MultipleAssociatedCommandLineArgument();

    private ModelFileName modelFileName =  new ModelFileName();
    private ModelClass    modelClass    =  new ModelClass();
    private ModelRoot     modelRoot     =  new ModelRoot();
    private AssociativeBooleanOperation modelingGroup = null;

    private KeyWord keyword = new KeyWord();
    private Value value = new Value();
    private AssociativeBooleanOperation    keyWordAndValuePair = null;

    public static String LIBRARY_TYPE= "LIBRARY";
    public static String MODCLASS_TYPE= "MOD.CLASS";

    private AutoPurge autoPurge = new AutoPurge();//INT3107
    private ExtendedCheck extendedCheck = new ExtendedCheck();//INT3108


    public UserSourceDrivenBuild(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command builds a driver using local parts.";
    }

    public String getHelpExamples() {
        return "1.To build parts that reside on your workstation\n"+
        "USERBUILD LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "     *BUILDTYPE=<buildtype> DESCRIPTION=<dscr>\n"+
        "     LOCALPART1=<path>... LOCALPARTn=<path> ROOTDIRECTORY=<root>\n\n"+
        "2.To build parts that reside in a PDS\n"+
        "USERBUILD LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "     *BUILDTYPE=<buildtype> DESCRIPTION=<dscr> PDSNAME=<pds name>\n"+
        "     PDSCLASS=<part class> PDSMEMBER1=<member name>... PDSMEMBERn=<member name>\n\n"+
        "Note:  In the above example, keywords marked (*) are not supported\nin fasttrack mode.\nRefer Client command line interface guide for more examples.\n";

    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {

        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libraryRelease);
        baseAnd.addBooleanInterface(driver);
        baseAnd.addBooleanInterface(description);

        BooleanExclusiveOr workstationOrPDS = new BooleanExclusiveOr();
        BooleanAnd workstationFileInformation = new BooleanAnd();
        workstationFileInformation.addBooleanInterface(rootDirectory);
        AssociatedArgument localGroup = new AssociatedArgument(workstationFileInformation);
        localGroup.addIndexedArgument(localFilePath);
        workstationFileInformation.addBooleanInterface(localGroup);
        localFilePaths = new AssociativeBooleanOperation(workstationFileInformation);
        workstationOrPDS.addBooleanInterface(localFilePaths);

        BooleanAnd pdsFileInformation = new BooleanAnd();
        pdsFileInformation.addBooleanInterface(pdsName);
        pdsFileInformation.addBooleanInterface(pdsClass);
        AssociatedArgument pdsGroup = new AssociatedArgument(pdsFileInformation);
        pdsGroup.addIndexedArgument(pdsMember);
        pdsFileInformation.addBooleanInterface(pdsGroup);
        pdsMembers = new AssociativeBooleanOperation(pdsFileInformation);
        workstationOrPDS.addBooleanInterface(pdsMembers);
        baseAnd.addBooleanInterface(workstationOrPDS);


        BooleanExclusiveOr fastTrackOrBuildType = new BooleanExclusiveOr();
        fastTrackOrBuildType.addBooleanInterface(fastTrack);
        fastTrackOrBuildType.addBooleanInterface(buildType);
        baseAnd.addBooleanInterface(fastTrackOrBuildType);

        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(embeddedMetadata);
        userMacs.addCommandLineArgument(new UserSpecifiedMacLibConcatenation());
        argumentStructure.addOption(userMacs);

        BooleanAnd modelAnd = new BooleanAnd();
        BooleanExclusiveOr classOrPathModeling = new BooleanExclusiveOr();
        AssociatedArgument modelClassesGroup = new AssociatedArgument(classOrPathModeling);
        modelClassesGroup.addIndexedArgument(modelClass);
        modelClassesGroup.addIndexedArgument(modelRoot);
        classOrPathModeling.addBooleanInterface(modelClassesGroup);
        modelAnd.addBooleanInterface(classOrPathModeling);  

        AssociatedArgument modelPartNameGroup = new AssociatedArgument(modelAnd);
        modelPartNameGroup.addIndexedArgument(modelFileName);
        modelAnd.addBooleanInterface(modelPartNameGroup);
        modelingGroup = new AssociativeBooleanOperation(modelAnd);
        modelingGroup.setIgnoreInCompleteGroup();

        argumentStructure.addOption(modelingGroup);
        /* argumentStructure.addOption(fastTrack); TST1614 fasttrack is optional */
        argumentStructure.addOption(listGen);
        argumentStructure.addOption(runScanners);
        argumentStructure.addOption(dryRun);
        argumentStructure.addOption(buildCC);
        argumentStructure.addOption(autoBuild);
        argumentStructure.addOption(rebuild);
        argumentStructure.addOption(purge);
        argumentStructure.addOption(sync);
        argumentStructure.addOption(haltShadCheck);
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

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{
        MBUBuild build = new MBUBuild(getLEP());
        build.set_buildtype(buildType.getValue());

        ReleaseInformation releaseInfo = getReleaseInformation(libraryRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo   = getDriverInformation(driver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        build.set_descr(description.getValue());

        //Begin TST3182
        /* if (fastTrack.getBooleanValue()==false) {
             if (!buildType.isSatisfied()) {
                 throw new SyntaxError("The following problems were found with the command arguments:\n"+"BUILDTYPE must be defined if not FASTTRACK"); 
             }
         }
         //End TST3182
 */
        build.setFastTrack(BinarySettingUtilities.isTrueSetting(fastTrack.getValue()));


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
        build.getOptions().setXmitTo(xmitTo.getValue());
        build.getOptions().setXmitType(xmitType.getValue());

        build.getOptions().setAutoPurgeSuccessfulJobs(autoPurge.getBooleanValue());//INT3107

        //INT3108
        String checkValue = extendedCheck.getValue();
        if (checkValue.equals("FAIL")) {
            build.getOptions().setExtraDriverCheck("YES");
        } else {
            build.getOptions().setExtraDriverCheck(extendedCheck.getValue());
        }
        //INT3108

        build.getBuildSettings().putAll(getAdditionalBuildSettingsMap());

        setSourceParts(build);
        setModMacList(build);
        ((BuildOptionsLocal) build.getOptions()).setEmbeddedMetadata(embeddedMetadata.getBooleanValue());


        com.ibm.sdwb.build390.process.UserBuildProcess userBuildProcess;

        if (!build.getFastTrack()) {
            userBuildProcess =  new com.ibm.sdwb.build390.process.UserBuildProcess(build, this);
        } else {
            build.set_buildtype("");//fixes a null in bad logic in LoadMissingFilesOntoMVS line 50
            getStatusHandler().updateStatus("Running build " + build.get_buildid(),false); //TST3304
            userBuildProcess = new com.ibm.sdwb.build390.process.FastTrackBuildProcess(build, this);
        }


        setCancelableProcess(userBuildProcess);

        userBuildProcess.externalRun();

        if (userBuildProcess.hasCompletedSuccessfully()) {
            if (userBuildProcess.isPartsInDriverUpToDate()) {
                getStatusHandler().updateStatus("Driver " + build.getDriverInformation().getName() + " is up to date.",false);
            } else {
                getStatusHandler().updateStatus(userBuildProcess.getName() +" - [driver " + build.getDriverInformation().getName() + "] successful.",false);

            }

        } else {
            getStatusHandler().updateStatus(userBuildProcess.getName() + " - [driver " + build.getDriverInformation().getName() + "] failed.",true);
        } 

    }

    private void setSourceParts(MBUBuild build) throws com.ibm.sdwb.build390.MBBuildException{
        int totalPartNumber = -1;

        if (localFilePaths.inputAvailable() && localFilePaths.isSatisfied()) {
            totalPartNumber = localFilePaths.getIndexToArgumentsMap().keySet().size()+1; // add 1 for root path holder
            String[] fileList = new String[totalPartNumber];
            String rootPath = rootDirectory.getValue();
            if (!rootPath.endsWith(java.io.File.separator)) {
                rootPath += java.io.File.separator; /*TST1614 */
            }
            fileList[0]=rootPath;

            for (Iterator argIterator = localFilePaths.getIndexToArgumentsMap().keySet().iterator(); argIterator.hasNext();) {
                String indexString = (String) argIterator.next();
                int index = Integer.parseInt(indexString);
                String setting = ((CommandLineArgument) ((Set) localFilePaths.getIndexToArgumentsMap().get(indexString)).iterator().next()).getValue();
                fileList[index]=rootPath + setting; /*TST1614 */
            }
            build.setSourceType(MBUBuild.LOCAL_SOURCE_TYPE);
            build.setPDSMemberClass(null);
            build.setPDSMemberVersions(null);
            build.setLocalParts(fileList);

            //Begin CmdLineUpdate
            Set parts = new HashSet(Arrays.asList(fileList));

            LocalSourceInfo localSourceInfo = new LocalSourceInfo(parts, "");

            build.setSource(localSourceInfo);
            //End CmdLineUpdate

        } else {
            totalPartNumber = pdsMembers.getIndexToArgumentsMap().keySet().size()+1; // add 1 for root path holder
            build.setPDSMemberClass(pdsClass.getValue());
            String[] fileList = new String[totalPartNumber];
            fileList[0]= pdsName.getValue();
            for (Iterator argIterator = pdsMembers.getIndexToArgumentsMap().keySet().iterator(); argIterator.hasNext();) {
                String indexString = (String) argIterator.next();
                int index = Integer.parseInt(indexString);
                String setting = ((CommandLineArgument) ((Set) pdsMembers.getIndexToArgumentsMap().get(indexString)).iterator().next()).getValue();
                //#DEF.TST1731:
                fileList[index]=setting.toUpperCase();
            }
            build.setSourceType(MBUBuild.PDS_SOURCE_TYPE);
            build.setLocalParts(fileList);
            setPDSMemberVersions(build,build.getLocalParts()[0]);

            //Begin CmdLineUpdate
            Set parts = new HashSet(Arrays.asList(fileList));

            LocalSourceInfo localSourceInfo = new LocalSourceInfo(parts, "");

            build.setSource(localSourceInfo);
            //End CmdLineUpdate

        }

        // handle modeling crap : hah. hah!  certainly :)
        if (modelingGroup.inputAvailable() && (totalPartNumber !=-1)) { // yeehaw! i think finally this is fixed for sure !
            if (fastTrack.isSatisfied() && BinarySettingUtilities.isTrueSetting(fastTrack.getValue())) {
                String[] modelPartArray        = new String[totalPartNumber +1];
                String[] modelClassOrPathArray = new String[totalPartNumber +1];
                String[] modelPartTypes        = new String[totalPartNumber +1];
                for (String indexString : modelingGroup.getIndexToArgumentsMap().keySet()) {
                    int index = Integer.parseInt(indexString);
                    if (index==0) {
                        throw new com.ibm.sdwb.build390.SyntaxError("Index values for modeling parameters must begin with 0");
                    }

                    Set<CommandLineArgument> tempModelGroupSet = modelingGroup.getIndexToArgumentsMap().get(indexString);

                    if (tempModelGroupSet !=null && (index < totalPartNumber)) {
                        for (CommandLineArgument singleArgument : tempModelGroupSet) {
                            if (singleArgument.getCommandLineName().equals(modelFileName.getCommandLineName())) {
                                modelPartArray[index]= singleArgument.getValue();
                            }
                            if (singleArgument.getCommandLineName().equals(modelRoot.getCommandLineName())) {
                                modelClassOrPathArray[index]= singleArgument.getValue();
                                modelPartTypes[index] = LIBRARY_TYPE;
                            }
                            if (singleArgument.getCommandLineName().equals(modelClass.getCommandLineName())) {
                                modelClassOrPathArray[index]= singleArgument.getValue();
                                modelPartTypes[index] = MODCLASS_TYPE; 
                            }

                        }
                    }
                }

               

                build.setPartModels(modelPartArray, modelClassOrPathArray);
                build.setPartModelTypes(modelPartTypes);
            } else {
                throw new com.ibm.sdwb.build390.SyntaxError(fastTrack.getReasonNotSatisfied());
            }
        }
    }


    private void setModMacList(MBUBuild build) {
        if (!userMacs.getIndexToArgumentsMap().keySet().isEmpty()) {
            String[] modMacArray = new String[userMacs.getIndexToArgumentsMap().keySet().size()]; /** TST1730 */
            for (Iterator argIterator = userMacs.getIndexToArgumentsMap().keySet().iterator(); argIterator.hasNext();) {
                String indexString = (String) argIterator.next();
                int index = Integer.parseInt(indexString);
                modMacArray[index-1]=((CommandLineArgument) ((Set) userMacs.getIndexToArgumentsMap().get(indexString)).iterator().next()).getValue(); /** TST1730 **/
            }
            ((BuildOptionsLocal) build.getOptions()).setUserMacs(modMacArray);
        }
    }

    private void setPDSMemberVersions(MBUBuild build,String pdsName) throws com.ibm.sdwb.build390.MBBuildException {
        com.ibm.sdwb.build390.process.ListPDSMembers pdsMemberLister = new com.ibm.sdwb.build390.process.ListPDSMembers(build,pdsName,this);

        setCancelableProcess(pdsMemberLister);

        pdsMemberLister.externalRun();

        if (pdsMemberLister.getPDSMemberList()==null) {
            throw new SyntaxError("PDS " + pdsName+ " is empty." + MBConstants.NEWLINE + "Please choose a different PDS.");
        } else {
            // parse the ssi number from each member name
            List noSSImem   = new ArrayList();
            List notFound   = new ArrayList();

            String tempVersion[] = new String[pdsMemberLister.getPDSMemberList().size()+1];

            for (int i=1;i <build.getLocalParts().length;i++) {
                String tempLocalPart = build.getLocalParts()[i];
                for (Iterator  membersIterator = pdsMemberLister.getPDSMemberList().iterator(); membersIterator.hasNext();) {
                    String[] sthismem = ((String)membersIterator.next()).split("\\s+");
                    if (sthismem[0].startsWith(tempLocalPart)) {
                        if (sthismem.length >=2) {
                            tempVersion[i] = sthismem[1].trim();
                        } else {
                            noSSImem.add(sthismem[0]);
                        }
                    }
                }

                if (tempVersion[i]==null && !noSSImem.contains(tempLocalPart)) {
                    notFound.add(tempLocalPart);
                }

            }

            // show user members that have no SSI
            StringBuffer msg = new StringBuffer();
            if (!noSSImem.isEmpty()) {
                msg.append(MBConstants.NEWLINE+"A version number cannot be associated with the following members,\n");
                for (Iterator iter=noSSImem.iterator();iter.hasNext();) {
                    msg.append((String)iter.next());
                }
                msg.append(MBConstants.NEWLINE+"Resave the above members in ISPF with STATS set to ON.\n");
            }

            if (!notFound.isEmpty()) {
                msg.append(MBConstants.NEWLINE+"The following members does not exists in PDS " + pdsName +"\n");
                for (Iterator iter=notFound.iterator();iter.hasNext();) {
                    msg.append((String)iter.next()+"\n");
                }
            }

            if (msg.length() >0) {
                throw new SyntaxError(msg.toString());
            }


            build.setPDSMemberVersions(tempVersion);

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

