package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.SyntaxError;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.MainframeStorageParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.CreateMVSRelease;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.user.SetupManager;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.utilities.BooleanAnd;
import com.ibm.sdwb.build390.utilities.BooleanExclusiveOr;
import com.ibm.sdwb.build390.utilities.BooleanOr;

public class MainframeShadowCreate extends CommandLineProcess {

    public static final String PROCESSNAME = com.ibm.sdwb.build390.process.steps.CreateMVSRelease.CREATEMVSRELEASE;

    private MainframeHighLevelQualifier highLevelQualifier = new MainframeHighLevelQualifier();
    private MainframeRelease shadow = new MainframeRelease();
    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDataSetParameterBulkPrimaryCylinders bulkPrimary = new MainframeDataSetParameterBulkPrimaryCylinders();
    private MainframeDataSetParameterBulkSecondaryCylinders bulkSecondary = new MainframeDataSetParameterBulkSecondaryCylinders();
    private MainframeDataSetParameterUnibankCylinders unibankPrimary = new MainframeDataSetParameterUnibankCylinders();
    private MainframeDataSetParameterBulkMaxCylinders maxCylinders = new MainframeDataSetParameterBulkMaxCylinders();
    private MainframeDataSetParameterBulkMaxExtents maxExtents = new MainframeDataSetParameterBulkMaxExtents();
    private MainframeCollectors collectors = new MainframeCollectors();
    private MainframeProcessSteps processSteps = new MainframeProcessSteps();
    private MainframeVolumeID volumeID =  new MainframeVolumeID();
    private SMSManagementClass managementClass = new SMSManagementClass();
    private SMSStorageClass storageClass = new SMSStorageClass();

    public MainframeShadowCreate(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }
    public String getHelpDescription() {
        return getProcessTypeHandled()+ " command creates a shadow data base.";
    }

    public String getHelpExamples() {
        StringBuffer exampleBuffer= new StringBuffer();
        exampleBuffer.append("1."+getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<newshadow> LIBRELEASE=<librelease>\n");
        exampleBuffer.append("2."+getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<newshadow> LIBRELEASE=<librelease> MVSVOLUMEID=<mvsvolumeid>\n");
        exampleBuffer.append("3."+getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<newshadow> LIBRELEASE=<librelease> MVSVOLUMEID=<mvsvolumeid> SMSSTORAGECLASS=<smsstorage>\n");
        exampleBuffer.append("4."+getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<newshadow> LIBRELEASE=<librelease> SMSSTORAGECLASS=<smsstorage>\n");
        exampleBuffer.append("5."+getProcessTypeHandled()+" MVSHLQ=<hlq> MVSRELEASE=<newshadow> LIBRELEASE=<librelease> SMSSTORAGECLASS=<smsstorage> SMSMANAGEMENTCLASS=<smsmanagement>\n\n");
        exampleBuffer.append("Note:\n\n");
        exampleBuffer.append(MainframeStorageParameters.TABLE);
        return exampleBuffer.toString();
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(shadow);
        baseAnd.addBooleanInterface(highLevelQualifier);
        baseAnd.addBooleanInterface(libRelease);
        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(bulkPrimary);
        argumentStructure.addOption(bulkSecondary);
        argumentStructure.addOption(unibankPrimary);
        argumentStructure.addOption(maxCylinders);
        argumentStructure.addOption(maxExtents);
        argumentStructure.addOption(collectors);
        argumentStructure.addOption(processSteps);

        BooleanExclusiveOr storageChoiceXOR = new BooleanExclusiveOr();
        storageChoiceXOR.addBooleanInterface(managementClass);
        storageChoiceXOR.addBooleanInterface(volumeID);

        BooleanOr storageOR = new BooleanOr();
        storageOR.addBooleanInterface(storageChoiceXOR);
        storageOR.addBooleanInterface(storageClass);

        argumentStructure.addOption(storageOR);

    }

    public void runProcess() throws com.ibm.sdwb.build390.MBBuildException{


        ReleaseInformation releaseInfo = new ReleaseInformation(libRelease.getValue(), shadow.getValue(),highLevelQualifier.getValue());
        ReleaseAndDriverParameters driverParms = new ReleaseAndDriverParameters();
        if (maxExtents.isSatisfied()) {
            driverParms.setBulkDatasetMaximumExtentsInCylinders(maxExtents.getValue());
        }
        if (maxCylinders.isSatisfied()) {
            driverParms.setBulkDatasetMaximumSizeInCylinders(maxCylinders.getValue());
        }

        //not an ideal way to handle it. The problem is we need to have to handle a case like this.
        // refer the table in  MainframeStorageParameter.TABLE
        if ((!volumeID.isSatisfied() & !storageClass.isSatisfied() & managementClass.isSatisfied()) ||
            (volumeID.isSatisfied() & managementClass.isSatisfied() &
             storageClass.isSatisfied())) {
            //error.
            throw new SyntaxError("Invalid storage class or volume serial combination.");
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

        if (bulkPrimary.isSatisfied()) {
            driverParms.setDriverBulkDatasetPrimarySpaceInCylinders(bulkPrimary.getValue());
        }
        if (bulkSecondary.isSatisfied()) {
            driverParms.setDriverBulkDatasetSecondarySpaceInCylinders(bulkSecondary.getValue());
        }
        if (unibankPrimary.isSatisfied()) {
            driverParms.setDriverUnibankDatasetPrimarySpaceInCylinders(unibankPrimary.getValue());
        }
        final String DEFAULT ="2";
        if (collectors.isSatisfied()) {
            driverParms.setAdditionalCollectors(collectors.getValue());
        } else {
            driverParms.setAdditionalCollectors(DEFAULT);

        }
        if (processSteps.isSatisfied()) {
            driverParms.setAdditionalProcessSteps(processSteps.getValue());
        } else {
            driverParms.setAdditionalProcessSteps(DEFAULT);

        }

        Setup setup = SetupManager.getSetupManager().createSetupInstance();

        CreateMVSRelease releaseCreate = new CreateMVSRelease(setup, setup.getLibraryInfo(), releaseInfo, driverParms,this);

        setCancelableProcess(releaseCreate);

        releaseCreate.externalRun();
    }
}
