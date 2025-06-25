package com.ibm.sdwb.build390.library.cmvc.metadata.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.sdwb.build390.database.DatabaseException;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.utilities.SQLUtilities;

public class VersionPopulator {

    private Connection con=null;
    private static final  String PARTMETADATA_TABLE="Build390Metadata";
    public static final String VERSIONSID_KEY = "LIBRARY_VERSIONSID";


    public VersionPopulator(Connection con) {
        this.con = con;
    }


    public void  populate(String release,Set infos) throws DatabaseException {
        try {


            Statement versionStatement = con.createStatement();
            /** retrieve all the version ids of parts present in MetadataUpdateParameters.
             *This is  a subquery. 
             * The first part queries for concurrent version.
             * The union part queries serial version(not like "v.%" filters concurrent versions) 
             **/



            String versionQuery  = "SELECT DISTINCT cv.VersionSID, cv.VersionId,cv.pathname "+
                                   "FROM ChangeView cv "+
                                   "WHERE cv.pathname in ("+ MetadataQueryUtilities.addAllPartNames(infos)  +") and "+
                                   "cv.releaseName='"+ release+"' and "+
                                   "cv.versionSID LIKE 'v.%' "+
                                   "UNION " + 
                                   "SELECT DISTINCT F.nuVersionSID,F.nuVersionId, F.nuPathName "+
                                   "FROM FileView F "+
                                   "WHERE F.releaseName='"+ release+"' "+
                                   "and F.nupathname in ("+ MetadataQueryUtilities.addAllPartNames(infos) +") "+
                                   "and F.nuPathName NOT IN " + 
                                   "(SELECT DISTINCT cv.pathname "+
                                   "FROM ChangeView cv "+ 
                                   "WHERE cv.pathname in ("+ MetadataQueryUtilities.addAllPartNames(infos)  +") and "+
                                   "cv.releaseName='"+ release+"' and "+
                                   "cv.versionSID LIKE 'v.%') ";




            ResultSet versionResultSet = versionStatement.executeQuery(versionQuery);

            /** set the versionids of parts. in MetadataUpdateParameters.
             * and formulate the versionid buffer in that class.
             * 
             */


            List infosList = Arrays.asList(infos.toArray());
            Collections.sort(infosList,FileInfo.BASIC_FILENAME_COMPARATOR);
            while (versionResultSet.next()) {
                String versionSID = versionResultSet.getString(1);
                String versionid = versionResultSet.getString(2);
                String pathname = versionResultSet.getString(3); 
                int index = Collections.binarySearch(infosList,MetadataQueryUtilities.makeInfoFromName(pathname),FileInfo.BASIC_FILENAME_COMPARATOR);
                if (index > -1) {
                    FileInfo info = (FileInfo)infosList.get(index);
                    info.setVersion(versionid);
                    info.getMetadata().put(VERSIONSID_KEY,versionSID); //just a hack for now. not a recommended way of doing things.
                }
            }

            infos = new HashSet(infosList);

            versionResultSet.close();
            versionStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred  getting the versionid of parts.",se); //an unmarshall exception occurred when SQLException was sent across.
        }
    }


    public boolean delete(Set  infos) throws DatabaseException {
        try {

            // delete the old metadata pertaining to this version
            Statement deleteStatement = con.createStatement();
            String deleteOrder = "DELETE FROM "+ PARTMETADATA_TABLE+" "+
                                 "WHERE CHAR(VERSIONID) in ("+ MetadataQueryUtilities.addAllVersionIds(infos) +")";



            deleteStatement.executeUpdate(deleteOrder);
            deleteStatement.close(); 
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred  deleting the versionid of parts.",se); //an unmarshall exception occurred when SQLException was sent across.
        }
        return true; 
    }




}


