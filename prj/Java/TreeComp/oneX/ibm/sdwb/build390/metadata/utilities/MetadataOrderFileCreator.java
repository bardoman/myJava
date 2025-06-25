package com.ibm.sdwb.build390.metadata.utilities;

import java.io.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.VersionPopulator;


public class  MetadataOrderFileCreator {

    public static void createOrderFile(String saveFilePath, Set holder) throws IOException {
        final String ORDER = "&  &ORDER ";
        BufferedWriter metaOrderFileWriter = new BufferedWriter(new FileWriter(saveFilePath));
        for (Iterator iter= holder.iterator();iter.hasNext();) {
            FileInfo info = (FileInfo)iter.next();

            String partLine = "";

            if (info.getMainframeFilename()!=null && (info.getMainframeFilename().trim().length() > 0)
                && (info.getMainframeFilename().indexOf(".") > 0)) {
                String  partClass     = info.getMainframeFilename().substring(0,info.getMainframeFilename().indexOf("."));
                String partName       = info.getMainframeFilename().substring(info.getMainframeFilename().indexOf(".")+1);
                partLine =  " CLASS="+ partClass + ",MOD="+ partName;
            } else {
                partLine = " DIR="+info.getDirectory() + ",PATH="+ info.getName() ;
            }

            String orderLine = ORDER +  partLine;
            metaOrderFileWriter.write(orderLine);
            int keywordIndex=0;
            for (Iterator iterb=info.getMetadata().entrySet().iterator();iterb.hasNext();) {
                Map.Entry entry = (Map.Entry)iterb.next();

                if (!(((String)entry.getKey()).equals(MBConstants.METADATAVERSIONKEYWORD)) &&
                    //to do later. maybe each of the libraryinfo can have a set that says
                    //these aren't metadata keywords. Instead of using VersionPopulator.VERSIONSID_KEY which is cmvc specific
                    !(((String)entry.getKey()).equals(VersionPopulator.VERSIONSID_KEY))) {
                    keywordIndex++;
                    orderLine = ","+MBConstants.NEWLINE+"&  MKN"+keywordIndex+"="+ (String)entry.getKey()+",MKV"+keywordIndex+"="+ (String)entry.getValue();
                    metaOrderFileWriter.write(orderLine,0,orderLine.length());
                }
            }  
            orderLine =","+MBConstants.NEWLINE+"&  #MK="+(keywordIndex) + MBConstants.NEWLINE; //writers the count.
            metaOrderFileWriter.write(orderLine,0,orderLine.length());

        }
        metaOrderFileWriter.close();


    }

}
