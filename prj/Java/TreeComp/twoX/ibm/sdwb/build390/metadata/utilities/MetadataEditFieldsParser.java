package com.ibm.sdwb.build390.metadata.utilities;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.metadata.*;

public class MetadataEditFieldsParser {

    public static  List parseFilterReport(String filterReport) throws MBBuildException  {

        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(filterReport));
            String currentLine;
            List tmpVect=new ArrayList();
            while (((currentLine = fileReader.readLine()) != null)) {
                String[]  splitStr = currentLine.split("\\s+");
                FileInfo info = createPartInfo(splitStr,fileReader);
                tmpVect.add(info); 
            }
            fileReader.close();
            return tmpVect;
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred while trying to parse " + filterReport,ioe);
        } finally {
            if (fileReader!=null) {
                try {
                    fileReader.close();
                } catch (IOException ioea) {

                }
            }
        }
    }

    static FileInfo createPartInfo(String[] splitStr,BufferedReader reader) throws IOException {
        String mvsPartClass = splitStr[0].trim();
        String mvsPartName =  splitStr[1].trim();

        FileInfo  info = new FileInfo("","");
        info.setMainframeFilename(mvsPartClass + "."+ mvsPartName);
        info.setTypeOfChange("N");

        if (splitStr.length==3) {
            if (splitStr[2].endsWith("/")) {
                info.setDirectory(splitStr[2]);
                appendLibraryPath(info,reader);
            } else {
                info.setName(splitStr[2]);
            }
        } else {
            reader.mark(80); /* allow a 80 character read only */
            appendLibraryPath(info,reader);
        }
        return info;
    }

    static void appendLibraryPath(FileInfo info, BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line!=null) {
            if (Character.isLetterOrDigit(line.charAt(0))) {
                reader.reset();
                return;
            }
            String[] test = line.trim().split("\\s+");
            info.setName(test[0]);
        }
    }


    public static void main(String[] args) throws Exception {
        MetadataEditFieldsParser.parseFilterReport("test.out");


    }




}

