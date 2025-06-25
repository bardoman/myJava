package com.ibm.sdwb.build390.library.cmvc.metadata;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.MetadataServerOperationsInterface;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.VersionPopulator;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.metadata.MetadataOperationsInterface;



public class CMVCMetadataOperationsHandler implements MetadataOperationsInterface {



    private MetadataServerOperationsInterface remoteMetadataInterface;

    public CMVCMetadataOperationsHandler(MetadataServerOperationsInterface remoteMetadataInterface) {
        this.remoteMetadataInterface= remoteMetadataInterface;

    }


    /**
     * A set of file info objects are passed in.  The method will
     * look up metadata for each file info object and populate each
     * object's metadata field.  The metadata is returned in each file
     * info object.
     *
     * @param fileInfoSet
     *               Set object full of fileinfo objects
     */
    public void populateMetadataMapFieldOfPassedInfos(Set fileInfoSet) {
        try {
            Set tempSet = remoteMetadataInterface.populateMetadataMapFieldOfPassedInfos(fileInfoSet);
//dont understand why we have to update fileInfoSet, why not just send back tempSet, by appending the  mainframename to its elements.
            List infosList = Arrays.asList(fileInfoSet.toArray());
            Collections.sort(infosList, FileInfo.BASIC_FILENAME_COMPARATOR);
            for (Iterator iter = tempSet.iterator();iter.hasNext();) {
                FileInfo tempinfo = (FileInfo)iter.next();
                int index = Collections.binarySearch(infosList,tempinfo,FileInfo.BASIC_FILENAME_COMPARATOR);
                if (index > -1) {
                    FileInfo info = (FileInfo)infosList.get(index);
                    info.setMetadata(tempinfo.getMetadata());
                    info.setMetadataVersion(tempinfo.getMetadataVersion());
                    info.setVersion(tempinfo.getVersion());
                    info.setDirectory(tempinfo.getDirectory());
                    info.setName(tempinfo.getName());
                }
            }

            fileInfoSet = new HashSet(infosList);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }







    /**
     * A set of file info objects are passed in.  The metadata will
     * be read from each object and it's metadata in the library will
     * be set to the metadata in the object.
     *
     * @param fileInfoSet
     *               Set object full of fileinfo objects with populated metadata
     *               fields
     */
    public void storeMetadataValuesFromPassedInfos(Set fileInfoSet) {
        try {
            fileInfoSet = remoteMetadataInterface.storeMetadataValuesFromPassedInfos(fileInfoSet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }


    }



    /**
     * A set of file info objects are passed in.  The metadata will
     * be read from each object and it's metadata in the library will
     * be updated to the metadata in the object. Any metadata keywords
     * set in the library that is not set in the file info objects
     * will be left.
     * If the file infos only have metadata1=value1, then they will
     * have the metadata1 keyword set if they don't have it, and
     * the metadata1 value updated to value1 if they do have it.  Other
     * values that are already set in the library will not be updated.
     * If this method is run with empty fields nothing will happen.
     *
     * @param fileInfoSet
     *               Set object full of fileinfo objects with populated metadata
     *               fields
     */
    public void updateMetadataValuesInStorageFromPassedInfos(Set fileInfoSet) {
        try {
            fileInfoSet = remoteMetadataInterface.updateMetadataValuesInStorageFromPassedInfos(fileInfoSet);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Send the input file info set. and the infoset to compare against.
     * 
     * @param fileInfoSet
     *               Input fileInfo set. This should get populated with metadata
     *               in storage
     * @param toCompareInfoSet
     *               The compare file infoset which already has stuff.
     * 
     * @return Output is a map of key (Input file Info) to any value.
     */
    public Map comparisonFacilitatorForPassedInfos(Set fileInfoSet,Set toCompareInfoSet) {

        java.util.List compareList = new ArrayList(toCompareInfoSet);
        Collections.sort(compareList,FileInfo.BASIC_FILENAME_COMPARATOR);
        Map messageMap = new HashMap();

        for (Iterator iter = fileInfoSet.iterator();iter.hasNext();) {
            FileInfo inputInfo = (FileInfo)iter.next();
            int index = Collections.binarySearch(compareList, inputInfo,FileInfo.BASIC_FILENAME_COMPARATOR);
            //we need to throw a runtime exception if index is -1. But basically it shouldn't happen.

            FileInfo compareInfo = (FileInfo)compareList.get(index); 

            String compareLibraryPartName = "";
            String message = null;

            if (compareInfo.getDirectory() !=null & compareInfo.getName()!=null) {
                compareLibraryPartName = compareInfo.getDirectory()+compareInfo.getName();
            } else if (compareInfo.getName()!=null) {
                compareLibraryPartName = compareInfo.getName();
            }

            String libraryVersion = (String)inputInfo.getMetadata().get(VersionPopulator.VERSIONSID_KEY); //use the VERSIONSID. a hack for now. need to rethink it later.
            if (compareLibraryPartName !=null && compareLibraryPartName.trim().length() > 0) {
                if (libraryVersion == null) {
                    message = "Library version problem:\n" + "The part " + compareLibraryPartName + " was not found in the library. " +
                              "It may have been added by a user build.";
                } else if (libraryVersion.trim().toUpperCase().startsWith("V")) {
                    message = "Concurrent Library Version:\n" + "The current library version cannot be determined because of concurrent development.";
                } else if ((compareInfo.getVersion()!=null) && (!compareInfo.getVersion().trim().equals(libraryVersion.trim()))) {
                    message = "Driver and Library Version conflict:\n" + "The version of this part currently built into the driver is not " +
                              "the latest version in the library.  Version " + compareInfo.getVersion() + " is the built version, "+
                              "version "+ libraryVersion+" is the library version.";
                }
            }
            if (message!=null && message.trim().length() > 0) {
                messageMap.put(inputInfo, message);
            }
        }
        return messageMap;

    }



}
