package com.ibm.sdwb.build390.process.steps.library.metadata;


import java.io.*;
import java.util.*;


import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.mainframe.*;
import com.ibm.sdwb.build390.metadata.utilities.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.metadata.*;
import com.ibm.sdwb.build390.process.*;
import com.ibm.sdwb.build390.process.steps.*;

public class GenerateAndUploadMetadataOrder extends ProcessStep {

    static final long serialVersionUID = 1111111111111111L;

    private String fileSavePath = null;
    private String hostFileName  = "";
    private String hostPDSPrefix = "";
    private Set parts;
    private MBMainframeInfo mainframeInfo;
    private Random randomGenerator  = new Random();


    public GenerateAndUploadMetadataOrder(Set parts, MBMainframeInfo mainframeInfo, AbstractProcess tempProc) {
        super(tempProc,"Generate and Upload  Metadata Order ");
        setVisibleToUser(true);
        setUndoBeforeRerun(false);
        this.parts = parts;
        this.mainframeInfo = mainframeInfo;
    }                                      

    public void setSavePath(String fileSavePath){
        this.fileSavePath = fileSavePath;
    }

    public String getUploadedFileName(){
        return hostFileName;
    }

    public String getUploadedPDSPrefix(){
        return hostPDSPrefix;
    }

    public String getUploadedPDSSuffix(){
        return ")";
    }


    public void setUploadPDSPrefix(String hostPDSPrefix){
        this.hostPDSPrefix= hostPDSPrefix;
    }


    public void execute() throws MBBuildException  {
        getLEP().LogSecondaryInfo(getFullName(),"Entry");
        try {


            MetadataOrderFileCreator.createOrderFile(fileSavePath,parts);

            MBFtp ftpClient = new MBFtp(mainframeInfo, getLEP());

            hostFileName =  getRandomCharacters("M",7);


            ftpClient.put(new File(fileSavePath), getUploadedPDSPrefix() +getUploadedFileName()+ getUploadedPDSSuffix());      
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred during metadata filter order creation \n" + fileSavePath,ioe);
        }


    }

/*this should be a utility static method  */
    private String getRandomCharacters(String prefix, int limitedToCount){
        char[] zeros = new char[limitedToCount]; /*might be easier to use some formatter class. to-do later */
        for (int i=0;i<zeros.length;i++) {
            zeros[0]='0';
        }

        String zeroString = new String(zeros);
        String randomChrs = prefix   + (new String(Math.abs(randomGenerator.nextLong()) + "0000000")).substring(0, limitedToCount); 
        return randomChrs;

    }
}

