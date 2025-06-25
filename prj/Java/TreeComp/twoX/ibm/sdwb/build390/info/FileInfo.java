package com.ibm.sdwb.build390.info;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.utilities.ParsingFunctions;

public class FileInfo implements Serializable {

    static final long serialVersionUID = 1111111111111111L;
    private String version = new String();
    private String date = new String();
    private String baseName = new String();
    private String directory = new String();
    private String typeOfChange = new String();
    private String scode = null;
    private String distType = null;
    private String codePage = null;
    private String mainframeCodepageLocation = null;
    private String VHJN = null;
    private String partOwnerEmail = null;
    private String lastUpdaterEmail = null;
    private String fileType = null;
    private String mainframeRecordType = null;
    private String project = null;
    private int mainframeRecordLength = -1;
    private boolean mainframeStructR = false;
    private String ftpArguments = null;
    private int size = -1;
    private Map metadata = new HashMap();
    private String mainframeFilename = null;
    private File localFile = null;
    private boolean beenUploaded = false;

    private static final String BINARYTYPE= "binary";

    public static Comparator BASIC_FILENAME_COMPARATOR  = new BasicFileNameComparator();

    public FileInfo(String tempDir, String tempName) {
        baseName = tempName;
        directory = tempDir;
    }

    public void setOwnerEmail(String tempEmail) {
        partOwnerEmail = tempEmail;
    }

    public void setUpdaterEmail(String tempEmail) {
        lastUpdaterEmail = tempEmail;
    }

    public void setVersion(String tempVer) {
        version = tempVer;
    }

    public void setDate(String tempDate) {
        date = tempDate;
    }

    public void setName(String tempName) {
        baseName = tempName;
    }

    public void setDirectory(String newDir) {
        directory = newDir;
    }

    public void setProject(String tempProj) {
        project = tempProj;
    }

    public void setTypeOfChange(String tempType) {
        typeOfChange = tempType;
    }

    public void setFileType(String tempType) {
        fileType = tempType;
    }

    public void setVHJN(String tempVHJN) {
        VHJN = tempVHJN;
    }

    public void setSize(int newSize) {
        size = newSize;
    }

    public void setCodePage(String tempCP) {
        codePage = tempCP;
    }

    public void setMainframeCodepage(String tempCP) {
        mainframeCodepageLocation = tempCP;
    }

    public void setSCode(String tempSCode) {
        scode = tempSCode;
    }

    public void setDistType(String tempDistType) {
        distType = tempDistType;
    }

    public void setMainframeRecordLength(int newRecordLength) {
        mainframeRecordLength = newRecordLength;
    }

    public void setMainframeRecordType(String newRecordType) {
        mainframeRecordType = newRecordType;
    }

    public void setMainframeStructR(boolean newStructR) {
        mainframeStructR = newStructR;
    }

    public void setFTPArguments(String newArgs) {
        ftpArguments = newArgs;
    }

    public void setMainframeFilename(String newMainframeName) {
        mainframeFilename = newMainframeName;
    }

    public void setLocalFile(File newFile) {
        localFile = newFile;
    }

    public void setUploaded(boolean newSetting) {
        beenUploaded = newSetting;
    }

    public void setMetadataVersion(String newMetadataVersion) {
        if(newMetadataVersion!=null) {
            newMetadataVersion = ParsingFunctions.stripNonNumeric(newMetadataVersion);
        }
        getMetadata().put(MBConstants.METADATAVERSIONKEYWORD,newMetadataVersion );
    }

    public void setMetadata(Map newMetadata) {
        this.metadata=newMetadata;
    }

    public String getMetadataVersion() {
        String metadataVersion  = (getMetadata().get(MBConstants.METADATAVERSIONKEYWORD)!=null ?
                                   (String)getMetadata().get(MBConstants.METADATAVERSIONKEYWORD) : null);
        if(metadataVersion!=null) {
            metadataVersion = ParsingFunctions.stripNonNumeric(metadataVersion);
        }

        return metadataVersion;
    }

    public Map getMetadata() {
        return metadata;
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return baseName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getProject() {
        return project;
    }

    public String getTypeOfChange() {
        return typeOfChange;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFTPFileType() {
        if(isBinary()) {
            return "BINARY";
        }
        return "ASCII";
    }

    public String getCodePage() {
        return codePage;
    }

    public String getMainframeCodepage() {
        return mainframeCodepageLocation;
    }

    public String getDistType() {
        return distType;
    }

    public String getScode() {
        return scode;
    }

    public String getPartOwnerEmail() {
        return partOwnerEmail;
    }

    public String getLastUpdaterEmail() {
        return lastUpdaterEmail;
    }

    public String getVHJN() {
        return VHJN;
    }

    public int getSize() {
        return size;
    }

    public int getMainframeRecordLength() {
        return mainframeRecordLength;
    }

    public String getMainframeRecordType() {
        return mainframeRecordType;
    }

    public boolean isUsingMainframeStructR() {
        return mainframeStructR;
    }

    public String getFTPArguments() {
        return ftpArguments;
    }

    public String getMainframeFilename() {
        return mainframeFilename;
    }

    public File getLocalFile() {
        return localFile;
    }

    public boolean isUploaded() {
        return beenUploaded;
    }

    public boolean isBinary() {
        return BINARYTYPE.equalsIgnoreCase(fileType);
    }

    public boolean equals(Object testObject) {
        if(!(testObject instanceof FileInfo)) {
            return false;
        }
        FileInfo testInfo = (FileInfo) testObject;
        if(typeOfChange != null) {
            if(!typeOfChange.equals(testInfo.getTypeOfChange())) {
                return false;
            }
        }
        else if(testInfo.getTypeOfChange() != null) {
            return false;
        }

        if(baseName != null) {
            if(!baseName.equals(testInfo.getName())) {
                return false;
            }
        }
        else if(testInfo.getName() != null) {
            return false;
        }

        if(version != null) {
            if(!version.equals(testInfo.getVersion())) {
                return false;
            }
        }
        else if(testInfo.getVersion() != null) {
            return false;
        }

        if(date != null) {
            if(!date.equals(testInfo.getDate())) {
                return false;
            }
        }
        else if(testInfo.getDate() != null) {
            return false;
        }

        if(codePage != null) {
            if(!codePage.equals(testInfo.getCodePage())) {
                return false;
            }
        }
        else if(testInfo.getCodePage() != null) {
            return false;
        }

        if(distType != null) {
            if(!distType.equals(testInfo.getDistType())) {
                return false;
            }
        }
        else if(testInfo.getDistType() != null) {
            return false;
        }

        if(scode != null) {
            if(!scode.equals(testInfo.getScode())) {
                return false;
            }
        }
        else if(testInfo.getScode() != null) {
            return false;
        }

        if(VHJN != null) {
            if(!VHJN.equals(testInfo.getVHJN())) {
                return false;
            }
        }
        else if(testInfo.getVHJN() != null) {
            return false;
        }

        return true;
    }

    public String toString() {
        return "{"+baseName+","+directory+","+mainframeFilename+","+ project+","+date+","+version+","+typeOfChange+","+codePage+","+distType+","+scode+","+VHJN+","+metadata+"}";
    }


    //Begin TST3337
    public String getSourcePartKey() {
        return "SRCPART='', DIR='"+getDirectory()+"', PATH='"+getName()+"'";
    }
    //End TST3337

    private static class BasicFileNameComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            FileInfo sm1 = (FileInfo)o1;
            FileInfo sm2 = (FileInfo)o2;

            String compareFileName = "";
            String toCompareFileName = "";

            if(sm1.getDirectory()!=null) {
                compareFileName += sm1.getDirectory().trim();
            }

            if(sm1.getName()!=null) {
                compareFileName += sm1.getName().trim();
            }

            if(sm2.getDirectory()!=null) {
                toCompareFileName += sm2.getDirectory().trim();
            }

            if(sm2.getName()!=null) {
                toCompareFileName += sm2.getName().trim();
            }

            if(toCompareFileName.trim().length() > 0 && compareFileName.trim().length() > 0) {
                return toCompareFileName.compareTo(compareFileName);
            }

            if(sm2.getMainframeFilename()==null || sm1.getMainframeFilename()==null) {
                return -1;
            }

            return sm2.getMainframeFilename().trim().compareTo(sm1.getMainframeFilename().trim());

        }

        public String toString() {
            return  "FileInfo : BasicFileName Comparator";
        }
    }

}
