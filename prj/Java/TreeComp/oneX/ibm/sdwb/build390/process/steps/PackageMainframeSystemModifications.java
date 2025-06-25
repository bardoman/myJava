package com.ibm.sdwb.build390.process.steps;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.ChangesetGroupInfo;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;

//*****************************************************************************
//07/07/2003 #DEF.TST1288: ++USERMOD failure due to extra comma in SMODBLD call
//*****************************************************************************

public class PackageMainframeSystemModifications extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    public static final int APAR = 1;
    public static final int USERMOD = 3;
    private static final String ERRORLINE = "*ERROR*";

    private MBBuild build = null;
    private String logicToUse = null;
    private String comments = null;
    private String holdInformation = null;
    private String xmitInformation = null;
    private String smodName = null;
    private String fmid = null;
    private String saveDataSetName = null; // only used in usermods I think
    private int packageType = 0;
    private boolean useManualSMPLogic = false;
    private String rebuildSetting = null;
    private transient File packageContentsOrder = null;
    private boolean returnOutputDataAsPrintData = true;
    private boolean isElementDeletes = false;


    public PackageMainframeSystemModifications(MBBuild tempBuild, String tempSMODName, int tempType, String tempRebuild, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath()+"SMODBLD", "PackageMainframeSysteModifications", tempProc);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        build = tempBuild;
        rebuildSetting = tempRebuild;
        packageType = tempType;
        smodName = tempSMODName;
    }

    public void setLogicToUse(String tempLogic) {
        logicToUse = tempLogic;
    }

    public void setCommentsToUse(String tempComments) {
        comments = tempComments;
    }

    public void setFMID(String tempFMID) {
        fmid = tempFMID;
    }

    public void setHoldInformation(String tempHold) {
        holdInformation = tempHold;
    }

    public void setXMITInformation(String tempXmit) {
        xmitInformation = tempXmit;
    }


    public void setUseManualSMPLogic(boolean tempUseManual) {
        useManualSMPLogic = tempUseManual;
    }

    public void setSaveDatasetName(String tempDataset) {
        saveDataSetName = tempDataset;
    }

    public void setPackageContentsOrder(File tempOrder) {
        packageContentsOrder = tempOrder;
    }

    public void doNotReturnOutputDataAsPrintData() {
        returnOutputDataAsPrintData = false;
    }

    public void setElementDeletesGeneration(boolean isElementDeletes) {
        this.isElementDeletes =  isElementDeletes;
    }

    public String getFMID() {
        return fmid;
    }

    public String getSMODName() {
        return smodName;
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        setOutputHeaderLocation(getOutputHeaderLocation() + "-BUILD-" + (smodName!=null ? smodName : "SMOD") + "-"+ (fmid!=null ? fmid : "FMID" )); 
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        String dsName = build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+"."+build.getDriverInformation().getName()+"."+"ORDERS";

        if (packageContentsOrder!=null) {
            String MVSOrdersFile = dsName+"("+build.get_buildid()+")";
            MBFtp mftp = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());
            if (!mftp.put(packageContentsOrder, MVSOrdersFile)) {
                throw new FtpError("Could not upload "+packageContentsOrder.getAbsolutePath()+" to "+MVSOrdersFile);
            }
        }

        String commentString = handleComments();
        String packageCommand = "SMODBLD  CMVCREL=\'"+build.getReleaseInformation().getLibraryName()+"\', DRIVER=\'"+build.getDriverInformation().getName()+"\', BUILDID="+build.get_buildid()+", "+build.getLibraryInfo().getDescriptiveStringForMVS()+
                                ", OP=BUILD"+commentString;
        packageCommand+=", REBUILD="+rebuildSetting;

        if (holdInformation!=null) {
            if (holdInformation.trim().length() > 0) {
                packageCommand +=  holdInformation + ", HOLD=YES";
            }
        }
        if (fmid!=null) {
            packageCommand+=", FUNCTION="+fmid;
        }
        //The XMITOBJ is actually transmitting the built object to a particular NodeID
        if (xmitInformation != null) {
            if (xmitInformation.trim().length() >0) {
                packageCommand += ", XMITOBJ=\'"+xmitInformation+"\', OBJTYPE=SMOD";
            }
        }

        if (build.getOptions().isGeneratingDebugFiles()) {
            if (build.getOptions().isSaveDebugFiles()) {
                packageCommand += ", LOGIC=DEBUGSAVE";
            } else {
                packageCommand += ", LOGIC=DEBUG";
            }

            if (build.getOptions().isXmitDebugFiles()) {
                packageCommand += ", XMITDBUG="+build.getOptions().getXmitDebugFileLocation();
            }
        }

        if (isElementDeletes) {
            packageCommand += ", EDELETE=YES";
        }

        for (Map.Entry<String,String> entry : build.getBuildSettings().entrySet()) {
            String oneKeyword = entry.getKey();
            if (oneKeyword.length() > 0) {
                packageCommand += ", "+oneKeyword+"=";
                String oneValue = entry.getValue().trim();
                // add the value
                if (!(oneValue.startsWith("'") & oneValue.endsWith("'"))) {
                    oneValue = oneValue.toUpperCase();
                }
                packageCommand += oneValue;
            }
        }

        if (saveDataSetName != null) {
            if (saveDataSetName.length() > 0) {
                packageCommand += ", SAVEDSN="+saveDataSetName;
            }
        }

        //Include  the logics the in command we are building if a specific logic we need to use
        //or else the default logic from the pdt would be used.
        String logicSetting = handleLogicSettings();
        if (logicSetting!=null) {
            if (logicSetting.length() > 0) {
                packageCommand += ", "+ logicSetting;
            }
        }
        //if the buildUsermod then make use of TYPE=USERMOD and not TYPE=APAR
        String typeOfPackageToBuild = null;
        if (packageType == APAR) {
            packageCommand +=", TYPE=APAR";
            typeOfPackageToBuild = "APAR";
        } else if (packageType == USERMOD) {
            packageCommand +=", TYPE=USERMOD";
            if (smodName!=null) {
                packageCommand += ", SYSMOD="+smodName;
            }
            typeOfPackageToBuild = "USERMOD";
        } else {
            throw new GeneralError("Packaging attempted without specifying type of package to build");
        }


        if (useManualSMPLogic) {
            packageCommand += ", SMPLOGIC=INPUT";
        }
        createMainframeCall(packageCommand, "Building "+typeOfPackageToBuild+" package", true, build.getSetup().getMainframeInfo());
        setPathToVerbFile(dsName);
        if (returnOutputDataAsPrintData) {
            setSystsprt(); // return SYSTSPRT data as CLRPRINT data
        }
        runMainframeCall();

        boolean clearErrorFlag = MBUtilities.scanForString(ERRORLINE, getPrintFile().getAbsolutePath());

        // check for clear report error when hostRC was good
        // rc=4 warning, show the user the warning message and continue
        if (getReturnCode() >0 & getReturnCode() <= 4) {
            String msg = new String("There were warnings in the build.");
            MBChkFileParser parser = new MBChkFileParser(getPrintFile(),null,getLEP());
            Set stringsSet = new HashSet();
            stringsSet.add("*WARN*");
            stringsSet.add("*INFO*");
            Set displaySet  =  parser.getTableEntriesContaining(stringsSet);
            if (displaySet !=null && !displaySet.isEmpty()) {
                StringBuffer strb = new StringBuffer();
                for (Iterator iter= displaySet.iterator(); iter.hasNext();) {
                    strb.append((String)iter.next() + "\n");
                }
                if (MainInterface.getInterfaceSingleton()!=null) {
                    new MBMsgBox("Warning", strb.toString());
                } else {
                    getStatusHandler().updateStatus(strb.toString() + "\n",false);
                }
            }
            if (MainInterface.getInterfaceSingleton()!=null) {
                MBMsgBox viewQuestion = new MBMsgBox("Warning", msg+"\nDo you want to view the results?", null,true);
                if (viewQuestion.isAnswerYes()) {
                    MBEdit edit = new MBEdit(getPrintFile().getAbsolutePath(),getLEP());
                }
            } else {
                new MBEdit(getPrintFile().getAbsolutePath(), getLEP());
            }
        }

        // check the response from the server
        // search for "successfuly built" in the build.prt file
        MBClearReport crpt = new MBClearReport(getPrintFile().getAbsolutePath(), true);
        String statMsg = crpt.getBuildStatus();
        if (statMsg == null) {
            throw new HostError("There were errors in the build.\n" + build.getDriverInformation().getName()+" build failed.",this);
        } else {
            smodName= crpt.getPackageName();
            if (build.get_descr()==null) {
                build.set_descr(new String ());
            }
            build.set_descr(build.get_descr()+ " "+smodName);

        }
    }

    private String handleLogicSettings() throws com.ibm.sdwb.build390.MBBuildException{
        final String AUTOLOGICSTRING = "AUTO";
        String logicString = null;

        if (logicToUse != null) {
            logicString = logicToUse;
        } else {
            logicString = " SMPLOGIC="+AUTOLOGICSTRING;

        }
        if (logicString.startsWith(",")) {
            // yank the , since we add it above
            logicString = logicString.substring(1);
        }
        return logicString;
    }

    private String handleComments()throws com.ibm.sdwb.build390.MBBuildException{
        String commentString = ", COMMENTS=\'NO\'";
        if (comments!=null) {
            if (comments.equals("*")) {
                commentString = ", COMMENTS=\'*\'";
            } else {
                // ftp comments file to host
                File commentFile= new File(build.getBuildPath(),comments);
                if (commentFile.exists()) {
                    sendComments(build.getBuildPath(), comments);
                    commentString = ", COMMENTS=\'YES\'";
                } else { // comments file exists on local workstation
                    throw new ServiceError("The comments file, " + commentString+ " does not exist.") ;
                }
            }
        }
        return commentString;
    }

    private void sendComments(String path, String commFile) throws com.ibm.sdwb.build390.MBBuildException {
        getLEP().LogSecondaryInfo("Debug","sendComments:Entry");

        // use current directory if path is null
        File    commentFile = new File(path, commFile);

        String hostCommentFile = new String(build.getReleaseInformation().getMvsHighLevelQualifier()+"."+build.getReleaseInformation().getMvsName()+".COMMENTS("+build.getDriverInformation().getName()+")");
        getLEP().LogSecondaryInfo("Debug","sendComments:local:"+commentFile.getAbsolutePath()+" remote:"+hostCommentFile);

        // upload the comment file to the server
        MBFtp ftpObj = new MBFtp(build.getMainframeInfo(),getLEP());
        ftpObj.put(commentFile, hostCommentFile);
    }
}

