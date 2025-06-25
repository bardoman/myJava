package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.process.steps.NoOp;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.mainframe.*;
import java.util.*;
/* INT1765 release parameter changes */
public class CreateMVSDriver extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private ReleaseAndDriverParameters creationParameters = null;
    private com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults getMVSDefaults = null;
    private com.ibm.sdwb.build390.process.steps.CreateMVSDriver createMVSDriverStep = null;

    public CreateMVSDriver(MBBuild tempBuild, DriverInformation sourceDriver,  DriverInformation destinationDriver, boolean isDelta, ReleaseAndDriverParameters tempParameters, UserCommunicationInterface userComm) {
        super("Create MVS Driver",2, userComm); 
        build = tempBuild;
        creationParameters = tempParameters;
        createMVSDriverStep = new com.ibm.sdwb.build390.process.steps.CreateMVSDriver(build, sourceDriver, destinationDriver, isDelta, creationParameters, this);
    }

    public void setOverrideDefaultSettings(boolean tempOverride) {
        createMVSDriverStep.setOverrideDefaultSettings(tempOverride);
    }

    public void setDriverSize(String tempSize) {
        createMVSDriverStep.setDriverSize(tempSize);
    }

    public void setIncludeSysMods(boolean tempInclude) {
        createMVSDriverStep.setIncludeSysMods(tempInclude);
    }

    public void setNumberOfParts(String tempNumber) {
        createMVSDriverStep.setNumberOfParts(tempNumber);
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            if (unsetParametersExist(creationParameters)) {
                getMVSDefaults = new com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults(build.getSetup(), this);
                return getMVSDefaults;
            } else {
                return new NoOp(this);
            }
        case 1:
            if (unsetParametersExist(creationParameters)) {
                populateStorageParametersFromDefaultFile(creationParameters, getMVSDefaults.getDefaultSettingsForMVS());
            }
            return createMVSDriverStep;
        }
        return null;
    }

    private boolean unsetParametersExist(ReleaseAndDriverParameters creationParameters) {
        boolean isUnset = false;
        isUnset = isUnset | creationParameters.getDriverBulkDatasetPrimarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getDriverBulkDatasetSecondarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getDriverUnibankDatasetPrimarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getBulkDatasetMaximumExtentsInCylinders()==null;
        isUnset = isUnset | creationParameters.getBulkDatasetMaximumSizeInCylinders()==null;
        isUnset = isUnset | (creationParameters.getMainframeStorageParameters().unsetParametersExist());
        return isUnset;
    }

    /** please refer command line command for the valid options. It is presented in a table. addressed in defect TST2936*/
    private boolean unsetStorageParameters(String VOLid, String SMSStorageClass, String SMSManagementClass) {
        if (VOLid!=null) {
        } else {
            return false;
        }
        return true;

    }

    private void populateStorageParametersFromDefaultFile(ReleaseAndDriverParameters creationParameters, ReleaseAndDriverParameters defaultParameters) {
        if (creationParameters.getDriverBulkDatasetPrimarySpaceInCylinders()==null) {
            creationParameters.setDriverBulkDatasetPrimarySpaceInCylinders(defaultParameters.getDriverBulkDatasetPrimarySpaceInCylinders());
        }
        if (creationParameters.getDriverBulkDatasetSecondarySpaceInCylinders()==null) {
            creationParameters.setDriverBulkDatasetSecondarySpaceInCylinders(defaultParameters.getDriverBulkDatasetSecondarySpaceInCylinders());
        }
        if (creationParameters.getDriverUnibankDatasetPrimarySpaceInCylinders()==null) {
            creationParameters.setDriverUnibankDatasetPrimarySpaceInCylinders(defaultParameters.getDriverUnibankDatasetPrimarySpaceInCylinders());
        }
        if (creationParameters.getBulkDatasetMaximumExtentsInCylinders()==null) {
            creationParameters.setBulkDatasetMaximumExtentsInCylinders(defaultParameters.getBulkDatasetMaximumExtentsInCylinders());
        }
        if (creationParameters.getBulkDatasetMaximumSizeInCylinders()==null) {
            creationParameters.setBulkDatasetMaximumSizeInCylinders(defaultParameters.getBulkDatasetMaximumSizeInCylinders());
        }

        if (creationParameters.getMainframeStorageParameters().unsetParametersExist()) {
            if (creationParameters.getDASDVolumeIdentifier()==null) {
                creationParameters.setDASDVolumeIdentifier(defaultParameters.getDASDVolumeIdentifier());
            }
            if (creationParameters.getSMSManagementClass() == null) {
                creationParameters.setSMSManagementClass(defaultParameters.getSMSManagementClass());
            }

            if (creationParameters.getSMSStorageClass() == null) {
                creationParameters.setSMSStorageClass(defaultParameters.getSMSStorageClass());
            }
        }


        if (creationParameters.getHighLevelQualifier() == null) {
            creationParameters.setHighLevelQualifier(defaultParameters.getHighLevelQualifier());
        }
    }
}
