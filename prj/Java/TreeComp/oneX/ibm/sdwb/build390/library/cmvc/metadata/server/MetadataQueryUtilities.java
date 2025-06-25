package com.ibm.sdwb.build390.library.cmvc.metadata.server;
/*********************************************************************/
/* MetadataQueryFormatter              class for the Build/390 client*/
/* A helper class to format partnames in queries, versionids   for   */
/* queries, and metadata for queries.                                */
/*********************************************************************/
//02/11/2005 SDWB2406 Batch processing of metadata
/*********************************************************************/
import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.utilities.*;
import com.ibm.sdwb.build390.info.FileInfo;

public class MetadataQueryUtilities {


    public static Map collapseByProject(Set infoSet) {
        Map projectInfosMap = new HashMap();
        for (Iterator iter=infoSet.iterator();iter.hasNext();) {
            FileInfo info = (FileInfo)iter.next();
            Set projectSpecificInfosSet = null;
            if (projectInfosMap.containsKey(info.getProject())) {
                projectSpecificInfosSet = (Set)projectInfosMap.get(info.getProject());
            } else {
                projectSpecificInfosSet = new HashSet();
                projectInfosMap.put(info.getProject(),projectSpecificInfosSet);
            }
            projectSpecificInfosSet.add(info);
        }
        return projectInfosMap;
    }


    public static FileInfo makeInfoFromName(String pathname ) {
        String dir;
        String baseName;
        String pathName = pathname;

        int pathNameEndIndex = pathName.lastIndexOf('/');
        if (pathNameEndIndex > -1) {
            dir = pathName.substring(0,pathNameEndIndex+1);
            baseName = pathName.substring(pathNameEndIndex+1);
        } else {
            dir = "";
            baseName = pathName;
        }
        if (dir.equals("/")) {
            dir = "";
        }

        return new FileInfo(dir,baseName);

    }

    private static String getSingleCommaSeparatedMetadata(FileInfo info) {
        StringBuffer metadataSingleInsertCommand = new StringBuffer();
        if (!info.getMetadata().isEmpty()) {
            if (info.getMetadata().containsKey(VersionPopulator.VERSIONSID_KEY)) { //The key gets populated during the MetadataPopulator.populate call. 
                info.getMetadata().remove(VersionPopulator.VERSIONSID_KEY);
            }
            for (Iterator metadataElements = info.getMetadata().entrySet().iterator();metadataElements.hasNext();) {
                Map.Entry metadataElement  = (Map.Entry)metadataElements.next();
                String singleMetadataElementInsert = getSingleCommaSeparatedMetadataElement(info.getVersion() ,(String)metadataElement.getKey(), (String)metadataElement.getValue());
                if (singleMetadataElementInsert.trim().length() > 0) {
                    metadataSingleInsertCommand.append(singleMetadataElementInsert);

                    if (metadataElements.hasNext()) {
                        metadataSingleInsertCommand.append(", ");
                    }

                }

            }

        }
        return metadataSingleInsertCommand.toString();

    }


    private static String getSingleCommaSeparatedMetadataElement(String sourceVersion,String metadataKeyword,String metadataValue) {
        StringBuffer metadataElementSingleInsertCommand = new StringBuffer();
        String paddedValue=SQLUtilities.padWithQuotes(metadataValue);

        if (sourceVersion!=null && sourceVersion.trim().length() > 0 ) {
            metadataElementSingleInsertCommand.append("("+sourceVersion+", CURRENT TIMESTAMP,'"+ metadataKeyword.toUpperCase()+"','"+ paddedValue+"')");

        }

        return metadataElementSingleInsertCommand.toString();

    }



    private static final String trimEndingComma(StringBuffer commandBuffer) {
        /*cleanup the dangling comma at the end. */
        if (commandBuffer.toString().trim().endsWith(",")) {
            int lastIndexOfComma =  commandBuffer.toString().lastIndexOf(",");
            commandBuffer.delete(lastIndexOfComma,commandBuffer.length());
        }
        return commandBuffer.toString();
    }


    public static final String  addAllPartNames(Set partsSet) {
        StringBuffer names = new StringBuffer();
        for (Iterator iter=partsSet.iterator();iter.hasNext();) {
            names.append("'");
            FileInfo info = (FileInfo)iter.next();
            String dir = info.getDirectory();
            String fileName = info.getName();

            if (dir!=null) {
                fileName = dir + fileName;
            }

            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1); /*INT1184*/

            }
            names.append(fileName);
            names.append("'");
            if (iter.hasNext()) {
                names.append(", ");
            }
        }

        return names.toString();

    }



    public static final String addAllVersionIds(Set parts) {
        StringBuffer versionids = new StringBuffer();

        for (Iterator iter=parts.iterator();iter.hasNext();) {
            FileInfo info = (FileInfo)iter.next();
            String singleVersionAppend = info.getVersion();
            if (singleVersionAppend!=null && singleVersionAppend.trim().length() >0) {
                versionids.append("\'"+singleVersionAppend+"\'");
                if (iter.hasNext()) {
                    versionids.append(", ");
                }
            }
        }

        return trimEndingComma(versionids);
    }




    public static final String addAllMetadata(Set partsHolder) {
        StringBuffer metadataInsertCommand = new StringBuffer();
        for (Iterator iter=partsHolder.iterator();iter.hasNext();) {
            FileInfo info = (FileInfo)iter.next();
            Map  metadataToUpdate = info.getMetadata();
            if (!metadataToUpdate.isEmpty()) { //if there is metadata then create insert values
                String singleMetadataString = getSingleCommaSeparatedMetadata(info);
                if (singleMetadataString!=null && singleMetadataString.trim().length() > 0) {
                    metadataInsertCommand.append(singleMetadataString);
                    if (iter.hasNext()) {
                        metadataInsertCommand.append(", ");
                    }
                }
            }
        }

        return trimEndingComma(metadataInsertCommand);
    }


}

