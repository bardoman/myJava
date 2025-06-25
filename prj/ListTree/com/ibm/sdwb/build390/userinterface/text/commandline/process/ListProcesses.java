package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBBuildLoader;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBUBuild;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.text.commandline.RequiredAndOptionalArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.CompletionState;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.Delimiter;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.ListProcessSortType;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.NoArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.ProcessType;
import com.ibm.sdwb.build390.utilities.MultipleConcurrentException;
import com.ibm.sdwb.build390.utilities.SerializedBuildsLister;
import com.ibm.sdwb.build390.utilities.process.BuildDirectoriesFilter;
import com.ibm.sdwb.build390.utilities.process.FormattedDateAdapter;

public class ListProcesses extends CommandLineProcess {
    private String typeHeader="Type";
    private String buildIdHeader="BuildID";
    private String releaseHeader="Release";
    private String driverHeader="Driver";
    private String dateTimeHeader="Date/Time";
    private String descriptionHeader="Description";

    private String dateString = "MM/dd/yy hh:mm aaa";
    private SimpleDateFormat formatter = new java.text.SimpleDateFormat(dateString);
    public static final String PROCESSNAME = "LISTPROCESSES";
    private ProcessType type = new ProcessType();
    private CompletionState completed = new CompletionState();
    private ListProcessSortType sort = new ListProcessSortType();
    private File build390HomeDirectory = new File(MBGlobals.Build390_path);
    private static final String buildIdColumnName = "BuildID";
    private Delimiter delimiter= new Delimiter();
    private String displayDelimiter=" ";

    public ListProcesses(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return getProcessTypeHandled()+ 
        " command searches the tree of the Build/390  \n"+
        "installation directory for a list of available processes.\n";
    }

    public String getHelpExamples() {
        return getProcessTypeHandled()+" PROCESSTYPE=<process type> ";
    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        argumentStructure.setRequiredPart(new NoArguments());

        argumentStructure.addOption(type);

        argumentStructure.addOption(completed);

        argumentStructure.addOption(sort);

        argumentStructure.addOption(delimiter);

    }

    public void runProcess() throws MBBuildException     { 

        if (delimiter.isSatisfied()) {
            displayDelimiter = delimiter.getValue();
        }


        com.ibm.sdwb.build390.utilities.MultipleConcurrentException allExceptions = new com.ibm.sdwb.build390.utilities.MultipleConcurrentException("Exceptions encountered during loading builds:!"); /*TST1828 */

        java.util.List list = new java.util.ArrayList();

        java.util.List buildFilesList = SerializedBuildsLister.getInstance(build390HomeDirectory).listBUILDFilesTree(build390HomeDirectory.getAbsolutePath(),null);


        for (Iterator iter = buildFilesList.iterator(); iter.hasNext();) {
            File file = (File)iter.next();
            try {
                MBBuild loadedBuild  = MBBuildLoader.loadBuild(file);
                if (loadedBuild!=null && SerializedBuildsLister.BUILD_DISPLAY_CRITERIA.passes(loadedBuild)) {
                    //Begin handling the completed parm function 
                    boolean consolePrint =true;
                    if (completed.isSatisfied()) {
                        if (completed.getValue().equals("TRUE")) {
                            consolePrint = loadedBuild.getProcessForThisBuild().hasCompletedSuccessfully();
                        } else if (completed.getValue().equals("FALSE")) {
                            consolePrint = !loadedBuild.getProcessForThisBuild().hasCompletedSuccessfully();
                        }
                    }
                    //End handling the completed parm function 


                    //Begin handling the processtype parm function 
                    if (consolePrint) {
                        if (!type.getValue().toUpperCase().equals("ALL")) {
                            if (type.getValue().toUpperCase().equals("DRIVERBUILD")) {
                                if (getBuildType(loadedBuild).equals("Driver")) {
                                    list.add(loadedBuild);
                                }
                            } else if (type.getValue().toUpperCase().equals("USERBUILD")) {
                                if (getBuildType(loadedBuild).equals("User")) {
                                    list.add(loadedBuild);
                                }

                            } else if (type.getValue().toUpperCase().equals("USERMOD")) {
                                if (getBuildType(loadedBuild).equals("Usermod")) {
                                    list.add(loadedBuild);
                                }
                            }
                        } else {
                            list.add(loadedBuild);
                        }
                    }
                    //End handling the processtype parm function 

                }

            } catch (MBBuildException mbe) {
                allExceptions.addException(mbe);

            }
        }




        //Begin handling the sort parm function 
        String sortKey = sort.getValue().toUpperCase();

        if (sortKey.equals("PROCESSID")) {
            ProcIDComparator procComp = new ProcIDComparator();

            Collections.sort(list, procComp);
        } else if (sortKey.equals("TIME")) {
            TimeComparator timeComp = new TimeComparator();

            Collections.sort(list, timeComp);
        }
        //End handling the sort parm function 

        printList(list);

        if (!allExceptions.getExceptionSet().isEmpty()) {
            throw allExceptions;
        }
    }

    void printList(List list) {
        ArrayList types = new ArrayList();
        ArrayList ids = new ArrayList();
        ArrayList releases = new ArrayList();
        ArrayList drivers = new ArrayList();
        ArrayList dateTimes = new ArrayList();
        ArrayList description = new ArrayList();
        int typeWidth=0;
        int idWidth=0;
        int releaseWidth=0;
        int driverWidth=0;
        int dateTimeWidth=0;
        int descriptionWidth=0;


        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            MBBuild loadedBuild = (MBBuild) iter.next();

            types.add(getBuildType(loadedBuild));

            ids.add(loadedBuild.get_buildid());

            if (loadedBuild.getReleaseInformation()!=null) {
                releases.add(loadedBuild.getReleaseInformation().getLibraryName());
            } else {
                releases.add(new String());
            }

            //  releases.add();
            if (loadedBuild.getDriverInformation()!=null) {
                drivers.add(loadedBuild.getDriverInformation().getName());
            } else {
                drivers.add(new String());
            }

            if (loadedBuild.get_date()!=null) {
                FormattedDateAdapter dateAdapter = new FormattedDateAdapter(loadedBuild.get_date());

                dateAdapter.setDateFormatter(formatter);

                dateTimes.add(dateAdapter.toString());
            }

            description.add(loadedBuild.get_descr());
        }

        typeWidth=getColmnWidth(types,typeHeader);
        idWidth=getColmnWidth(ids,buildIdHeader);
        releaseWidth=getColmnWidth(releases,releaseHeader);
        driverWidth=getColmnWidth(drivers,driverHeader);
        dateTimeWidth=getColmnWidth(dateTimes,dateTimeHeader);
        descriptionWidth=getColmnWidth(description,descriptionHeader);

        printColumnItem(typeHeader, typeWidth);
        printColumnItem(buildIdHeader, idWidth);
        printColumnItem(releaseHeader, releaseWidth);
        printColumnItem(driverHeader, driverWidth);
        printColumnItem(dateTimeHeader, dateTimeWidth);
        printColumnItem(descriptionHeader, descriptionWidth);

        System.out.println();

        for (int i=0; i!=list.size();i++) {

            //for(Iterator iter = list.iterator(); iter.hasNext(); ) {
            printColumnItem((String)types.get(i), typeWidth);

            printColumnItem((String)ids.get(i), idWidth);

            printColumnItem((String)releases.get(i), releaseWidth);

            printColumnItem((String)drivers.get(i), driverWidth);

            printColumnItem((String)dateTimes.get(i), dateTimeWidth);

            printColumnItem((String)description.get(i), descriptionWidth);

            System.out.println();
        }
    }

    int getColmnWidth(List list,String header) {
        int width = header.length();

        for (Iterator iter = list.iterator(); iter.hasNext();) {

            Object obj = iter.next();

            if (obj!=null) {
                int tempWidth = ((String)obj).length();

                if (tempWidth > width) {
                    width = tempWidth;
                }

            }
        }

        return width;

    }

    void printColumnItem(String str, int width) {
        int delta=0;
        String tempStr = "";

        if (str!=null) {
            delta=width-str.length();

            for (int i=0;i!=delta;i++) {
                tempStr+=" ";
            }
            if (str!=null) {
                System.out.print(str + displayDelimiter + tempStr+" ");
            }

        } else {
            System.out.print(displayDelimiter +tempStr+" ");
        }
    }

    String getBuildType(Object obj) {
        if (obj instanceof MBUBuild) {
            return "User";
        }

        if (obj instanceof com.ibm.sdwb.build390.info.UsermodGeneralInfo) {
            return "Usermod";
        }

        if (obj instanceof MBBuild) {
            return "Driver";
        }

        return "Unknown";
    }

    private class TimeComparator implements Comparator {
        public int compare(Object obj1, Object obj2)
        throws ClassCastException
        {
            MBBuild b1 = (MBBuild) obj1;

            MBBuild b2 = (MBBuild) obj2;

            Date d1 = b1.get_date();

            Date d2 = b2.get_date();

            return d1.compareTo(d2);
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    private class ProcIDComparator implements Comparator {
        public int compare(Object obj1, Object obj2)
        throws ClassCastException
        {   
            int i=0;

            MBBuild b1 = (MBBuild) obj1;

            MBBuild b2 = (MBBuild) obj2;

            String s1 = b1.get_buildid();

            String s2 = b2.get_buildid();

            return s1.compareTo(s2);
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

}




