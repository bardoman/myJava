package com.ibm.sdwb.build390.metadata.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.*;
import com.ibm.sdwb.build390.filter.EliminateDuplicatesFilter;
import com.ibm.sdwb.build390.metadata.filter.EliminateDuplicatePartsCriteria;

public class MetaCheckParser {

    private String metaCheckFileName;
    private static final String PART_FAILED_METACHECK = "Part failed metadata check";
    private Set failedPartsSet;

    public MetaCheckParser(String metaCheckFileName){
        this.metaCheckFileName= metaCheckFileName;
        this.failedPartsSet= new HashSet();
    }

    public void parse() throws MBBuildException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metaCheckFileName));
            String currentLine;

            while ((currentLine=reader.readLine())!=null) {
                if (currentLine.indexOf(PART_FAILED_METACHECK)>=0) {
                    if (currentLine.startsWith(PART_FAILED_METACHECK)) {/* then parse for DIR/PATH in the lines following it. */
                        String[] secondLine = reader.readLine().trim().split("=");
                        String dir = secondLine.length==2 ?  secondLine[1] : "";

                        String thirdLine[] = reader.readLine().trim().split("=");
                        String path = thirdLine.length==2 ? thirdLine[1] : "";

                        FileInfo info = new FileInfo(dir,path);
                        info.setTypeOfChange("N");
                        failedPartsSet.add(info);
                    } else { /*or the same line contains class.mod */
                        StringTokenizer strk = new StringTokenizer(currentLine);
                        String classAndMod = strk.nextToken().trim();
                        int indexOfDot = classAndMod.indexOf(".");
                        FileInfo info = new FileInfo("","");
                        info.setMainframeFilename(classAndMod);
                        info.setTypeOfChange("N");
                        failedPartsSet.add(info);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new GeneralError("An error occurred while trying to parse " + metaCheckFileName);
        }

    }




    public Set getFailedParts(){
        return failedPartsSet;
    }

    public static void main(String[] args) throws Exception {
        MetaCheckParser parser = new MetaCheckParser("c:\\kishore\\dev\\ibm\\build390\\5.0.8\\metadata\\test\\METACHECK-BATCH.out");
        parser.parse();
      /*  for (Iterator iter0=parser.getFailedParts().iterator();iter0.hasNext();) {
            BuiltPartInfo info = (BuiltPartInfo)iter0.next();
            System.out.println("0."+info.getDirectory() + "," + info.getName()+","+info.getMVSPartClass() +"."+info.getMVSPartName());
        }

        java.util.List test = new java.util.LinkedList();
        test.add(new BuiltPartInfo("@CLRAUTH","MODULE","","","N"));
        test.add(new BuiltPartInfo("","","OESCRIPT/","oetest2.osc","N"));
        test.add(new BuiltPartInfo("","","","hello1.osc","N"));
        test.add(new BuiltPartInfo("","","OESCRIPT/","oetest21.osc","N"));
        test.add(new BuiltPartInfo("CLRALIAS","MODULE","","","N"));
        test.add(new BuiltPartInfo("CLRASM","MODULE","","","N"));
        test.add(new BuiltPartInfo("TEST1","UPD","","","N"));

        for (Iterator iter1=test.iterator();iter1.hasNext();) {
            BuiltPartInfo info = (BuiltPartInfo)iter1.next();
            System.out.println("1."+info.getDirectory() + "," + info.getName()+","+info.getMVSPartClass() +"."+info.getMVSPartName());
        }

        test.removeAll(parser.getFailedParts());

       for (Iterator iter3=test.iterator();iter3.hasNext();) {
            BuiltPartInfo temp = (BuiltPartInfo)iter3.next();
                System.out.println("3.test contains. " + temp + ","+temp.getMVSPartClass() +"."+ temp.getMVSPartName());

        }

*/

    }
    
}


