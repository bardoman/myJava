package com.ibm.sdwb.build390.userinterface.text.commandline.process;

import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import com.ibm.sdwb.build390.userinterface.text.commandline.*;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.utilities.*;

//***********************************************************************
//11/06/2003 #DEF.TST1681: Command Line - LOGRETRIEVE ClassCastException
//11/07/2003 #DEF.TST1680: Command Line - LOGRETRIEVE - SENDTO should be invalid
//***********************************************************************

public class LogRetrieve extends CommandLineProcess {

    public static final String PROCESSNAME = "LOGRETRIEVE";

    private LibraryRelease libRelease = new LibraryRelease();
    private MainframeDriver driver = new MainframeDriver();
    private MultipleAssociatedCommandLineArgument partRetrievalInfo = new MultipleAssociatedCommandLineArgument();
    private DataRetrievalSendTo sendTo = new DataRetrievalSendTo();

    //Begin INT2395
    private MultipleAssociatedCommandLineArgument binaryColumns = new MultipleAssociatedCommandLineArgument();
    private DataRetrievalBinaryColumn binaryColumn = new DataRetrievalBinaryColumn();

    private MultipleAssociatedCommandLineArgument dependencyTypes = new MultipleAssociatedCommandLineArgument();
    private DataRetrievalDependencyType dependencyType = new DataRetrievalDependencyType();

    private MultipleAssociatedCommandLineArgument metadataTypes = new MultipleAssociatedCommandLineArgument();
    private DataRetrievalMetadataType metadataType = new DataRetrievalMetadataType();
    //End INT2395

    private LocalPath localPath = new LocalPath();
    private PDSSavePath pdsSave = new PDSSavePath();
    private HFSSavePath hfsSave = new HFSSavePath();
    //#DEF.TST1680:
    private boolean sendToInvalid = false;


    private static StringBuffer hostDataSetWarning = new StringBuffer();
    static {
        hostDataSetWarning.append("Warning!\n\n");
        hostDataSetWarning.append("Fully qualified OS/390 data set names must be used.\n");
        hostDataSetWarning.append("In case of dependency and metadata, a qualifier DEPXXXX for dependency (or) METXXXX for metadata\n");
        hostDataSetWarning.append("is appended to L254220.DEPEND.REPORT for sequential datasets;\n");
        hostDataSetWarning.append("member DEPXXXX and METXXXX is created in L254220.DEPEND.REPORT for partitioned datasets.\n");
        hostDataSetWarning.append("eg:\n");
        hostDataSetWarning.append("1.DSN:BINGO.TEST.REPORT is the user entry.\n");
        hostDataSetWarning.append("The output will be stored in BINGO.TEST.REPORT.DEPXXXX for sequential\n");
        hostDataSetWarning.append("and BINGO.TEST.REPORT(DEPXXXX) for partitioned, where XXXX are random nos.\n");
    }

    public LogRetrieve(LogEventProcessor tempLep, com.ibm.sdwb.build390.MBStatus tempStatus) {
        super(PROCESSNAME, tempLep, tempStatus);
    }

    public String getHelpDescription() {
        return "The "+getProcessTypeHandled() + " command pulls source, metadata or output for a\n"+
        "part from a driver on the server.";
    }

    public String getHelpExamples() {
        return "1.To retrieve SRC  and store it in localpath=C:\\src\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=SRC LOGCLASS1=<classOfPart>\n"+
        "      LOCALPATH=C:\\src\n"+
        "2.To retrieve  OBJ and store it in a PDS=<mypds>\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=OBJ LOGCLASS1=<classOfPart>\n"+
        "      DSNPATH=<mypds>\n"+
        "3.To retrieve LST and store it in hfspath=<myhfspath>\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=LST LOGCLASS1=<classOfPart>\n"+ 
        "      HFSPATH=<myhfspath>\n"+
        "4.To retrieve METADATA (default "+metadataType.getDefaultValue()+") \n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=METADATA LOGCLASS1=<classOfPart>\n"+ 
        "5.To retrieve METADATA of type SHORT and store it in a DSN=<mydsn>\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=LST LOGCLASS1=<classOfPart>\n"+ 
        "      METADATA1=SHORT DSN=<mydsn>\n"+
        "6.To retrieve DEPENDENCY (default " + dependencyType.getDefaultValue()+ ") \n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=DEPENDENCY LOGCLASS1=<classOfPart>\n"+ 
        "7.To retrieve DEPENDENCY of type USER \n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=DEPENDENCY LOGCLASS1=<classOfPart>\n"+ 
        "      DEPENDTYPE1=USER\n"+ 
        "8.To retrieve BINCOL (default " + binaryColumn.getDefaultValue() + ") \n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=BINCOL LOGCLASS1=<classOfPart>\n"+ 
        "      HFSPATH=<myhfspath>\n"+
        "9.To retrieve BINCOL of type YES\n"+
        getProcessTypeHandled()+" LIBRELEASE=<librelease> DRIVER=<driver>\n"+
        "      LOGNAME1=<partname> LOGTYPE1=LST LOGCLASS1=<classOfPart>\n"+ 
        "      BINCOL1=YES\n";


    }

    protected void setArgumentStructure(RequiredAndOptionalArguments argumentStructure) {
        BooleanAnd baseAnd = new BooleanAnd();
        baseAnd.addBooleanInterface(libRelease);
        baseAnd.addBooleanInterface(driver);

        partRetrievalInfo.addCommandLineArgument(new DataRetrievalPartName());
        partRetrievalInfo.addCommandLineArgument(new DataRetrievalType());
        partRetrievalInfo.addCommandLineArgument(new DataRetrievalPartClass());
        baseAnd.addBooleanInterface(partRetrievalInfo);
        argumentStructure.setRequiredPart(baseAnd);

        argumentStructure.addOption(sendTo);
        argumentStructure.addOption(localPath);

        //Begin INT2395
        BooleanOr baseOr = new BooleanOr();
        baseOr.addBooleanInterface(pdsSave);

        baseOr.addBooleanInterface(hfsSave);
        argumentStructure.addOption(baseOr);

        binaryColumns.addCommandLineArgument(binaryColumn);
        argumentStructure.addOption(binaryColumns);

        dependencyTypes.addCommandLineArgument(dependencyType);
        argumentStructure.addOption(dependencyTypes);

        metadataTypes.addCommandLineArgument(metadataType);
        argumentStructure.addOption(metadataTypes);
    }

    public void runProcess() throws MBBuildException{


        Map tempMap            = partRetrievalInfo.getIndexToArgumentsMap();
        Map bincolList         = binaryColumns.getIndexToArgumentsMap();
        Map dependencyTypeList = dependencyTypes.getIndexToArgumentsMap();
        Map metadataTypeList   = metadataTypes.getIndexToArgumentsMap();


        Set metadataReportsToGet = new HashSet();
        Set dependencyReportsToGet = new HashSet();
        Set partDataReportsToGet = new HashSet();

        TreeSet tempTree= new TreeSet(tempMap.keySet());

        for (Iterator Iterator0 = tempTree.iterator(); Iterator0.hasNext();) {
            String key = (String) Iterator0.next();

            Set oneArgumentSet = (Set) tempMap.get(key);
            String typeOfRetrieve = null;
            String retrievalClass = null;
            String retrievalName = null;
            for (Iterator argumentsWithinASetIterator = oneArgumentSet.iterator(); argumentsWithinASetIterator.hasNext();) {
                CommandLineArgument oneArg = (CommandLineArgument) argumentsWithinASetIterator.next();
                if (oneArg instanceof DataRetrievalPartName) {
                    retrievalName = oneArg.getValue();
                } else if (oneArg instanceof DataRetrievalPartClass) {
                    retrievalClass = oneArg.getValue();
                } else if (oneArg instanceof DataRetrievalType) {
                    typeOfRetrieve = oneArg.getValue();
                }
            }
            InfoForMainframePartReportRetrieval retrieveInfo = new InfoForMainframePartReportRetrieval(retrievalName,retrievalClass); 

            if (typeOfRetrieve.toUpperCase().startsWith("C")) {
                String temp = typeOfRetrieve.substring(1);

                try {
                    Integer.valueOf(temp).intValue();

                    CommandLineArgument oneArg = getArgumentAt(key,bincolList);
                    if (oneArg!=null) {
                        String value = oneArg.getValue();

                        if (value.toUpperCase().equals("YES")) {
                            retrieveInfo.setBinary(true);
                        }

                    }

                    retrieveInfo.setReportType(typeOfRetrieve);

                    partDataReportsToGet.add(retrieveInfo);

                } catch (NumberFormatException nfe) {
                }
            } else if (typeOfRetrieve.equals("METADATA")) {

                CommandLineArgument oneArg = getArgumentAt(key,metadataTypeList);
                if (oneArg!=null) {
                    retrieveInfo.setReportType(oneArg.getValue());
                } else {
                    //set the default type
                    retrieveInfo.setReportType(metadataType.getDefaultValue());
                }
                if (sendTo.isSatisfied()) {
                    sendToInvalid = true;
                }

                metadataReportsToGet.add(retrieveInfo);//TST2525
            } else if (typeOfRetrieve.equals("DEPENDENCY")) {
                CommandLineArgument oneArg = getArgumentAt(key,dependencyTypeList);//TST2525
                if (oneArg!=null) {
                    retrieveInfo.setReportType(oneArg.getValue());
                } else {
                    retrieveInfo.setReportType(dependencyType.getDefaultValue());
                }
                if (sendTo.isSatisfied()) {
                    sendToInvalid = true;
                }
                dependencyReportsToGet.add(retrieveInfo);//TST2525
            } else {
                retrieveInfo.setReportType(typeOfRetrieve);
                partDataReportsToGet.add(retrieveInfo);
            }

        }

        if (sendToInvalid == true) {
            System.out.println("Warning! The Send To option does NOT apply to metadata and dependency reports\n"+ 
                               "Your reports will be stored in "+MBGlobals.Build390_path+"logfiles"+java.io.File.separator);
        }

        Set returnedFiles = new HashSet();

        MBBuild build = new MBBuild(getLEP());


        ReleaseInformation releaseInfo = getReleaseInformation(libRelease.getValue(),build.getSetup(), true);
        DriverInformation driverInfo = getDriverInformation(driver.getValue(),releaseInfo,build.getSetup());

        build.setReleaseInformation(releaseInfo);
        build.setDriverInformation(driverInfo);

        if (!partDataReportsToGet.isEmpty()) {
            com.ibm.sdwb.build390.process.PartDataRetrieval partDataRetriever = new com.ibm.sdwb.build390.process.PartDataRetrieval(build,  partDataReportsToGet, this);
            if (localPath.isSatisfied() ) {
                partDataRetriever.setLocalSavePath(new java.io.File(localPath.getValue()));
            }

            if (hfsSave.isSatisfied()) {
                partDataRetriever.setHFSSavePath(hfsSave.getValue());
            }
            if (pdsSave.isSatisfied()) {
                partDataRetriever.setPDSSavePath(pdsSave.getValue());
            }
            if (sendTo.isSatisfied()) {
                partDataRetriever.setSendToAddress(sendTo.getValue());
            }

            setCancelableProcess(partDataRetriever);

            partDataRetriever.externalRun();
            returnedFiles.addAll(partDataRetriever.getLocalOutputFiles());
        }

        String localPathString = null;
        if (localPath.isSatisfied()) {
            localPathString = localPath.getValue();
        } else {
            localPathString = MBGlobals.Build390_path+"logfiles"+java.io.File.separator;
        }

        if (!metadataReportsToGet.isEmpty()) {
            com.ibm.sdwb.build390.process.MetadataReport metadataReportRetriever = new com.ibm.sdwb.build390.process.MetadataReport(build, new java.io.File(localPathString), metadataReportsToGet, this);
            if (hfsSave.isSatisfied()) {
                metadataReportRetriever.setHFSSavePath(hfsSave.getValue());
            }
            if (pdsSave.isSatisfied()) {
                System.out.println(hostDataSetWarning);
                metadataReportRetriever.setPDSSavePath(pdsSave.getValue());
            }
            setCancelableProcess(metadataReportRetriever);

            metadataReportRetriever.externalRun();
            returnedFiles.addAll(metadataReportRetriever.getLocalOutputFiles());
            if (!returnedFiles.isEmpty()) {
                String valueReportFilename = (String) returnedFiles.iterator().next();

                //Begin TST3036A
                if (metadataReportRetriever.getReturnCode() > 0) {
                    throw new HostError("There was an error retrieving a metadata report.", valueReportFilename.substring(0,valueReportFilename.indexOf(MBConstants.CLEARFILEEXTENTION)), MBConstants.HOSTERROR);
                }
                //End TST3036A

                for (Iterator iter=metadataReportRetriever.getLocalOutputFiles().iterator();iter.hasNext();) {
                    getStatusHandler().updateStatus("METADATA - The local output have been  saved in\n"+ (String)iter.next(),false);
                }

                for (Iterator iter=metadataReportRetriever.getHostSavedLocation().iterator();iter.hasNext();) {
                    getStatusHandler().updateStatus("METADATA - The report have been saved in the host as follows:\n"+ (String)iter.next(),false);
                }
            }

        }
        if (!dependencyReportsToGet.isEmpty()) {
            com.ibm.sdwb.build390.process.DependencyReport dependencyReportRetriever = new com.ibm.sdwb.build390.process.DependencyReport(build, new java.io.File(localPathString), dependencyReportsToGet, this);
            if (hfsSave.isSatisfied()) {
                dependencyReportRetriever.setHFSSavePath(hfsSave.getValue());
            }
            if (pdsSave.isSatisfied()) {
                System.out.println(hostDataSetWarning);
                dependencyReportRetriever.setPDSSavePath(pdsSave.getValue());
            }
            setCancelableProcess(dependencyReportRetriever);

            dependencyReportRetriever.externalRun();
            returnedFiles.addAll(dependencyReportRetriever.getLocalOutputFiles());

            for (Iterator iter=dependencyReportRetriever.getLocalOutputFiles().iterator();iter.hasNext();) {
                getStatusHandler().updateStatus("DEPENDENCY - The local output have been saved in\n"+ (String)iter.next(),false);
            }

            for (Iterator iter=dependencyReportRetriever.getHostSavedLocation().iterator();iter.hasNext();) {
                getStatusHandler().updateStatus("DEPENDENCY - The report have been saved in the host as follows:\n"+ (String)iter.next(),false);
            }

        }
        //End #DEF.TST1680:
    }

    private CommandLineArgument getArgumentAt(String index,Map indexToArgMap) {
        if (indexToArgMap.containsKey(index)) {
            Set argsSet = (Set)indexToArgMap.get(index);
            if (!argsSet.isEmpty()) {
                return(CommandLineArgument)argsSet.iterator().next();
            }
        }
        return null;
    }


    private void printOutFileLocations(Set fileSet) {
        for (Iterator fileIterator = fileSet.iterator(); fileIterator.hasNext();) {
            String currentFile = (String) fileIterator.next();
            MBEdit edit = new MBEdit(currentFile,getLEP());
        }
    }
}
