package com.ibm.sdwb.build390.mainframe;

public class MainframeStorageParameters implements java.io.Serializable {

    static final long serialVersionUID = 1111111111111111L;

    private String DASDVolumeIdentifier = null;
    private String SMSStorageClass = null;
    private String SMSManagementClass = null;

    public static StringBuffer TABLE = new StringBuffer();

    static{
        TABLE.append("The following are the valid storage and volume serial combinations.\n\n");
        TABLE.append("* ------------------------------------------------------*\n");
        TABLE.append("| MVSVOLUMEID  |  SMSSTORAGECLASS  | SMSMANAGEMENTCLASS |\n"); 
        TABLE.append("| ------------------------------------------------------|\n");
        TABLE.append("|     X        |         -         |          -         |\n");   
        TABLE.append("|     -        |         X         |          -         |\n");   
        TABLE.append("|     X        |         X         |          -         |\n");   
        TABLE.append("|     -        |         X         |          X         |\n"); 
        TABLE.append("* ------------------------------------------------------*\n");
        TABLE.append("X -> means the parameter has value    (input   available)\n");
        TABLE.append("- -> means the parameter has no value (input unavailable)\n");
    }

    public static StringBuffer TABLE_TEXT = new StringBuffer();
    static {
        TABLE_TEXT.append("* ------------------------------------------------------*\n");
        TABLE_TEXT.append("DASD volume serial only.\n");
        TABLE_TEXT.append("DASD volume serial & SMS storage class.\n");
        TABLE_TEXT.append("SMS  storage class only.\n");
        TABLE_TEXT.append("SMS  storage class & SMS management class.\n");
        TABLE_TEXT.append("* ------------------------------------------------------*\n");
    }

    public MainframeStorageParameters() {
    }

    public MainframeStorageParameters(String tempDASDVolumeIdentifier, String tempSMSStorageClass, String tempSMSManagementClass) {
        this.DASDVolumeIdentifier = tempDASDVolumeIdentifier;
        this.SMSStorageClass      = tempSMSStorageClass;
        this.SMSManagementClass   = tempSMSManagementClass;
    }

    public void setDASDVolumeIdentifier(String tempSetting) {
        DASDVolumeIdentifier = tempSetting;
    }

    public String getDASDVolumeIdentifier() {
        return insureQuotes(DASDVolumeIdentifier);
    }

    public void setSMSStorageClass(String tempSetting) {
        SMSStorageClass = tempSetting;
    }

    public void setSMSManagementClass(String tempSetting) {
        SMSManagementClass = tempSetting;
    }

    public String getSMSStorageClass() {
        return SMSStorageClass;
    }

    public String getSMSManagementClass() {
        return SMSManagementClass;
    }

    private boolean isBeenSet() {
        return ((getDASDVolumeIdentifier()!=null) || 
                     (getSMSStorageClass()!=null) ||
                     (getSMSManagementClass()!=null));
    }

    /**
     *  when dasd or any parm is set    , iSBeenSet is true. combinationAllowed will be true.
     *  when dasd or any parm is not set, isBeenSet is false, combinationAllowed will be true.
     * */
    public boolean unsetParametersExist() {
        return !(isBeenSet() && combinationAllowed());
    }

    private String insureQuotes(String settingToQuote) {
        if (settingToQuote != null && settingToQuote.trim().length() > 0) {
            if (!settingToQuote.startsWith("'")) {
                settingToQuote = "'"+settingToQuote;
            }
            if (!settingToQuote.endsWith("'")) {
                settingToQuote +="'"; /*** INT1765 **/
            }
        } else{
            settingToQuote = null;
        }
        return settingToQuote;
    }

    /**
     * A=VOLSER
     * B=SMSSTORAGE
     * C=SMSMGMT
     * */
    public boolean combinationAllowed() {
        //if tweaked by the user then we need to allow the valid combination.
        //if not just use the defaults.
        boolean isAllowStorageParameters = true;
        if (isBeenSet()) {
            if (getSMSStorageClass()!=null && getSMSStorageClass().trim().length() > 0) {
                boolean isDASDVolumeSet = false;
                if (getDASDVolumeIdentifier()!=null && getDASDVolumeIdentifier().trim().length() > 0) {
                    isDASDVolumeSet = true; //good condition  A & B are there.
                }

                if ((getSMSManagementClass()!=null && getSMSManagementClass().trim().length() > 0) &&  isDASDVolumeSet) { //bad condition A B C are there
                    isAllowStorageParameters = false;
                }
                // if we are here then we hit good condition B & C, good condition B
            } else {
                if ((getDASDVolumeIdentifier()!=null && getDASDVolumeIdentifier().trim().length() > 0)
                    || (getSMSManagementClass()!=null && getSMSManagementClass().trim().length() > 0)) { 
                    if (getSMSManagementClass()!=null && getSMSManagementClass().trim().length() > 0) { //bad condition C only
                        isAllowStorageParameters = false;
                    }

                    //good condition A only
                }

            } 
        }
        return isAllowStorageParameters;

    }

}
