package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import java.util.*;
import java.io.*;

public class DriverCreationParameterReport extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private boolean checkSuceeded = false;
    private String MVSHighLevelQualifier = null;
    private String MVSReleaseName = null;
    private String driver = null;
    private Map settingMap = null;
    private File alternateSaveLocation = null;

    public DriverCreationParameterReport(MBBuild tempBuild, String tempMVSHLQ, String tempMVSRelease, String tempDriver, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempBuild.getBuildPath()+tempMVSRelease+"-"+tempDriver +"-DriverCreationParameterReport","Driver Creation Parameter Report", tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        build = tempBuild;
        MVSHighLevelQualifier = tempMVSHLQ;
        MVSReleaseName = tempMVSRelease;
        driver = tempDriver;
    }

    public boolean isCheckSuccessful(){
        return checkSuceeded;
    }

    public Map getSettingMap(){
        return new HashMap(settingMap);
    }

    public void setAlternativeSaveLocation(File tempLocation){
        alternateSaveLocation = tempLocation;
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

        String driverParameterReport = "CPYDB DRVRDB OP=CHECK CHILVL="+MVSHighLevelQualifier+
                                       " CRELEASE="+MVSReleaseName+" CDRIVER="+driver;
        if (alternateSaveLocation!=null) {
            setOutputHeaderLocation(alternateSaveLocation.getAbsolutePath()+java.io.File.separator + MVSReleaseName+"-"+driver +"-DriverCreationParameterReport");
        }

        createMainframeCall(driverParameterReport, "Getting base driver parameters", build.getSetup().getMainframeInfo());
        setTSO();
        setSystsprt(); // return SYSTSPRT data as CLRPRINT data
        runMainframeCall();
        if (getReturnCode()!=0) {
            checkSuceeded = false;
            return;
        }
        parseResultFile(getPrintFile());
    }

    private void parseResultFile(File reportLocation) throws com.ibm.sdwb.build390.MBBuildException {
        settingMap = new HashMap();
        try {
            BufferedReader ResultFileReader = new BufferedReader(new FileReader(reportLocation));
            String currentLine = new String();
            String alllines = new String();
            while ((currentLine=ResultFileReader.readLine())!=null) {
                StringTokenizer st = new StringTokenizer(currentLine);
                if (st.hasMoreTokens()) {
                    String key = st.nextToken();
                    if (key.equals("Bankused:")) {
                        if (st.hasMoreTokens()) {
                            String temp = st.nextToken();
                            if (temp.trim().equals("0")) temp = "1";
                            settingMap.put("CUBKP", temp);
                        }
                    } else if (key.equals("Bulkcyl:")) {
                        if (st.hasMoreTokens()) {
                            String temp = st.nextToken();
                            if (temp.trim().equals("0")) temp = "1";
                            settingMap.put("BULKCYL", temp);
                        }
                    } else if (key.equals("Pricyl:") & st.hasMoreTokens()) {
                        settingMap.put("CBLKP", st.nextToken());
                    } else if (key.equals("Seccyl:") & st.hasMoreTokens()) {
                        settingMap.put("CBLKS", st.nextToken());
                    } else if (key.equals("Maxcyl:") & st.hasMoreTokens()) {
                        settingMap.put("CMAXCYL", st.nextToken());
                    } else if (key.equals("Maxext:") & st.hasMoreTokens()) {
                        settingMap.put("CMAXEXT", st.nextToken());
                    } else if (key.equals("Stgcls:") & st.hasMoreTokens()) {
                        settingMap.put("CSTGCLS", st.nextToken());
                    } else if (key.equals("Mgtcls:") & st.hasMoreTokens()) {
                        settingMap.put("CMGTCLS", st.nextToken());
                    } else if (key.equals("Volid:") & st.hasMoreTokens()) {
                        settingMap.put("CVOLID", st.nextToken());
                    } else if (key.equals("DriverSizes:")) {
                        int cnt = 0;
                        boolean datamissing = false;
                        boolean continueLooping = true;
                        while ((currentLine = ResultFileReader.readLine()) != null & continueLooping) {
                            StringTokenizer stn = new StringTokenizer(currentLine, "=(),");
                            if (stn.hasMoreTokens()) {
                                String keyn = stn.nextToken().trim();
                                if (!keyn.equals("DriverSizesEnd:")) {
                                    cnt++;
                                    // get size info
                                    if (stn.countTokens()==4) {
                                        settingMap.put("SIZE"+cnt+"_NAME", keyn);
                                        settingMap.put("SIZE"+cnt+"_UBKP", stn.nextToken());
                                        settingMap.put("SIZE"+cnt+"_BLKP", stn.nextToken());
                                        settingMap.put("SIZE"+cnt+"_BLKS", stn.nextToken());
                                    } else datamissing=true;
                                } else {
                                    continueLooping = false;
                                }
                            }
                        }
                        if (datamissing) {
                            new MBMsgBox("Warning", "The driver size defaults are not defined in the LIBORDER, using system defaults.");
                        }
                    }
                }
            }
            ResultFileReader.close();
            checkSuceeded = true;
        } catch (IOException ioe) {
            throw new GeneralError("IOException reading file "+reportLocation, ioe);
        }
    }

    }
