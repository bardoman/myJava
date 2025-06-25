package com.ibm.sdwb.build390.mainframe;

import java.util.*;


/** INT1765 mark's request for change in parameters */
public class ReleaseAndDriverParameters implements java.io.Serializable {
    static final long serialVersionUID = 1111111111111111L;

    private String highLevelQualifier = null;
    private String shadowBulkDatasetPrimarySpaceInCylinders = null;
    private String shadowBulkDatasetSecondarySpaceInCylinders = null;
    private String shadowUnibankDatasetPrimarySpaceInCylinders = null;
    private String driverBulkDatasetPrimarySpaceInCylinders = null;
    private String driverBulkDatasetSecondarySpaceInCylinders = null;
    private String driverUnibankDatasetPrimarySpaceInCylinders = null;
    private String bulkDatasetMaximumSizeInCyclinders = null;
    private String bulkDatasetMaximumExtents = null;
    private String additionalCollectors = null;
    private String additionalProcessSteps = null;
    private MainframeStorageParameters storageParameters = null;
    private String compIDHeader = null;
    private Set driverSizes = null;
    private String buildCC = null;
    private String autoBuild = null;
    private String listGen = null;
    private String runScan = null;
    private String pdtHeader = null;



    public ReleaseAndDriverParameters() {
        storageParameters = new MainframeStorageParameters();
    }

    

    public String getHighLevelQualifier() {
        return highLevelQualifier;
    }

    public MainframeStorageParameters getMainframeStorageParameters(){
        return storageParameters;
    }

    public String getDASDVolumeIdentifier() {
        return storageParameters.getDASDVolumeIdentifier();
    }

    public String getSMSStorageClass() {
        return storageParameters.getSMSStorageClass();
    }

    public String getSMSManagementClass() {
        return storageParameters.getSMSManagementClass();
    }

    public String getAutoBuild() {
        return autoBuild;
    }

    public String getListGen() {
        return listGen;
    }

    public String getRunScan() {
        return runScan;
    }

    public String getShadowBulkDatasetPrimarySpaceInCylinders() {
        return shadowBulkDatasetPrimarySpaceInCylinders;
    }

    public String getShadowBulkDatasetSecondarySpaceInCylinders() {
        return shadowBulkDatasetSecondarySpaceInCylinders;
    }

    public String getShadowUnibankDatasetPrimarySpaceInCylinders() {
        return shadowUnibankDatasetPrimarySpaceInCylinders;
    }

    public String getDriverBulkDatasetPrimarySpaceInCylinders() {
        return driverBulkDatasetPrimarySpaceInCylinders;
    }

    public String getDriverBulkDatasetSecondarySpaceInCylinders() {
        return driverBulkDatasetSecondarySpaceInCylinders;
    }

    public String getDriverUnibankDatasetPrimarySpaceInCylinders() {
        return driverUnibankDatasetPrimarySpaceInCylinders;
    }

    public String getBulkDatasetMaximumExtentsInCylinders() {
        return bulkDatasetMaximumExtents;
    }

    public String getBulkDatasetMaximumSizeInCylinders() {
        return bulkDatasetMaximumSizeInCyclinders;
    }

    public String getAdditionalCollectors() {
        return additionalCollectors;
    }

    public String getAdditionalProcessSteps() {
        return additionalProcessSteps;
    }

    public String getBuildCC() {
        return buildCC;
    }

    public String getComponentIDHeader() {
        return compIDHeader;
    }

    public String getPDTHeader() {
        return pdtHeader;
    }

    public Set getDriverSizes() {
        return driverSizes;
    }

    
    public void setHighLevelQualifier(String tempSetting) {
        highLevelQualifier = tempSetting;
    }

    public void setDASDVolumeIdentifier(String tempSetting) {
        getMainframeStorageParameters().setDASDVolumeIdentifier(tempSetting);
    }

    public void setSMSStorageClass(String tempSetting) {
        getMainframeStorageParameters().setSMSStorageClass(tempSetting);
    }

    public void setSMSManagementClass(String tempSetting) {
        getMainframeStorageParameters().setSMSManagementClass(tempSetting);
    }

    public void setAutoBuild(String tempSetting) {
        autoBuild = tempSetting;
    }

    public void setListGen(String tempSetting) {
        listGen = tempSetting;
    }

    public void setRunScanClass(String tempSetting) {
        runScan = tempSetting;
    }

    public void setShadowBulkDatasetPrimarySpaceInCylinders(String tempSetting) {
        shadowBulkDatasetPrimarySpaceInCylinders = tempSetting;
    }

    public void setShadowBulkDatasetSecondarySpaceInCylinders(String tempSetting) {
        shadowBulkDatasetSecondarySpaceInCylinders = tempSetting;
    }

    public void setShadowUnibankDatasetPrimarySpaceInCylinders(String tempSetting) {
        shadowUnibankDatasetPrimarySpaceInCylinders = tempSetting;
    }

    public void setDriverBulkDatasetPrimarySpaceInCylinders(String tempSetting) {
        driverBulkDatasetPrimarySpaceInCylinders = tempSetting;
    }

    public void setDriverBulkDatasetSecondarySpaceInCylinders(String tempSetting) {
        driverBulkDatasetSecondarySpaceInCylinders = tempSetting;
    }

    public void setDriverUnibankDatasetPrimarySpaceInCylinders(String tempSetting) {
        driverUnibankDatasetPrimarySpaceInCylinders = tempSetting;
    }

    public void setBulkDatasetMaximumExtentsInCylinders(String tempSetting) {
        bulkDatasetMaximumExtents = tempSetting;
    }

    public void setBulkDatasetMaximumSizeInCylinders(String tempSetting) {
        bulkDatasetMaximumSizeInCyclinders = tempSetting;
    }

    public void setAdditionalCollectors(String tempSetting) {
        additionalCollectors = tempSetting;
    }

    public void setAdditionalProcessSteps(String tempSetting) {
        additionalProcessSteps = tempSetting;
    }

    public void setBuildCC(String tempSetting) {
        buildCC = tempSetting;
    }

    public void setComponentIDHeader(String tempSetting) {
        compIDHeader = tempSetting;
    }

    public void setDriverSizes(Set tempSetting) {
        driverSizes = tempSetting;
    }

    public void setPDTHeader(String tempSetting) {
        pdtHeader = tempSetting;
    }

/*
    public String getValueForMainframeSetting(String obscureMainframeName) throws com.ibm.sdwb.build390.GeneralError{
        if (obscureMainframeName.equals("CHILVL")) {
            return highLevelQualifier;
        }else if (obscureMainframeName.equals("CLNAME")) {
            return shadowName;
        }else if (obscureMainframeName.equals("CRELEASE")) {
            return shadowName;  // this is how driver creation calls the shadow name
        }else if (obscureMainframeName.equals("CCLTR")) {
            return additionalCollectors;
        }else if (obscureMainframeName.equals("CPRCS")) {
            return additionalProcessSteps;
        }else if (obscureMainframeName.equals("CBLKP")) {
            return shadowBulkDatasetPrimarySpaceInCylinders;
        }else if (obscureMainframeName.equals("CBLKS")) {
            return shadowBulkDatasetSecondarySpaceInCylinders;
        }else if (obscureMainframeName.equals("CMAXCYL")) {
            return bulkDatasetMaximumSizeInCyclinders;
        }else if (obscureMainframeName.equals("CMAXEXT")) {
            return bulkDatasetMaximumExtents;
        }else if (obscureMainframeName.equals("CVOLID")) {
            return  DASDVolumeIdentifier;
        }else if (obscureMainframeName.equals("CSTGCLS")) {
            return SMSStorageClass;
        }else if (obscureMainframeName.equals("CMGTCLS")) {
            return SMSManagementClass;
        }else if (obscureMainframeName.equals("NUMPARTS")) {
            return numberOfParts;
        }else if (obscureMainframeName.equals("CPDT")) {
            return pdtHeader;
        }else if (obscureMainframeName.equals("HLCOMP")) {
            return compIDHeader;
        }else if (obscureMainframeName.equals("CDVRSIZE")) {
            return driverSize;
        }else if (obscureMainframeName.equals("CSYSMODS")) {
            return includeSysmods;
        }else if (obscureMainframeName.equals("CFULLDEL")) {
            return allPartsInDelta;
        }else if (obscureMainframeName.equals("CDRVRTYP")) {
            return deltaBuild;
        }
        throw new com.ibm.sdwb.build390.GeneralError("Mainframe keyword " + obscureMainframeName + " is not found.");
    }
*/

    public void setValueFromMainframeSetting(String obscureMainframeName, String obscureMainframeSetting) throws com.ibm.sdwb.build390.GeneralError{
        if (obscureMainframeName.equals("CHILVL")|obscureMainframeName.equals("HILVL")) {
            highLevelQualifier = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("COLECTRS")) {
            additionalCollectors = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("PROCESES")) {
            additionalProcessSteps = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("SHADBP")) {
            shadowBulkDatasetPrimarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("SHADBS")) {
            shadowBulkDatasetSecondarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("SHADUP")) {
            shadowUnibankDatasetPrimarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("DRVRBP")) {
            driverBulkDatasetPrimarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("DRVRBS")) {
            driverBulkDatasetSecondarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("DRVRUP")) {
            driverUnibankDatasetPrimarySpaceInCylinders = obscureMainframeSetting;
        } else if (obscureMainframeName.endsWith("MAXCYL")) {
            bulkDatasetMaximumSizeInCyclinders = obscureMainframeSetting;
        } else if (obscureMainframeName.endsWith("MAXEXT")) {
            bulkDatasetMaximumExtents = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("BUILDCC")) {
            buildCC = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("AUTOBLD")) {
            autoBuild = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("LISTGEN")) {
            listGen = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("RUNSCAN")) {
            runScan = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("VOLID")) {
            getMainframeStorageParameters().setDASDVolumeIdentifier(obscureMainframeSetting);
        } else if (obscureMainframeName.equals("STGCLS")) {
            getMainframeStorageParameters().setSMSStorageClass(obscureMainframeSetting);
        } else if (obscureMainframeName.equals("MGTCLS")) {
            getMainframeStorageParameters().setSMSManagementClass(obscureMainframeSetting);
        } else if (obscureMainframeName.equals("PDT")) {
            pdtHeader = obscureMainframeSetting;
        } else if (obscureMainframeName.equals("HLCOMP")) {
            compIDHeader = obscureMainframeSetting;
        } else {
            throw new com.ibm.sdwb.build390.GeneralError("Mainframe keyword " + obscureMainframeName + " is not found.");
        }
    }

    
/*
    public String equals(Object anotherRelease){
        if (anotherRelease==null) {
            return null;
        }else if (!(anotherRelease instanceof ReleaseAndDriverParameters)) {
            return null;
        }else {
            ReleaseAndDriverParameters castRelease = (ReleaseAndDriverParameters) anotherRelease;
            String isEqual =   bulkDatasetMaximumExtents==castRelease.getBulkDatasetMaximumExtentsInCylinders() &
                                bulkDatasetMaximumSizeInCyclinders==castRelease.getBulkDatasetMaximumSizeInCylinders() &
                                shadowBulkDatasetPrimarySpaceInCylinders==castRelease.getBulkDatasetPrimarySpaceInCylinders() &
                                shadowBulkDatasetSecondarySpaceInCylinders==castRelease.getBulkDatasetSecondarySpaceInCylinders() &
                                unibankPrimarySpaceInCycliners==castRelease.getUnibankPrimarySpaceInCylinders() &
                                additionalCollectors==castRelease.getAdditionalCollectors() &
                                additionalProcessSteps==castRelease.getAdditionalProcessSteps();
            if (highLevelQualifier!=null) {
                isEqual = isEqual & highLevelQualifier.equals(castRelease.getHighLevelQualifier());
            }else if (castRelease.getHighLevelQualifier()!=null) {
                return null;
            }
            if (shadowName!=null) {
                isEqual = isEqual & shadowName.equals(castRelease.getShadowName());
            }else if (castRelease.getShadowName()!=null) {
                return null;
            }
            if (SMSStorageClass!=null) {
                isEqual = isEqual & SMSStorageClass.equals(castRelease.getSMSStorageClass());
            }else if (castRelease.getSMSStorageClass()!=null) {
                return null;
            }
            if (SMSManagementClass!=null) {
                isEqual = isEqual & SMSManagementClass.equals(castRelease.getSMSManagementClass());
            }else if (castRelease.getSMSManagementClass()!=null) {
                return null;
            }
            return isEqual;
        }
    }
*/  
}
