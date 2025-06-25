package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.process.DriverCreationParameterReport;
import com.ibm.sdwb.build390.process.ProcessWrapperForSingleStep;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;


//*************************************************************************
//12/05/2003 #DEF.TST1726: Command Line - NullPointerException on cleanup
//*************************************************************************

public class MainframeDriverCreate extends CommandLineProcess {

    public static final String PROCESSNAME = "CREATEDRIVER";

    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();
    private MainframeRelease mainframeRelease = new MainframeRelease();
    private MainframeDriverNew newDriver = new MainframeDriverNew();
    private MainframeDriverBase baseDriver = new MainframeDriverBase(); 
    private MainframeDriverDeltaOrBase deltaDriver = new MainframeDriverDeltaOrBase();
    private MainframeDriverSize driverSize = new MainframeDriverSize();
    private SysmodCopy sysmodCopy = new SysmodCopy();
    private MainframeVolumeID volumeID =  new MainframeVolumeID();
    private SMSManagementClass managementClass = new SMSManagementClass();
    private SMSStorageClass storageClass = new SMSStorageClass();
    private MainframeDataSetParameterBulkPrimaryCylinders primaryBulk = new MainframeDataSetParameterBulkPrimaryCylinders();
    private MainframeDataSetParameterBulkSecondaryCylinders secondaryBulk = new MainframeDataSetParameterBulkSecondaryCylinders();
    private MainframeDataSetParameterUnibankCylinders unibank = new MainframeDataSetParameterUnibankCylinders();
    private MainframeDataSetParameterBulkMaxCylinders maxCylinders = new MainframeDataSetParameterBulkMaxCylinders();
    private MainframeDataSetParameterBulkMaxExtents maxExtents = new MainframeDataSetParameterBulkMaxExtents();
    private NumberOfPartsInDriver numberOfParts = new NumberOfPartsInDriver();    
    private boolean isDelta = false;//INT3094
    private boolean isFullDelta = false;//INT3094
    private String defaultSize = "SMALL";//INT3094
    private boolean overrideDefaults = false;//INT3094

    public MainframeDriverCreate(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return "The "+getProcessTypeHandled() + " command creates a driver on the host system.";
    }

    public String getHelpExamples() {
        StringBuffer exampleBuffer = new StringBuffer();
        exampleBuffer.append("1.To create a new base driver:\n");
        exampleBuffer.append(getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<mvsrelease>\n");
        exampleBuffer.append("        NEWDRIVER=<newdriver>\n\n");
        exampleBuffer.append("2.To create a new base driver and copy from another driver:\n");
        exampleBuffer.append(getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<mvsrelease>\n");
        exampleBuffer.append("        NEWDRIVER=<newdriver> BASEDRIVER=<basedriver> DRIVERTYPE=FULL\n");
        exampleBuffer.append("        SYSMODCOPY=<yes/(no)> DRIVERSIZE=<driversize>\n\n");
        exampleBuffer.append("3.To create a new delta driver:\n");
        exampleBuffer.append(getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<mvsrelease>\n");
        exampleBuffer.append("        NEWDRIVER=<newdriver> BASEDRIVER=<basedriver> DRIVERTYPE=DELTA\n");
        exampleBuffer.append("        DRIVERSIZE=<driversize>\n");
        exampleBuffer.append("Note:\n\n");
        exampleBuffer.append(MainframeStorageParameters.TABLE);
        return exampleBuffer.toString();

    }


    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(mainframeRelease);
        baseAnd.addBooleanInterface(newDriver);
        baseAnd.addBooleanInterface(highLevelQualifier);
        argumentStructure.setRequiredPart(baseAnd);

        BooleanAnd driverTypeFullAnd = new BooleanAnd();
        driverTypeFullAnd.addBooleanInterface(deltaDriver);
        driverTypeFullAnd.addBooleanInterface(baseDriver);

        BooleanExclusiveOr storageChoiceXOR = new BooleanExclusiveOr();
        storageChoiceXOR.addBooleanInterface(managementClass);
        storageChoiceXOR.addBooleanInterface(volumeID);

        BooleanOr storageOR = new BooleanOr();
        storageOR.addBooleanInterface(storageChoiceXOR);
        storageOR.addBooleanInterface(storageClass);

        argumentStructure.addOption(driverSize);
        argumentStructure.addOption(sysmodCopy);
        argumentStructure.addOption(driverTypeFullAnd);
        argumentStructure.addOption(storageOR);
        argumentStructure.addOption(primaryBulk);
        argumentStructure.addOption(secondaryBulk);
        argumentStructure.addOption(unibank);
        argumentStructure.addOption(maxCylinders);
        argumentStructure.addOption(maxExtents);
        argumentStructure.addOption(numberOfParts);
    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException {
        MBBuild tempBuild = new MBBuild(getLEP());

        ReleaseInformation releaseInfo = getReleaseInformation(mainframeRelease.getValue(),tempBuild.getSetup(), false);
        tempBuild.setReleaseInformation(releaseInfo);

        //not an ideal way to handle it. The problem is we need to have to handle a case like this.
        // refer the table in getHelpExamples().
        if ((!volumeID.isSatisfied() & !storageClass.isSatisfied() & managementClass.isSatisfied()) ||
            (volumeID.isSatisfied() & managementClass.isSatisfied() &
             storageClass.isSatisfied())) {
            //error.
            throw new SyntaxError("Invalid storage class or volume serial combination.");
        }

        if (highLevelQualifier.isSatisfied()) {
            releaseInfo = new ReleaseInformation(releaseInfo.getLibraryName(),releaseInfo.getMvsName(),highLevelQualifier.getValue());
        }

        //Begin CmdLineUpdate
        DriverInformation drvInfo = new DriverInformation(newDriver.getValue());

        drvInfo.setReleaseInfomation(tempBuild.getReleaseInformation());

        tempBuild.setDriverInformation(drvInfo);
        //End CmdLineUpdate
               
        if (deltaDriver.isSatisfied()) {
            isDelta = deltaDriver.getValue().equalsIgnoreCase("delta");        
            isFullDelta = deltaDriver.getValue().equalsIgnoreCase("full");
        }
        
        String basedOnMvsRelease = releaseInfo.getMvsName();//TST3572
        String basedOnDriverName = baseDriver.getValue(); //TST3572        

        DriverInformation baseDriverInfo = null;
        if (baseDriver.isSatisfied()) {
            if (baseDriver.getValue().indexOf(".") > 0) {
                basedOnMvsRelease =baseDriver.getValue().substring(0,baseDriver.getValue().indexOf("."));
                basedOnDriverName =baseDriver.getValue().substring(baseDriver.getValue().indexOf(".")+1);
            }
            
            ReleaseInformation tempSourceRelease = tempBuild.getSetup().getMainframeInfo().getReleaseByMVSName(basedOnMvsRelease,tempBuild.getSetup().getLibraryInfo()); //TST3572
            baseDriverInfo = tempSourceRelease.getDriverByName(basedOnDriverName); //TST3572
            baseDriverInfo.setReleaseInfomation(tempSourceRelease); //TST3572
        }

        DriverInformation newDriverInfo = new DriverInformation(newDriver.getValue());
        ReleaseAndDriverParameters driverParms = new ReleaseAndDriverParameters();
        Map driverCreationSettingsMap = null;
        
        if (baseDriverInfo!=null) {
            newDriverInfo.setBaseDriver(baseDriverInfo);
            driverCreationSettingsMap = getDriverCreationParameters(tempBuild);
        }
        
        //TST3572<BEGIN>
        if (isDelta) {
            newDriverInfo.setBaseDriver(baseDriverInfo);
        }        
        releaseInfo = tempBuild.getSetup().getMainframeInfo().getReleaseByMVSName(basedOnMvsRelease,tempBuild.getSetup().getLibraryInfo());        
        newDriverInfo.setReleaseInfomation(releaseInfo);       
        //TST3572<END>
        
        if (driverCreationSettingsMap!=null && !driverCreationSettingsMap.isEmpty()) {
            useDriverCreationParameters(driverCreationSettingsMap,driverParms);//INT3094
            if (isDelta) {
                useDriverSize(isFullDelta,driverCreationSettingsMap,driverParms);
                overrideDefaults=true;
            }
        }
        
        if (maxExtents.isSatisfied()) {
            driverParms.setBulkDatasetMaximumExtentsInCylinders(maxExtents.getValue());
        }
        if (maxCylinders.isSatisfied()) {
            driverParms.setBulkDatasetMaximumSizeInCylinders(maxCylinders.getValue());
        }
        if (volumeID.isSatisfied()) {
            driverParms.setDASDVolumeIdentifier(volumeID.getValue());
        }
        if (storageClass.isSatisfied()) {
            driverParms.setSMSStorageClass(storageClass.getValue());
        }

        if (managementClass.isSatisfied()) {
            driverParms.setSMSManagementClass(managementClass.getValue());
        }
        if (primaryBulk.isSatisfied()) {
            driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(primaryBulk.getValue());
        }
        if (secondaryBulk.isSatisfied()) {
            driverParms.setDriverBulkDatasetSecondarySpaceInCylinders(secondaryBulk.getValue());
        }
        if (unibank.isSatisfied()) {
            driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(unibank.getValue());
        }        
        
        com.ibm.sdwb.build390.process.CreateMVSDriver driverCreate = new com.ibm.sdwb.build390.process.CreateMVSDriver(tempBuild, baseDriverInfo, newDriverInfo, isDelta, driverParms, this);
        
        driverCreate.setOverrideDefaultSettings(isOverrideSet());
        
        if (driverSize.isSatisfied()) {
            driverCreate.setDriverSize(driverSize.getValue());
        }
        if (sysmodCopy.isSatisfied()) {
            driverCreate.setIncludeSysMods(sysmodCopy.getBooleanValue());

        }
        if (numberOfParts.isSatisfied()) {
            driverCreate.setNumberOfParts(numberOfParts.getValue());
        }


        setCancelableProcess(driverCreate);

        driverCreate.externalRun();

        com.ibm.sdwb.build390.process.MVSReleaseAndDriversList releaseAndDriversList = new com.ibm.sdwb.build390.process.MVSReleaseAndDriversList(tempBuild.getMainframeInfo(), tempBuild.getLibraryInfo(), new java.io.File(MBGlobals.Build390_path+"misc"+java.io.File.separator), this);

        setCancelableProcess(releaseAndDriversList);

        releaseAndDriversList.externalRun();

    }

    //INT3094<START>  

    private boolean isOverrideSet() {
        return overrideDefaults;
    }
    
    private Map getDriverCreationParameters(MBBuild tempBuild) throws MBBuildException {
        ProcessWrapperForSingleStep driverReportWrapper = new ProcessWrapperForSingleStep(this);
        setCancelableProcess(driverReportWrapper);
        com.ibm.sdwb.build390.process.steps.DriverCreationParameterReport driverCreationReportStep = new com.ibm.sdwb.build390.process.steps.DriverCreationParameterReport(tempBuild, highLevelQualifier.getValue(),mainframeRelease.getValue(), baseDriver.getValue(),getCancelableProcess());
        driverCreationReportStep.execute();
        if (driverCreationReportStep.isCheckSuccessful()) {
            return driverCreationReportStep.getSettingMap();
        }
        return new HashMap();

    }

    /**if this is a copy request or a delta, submit op=check to get default space settings
    DriverAlloc -
     New Base Driver - use defaults
     New base copied - UBKSP = Bankused
                       BULKP = Larger of Bulkcyl or Pricyl
                       BULKS = Seccyl
     New Full Delta  - UBKSP = Larger of Bankused or Value 1 from size selection
                       BULKP = Second value from size selection
                       BULKS = Third value from size selection
     New Thin Delta  - UBKSP = First value from size selection
                       BULKP = Second value from size selection
                       BULKS = Third value from size selection
                       **/
    private void useDriverCreationParameters(Map settingsMap, ReleaseAndDriverParameters driverParms) throws MBBuildException {
        if (!isDelta) {
            String pricyl= settingsMap.get("CBLKP").toString();
            String bulkcyl= settingsMap.get("BULKCYL").toString();
            Integer ix = new Integer(0);
            if ((ix.decode(bulkcyl)).intValue() > (ix.decode(pricyl)).intValue()) {
                driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(settingsMap.get("BULKCYL").toString()); //Bulkcyl
            } else {
                driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(settingsMap.get("CBLKP").toString()); // Pricyl
            }
            driverParms.setDriverBulkDatasetSecondarySpaceInCylinders(settingsMap.get("CBLKS").toString()); //Seccyl
            driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("CUBKP").toString()); //Bank Used
        }
        // otherwise they will be set if it is delta
        driverParms.setBulkDatasetMaximumExtentsInCylinders(settingsMap.get("CMAXEXT").toString());            
        driverParms.setBulkDatasetMaximumSizeInCylinders(settingsMap.get("CMAXCYL").toString());
        if(settingsMap.get("CVOLID")!=null) {
        	driverParms.setDASDVolumeIdentifier(settingsMap.get("CVOLID").toString());
        }
        if(settingsMap.get("CSTGCLS")!=null) {
        	driverParms.setSMSStorageClass(settingsMap.get("CSTGCLS").toString());
        } else driverParms.setSMSStorageClass("");
        if(settingsMap.get("CMGTCLS")!=null) {
        	driverParms.setSMSManagementClass(settingsMap.get("CMGTCLS").toString()); 
        } else driverParms.setSMSManagementClass("");
                 

        int SIZEKeywordCount=0;
        for (Iterator iter=settingsMap.entrySet().iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (((String)entry.getKey()).startsWith("SIZE") & ((String)entry.getKey()).endsWith("NAME")) {
                SIZEKeywordCount++;
            }
        }

        for (int cnt=1; cnt < SIZEKeywordCount; cnt++) {
            String name = settingsMap.get("SIZE"+cnt+"_NAME").toString();
            if (name!=null) {
                driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_UBKP").toString());
                driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_BLKP").toString());
                driverParms.setDriverBulkDatasetSecondarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_BLKS").toString());
            }
        }


    }

    private void useDriverSize(boolean isFullDelta, Map settingsMap, ReleaseAndDriverParameters driverParms) {
        //only mess with this stuff if this is a delta driver
        if (isFullDelta) {
            // full delta
            driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("CUBKP").toString());
        }
        // get selected size
        String drvrSize ="";
        if (driverSize.isSatisfied()) {
            drvrSize =  driverSize.getValue();
        } else drvrSize=defaultSize;
        // update setting based on size
        for (int cnt=1; cnt<4; cnt++) {
            Object sizeName = settingsMap.get("SIZE"+cnt+"_NAME");
            if ( sizeName!= null) {
                if (drvrSize.equals(settingsMap.get("SIZE"+cnt+"_NAME").toString())) {
                    driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_UBKP").toString());
                    // DriverAlloc
                    if (isFullDelta) { // full delta
                        Integer ix = new Integer(0);
                        if ((ix.decode(settingsMap.get("CUBKP").toString())).intValue() < (ix.decode(settingsMap.get("SIZE"+cnt+"_UBKP").toString())).intValue()) {
                            driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_UBKP").toString());
                        }
                    } else {
                        driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_UBKP").toString());
                    }
                    driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_BLKP").toString());
                    driverParms.setDriverBulkDatasetSecondarySpaceInCylinders(settingsMap.get("SIZE"+cnt+"_BLKS").toString());
                }
            }
        }        
    }
    //INT3094<END>

}