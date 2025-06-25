package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.mainframe.parser.*;
import com.ibm.sdwb.build390.process.AbstractProcess;
import com.ibm.sdwb.build390.process.AbstractProcess.RepeatedProcessStep;
import com.ibm.sdwb.build390.process.DriverBuildProcess;
import com.ibm.sdwb.build390.process.UserBuildProcess;
import com.ibm.sdwb.build390.process.UsermodGeneral;
import com.ibm.sdwb.build390.process.steps.*;
import com.ibm.sdwb.build390.user.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.process.*;
import com.ibm.sdwb.build390.utilities.*;
import com.ibm.sdwb.build390.utilities.process.*;


public class ProcessInfo  extends CommandLineProcess {

    private static final int AUTOBLD_INDEX = 0;
    private static final int FORCE_INDEX   = 1;
    private static final int LISTGEN_INDEX = 2;
    private static final int RUNSCAN_INDEX = 3;
    private static final int BUILDCC_INDEX = 4;
    public static final String PROCESSNAME = "PROCESSINFO";
    private ProcessID processID = new ProcessID();
    private ProcessInfoType type = new ProcessInfoType();
    private File build390HomeDirectory = new File(MBGlobals.Build390_path);
//    private List phaseList=null;
    private MBBuild build= null;
    private File locatedBuildFile=null;//TST3175

    public ProcessInfo(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ 
        " command returns information about specified build process.\n";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" PROCESSID=U6206A9A";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        argumentStructure.setRequiredPart(processID);

        argumentStructure.addOption(type);

    }

    public void runProcess() throws MBBuildException    { 

        locatedBuildFile = SerializedBuildsLister.getInstance(build390HomeDirectory).locateBuildId(processID.getValue().toUpperCase());//TST3175

        try {
            if (locatedBuildFile!=null) {
                if (locatedBuildFile.exists()) {
                    build = MBBuildLoader.loadBuild(locatedBuildFile);

                    if (build!=null) {
                        if (build.get_buildid().equals(processID.getValue().toUpperCase())) {
                            /*
                            if(!getBuildType(build).equals("Usermod")) {
                                if(type.getValue().equals("FULL")|type.getValue().equals("PHASES")) {
                                    phaseList = getPhaseInfo();
                                }
                            }
                            */

                            if (type.getValue().equals("FULL")) {
                                printBuildInfo();

                                printPhases();

                                printOptions();
                            } else
                                if (type.getValue().equals("BASIC")) {
                                printBuildInfo();
                            } else
                                if (type.getValue().equals("PHASES")) {
                                printPhases();
                            } else
                                if (type.getValue().equals("OPTIONS")) {
                                printOptions();
                            }
                        }
                    } else {
                        throw new SyntaxError("Specified build " + processID.getValue() + " not found.");
                    }
                }
            }
        } catch (MBBuildException mbe) {
            getLEP().LogException(mbe);
        }


        if (locatedBuildFile==null) {
            throw new SyntaxError("Specified build " + processID.getValue() + " not found.");
        }
    }

    private void printOptions() {

        BuildOptions options = build.getOptions();

        List list = new ArrayList();

        System.out.println("\nOPTIONS:");

        list.add("Controlled="+getOutputBoolean(options.isControlled()));

        list.add("ListGen="+options.getListGen());

        list.add("RunScan="+getOutputBoolean(options.isRunScanners()));//SDWB2325D

        list.add("PurgeJobs="+getOutputBoolean(options.isPurgeJobs()));

        list.add("BuildJobReturnCode="+options.getBuildCC());//SDWB2325D

        list.add("AutoBuild="+options.getAutoBuild());

        list.add("Force="+options.getForce());

        list.add("DryRun="+getOutputBoolean(options.isDryRun()));

        list.add("XmitTo="+options.getXmitTo());

        list.add("XmitType="+options.getXmitType());

        list.add("HaltOnShadowWarnings="+getOutputBoolean(options.isHaltOnShadowCheckWarnings()));

        list.add("SkipDCheck="+getOutputBoolean(options.isSkippingDriverCheck()));//SDWB2325D

        list.add("XmitDebugFileLocation="+options.getXmitDebugFileLocation());

        list.add("GeneratingDebugFiles="+getOutputBoolean(options.isGeneratingDebugFiles()));

        list.add("XmitDebugFiles="+getOutputBoolean(options.isXmitDebugFiles()));

        list.add("SaveDebugFiles="+getOutputBoolean(options.isSaveDebugFiles()));

        list.add("SyncDriver="+getOutputBoolean(options.isSynchronizingDriver()));//SDWB2325D

        //Begin TST3211
        if (options.getExtraDriverCheck()!=null) {
            if (options.getExtraDriverCheck().equals("YES")) {
                list.add("ExtendedCheck=FAIL");
            } else {
                list.add("ExtendedCheck="+options.getExtraDriverCheck());
            }
        }
        //End TST3211

        list.add("AutoPurge="+getOutputBoolean(options.isAutoPurgeSuccessfulJobs()));//TST3184

        if (build instanceof MBUBuild) {
            MBUBuild userbuild = (MBUBuild) build; 

            BuildOptionsLocal localOptions = (BuildOptionsLocal)userbuild.getOptions();

            String[] macs = localOptions.getUserMacs();

            if (macs !=null) {
                for (int n=0;n!=macs.length;n++) {
                    if (macs[n]!=null && macs[n].trim().length() >0) {
                        list.add("UserMac"+(n+1)+"="+macs[n]);//SDWB2325D
                    }
                }
            }

            list.add("EmbeddedMetadata="+ getOutputBoolean(localOptions.isUsingEmbeddedMetadata()));//SDWB2325D
        }

        Map<String,String>  setMap = build.getBuildSettings();
        if (setMap!=null) {
            int n = 0;
            for (Map.Entry<String,String> entry : setMap.entrySet()) {
                list.add("AdditionalBuildSettingsKeyword"+(n+1)+"=\""+entry.getKey()+"\"");
                list.add("AdditionalBuildSettingsValue"+(n+1)+"=\""+entry.getValue()+"\"");
                n++;
            }
        }

        printList(list);
    }

    private void printPhases() {
        AbstractProcess proc = build.getProcessForThisBuild();
        System.out.println("\nPHASES:");
        if (!getBuildType(build).equals("Usermod")) {

            if (build.getProcessForThisBuild().hasCompletedSuccessfully()) {
                System.out.println("Build "+build.get_buildid() +" has been run to completion, it can not be restarted");
            } else

                if (proc.getStepsThatHaveRun().get(0)!=null) {

                System.out.println("1:Beginning");

                AbstractProcess.RepeatedProcessStep step = (AbstractProcess.RepeatedProcessStep)proc.getStepsThatHaveRun().get(0);
                DriverReport driverReportStep = (DriverReport)step.getStep();
                if (driverReportStep!=null) {

                    Iterator stepIterator = proc.getStepsThatHaveRun().iterator();

                    int index=2;

                    while (stepIterator.hasNext()) {
                        AbstractProcess.RepeatedProcessStep theStep = (AbstractProcess.RepeatedProcessStep) stepIterator.next();

                        if (theStep.getStep().isVisibleToUser()) {
                            String stepName = theStep.getStep().getName();

                            System.out.println(index+++":"+stepName);//***BE
                        }

                        if (theStep.getStep() instanceof FullProcess) {
                            FullProcess fullprocess = (FullProcess)theStep.getStep();
                            if (!fullprocess.hasCompletedSuccessfully())
                                break;
                        }
                    }
                }
            }

            System.out.println("\nPHASE OVERRIDES:");
            printPhaseInfo(); 
        } else {
            System.out.println("\nUsermods contain no restartable phases.");
        }

    }

    private void printBuildInfo() {

        BuildOptions options = build.getOptions();

        List list = new ArrayList();

        list.add("\nBASIC:");

        list.add("ProcessType="+getBuildType(build));

        list.add("Build ID="+build.get_buildid());

        list.add("Library Release="+build.getReleaseInformation().getLibraryName());

        DriverInformation driverInfo = build.getDriverInformation();

        list.add("MVS Release="+driverInfo.getRelease().getMvsName());

        ReleaseInformation  releaseInfo = build.getReleaseInformation();

        list.add("HLQ="+releaseInfo.getMvsHighLevelQualifier());

        list.add("Driver Name="+build.getDriverInformation().getName());

        list.add("Build Type="+build.get_buildtype());

        list.add("Description="+build.get_descr());

        list.add("Date="+build.get_date());

        list.add("Library Server Address="+build.getLibraryInfo().getProcessServerAddress());

        list.add("MVS Server Address="+build.getSetup().getMainframeInfo().getMainframeAddress());


        //Begin TST3021
        if (build.getProcessForThisBuild().getStepsThatHaveRun().size()>0) {
            list.add("Status="+"Last step that ran => "+((com.ibm.sdwb.build390.process.AbstractProcess.RepeatedProcessStep)((java.util.LinkedList)build.getProcessForThisBuild().getStepsThatHaveRun()).getLast()).getStep().getName());
        } else {
            list.add("Status= No steps run.");
        }
        //End TST3021

        //Begin SDWB2325D
        SourceInfo sourceInfo = build.getSource();
        ChangeRequest changeRequest = null;
        
        if (build instanceof UsermodGeneralInfo) {
        	UsermodGeneralInfo usermodBuild = (UsermodGeneralInfo)build;
            
            if (!(usermodBuild.getChangeRequests().isEmpty())) {
                for (Iterator iter = usermodBuild.getChangeRequests().iterator();iter.hasNext();) {
                	changeRequest = (ChangeRequest)iter.next();
                    break;
                }
                if (!changeRequest.getIndividualSourceInfos().isEmpty()) {
                    for (Iterator iter = changeRequest.getIndividualSourceInfos().iterator();iter.hasNext();) {
                    	sourceInfo = (SourceInfo)iter.next();
                        break;
                    }                        
                }
            }
            
            list.add("Bundled="+getOutputBoolean(usermodBuild.isBundled()));
            list.add("LibraryReqCheck="+getOutputBoolean(usermodBuild.isDryRun()));
            int n=0;
            for (Iterator iter =usermodBuild.getChangeRequests().iterator();iter.hasNext();) {
                ChangeRequest request = (ChangeRequest)iter.next();
                StringBuilder strbd = new StringBuilder();
                if (usermodBuild.getIfReqList(request.getName())!=null) {
                    for (String ifREQString: usermodBuild.getIfReqList(request.getName())) {
                        strbd.append(ifREQString + " ");
                    }
                }
                strbd.trimToSize();

                //since this is saved in a IFREQList, we statically provide the reqtype as IFREQ.
                if (strbd.length() > 0) {
                    n++;
                    list.add("LibraryTarget"+n+"="+ request.getName());
                    list.add("LibraryReqType"+n+"=IFREQ");
                    list.add("LibraryReq"+n+"=\""+strbd.toString().trim()+"\"");
                }

            }
        }
        
        if (sourceInfo!=null) {

            if (!getBuildType(build).equals("Usermod")) {

                list.add("DeltaBuild="+getOutputBoolean(!sourceInfo.isIncludingCommittedBase()));
            }

            if (sourceInfo instanceof CMVCSourceInfo) {
                
                if (((CMVCTrackSourceInfo)sourceInfo).getChangesetGroupContainingChangeset() !=null) {
                    list.add("Level="+sourceInfo.getName());
                } else{
                    list.add("Track="+sourceInfo.getName());
                }

                ComponentAndPathRestrictions restrictions = ((CMVCSourceInfo) sourceInfo).getRestrictions();
                
                if (restrictions!=null) {
                    
                    List components = restrictions.getComponentList();                  

                    if (components!=null) {
                        list.add("ExcludeComponent="+getOutputBoolean(!restrictions.isComponentsIncluded()));
                        if (!components.isEmpty()) {
                            list.add("Components_Path="+restrictions.getCompPath());
                            for (int i=0;i!=components.size();i++) {
                                list.add("RestrictComponent"+(i+1)+"="+components.get(i));
                            }
                        }
                    } else{
                        list.add("ExcludeComponent="+getOutputBoolean((components!=null)));
                    }

                    List directories = restrictions.getPathList();

                    if (directories!=null) {
                        list.add("ExcludeDirectory="+getOutputBoolean(!restrictions.isPathsIncluded()));
                        if (!directories.isEmpty()) {
                            list.add("Directory_Path="+restrictions.getDirectoryPath());
                            for (int m=0;m!=directories.size();m++) {
                                list.add("RestrictDirectory"+(m+1)+"="+directories.get(m));
                            }
                        }
                    } else {
                        list.add("ExcludeDirectory="+getOutputBoolean((directories!=null)));
                    }
                }
            }
        }
        
        if (build instanceof MBUBuild) {
            MBUBuild userbuild = (MBUBuild) build;

            list.add("Fast Track="+userbuild.getFastTrack());

            String partLabel = "";
            String localParts[]=null;
            String root="";

            if (isPDS(userbuild)) {
                list.add("PDSName="+getPDSName(userbuild));

                list.add("PDSClass="+userbuild.getPDSMemberClass());

                partLabel = "PDSMember";

                localParts = userbuild.getLocalParts();

                if (localParts!=null) {
                    for (int n=1;n!=localParts.length;n++) {

                        String part = localParts[n];

                        list.add(partLabel+n+"="+part);
                    }
                }
            } else {
                partLabel = "LocalPart";

                localParts = userbuild.getLocalParts();

                if (localParts!=null) {
                    root = localParts[0];

                    list.add("RootDirectory="+root);

                    for (int n=1;n!=localParts.length;n++) {

                        String part = localParts[n];

                        if (part.startsWith(root)) {
                            if (part.length() >= root.length()) {
                                list.add(partLabel+n+"="+part.substring(root.length()));
                            }
                        }
                    }
                }
            }


            String partModelsModPart []= userbuild.getPartModels_mod_part();
            if (partModelsModPart!=null &&partModelsModPart.length>=1) {
                for (int n=1;n!=partModelsModPart.length;n++) {
                    if (partModelsModPart[n] != null) {
                        list.add("ModelName"+(n)+"="+partModelsModPart[n]);
                    }
                }
            }

            String partModelTypes[] = userbuild.getPartModelTypes();
            String partModelClassPath[] = userbuild.getPartModels_class_path();

            if (partModelTypes!=null && partModelTypes.length>=1) {

                for (int n=1;n!=partModelTypes.length;n++) {
                    String type = partModelTypes[n];

                    if (type != null) {
                        if (type.equals(UserSourceDrivenBuild.LIBRARY_TYPE)) {

                            list.add("ModelRoot"+(n)+"="+partModelClassPath[n]);
                        } else if (type.equals(UserSourceDrivenBuild.MODCLASS_TYPE)) {

                            list.add("ModelClass"+(n)+"="+partModelClassPath[n]);
                        }
                    }
                }
            }
        }
        //End SDWB2325D

        printList(list);
    }

    String getBuildType(Object obj) {
        if (obj instanceof MBUBuild) {
            return "Userbuild";
        }

        if (obj instanceof UsermodGeneralInfo) {
            return "Usermod";
        }

        if (obj instanceof MBBuild) {
            return "Driverbuild";
        }

        return "Unknown";
    }

    List getPhaseInfo() {
        String buildType = build.get_buildtype();

        DriverInformation drvInfo = build.getDriverInformation(); 

        //Start TST3175
        String buildPath = locatedBuildFile.getPath();

        int index = buildPath.lastIndexOf(locatedBuildFile.separator);

        buildPath = buildPath.substring(0,index);

        //File saveLocation = MBClient.getCacheDirectory();
        File saveLocation = new File(buildPath);
        //End TST3175

        com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep wrapper = new com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep(this);
        DriverReport driverReport = new DriverReport(drvInfo, build.getSetup().getMainframeInfo(),build.getSetup().getLibraryInfo(), saveLocation, wrapper);

        try {
            driverReport.externalExecute();
        } catch (MBBuildException mbe) {
            getLEP().LogException(mbe);
        }

        List phases = driverReport.getParser().getPhaseInforamtion(buildType);

        return phases;
    }

    void printPhaseInfo() {

        List phaseList = getPhaseInfo();

        // sample overides in BLDORDER
        //      BFG1=XXFN4=G,
        //      BFG2=XXYY8=G
        // in this order -> autobld, force, listgen, runscan, buildcc

        // but each phases overrides into this array so they can be shown in the correct order
        java.util.List messages = new ArrayList();
        // Loop on phases, pull setting for each phase and build a msg
        for (Iterator phaseIterator = phaseList.iterator(); phaseIterator.hasNext();) {
            com.ibm.sdwb.build390.mainframe.PhaseInformation onePhase = (com.ibm.sdwb.build390.mainframe.PhaseInformation) phaseIterator.next();
            String phaseNum  = Integer.toString(onePhase.getPhaseNumber());
            String phaseSet  = onePhase.getPhaseOverrides();
            String title     = "Overrides for Phase: " + phaseNum;
            if (phaseSet !=null) {
                char[] overRide = phaseSet.toCharArray();      
                // Get AUTOBLD and FORCE setting
                // AUTOBLD = Y => build all dependent parts
                // AUTOBLD = N => only build parts in the build list
                // AUTOBLD = M => build all dependent parts
                // FORCE   = A => build all parts in the driver
                // FORCE   = Y => build parts in the part list
                // FORCE   = N => build parts that are not built
                // y,y "Unconditionally build all parts in list and all dependent parts";
                // y,n "Build all unbuilt parts in list and all dependent parts";
                // m,x "Build all unbuilt parts in list and parts with explicit dependencies";
                // n,x "Build only unbuilt parts";
                // y,a "Unconditionally build everything in the driver and partlist";         
                String depPart = null;
                switch (overRide[AUTOBLD_INDEX]) {
                case 'Y': // autobuild = yes            
                    switch (overRide[FORCE_INDEX]) {
                    case 'Y':
                        depPart = "Unconditionally build all parts in list and all dependent parts";
                        break;
                    case 'N':
                        depPart = "Build all unbuilt parts in list and all dependent parts";
                        break;
                    case 'A':
                        depPart = "Unconditionally build everything in the driver and partlist";
                        break;
                    }
                    break;
                case 'N': // autobuild = no
                    depPart = "Build only unbuilt parts";
                    break;
                case 'M': // autobuild = manual
                    depPart = "Build all unbuilt parts in list and parts with explicit dependencies";
                    break;
                }
                if (depPart != null)
                    depPart = "Dependent part processing = "+depPart;

                // Get LISTGEN setting
                // "Do not save any listings"; // listgen=no
                // "Save failed listings";     // listgen=fail
                // "Save good listings";	   // listgen=yes
                // "Save all listings";		   // listgen=all
                String listgen = null;
                switch (overRide[LISTGEN_INDEX]) {
                case 'Y':
                    listgen = "Save good listings";
                    break;
                case 'N':
                    listgen = "Do not save any listings";
                    break;
                case 'F':
                    listgen = "Save failed listings";
                    break;
                case 'A':
                    listgen = "Save all listings";
                    break;
                }
                if (listgen != null)
                    listgen = "Listings = "+listgen;

                // Get RUNSCAN setting
                String runscan = null;
                switch (overRide[RUNSCAN_INDEX]) {
                case 'Y':
                    runscan = "YES";
                    break;
                case 'N':
                    runscan = "NO";
                    break;
                }
                if (runscan != null)
                    runscan = "Run scanners and checkers = "+runscan;

                // Get BUILDCC setting
                char cterm = overRide[BUILDCC_INDEX];
                String term = null;
                term = "Termination criteria = "+cterm;

                // build msg text for this phase
                String text = new String();
                if (depPart != null) text += " -"+depPart+"\n";
                if (listgen != null) text += " -"+listgen+"\n";
                if (runscan != null) text += " -"+runscan+"\n";
                if (term != null)    text += " -"+term+"\n";
                if (text.length() > 1) {
                    messages.add(title+"\n"+text+"\n\n");
                } else {
                    messages.add(title+" None\n\n");
                }
            } else {
                messages.add(title+" None\n\n");
            }
        } 

        // Show msgs to user in phase order
        if (!messages.isEmpty()) {
            String omsg = new String();
            for (Iterator messageIterator = messages.iterator(); messageIterator.hasNext();) {
                omsg += (String) messageIterator.next();
            }
            System.out.print(omsg);
        }
    }

    void printList(List list) {
        Collections.sort(list);

        for (Iterator i = list.iterator(); i.hasNext(); ) {
            System.out.println(i.next());
        }
    }

    String getOutputBoolean(boolean flag) {
        if (flag == true) {
            return "YES";
        } else {
            return "NO";
        }
    }

    String getPDSName(MBUBuild build) {
        String name ="";
        String[] parts;

        if (isPDS(build)) {
            parts = build.getLocalParts();

            if (parts!=null) {
                if (parts[0]!=null) {
                    return parts[0];
                }
            }
        }
        return "";
    } 

    boolean isPDS(MBUBuild build) {
        return build.getSourceType()==MBUBuild.PDS_SOURCE_TYPE;
    }

}






