package com.ibm.sdwb.build390.process;

import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.mainframe.ReleaseInformation;
import com.ibm.sdwb.build390.process.steps.NoOp;
import com.ibm.sdwb.build390.process.steps.ProcessStep;
import com.ibm.sdwb.build390.user.Setup;
import com.ibm.sdwb.build390.userinterface.UserCommunicationInterface;

public class CreateMVSRelease extends AbstractProcess {
    static final long serialVersionUID = 1111111111111111L;

    private Setup setup = null;
    private LibraryInfo libInfo = null;
    private ReleaseAndDriverParameters creationParameters = null;
    private com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults getMVSDefaults = null;
    private ReleaseInformation relInfo = null;

    public CreateMVSRelease(Setup tempSetup, LibraryInfo tempLib, ReleaseInformation tempRelease, ReleaseAndDriverParameters tempParameters, UserCommunicationInterface userComm) {
        super("Create MVS Release",3, userComm); 
        setup = tempSetup;
        libInfo = tempLib;
        creationParameters = tempParameters;
        relInfo = tempRelease;
    }

    protected ProcessStep getProcessStep(int stepToGet, int stepIteration) {
        switch (stepToGet) {
        case 0:
            com.ibm.sdwb.build390.process.steps.CheckConnectionToLibrary checkLibraryConnection = new com.ibm.sdwb.build390.process.steps.CheckConnectionToLibrary(libInfo, this);
            checkLibraryConnection.setRelease(relInfo.getLibraryName());
            return checkLibraryConnection;
        case 1:
            if (unsetParametersExist(creationParameters)) {
                getMVSDefaults = new com.ibm.sdwb.build390.process.steps.GetMVSSiteDefaults(setup, this);
                return getMVSDefaults;
            } else {
                return new NoOp(this);
            }
        case 2:
            if (unsetParametersExist(creationParameters)) {
                populateStorageParametersFromDefaultFile(creationParameters, getMVSDefaults.getDefaultSettingsForMVS());
            }
            com.ibm.sdwb.build390.process.steps.CreateMVSRelease createMVSReleaseStep = new com.ibm.sdwb.build390.process.steps.CreateMVSRelease(setup.getMainframeInfo(), libInfo, relInfo, creationParameters,MBGlobals.Build390_path+"misc"+java.io.File.separator, this);
            return createMVSReleaseStep;
        }
        return null;
    }

    private boolean unsetParametersExist(ReleaseAndDriverParameters creationParameters) {
        boolean isUnset = false;
        isUnset = isUnset | creationParameters.getShadowBulkDatasetPrimarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getShadowBulkDatasetSecondarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getShadowUnibankDatasetPrimarySpaceInCylinders()==null;
        isUnset = isUnset | creationParameters.getBulkDatasetMaximumExtentsInCylinders()==null;
        isUnset = isUnset | creationParameters.getBulkDatasetMaximumSizeInCylinders()==null;
        isUnset = isUnset | (creationParameters.getMainframeStorageParameters().unsetParametersExist());
        return isUnset;
    }

    private void populateStorageParametersFromDefaultFile(ReleaseAndDriverParameters creationParameters, ReleaseAndDriverParameters defaultParameters) {
        if (creationParameters.getShadowBulkDatasetPrimarySpaceInCylinders()==null) {
            creationParameters.setShadowBulkDatasetPrimarySpaceInCylinders(defaultParameters.getShadowBulkDatasetPrimarySpaceInCylinders());
        }
        if (creationParameters.getShadowBulkDatasetSecondarySpaceInCylinders()==null) {
            creationParameters.setShadowBulkDatasetSecondarySpaceInCylinders(defaultParameters.getShadowBulkDatasetSecondarySpaceInCylinders());
        }
        if (creationParameters.getShadowUnibankDatasetPrimarySpaceInCylinders()==null) {
            creationParameters.setShadowUnibankDatasetPrimarySpaceInCylinders(defaultParameters.getShadowUnibankDatasetPrimarySpaceInCylinders());
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
    }
}
