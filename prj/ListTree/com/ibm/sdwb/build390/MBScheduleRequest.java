package com.ibm.sdwb.build390;
/*****************************************************************/
/*                                      */
/*                                                               */
// Changes
// Date     Defect/Feature      Reason
// 6/3/99   EmptyQueueError     When the queue was empty, it would crash.  This is fixed.
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/*********************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.user.Setup;

/** <br>The MBScheduleRequest class provides parsing for a driver report.*/
public class MBScheduleRequest implements MBStop {

    private Setup setup;
    private String clrout;
    private boolean stopped = false;
    public static final String QUERY = "QUERY";
    public static final String ADD = "ADD";
    public static final String REMOVE = "REMOVE";
    public static final String DEACTIVATE = "DEACTIVATE";
    public static final String REACTIVATE = "REACTIVATE";
    public static final String REPLACE = "REPLACE";
    private static final String NOJOBSFOUND = "There are no scheduler Events";
    private String requestType = null;
    private Vector queryJobVector = new Vector();
    private MBSocket mySock;
    private MBStatus status;
    private Hashtable argHash;
    private LogEventProcessor lep=null;

    /** @param path to the driver report */
    public MBScheduleRequest (Setup tempSetup, String tempRequest, MBStatus tempStatus, Hashtable tempArgHash,LogEventProcessor lep) {
        setup = tempSetup;
        status = tempStatus;
        requestType = tempRequest;
        argHash = tempArgHash;
        this.lep=lep;
        clrout = MBGlobals.Build390_path+"misc"+File.separator+MBConstants.SCHEDULEFILE+requestType;
    }

    public void doRequest() throws com.ibm.sdwb.build390.MBBuildException {
        // build command
        String cmd = requestType;
        // Submit the command
        String StatusMsg = "Submitting a "+requestType+ " to the scheduler";;
        try {
            if (!stopped) {
                if (requestType.equals(QUERY)) {
                    mySock = new MBSocket(cmd, clrout, StatusMsg, status, setup.getMainframeInfo(),lep);
                    mySock.setScheduler();
                    mySock.run();
                    parseQueryFile(mySock.getClearPrint());
                } else if (requestType.equals(ADD) | requestType.equals(REPLACE)) {
                    cmd +=  " EVENT="+(String) argHash.get("EVENT") +
                            ", INTERVAL="+(String) argHash.get("INTERVAL")+", START="+(String) argHash.get("START")+
                            ", STOP="+(String) argHash.get("STOP");
                    Object testObject;
                    if ((testObject = argHash.get("GROUP")) != null) {
                        if (((String) testObject).trim().length() > 0) {
                            cmd += ", GROUP="+(String) testObject;
                        }
                    }
                    if ((testObject = argHash.get("CC")) != null) {
                        if (((String) testObject).trim().length() > 0) {
                            cmd += ", CC="+(String) testObject;
                        }
                    }
                    if ((testObject = argHash.get("STEP")) != null) {
                        if (((String) testObject).trim().length() > 0) {
                            cmd += ", STEP="+(String) testObject;
                        }
                    }
                    if ((testObject = argHash.get("CONTACTS")) != null) {
                        Vector testVect = (Vector) testObject;
                        if (testVect.size() > 0) {
                            String contactString = "(";
                            for (int i = 0; i < testVect.size(); i++) {
                                contactString += (String) testVect.elementAt(i);
                                if (i < (testVect.size() - 1)) {
                                    contactString += ", ";
                                }
                            }
                            contactString += ")";
                            cmd += ", CONTACT="+contactString;
                        }
                    }
                    if ((testObject = argHash.get("RUNDAYS")) != null) {
                        Vector testVect = (Vector) testObject;
                        if (testVect.size() > 0) {
                            String rundayString = "(";
                            for (int i = 0; i < testVect.size(); i++) {
                                rundayString += (String) testVect.elementAt(i);
                                if (i < (testVect.size() - 1)) {
                                    rundayString += ", ";
                                }
                            }
                            rundayString += ")";
                            cmd += ", RUNDAYS="+rundayString;
                        }
                    }
                    mySock = new MBSocket(cmd, clrout, StatusMsg, status, setup.getMainframeInfo(),lep);
                    mySock.setScheduler();
                    mySock.setPathToVerbFile((String) argHash.get("DSN"));
                    mySock.run();
                } else {
                    cmd += " GROUP="+(String) argHash.get("GROUP")+", EVENT="+(String) argHash.get("EVENT");
                    mySock = new MBSocket(cmd, clrout, StatusMsg, status, setup.getMainframeInfo(),lep);
                    mySock.setScheduler();
                    mySock.run();
                }
            }
        } catch (IOException ioe) {
            throw new HostError("There was an error parsing the returned file", ioe, clrout);
        }
    }

    private void parseQueryFile(String queryFile) throws IOException, HostError{
        String currentLine = null;
        BufferedReader queryReader = new BufferedReader(new StringReader(queryFile));
        while ((currentLine = queryReader.readLine()) != null) {                                                                     // Ken 6/3/99   Just added this to handle an empty queue
            if (currentLine.trim().startsWith(NOJOBSFOUND)) {
                throw new HostError("No jobs were found in the queue.", new Exception());
            }
            String secondLine = queryReader.readLine();
            if (secondLine != null) {
                currentLine+= secondLine;
            }
            if (currentLine.trim().length() > 0) {
                MBSchedulerInfo tempInfo = parseQueryLine(currentLine);
                queryJobVector.addElement(tempInfo);
            }
        }
    }

    private MBSchedulerInfo parseQueryLine(String queryLine) {
        MBSchedulerInfo info = new MBSchedulerInfo();
        StringTokenizer reportLineParser = new StringTokenizer(queryLine);
        if (reportLineParser.nextToken().equals("1")) {
            info.groupName=reportLineParser.nextToken();
        }
        info.eventName=reportLineParser.nextToken();
        info.interval=reportLineParser.nextToken();
        info.start=reportLineParser.nextToken();
        info.stop=reportLineParser.nextToken();
        for (int i = 0 ; i < 7; i++) {
            info.daysToRun[i]=reportLineParser.nextToken().equals("1");
        };
        info.conditionCode=reportLineParser.nextToken();
        info.doConditionCheck = reportLineParser.nextToken().equals("1");
        info.active=reportLineParser.nextToken().equals("0");
        if (reportLineParser.nextToken().equals("1")) {
            info.stepName=reportLineParser.nextToken();
        }
        info.dataSetName=reportLineParser.nextToken();
        info.dateAdded=reportLineParser.nextToken();
        info.dateChanged=reportLineParser.nextToken();
        if (reportLineParser.nextToken().equals("1")) {
            String contactList = reportLineParser.nextToken();
            if (contactList.indexOf(",")>-1) {
                StringTokenizer contactParser = new StringTokenizer(contactList.trim(), ",");
                String nextContact;
                while (contactParser.hasMoreTokens()) {
                    info.contactList.addElement(contactParser.nextToken());
                }
            } else {
                info.contactList.addElement(contactList);
            }
        }
        return info;
    }


    public Vector getSchedule() {
        return queryJobVector;
    }

    public String toString() {
        return queryJobVector.toString();
    }

    public void stop() throws com.ibm.sdwb.build390.MBBuildException{
        stopped = true;
        if (mySock != null) mySock.stop();
    }
}
