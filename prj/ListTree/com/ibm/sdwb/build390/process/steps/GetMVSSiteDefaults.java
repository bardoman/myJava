package com.ibm.sdwb.build390.process.steps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.GeneralError;
import com.ibm.sdwb.build390.HostError;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.mainframe.ReleaseAndDriverParameters;
import com.ibm.sdwb.build390.user.Setup;

public class GetMVSSiteDefaults extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private static Map siteDefaultMap = new HashMap();
    private Setup setup = null;
    private static final String DEFAULTS = new String(".shadowdef.host");

    public GetMVSSiteDefaults(Setup tempSetup, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(MBGlobals.Build390_path+"misc"+java.io.File.separator+getUniqueHostKey(tempSetup.getMainframeInfo().getMainframeAddress().toLowerCase(), tempSetup.getMainframeInfo().getMainframePort())+ DEFAULTS,"Get MVS Site Defaults", tempProc);
        setVisibleToUser(false);
        setUndoBeforeRerun(false);
        setup = tempSetup;
    }

    public ReleaseAndDriverParameters getDefaultSettingsForMVS() {
        return(ReleaseAndDriverParameters) siteDefaultMap.get(getUniqueHostKey(setup.getMainframeInfo().getMainframeAddress(),setup.getMainframeInfo().getMainframePort()));
    }

    /**
     * This is the method that should be implemented to actually
     * run the process.	Use executionArgument if you need to 
     * access the argument from the execute method.
     * 
     * @return Object indicating output of the step.
     */
    public void execute() throws com.ibm.sdwb.build390.MBBuildException{
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        MBMainframeInfo mainInfo = setup.getMainframeInfo();
        String uniqueHostKey = getUniqueHostKey(mainInfo.getMainframeAddress().toLowerCase(), mainInfo.getMainframePort());
        synchronized (siteDefaultMap) {
            ReleaseAndDriverParameters defaultSettings = (ReleaseAndDriverParameters) siteDefaultMap.get(uniqueHostKey);
            if (defaultSettings ==null) {
                createMainframeCall("B390DFLT", "Getting defaults from host", setup.getMainframeInfo());
                runMainframeCall();
                try {
                    BufferedReader siteDefaultReader = new BufferedReader(new FileReader(getOutputFile()));
                    defaultSettings = new ReleaseAndDriverParameters();
                    parseSiteDefaultReport(siteDefaultReader, defaultSettings);
                    siteDefaultReader.close();
                    String errorsFound = getErrorInfo(defaultSettings).trim();
                    if (errorsFound.length() > 0) {
                        throw new GeneralError("These errors were found in the B390DFLT file.\n"+errorsFound+"\nContact your administrator to have the B390DFLT verb fixed.");
                    } else {
                        siteDefaultMap.put(uniqueHostKey, defaultSettings);
                    }
                } catch (IOException ioe) {
                    throw new HostError("An error occurred reading the B390DLFT file.", ioe);
                }
            }
        }
    }

    private void parseSiteDefaultReport(BufferedReader siteDefaultReader, ReleaseAndDriverParameters settingMap) throws IOException, GeneralError {
        String currentLine = null;
        while ((currentLine = siteDefaultReader.readLine()) != null) {
            int equalIndex = currentLine.indexOf("=");
            if ( equalIndex> 0) {
                String key = currentLine.substring(0,equalIndex).trim();
                String value = currentLine.substring(equalIndex+1).trim();
                settingMap.setValueFromMainframeSetting(key,value);
            } else if (currentLine.trim().startsWith("DRIVER SIZES:")) {
                Set sizes = new HashSet();
                // read size lines untill null or a line that does not start with 2 blanks
                while (currentLine != null) {
                    if ((currentLine = siteDefaultReader.readLine()) != null) {
                        if (!currentLine.startsWith(" ")) {
                            siteDefaultReader.reset();              //reset the file position and get out
                            currentLine = null;
                        } else {
                            siteDefaultReader.mark(1000);           // save the file posistion
                            sizes.add(currentLine.trim());
                        }
                    }
                }
                settingMap.setDriverSizes(sizes);
            }
        }
    }

    private static String getUniqueHostKey(String mvsAddress, String mvsPort) {
        return mvsAddress.toLowerCase()+"@"+mvsPort;
    }

    private String getErrorInfo(ReleaseAndDriverParameters settingMap) {
        String errorInfo = new String();

        if (!settingMap.getDriverSizes().isEmpty()) {
            errorInfo += checkType((String)settingMap.getDriverSizes().iterator().next(), false, "SIZES");
        } else {
            errorInfo += checkType(null, false, "SIZES");
        }
        errorInfo += checkType(settingMap.getShadowBulkDatasetPrimarySpaceInCylinders(), true, "SHADBP");
        errorInfo += checkType(settingMap.getShadowBulkDatasetSecondarySpaceInCylinders(), true, "SHADBS");
        errorInfo += checkType(settingMap.getShadowUnibankDatasetPrimarySpaceInCylinders(), true, "SHADUP");
        errorInfo += checkType(settingMap.getDriverBulkDatasetPrimarySpaceInCylinders(), true, "DRVRBP");
        errorInfo += checkType(settingMap.getDriverBulkDatasetSecondarySpaceInCylinders(), true, "DRVRBS");
        errorInfo += checkType(settingMap.getDriverUnibankDatasetPrimarySpaceInCylinders(), true, "DRVRUP");
        errorInfo += checkType(settingMap.getBulkDatasetMaximumSizeInCylinders(), true, "MAXCYL");
        errorInfo += checkType(settingMap.getBulkDatasetMaximumExtentsInCylinders(), true, "MAXEXT");
        errorInfo += checkType(settingMap.getAdditionalCollectors(), true, "COLECTRS");
        errorInfo += checkType(settingMap.getAdditionalProcessSteps(), true, "PROCESES");
        return errorInfo;
    }

    private String checkType(int intValue, boolean isType, String keyType) {
        String value = Integer.toString(intValue);
        return checkType(value,isType,keyType);
    }

    private String checkType(String value, boolean isType, String keyType) {
        String returnString = new String();
        final String text1 = ".Should be an Integer" + "\n";
        final String text2 = ".Should be a word(alpha)" + "\n";
        final String text3 = "-NULL. Should be an Integer" + "\n";
        final String text4 = "-NULL. Should be a word " + "\n";

        if (value.length() == 0) {
            if (isType) {
                returnString += keyType + text3;
            } else {
                returnString += keyType + text4;
            }
        }

        if (isType) {
            if (!(isNumber(value))) {
                returnString += keyType + "=" + value + text1;
            }
        } else {
            if (!(isAlpha(value))) {
                returnString += keyType + "=" + value + text2;
            }
        }
        return returnString;
    }


    private boolean isNumber(String value) {

        boolean isValid = true;
        char ch;
        int i=0;
        try {

            while ((ch = value.charAt(i)) != ' ') {
                if (!(Character.isDigit(ch))) {
                    isValid = false;
                    break;
                }
                i++;
            }
            return isValid;
        } catch (Exception e) {
            return isValid;
        }
    }

    private boolean isAlpha(String value) {

        boolean isValid = true;
        char ch;
        int i=0;
        try {

            while ((ch = value.charAt(i)) != ' ') {
                if (!(Character.isLetter(ch))) {
                    isValid = false;
                    break;
                }
                i++;
            }
            return isValid;
        } catch (Exception ie) {
            return isValid;
        }
    }
}
