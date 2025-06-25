package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.info.InfoForMainframePartReportRetrieval;
import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.process.AbstractProcess;

//***********************************************************
//04/16/2003 #Def.INT1161:  MetadataReport fails on TYPE=FIELDS
//***********************************************************

public class MetadataReport extends MainframeCommunication {
    static final long serialVersionUID = 1111111111111111L;

    private MBBuild build = null;
    private File localSavePath = null;
    private Set localSavedFiles = null;
    private String buildLevel = null;
    private Set partInfoSet = null;
    private boolean justGetFields = false;
    private MetadataType metadataTypes[] = null;

    private String HFSSavePath = null;
    private String dataSetName = null;
    private Set hostSavedFiles = null;
    private boolean isPartExtensionAsType = false;
    private transient java.util.Random randomSource = new java.util.Random();


    public MetadataReport(MBBuild tempBuild, java.io.File tempLocalSavePath,  Set tempPartInfoMap, AbstractProcess tempProc) {
        super(null,"Metadata Report",tempProc);
        setVisibleToUser(true);
        setUndoBeforeRerun(true);
        build = tempBuild;
        partInfoSet = tempPartInfoMap;
        localSavePath=tempLocalSavePath;
    }

    public void setBuildLevel(String tempBuildLevel) {
        buildLevel = tempBuildLevel;
    }

    public void setJustGetFields(boolean temp) {
        justGetFields = temp;
    }

    public MetadataType[] getMetadataTypes() {
        return metadataTypes;
    }

    public void setHFSSavePath(String tempPath) {
        HFSSavePath = tempPath;
    }

    public void setPDSSavePath(String tempPath) {
        dataSetName = tempPath;
    }
    public Set getLocalOutputFiles() {
        return localSavedFiles;
    }

    public Set getHostSavedLocation() {
        return hostSavedFiles;
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

        final File commandOutputPath;
        if(localSavePath==null) {
            commandOutputPath = new File(MBGlobals.Build390_path+"logfiles"+File.separator);
        }
        else {
            commandOutputPath = localSavePath;
        }

        localSavedFiles=new HashSet();
        hostSavedFiles = new HashSet();
        final LibraryInfo libInfo = build.getSetup().getLibraryInfo();

        //Begin INT2395
        if(justGetFields) {
            try {

                String cmd_ = "XMETARPT "+libInfo.getDescriptiveStringForMVS()+
                              ", CMVCREL='"+build.getReleaseInformation().getLibraryName()+"', TYPE=FIELDS, VERBNAME=YES";

                setOutputHeaderLocation(commandOutputPath.getAbsolutePath()+File.separator+"METAFIELDS");
                createMainframeCall(cmd_, "Running Metadata Report(TYPE=FIELDS)", build.getMainframeInfo());

                runMainframeCall();
                String localName = getOutputFile().getAbsolutePath();

                localSavedFiles.add(localName); /*PTM3531 */

                boolean isValidFile = (new File(localName)).exists() && (new File(localName)).length() > 0;

                //Begin TST3016
                if((dataSetName != null) &&  localName !=null && isValidFile) {
                    uploadFileToHost(localName,dataSetName);
                }
                if((HFSSavePath!=null) &&  localName !=null && isValidFile) {
                    uploadFileToHost(localName,HFSSavePath);
                }
                //End TST3016

                metadataTypes = parse(localName);

            }
            catch(MBBuildException mbe) {
                getLEP().LogException(mbe);
            }
        }
        else {
            for(Iterator partIterator = partInfoSet.iterator(); partIterator.hasNext();) {
                final InfoForMainframePartReportRetrieval partInfo = (InfoForMainframePartReportRetrieval) partIterator.next();
                final String metadataReportType = partInfo.getReportType();

                //try {
                    String cmd_ =
                    "XMETARPT "+libInfo.getDescriptiveStringForMVS()+
                    ", CMVCREL='"+build.getReleaseInformation().getLibraryName()+"', DRIVER="+build.getDriverInformation().getName();


                    String clrout_ = commandOutputPath.getAbsolutePath()+File.separator; 

                    cmd_ += partInfo.getPartName()!=null ? ", MOD="+partInfo.getPartName()+", CLASS="+partInfo.getPartClass() : ", DIR='"+partInfo.getDirectory() +"', PATH='"+partInfo.getName()+"'";

                    cmd_ += ", TYPE="+metadataReportType;

                    clrout_ += partInfo.getPartName()!=null ?(partInfo.getPartName() + "."+partInfo.getPartClass()) : (partInfo.getDirectory() +  partInfo.getName()).replace('/','-');

                    clrout_ +="-METADATA-"+metadataReportType;

                    //Begin TST3337

                    boolean isFakeLib = MBClient.getCommandLineSettings().getMode().isFakeLibrary();

                    if(isFakeLib) {

                        if(partInfo.getIsPDS()==InfoForMainframePartReportRetrieval.IS_PDS) {
                            cmd_+=", PDSPART=YES";
                        }

                        cmd_+= ", NOLIB=YES";
                    }
                    //End TST3337


                    if(buildLevel != null) {
                        cmd_ += ", BLDLVL="+buildLevel;
                    }

                    setOutputHeaderLocation(clrout_);
                    createMainframeCall(cmd_, "Running Metadata Report(METADATA=" + metadataReportType+") ,"+ (partInfo.getPartName()!=null ? partInfo.getPartName()+"."+partInfo.getPartClass() : ""), true, build.getMainframeInfo());
                    runMainframeCall();

                    String localName = getOutputFile().getAbsolutePath();

                    boolean isValidFile = (new File(localName)).exists() && (new File(localName)).length() > 0;
                    String uploadName = null;
                    String dispName =null;

                    //Begin TST3016
                    if((dataSetName != null) &&  localName !=null && isValidFile) {
                        String lastQualifier = "MET" + (new String(Math.abs(randomSource.nextLong()) + "00000")).substring(0, 4);
                        uploadName = dataSetName + "." + lastQualifier;
                        dispName  = "If partition dataset,           data saved as " + dataSetName +"("+lastQualifier +") (or)\n";
                        dispName += "If physical sequential dataset, data saved in " + dataSetName +"."+lastQualifier +"\n";
                    }
                    if((HFSSavePath!=null) &&  localName !=null && isValidFile) {
                        if(!HFSSavePath.endsWith("/")) {
                            HFSSavePath += "/";
                        }
                        uploadName = HFSSavePath +  "MET" + (new String(Math.abs(randomSource.nextLong()) + "00000")).substring(0, 4);
                        dispName = uploadName;
                    }

                    if(uploadName!=null) {
                        uploadFileToHost(localName,uploadName);
                        hostSavedFiles.add(dispName);
                    }
                    //End TST3016

                    localSavedFiles.add(localName); /*PTM3531 */
                //}
                //catch(MBBuildException mbe) {
                  //  getLEP().LogException(mbe);
                //}
            }
        }
    }
    //End INT2395


    private void uploadFileToHost(String localName, String dataSetName) throws MBBuildException {

        getStatusHandler().updateStatus("Upload file "+ localName + " to host.", false);
        MBFtp ftpObject = new MBFtp(build.getSetup().getMainframeInfo(),getLEP());

        if(!ftpObject.put(new File(localName),dataSetName)) {
            throw new FtpError("Could not upload file "+ localName + " to " + dataSetName);
        }

    }


    private MetadataType[] parse(String localFile)throws GeneralError{ 
        MetadataType metadataTypes[];
        BufferedReader inputReader;
        String inStr=null;
        String keyWord=null;
        String realName=null;
        String type=null;
        String description=null;
        String tmp=null;
        StringTokenizer tok;

        try {
            inputReader = new BufferedReader(new FileReader(localFile));//#Def.INT1161:
            Vector metaVector = new Vector();
            while(((inStr = inputReader.readLine())!=null) & !stopped) {
                int maxLength=0;
                int maxCnt=0;
                tok =new StringTokenizer(inStr," _");
                keyWord=tok.nextToken();//get keyword
                realName=tok.nextToken();//get type
                tmp=tok.nextToken();
                description="";
                if(tmp.equals("AC")) {
                    type=MetadataType.NUMERICAL_TYPE;
                }
                else
                    if(tmp.equals("SW")) {
                    type=MetadataType.BOOLEAN_TYPE;
                }
                else
                    if(tmp.startsWith("SE")) {
                    type=MetadataType.SINGLE_ENTRY_TYPE;
                    tmp=tok.nextToken();//get the n
                    maxLength=Integer.parseInt(tmp);
                }
                else {
                    if(tmp.startsWith("ME")) {
                        type=MetadataType.MULT_ENTRY_TYPE;
                        tmp=tok.nextToken();//get the n
                        maxLength=Integer.parseInt(tmp);
                        tmp=tok.nextToken();//get the m
                        maxCnt=Integer.parseInt(tmp);
                    }
                }
                while(tok.hasMoreTokens()) {//append the rest
                    description+=tok.nextToken();//get description

                }

                metaVector.add(new  MetadataType(keyWord, realName,type, description, maxLength, maxCnt));

            }


            Object objAry[]=metaVector.toArray();
            metadataTypes=new MetadataType[objAry.length];
            System.arraycopy(objAry,0,metadataTypes,0,objAry.length); //copy array
            Arrays.sort(metadataTypes,new MetadataTypeComparator());//sort array
        }
        catch(IOException ioe) {
            throw new GeneralError("Error reading "+localFile, ioe);//#Def.INT1161:
        }
        return metadataTypes;
    }

    private class MetadataTypeComparator implements Comparator {
        public int compare(Object o1,Object o2) {
            MetadataType tmp1=(MetadataType)o1;
            MetadataType tmp2=(MetadataType)o2;
            return tmp1.getKeyword().compareTo(tmp2.getKeyword());
        }
    }
}
