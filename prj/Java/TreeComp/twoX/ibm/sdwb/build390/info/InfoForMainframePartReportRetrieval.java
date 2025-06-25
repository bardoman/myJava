package com.ibm.sdwb.build390.info;

public class InfoForMainframePartReportRetrieval  implements java.io.Serializable {
    public static boolean IS_PDS=true;//TST3337
    private String partName = null;
    private String partClass= null;
    private String baseName = null;
    private String directory= null;
    private String reportType = null;
    private boolean binary = false;
    private boolean isPDS=false;

    public InfoForMainframePartReportRetrieval(String tempName, String tempClass) {
        partName = tempName;
        partClass = tempClass;
    }

    //Begin TST3337
    public InfoForMainframePartReportRetrieval(String tempName, String tempClass, boolean isPDS) {
        partName = tempName;
        partClass = tempClass;
        this.isPDS = isPDS;
    }

    public void setIsPDS(boolean isPDS) {
        this.isPDS = isPDS;
    }

    public boolean getIsPDS() {
        return isPDS;
    }
    //End TST3337

    public void setBinary(boolean temp) {
        binary = temp;
    }

    public void setReportType(String temp) {
        reportType = temp;
    }

    public void setDirectory(String temp) {
        this.directory= temp;
    }

    public void setName(String temp) {
        this.baseName=temp;
    }

    public String getPartName() {
        return partName;
    }

    public String getPartClass() {
        return partClass;
    }

    public String getReportType() {
        return reportType;
    }

    public String getDirectory() {
        return directory;
    }

    public String getName() {
        return baseName;
    }

    public boolean isBinary() {
        return binary;
    }
}
