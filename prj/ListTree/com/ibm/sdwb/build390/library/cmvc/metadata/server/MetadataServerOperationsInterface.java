package com.ibm.sdwb.build390.library.cmvc.metadata.server;

import java.rmi.*;
import java.util.*;

public interface MetadataServerOperationsInterface extends  Remote {



    /**
     * A set of file info objects are passed in.  The method will
     * look up metadata for each file info object and populate each
     * object's metadata field.  The metadata is returned in each file
     * info object.
     * 
     * @param fileInfoSet
     *               Set object full of fileinfo objects
     */
    public Set populateMetadataMapFieldOfPassedInfos(Set fileInfoSet) throws RemoteException;


    /**
     * A set of file info objects are passed in.  The metadata will 
     * be read from each object and it's metadata in the library will
     * be set to the metadata in the object.  
     * 
     * @param fileInfoSet
     *               Set object full of fileinfo objects with populated metadata
     *               fields
     */
    public Set storeMetadataValuesFromPassedInfos(Set fileInfoSet) throws RemoteException;

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
    public Set updateMetadataValuesInStorageFromPassedInfos(Set fileInfoSet) throws RemoteException;




}
