package com.ibm.sdwb.build390.library.cmvc.metadata.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.database.DatabaseException;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.utilities.SQLUtilities;



public class MetadataPopulator {


    private Connection con = null;

    private static final  String PARTMETADATA_TABLE="Build390Metadata";


    public MetadataPopulator(Connection con) {
        this.con = con;

    }



    public void  populate(String release, Set infos) throws DatabaseException {
        try {

            Statement retrieveMetadataStatement = con.createStatement();


            /** grab the metadata for the above version ids
             */
            String retrieveMetadataQuery  = "SELECT DISTINCT cv.pathName, CHAR(cv.versionid), md.metadataKeyword, md.metadataValue, md.version "+
                                            "FROM ChangeView cv, Build390Metadata md "+
                                            "WHERE CHAR(cv.versionid) in  ("+ MetadataQueryUtilities.addAllVersionIds(infos)  +") and "+
                                            "cv.pathName in ("+ MetadataQueryUtilities.addAllPartNames(infos)  +") and "+
                                            "cv.releaseName='"+ release+"' and "+
                                            "md.versionid=cv.versionid " +
                                            "UNION " +
                                            "SELECT DISTINCT F.nuPathName, CHAR(F.nuVersionId), md1.metadataKeyword, md1.metadataValue, md1.version "+
                                            "FROM FileView F, Build390Metadata md1 "+
                                            "WHERE CHAR(F.nuVersionid) in  ("+ MetadataQueryUtilities.addAllVersionIds(infos)  +") and "+
                                            "F.releaseName='"+ release +"' and " +
                                            "md1.versionid=F.nuVersionid and F.nuPathName NOT IN " + 
                                            "(SELECT DISTINCT cv1.pathName "+
                                            "FROM ChangeView cv1 "+
                                            "WHERE CHAR(cv1.versionid) in  ("+ MetadataQueryUtilities.addAllVersionIds(infos)  +") and "+
                                            "cv1.pathname in ("+ MetadataQueryUtilities.addAllPartNames(infos)  +") and "+
                                            "cv1.releaseName='"+ release+"')";

            ResultSet retrievedMetadataResultSet = retrieveMetadataStatement.executeQuery(retrieveMetadataQuery);

            List infosList = Arrays.asList(infos.toArray());
            Collections.sort(infosList, FileInfo.BASIC_FILENAME_COMPARATOR);

            while (retrievedMetadataResultSet.next()) {
                String pathname = retrievedMetadataResultSet.getString(1);
                String versionid = retrievedMetadataResultSet.getString(2);
                String metadatakeyword  = retrievedMetadataResultSet.getString(3).trim();
                String metadatavalue  = retrievedMetadataResultSet.getString(4).trim();
                String metadataversion =    retrievedMetadataResultSet.getString(5);


                int index = Collections.binarySearch(infosList,MetadataQueryUtilities.makeInfoFromName(pathname),FileInfo.BASIC_FILENAME_COMPARATOR);
                if (index > -1) {
                    FileInfo info = (FileInfo)infosList.get(index);
                    info.getMetadata().put(metadatakeyword,metadatavalue);
                    info.setMetadataVersion(metadataversion);
                }
            }

            infos = new HashSet(infosList);
            retrievedMetadataResultSet.close();
            retrieveMetadataStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred getting the metadata of parts.", se); 
        }
    }


    public void  update(Set infos) throws  DatabaseException  {

        try {
            String replaceMetadataQuery = "UPDATE Build390Metadata md " + 
                                          "SET  md.metadatavalue=?, md.metadataKeyword=? , md.version=CURRENT TIMESTAMP  " + 
                                          "WHERE md.versionid =? and md.metadatakeyword=?"  ;

            PreparedStatement replaceMetadataStatement = con.prepareStatement(replaceMetadataQuery);

            for (Iterator iter = infos.iterator(); iter.hasNext();) {
                FileInfo info            = (FileInfo)iter.next();

                /** the padWithQuotes thing is causing problems when we try to replace any string with quotes.
                 * ie. If we wanted to replace 'BUF(SOMETHING,OPT(STD)' with 'BUF(REPLACED)' the actual value that 
                 * gets replaced is ''BUF(REPLACED)'' - two single quotes. This causes the driverbuild to fail.
                 * For the rest of the queries like INSERT,SELECT we need it, because  we have to wrap any string inside 
                 * single quotes. ie. 'DOG' - would be stored as DOG
                 *  or ''DOG''   - would be stored as 'DOG'.
                 * replaceMetadataStatement.setString(1, SQLUtilities.padWithQuotes(newElement.getValue())); 
                 * In this case it doesnt work, becase of the prepared statement.
                 */
                for (Iterator itera=info.getMetadata().entrySet().iterator();itera.hasNext();) {
                    Map.Entry entry = (Map.Entry) itera.next();
                    replaceMetadataStatement.setString(1, (String)entry.getValue());
                    replaceMetadataStatement.setString(2, (String)entry.getKey());
                    replaceMetadataStatement.setString(3, (String)info.getVersion());
                    replaceMetadataStatement.setString(4, (String)entry.getKey());
                    int updCount = replaceMetadataStatement.executeUpdate();
                }


            }

            replaceMetadataStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred  updating  the metadata of parts.",se); //an unmarshall exception occurred when SQLException was sent across.
        }


    }



    public void create(Set infos) throws DatabaseException {
        try {
            StringBuffer  metadataInsertCommand =  new StringBuffer("INSERT INTO "+PARTMETADATA_TABLE+" (VERSIONID, VERSION, METADATAKEYWORD, METADATAVALUE) VALUES ");
            String insertValues = MetadataQueryUtilities.addAllMetadata(infos);

            /** The formulation of metadata keywords/value pairs for the insert query is handled in 
             * MetadataUpdateHelper class. 
             * If a DISTNAME keyword exists, then its removed from the modelmetadata hash, and the 
             * correct DISTNAME from the BuiltPartInfo.getDistributionName() is added to the hash. 
             * Each keyword value pair gets inserted for all versions.
             * eg: Lets say consider COMPILER=NONE and the versions are 1.1,1.2,1.3
             * The table would be populated with COMPILE=NONE value for all the versions 1.1,1.2,1.3.
             **/
            if (insertValues.trim().length() >0) {
                metadataInsertCommand.append(insertValues);

                Statement insertStatement = con.createStatement();
                insertStatement.executeUpdate(metadataInsertCommand.toString());
                insertStatement.close();
            } else {
            }
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred  inserting the metadata of parts.",se); //an unmarshall exception occurred when SQLException was sent across.
        }

    }


}
