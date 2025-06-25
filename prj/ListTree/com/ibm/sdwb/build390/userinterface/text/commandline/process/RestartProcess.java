package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.*;

public class RestartProcess extends CommandLineProcess {

    public static final String PROCESSNAME = "RESTARTPROCESS";
    private ProcessID processID = new ProcessID();
    private RestartPhase restartPhase = new RestartPhase();

    private AutomaticDependencyChecking autoBuild = new AutomaticDependencyChecking();
    private ForceRebuild rebuild = new ForceRebuild();
    private MainframeReturnCode buildCC = new MainframeReturnCode();
    private SkipFileBuiltStatusCheck skipBuiltCheck = new SkipFileBuiltStatusCheck();

    private DeleteAdditionalSettings deleteAddSet = new DeleteAdditionalSettings();

    private RootDirectory rootDirectory = new RootDirectory();
    private PDSName pdsName = new PDSName();
    private LocalFilePath localFilePath  = new LocalFilePath();
    private AssociativeBooleanOperation localFilePaths  = null;
    private PDSClass pdsClass = new PDSClass();
    private PDSMember pdsMember  = new PDSMember();
    private AssociativeBooleanOperation pdsMembers  = null;

    private ModelFileName modelFileName =  new ModelFileName();
    private ModelClass    modelClass    =  new ModelClass();
    private ModelRoot     modelRoot     =  new ModelRoot();
    private AssociativeBooleanOperation modelingGroup = null;

    private KeyWord keyword = new KeyWord();
    private Value value = new Value();
    private AssociativeBooleanOperation    keyWordAndValuePair = null;

    private boolean isFastTrack=false;
    //End TST2907
    private ExtendedCheck extendedCheck = new ExtendedCheck();//INT3108


    public RestartProcess(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ 
        " command restarts a process that has not run to completion.\n";
    }

    public String getHelpExamples() {
        return "1.To restart a processs from index 5.\n"+
        getProcessTypeHandled()+" PROCESSID=U6206AEB RESTARTPHASE=5\n" +
        "2.Find the phase index for a processid, and restart.\n"+
        "  a.First list the corresponding phases, before a restart is attempted\n"+
        "    PROCESSINFO PROCESSID=D5375B6C INFO=PHASES\n"+
        "    Starting ...\n"+
        "    Processing command PROCESSINFO...\n"+
        "     To Cancel enter the string \"CANCEL\" and hit enter\n"+
        "     PHASES:\n"+
        "     1:Beginning\n"+
        "     2:Library Partlist Generation\n"+
        "     3:Shadow Check File Generation\n"+
        "     4:Upload loadorder to MVS\n"+
        "  b.To restart from \"Library Partlist Generation step\"\n"+
        "  RESTARTPROCESS PROCESSID=D5375B6C RESTARTPHASE=2\n";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();

        baseAnd.addBooleanInterface(processID);

        baseAnd.addBooleanInterface(restartPhase);

        argumentStructure.setRequiredPart(baseAnd);


        //Begin TST2907
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
        argumentStructure.addOption(workstationOrPDS);

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
        //End TST2907

        argumentStructure.addOption(autoBuild);

        argumentStructure.addOption(rebuild);


        argumentStructure.addOption(buildCC);

        BooleanAnd kgroupedAnd = new BooleanAnd();
        AssociatedArgument keyWordValueGroup  = new AssociatedArgument(kgroupedAnd);
        keyWordValueGroup.addIndexedArgument(keyword);
        keyWordValueGroup.addIndexedArgument(value);
        kgroupedAnd.addBooleanInterface(keyWordValueGroup);
        keyWordAndValuePair = new AssociativeBooleanOperation(kgroupedAnd);
        keyWordAndValuePair.setIgnoreInCompleteGroup();
        argumentStructure.addOption(keyWordAndValuePair);

        argumentStructure.addOption(deleteAddSet);
        //Begin INT3108
        BooleanExclusiveOr checkGroup = new BooleanExclusiveOr();
        checkGroup.addBooleanInterface(skipBuiltCheck);
        checkGroup.addBooleanInterface(extendedCheck);
        argumentStructure.addOption(checkGroup);
        //End INT3108


    }

    public void runProcess() throws MBBuildException    { 

        File locatedBuildFile = SerializedBuildsLister.getInstance(new File(MBGlobals.Build390_path)).locateBuildId(processID.getValue().toUpperCase());

        if (locatedBuildFile!=null) {
            if (locatedBuildFile.exists()) {
                MBBuild build = MBBuildLoader.loadBuild(locatedBuildFile);

                if (build instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
                    throw new GeneralError("Usermod builds can not be restarted");
                } else if (build!=null) {
                    if (build.get_buildid().equals(processID.getValue().toUpperCase())) {
                        updateOptions(build);
                        restart(build, restartPhase.getIntValue());
                    }
                } else {
                    throw new SyntaxError("Specified build " + processID.getValue() + " not found.");
                }
            }

        } else {
            throw new SyntaxError("Specified build " + processID.getValue() + " not found.");
        }



    }

    //Begin TST2907
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
            HashSet parts = new HashSet(Arrays.asList(fileList));

            LocalSourceInfo localSourceInfo = new LocalSourceInfo(parts, "");

            build.setSource(localSourceInfo);
            //End CmdLineUpdate

        } else {
            if (pdsMembers.inputAvailable() && pdsMembers.isSatisfied()) {
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
                HashSet parts = new HashSet(Arrays.asList(fileList));

                LocalSourceInfo localSourceInfo = new LocalSourceInfo(parts, "");

                build.setSource(localSourceInfo);
                //End CmdLineUpdate
            }
        }

        // handle modeling crap
        if (modelingGroup.inputAvailable()) {
            if (isFastTrack) {
                if (totalPartNumber!=-1) {//TST3330 actually the totalPartNumber should take care of it.
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
                                    modelPartTypes[index] = UserSourceDrivenBuild.LIBRARY_TYPE;
                                }
                                if (singleArgument.getCommandLineName().equals(modelClass.getCommandLineName())) {
                                    modelClassOrPathArray[index]= singleArgument.getValue();
                                    modelPartTypes[index] = UserSourceDrivenBuild.MODCLASS_TYPE; 
                                }

                            }
                        }
                    }
                    build.setPartModels(modelPartArray, modelClassOrPathArray);
                    build.setPartModelTypes(modelPartTypes);
                    //Begin TST3330
                } else {
                    throw new com.ibm.sdwb.build390.SyntaxError("Part must be specified for modeling");
                }
                //End TST3330
            } else {
                throw new com.ibm.sdwb.build390.SyntaxError("Cannot use modeling for non FastTrack build");
            }
        } else {
            build.setPartModels(new String[0], new String[0]);
            build.setPartModelTypes(new String[0]);
        }
    }
    //End TST2907


    void updateOptions(MBBuild build)throws MBBuildException  {

        //Begin TST2907
        if (build instanceof MBUBuild) {
            MBUBuild ubuild = (MBUBuild) build;

            isFastTrack = ubuild.getFastTrack();

            setSourceParts(ubuild);
        }
        //End TST2907

        if (buildCC.inputAvailable() && buildCC.isSatisfied()) {  //ok for fasttrack.
            build.getOptions().setBuildCC(buildCC.getValueInteger());
        }

        if (!isFastTrack) {
            if (rebuild.inputAvailable() && rebuild.isSatisfied()) { //not needed.- for fasttrack.
                build.getOptions().setForce(rebuild.getValue());
            }

            if (autoBuild.inputAvailable() &&  autoBuild.isSatisfied()) { //not needed. - for fasttrack
                build.getOptions().setAutoBuild(autoBuild.getValue());
            }



            if (skipBuiltCheck.inputAvailable() &&  skipBuiltCheck.isSatisfied()) {
                if (skipBuiltCheck.getValue().equals("YES")) {
                    build.getOptions().setSkipDriverCheck(true);
                } else {
                    build.getOptions().setSkipDriverCheck(false);
                }
            }

            //Begin INT3108
            String checkValue = extendedCheck.getValue();
            if (checkValue.equals("FAIL")) {
                build.getOptions().setExtraDriverCheck("YES");
            } else {
                build.getOptions().setExtraDriverCheck(extendedCheck.getValue());
            }
            //End INT3108


            //Begin additional build settings

            Map<String,String> keyWordValueMap;

            if (deleteAddSet.getValue().equals("YES")) {
                build.getBuildSettings().clear();
            }

            keyWordValueMap = build.getBuildSettings();

            if (keyWordValueMap == null) {
                keyWordValueMap = new HashMap<String,String>();
            }

            keyWordValueMap.putAll(getAdditionalBuildSettingsMap());    
            build.getBuildSettings().putAll(keyWordValueMap);
        }

    }

    void restart(MBBuild build, int phaseToStart) throws MBBuildException
    {
        int phasePair[] = getPhasePairFromIndex(build,phaseToStart);

        if (build.getProcessForThisBuild().hasCompletedSuccessfully()) {
            throw new GeneralError("Build "+build.get_buildid() +" has been run to completion, it can not be restarted");
        } else if (build instanceof MBUBuild) {
            MBUBuild userBuild = (MBUBuild) build;

            UserBuildProcess userBuildProcess = (UserBuildProcess) userBuild.getProcessForThisBuild();
            getLEP().addEventListener(userBuild.getLogListener());

            if (!userBuild.getFastTrack() && !userBuildProcess.getStepsThatHaveRun().isEmpty()) {
                userBuildProcess.prepareRestart(phasePair[0],phasePair[1],this);
            } else if (userBuild.getFastTrack()) {
                System.out.println("Restarting fasttrack build from the start.");
                userBuild.getOptions().setForce("ALL");
                userBuildProcess.prepareRestart(0,0,this);
            }

            setCancelableProcess(userBuildProcess);

            userBuildProcess.externalRun();
        } else if (build instanceof MBBuild) {
            MBBuild driverBuild = (MBBuild) build;

            DriverBuildProcess driverBuildProcess = (DriverBuildProcess) driverBuild.getProcessForThisBuild();
            getLEP().addEventListener(driverBuild.getLogListener());

            if (!driverBuildProcess.getStepsThatHaveRun().isEmpty()) {
                driverBuildProcess.prepareRestart(phasePair[0],phasePair[1],this);
            }

            setCancelableProcess(driverBuildProcess);

            driverBuildProcess.externalRun();
        } else {
            System.out.println("This process is not restartable");

            MBClient.exitApplication(MBConstants.GENERALERROR);
        }
    }

    private int[] getPhasePairFromIndex(MBBuild build, int phaseIndex) throws SyntaxError {
        int phasePair[] = {0,0};
        int index=2;

        AbstractProcess proc = build.getProcessForThisBuild();

        if (proc.getStepsThatHaveRun().get(0)!=null) {
            AbstractProcess.RepeatedProcessStep step = (AbstractProcess.RepeatedProcessStep)proc.getStepsThatHaveRun().get(0);
            com.ibm.sdwb.build390.process.steps.DriverReport driverReportStep = (com.ibm.sdwb.build390.process.steps.DriverReport)step.getStep();

            if (phaseIndex == 1) {
                return phasePair;
            } else if (driverReportStep!=null) {
                for (Iterator stepIterator = proc.getStepsThatHaveRun().iterator();stepIterator.hasNext();) {
                    AbstractProcess.RepeatedProcessStep theStep = (AbstractProcess.RepeatedProcessStep) stepIterator.next();
                    if (theStep.getStep().isVisibleToUser()) {
                        if (phaseIndex == index) {
                            phasePair[0]= theStep.getStepNumber();
                            phasePair[1]= theStep.getRepeptition();
                            return phasePair;
                        }
                        index++;
                    }
                }
            }
        }
        throw new SyntaxError("Phase " + phaseIndex + " that was specified for process " + processID.getValue()+" was out of range.  The phase range must be between 1 and "+(index-1)+".");
    }

    private void setPDSMemberVersions(MBUBuild build,String pdsName) throws com.ibm.sdwb.build390.MBBuildException {
        com.ibm.sdwb.build390.process.ListPDSMembers pdsMemberLister = new com.ibm.sdwb.build390.process.ListPDSMembers(build,pdsName,this);

        setCancelableProcess(pdsMemberLister);

        pdsMemberLister.externalRun();
        if (pdsMemberLister.getPDSMemberList()==null) {
            throw new SyntaxError("PDS " + pdsName+ " is empty." + MBConstants.NEWLINE + "Please choose a different PDS.");
        } else {
            List  noSSImem = new ArrayList();
            String tempVersion[] = new String[pdsMemberLister.getPDSMemberList().size()+1];

            for (int i=1;i<build.getLocalParts().length;i++) {
                boolean isFound=false;

                for (Iterator  membersIterator = pdsMemberLister.getPDSMemberList().iterator(); membersIterator.hasNext();) {
                    // if the member name contains an SSI number, parse it out and use the member
                    String thismem = (String)membersIterator.next();

                    if (thismem.startsWith(build.getLocalParts()[i]+" ")) {
                        isFound=true;

                        if (thismem.indexOf(" ")==-1) {
                            // these members have no SSI, show them in a message
                            noSSImem.add(thismem);
                        } else {
                            tempVersion[i] = thismem.substring(thismem.indexOf(" ")+1);
                        }
                    }
                }

                if (!isFound) {
                    throw new SyntaxError("PDS member "+build.getLocalParts()[i]+" not found");
                }
                //End #DEF.TST1731:
                //i++;
                //End #DEF.TST1716:
            }

            // show user members that have no SSI
            if (!noSSImem.isEmpty()) {
                String msg = "A version number cannot be associated with the following members, ";
                for (Iterator  noSSIsIterator = noSSImem.iterator();noSSIsIterator.hasNext();) {
                    // if the member name contains an SSI number, parse it out and use the member
                    msg = msg+(String)noSSIsIterator.next()+" ";
                }
                msg=msg+"."+MBConstants.NEWLINE+"Resave these members in ISPF with STATS set to ON.";
                throw new SyntaxError("Warning:"  +  msg);
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
