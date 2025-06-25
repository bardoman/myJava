package com.ibm.sdwb.build390.process.steps;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.info.*;
import java.util.*;
import java.io.File;

public class LocalPartlistGeneration extends ProcessStep {
    static final long serialVersionUID = 1111111111111111L;

    private static final String MODCLASSSET = "MOD.CLASS";

    private MBUBuild build = null;
    private boolean hostdataStringRequired = false;
    private List partList = null;

    private static Random randomSource = new Random();//***BE

    public LocalPartlistGeneration(MBUBuild tempBuild, com.ibm.sdwb.build390.process.AbstractProcess tempProc) {
        super(tempProc,"Local Partlist Generation");
        build = tempBuild;
        setUndoBeforeRerun(false);
    }

    public List getListOfParts() {
        return partList;
    }

    public boolean isHostdataStringRequired() {
        return hostdataStringRequired;
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
        getStatusHandler().updateStatus("Generating part list", false);
        partList = new java.util.ArrayList();

        Set usedMainframeNames = new HashSet();//***BE

        for (int partIndex = 1; partIndex < build.getLocalParts().length; partIndex++ ) {
            String version = MBUtilities.getLocalFileVersion(build.getLocalParts()[partIndex], build);
            File tempFile = new File(build.getLocalParts()[partIndex]);
            String basicName = tempFile.getName();
            String pathString = getFileClass(build.getLocalParts()[partIndex], build.getLocalParts()[0].length(), basicName.length());
            FileInfo newPart = new FileInfo(pathString, basicName);
            if (build.getPartModels_mod_part().length >partIndex) {

// all these conditions just make sure a model exists for a part;
                if (build.getPartModels_mod_part()[partIndex]!=null ) {
                    newPart = new ModeledFileInfo(pathString, basicName, build.getPartModels_class_path()[partIndex], build.getPartModels_mod_part()[partIndex]); 
                    if (MODCLASSSET.equals(build.getPartModelTypes()[partIndex]) ) {
                        hostdataStringRequired = true;
                    }
                }
            }

            //*****************************BE

            String tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
            while (usedMainframeNames.contains(tempMainframeName)) {
                tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
            };
            newPart.setMainframeFilename(tempMainframeName);
            usedMainframeNames.add(tempMainframeName);

            //*****************************

            newPart.setVersion(version);
            newPart.setDate(version);
            newPart.setLocalFile(tempFile);
            newPart.setTypeOfChange("LOCAL");
            newPart.setCodePage("?");
            partList.add(newPart);
        }

        build.setPartInfo( partList);//***BE

    }

    private String getFileClass(String tempDirPath, int rootPathLength, int filenameLength) {
        if (((com.ibm.sdwb.build390.process.UserBuildProcess) build.getProcessForThisBuild()).isPDSBuild()) {
            return build.getPDSMemberClass();
        } else {
            String tempPath = tempDirPath.substring(rootPathLength, tempDirPath.length()-filenameLength).replace(File.separatorChar, '/').trim();

            if (tempPath.equals("/")) {
                tempPath = "";
            }
            return tempPath;


        }
    }
}
