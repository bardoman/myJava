package com.ibm.sdwb.build390.library.cmvc.metadata.server;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.ibm.sdwb.build390.database.DatabaseException;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.utilities.SQLUtilities;
import com.ibm.sdwb.build390.utilities.mail.EmailClient;
import com.ibm.sdwb.build390.utilities.mail.EmailInfo;


public class MetadataChangeNotifier {


    /** This should be made into a single query 
     * The query is split up into two parts. This might be a cmvc problem.
     * The first portion of the UNION, queries ChangeView table for the versionids.
     * The second portions goes after FileView table with versionid as key and 
     * the versionids shouldnt reside in the first portion of the union.
     * The second portion of the UNION is needed because, kathy's test stuff didnt have
     * versionids in changeHistory table.
    */
    public static void  notify(EmailInfo emailInfo, Set infos,Connection con) throws DatabaseException {

        try {
            String usersToEmailQuery = "SELECT DISTINCT c.pathName, u.address, c.versionSID FROM Users u, ChangeView c "+
                                       "WHERE CHAR(c.versionid) in ("+MetadataQueryUtilities.addAllVersionIds(infos)+") and c.userid=u.id " + 
                                       "UNION "+
                                       "SELECT DISTINCT P.Name, u1.address, V.SID FROM Users u1, Files F, Path P, Versions V "+
                                       "WHERE CHAR(F.nuVersionid) in ("+MetadataQueryUtilities.addAllVersionIds(infos)+") and P.id=F.nuPathId and CHAR(V.id)=CHAR(F.nuVersionId) and  V.userid=u1.id and P.Name NOT IN " + 
                                       "(SELECT DISTINCT c.pathName FROM Users u, ChangeView c "+
                                       "WHERE CHAR(c.versionid) in ("+MetadataQueryUtilities.addAllVersionIds(infos)+") and c.userid=u.id)";  



            Statement userEmailStatement = con.createStatement();

            ResultSet usersResultSet = userEmailStatement.executeQuery(usersToEmailQuery);

            List infosList = Arrays.asList(infos.toArray());
            Collections.sort(infosList, FileInfo.BASIC_FILENAME_COMPARATOR);

            while (usersResultSet.next()) {
                String onePart = usersResultSet.getString(1);


                int index = Collections.binarySearch(infosList,MetadataQueryUtilities.makeInfoFromName(onePart),FileInfo.BASIC_FILENAME_COMPARATOR);
                FileInfo info = (FileInfo)infosList.get(index);

                String oneEmail = usersResultSet.getString(2);
                String oneVersionSID = usersResultSet.getString(3);
                String oneVersion = info.getVersion();

                Vector destinationVector = new Vector();
                destinationVector.addElement(oneEmail);
                emailInfo.setMailToVector(destinationVector);


                for (Iterator iterb= info.getMetadata().entrySet().iterator();iterb.hasNext();) {
                    Map.Entry entry  = (Map.Entry)iterb.next();                                                          
                    emailInfo.addContent(" "+ onePart + "         " + oneVersion +  "        " + oneVersionSID + "           "  + ((String)entry.getKey()).trim() + "         " + ((String)entry.getValue()).trim());
                }
                // emailInfo.addContent("in release " + release);
                emailInfo.addContent("_____________________________________________________________________\n");
                EmailClient.sendEmail(emailInfo); 
                emailInfo.clearContent();
            }

            usersResultSet.close();
            userEmailStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred notifying  the metadata changes.",se); //an unmarshall exception occurred when SQLException was sent across.
        }

    }



}
