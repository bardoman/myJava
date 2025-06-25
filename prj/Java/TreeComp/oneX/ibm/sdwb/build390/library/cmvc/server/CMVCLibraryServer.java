package com.ibm.sdwb.build390.library.cmvc.server;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.* ;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.database.*;
import com.ibm.sdwb.build390.library.*;
import com.ibm.sdwb.build390.library.cmvc.*;
import com.ibm.sdwb.build390.library.cmvc.metadata.server.*;
import com.ibm.sdwb.build390.utilities.mail.EmailInfo;
import com.ibm.sdwb.build390.utilities.SQLUtilities;

public class CMVCLibraryServer extends UnicastRemoteObject implements CMVCLibraryServerInterface, MetadataServerOperationsInterface {

    private static final String DB_DRIVER_NAME = "COM.ibm.db2.jdbc.app.DB2Driver";

    private DatabaseUtilities dbConnections = null;
    private String processServerName = null;
    private Logger activityLogger = null;

    public CMVCLibraryServer(String tempProcessServerName, Logger tempLogger, DatabaseUtilities tempDb) throws RemoteException {
        dbConnections = tempDb;
        processServerName = tempProcessServerName;
        activityLogger = tempLogger;
        try {
            new InstallationHandler(dbConnections, processServerName);
        } catch (SQLException sqe) {
            throw new RemoteException("SQL problem running installation routines", sqe);
        } catch (DatabaseException de) {
            throw new RemoteException("Database problem running installation routines", de);
        }
    }

    public Set getPartlist(CMVCSourceInfo sourceInfo) throws RemoteException{
        Connection con=null;
        try {
            con = dbConnections.getDBConnection(true);
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            } catch (SQLException se) {
                activityLogger.log(Level.SEVERE, "Error setting transaction isolation", se);
                throw new RemoteException("Error getting the partlist\nDebug:"+SQLUtilities.getSQLExceptionAsString(se));
            }
            CMVCPartlistGenerator partlistGen = new CMVCPartlistGenerator(dbConnections);
            String partlistTableName = partlistGen.populatePartlistTable(con, sourceInfo.isIncludingCommittedBase(), sourceInfo instanceof CMVCLevelSourceInfo, sourceInfo.getName(), sourceInfo.getRelease(), sourceInfo.getRestrictions());
            Set returnSet =  partlistGen.getPartlist(con, partlistTableName, true, true, true);
            dbConnections.closeDBConnection(con);
            return returnSet;
        } catch (DatabaseException de) {
            activityLogger.log(Level.SEVERE, "Error getting the partlist", de);
            RemoteException re = null;
            if (con != null) {
                re = new RemoteExceptionWithLog(de, con.toString() + "\n" + SQLUtilities.getSQLExceptionAsString((SQLException)de.getOriginalException()));
            } else {
                re = new RemoteException("Error getting partlist", de);
            }
            throw re;
        }
    }


    public Set  populateMetadataMapFieldOfPassedInfos(Set fileInfoSet)  throws RemoteException  {
        Connection con=null;
        try {
            con = dbConnections.getDBConnection(true);

            Map collapsedMapByProject = MetadataQueryUtilities.collapseByProject(fileInfoSet);


            for (Iterator iter=collapsedMapByProject.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                String release = (String)entry.getKey();
                Set    infosSetByRelease =  (Set)entry.getValue();

                VersionPopulator vpop = new VersionPopulator(con);
                vpop.populate(release,infosSetByRelease);
                MetadataPopulator populater = new MetadataPopulator(con);
                populater.populate(release,infosSetByRelease);
            }

            dbConnections.closeDBConnection(con);


        } catch (DatabaseException de) {
            activityLogger.log(Level.SEVERE, "Error populating metadata", de);
            RemoteException re = null;
            if (con != null) {
                re = new RemoteExceptionWithLog(de.getMessage(), con.toString() + "\n" + SQLUtilities.getSQLExceptionAsString((SQLException)de.getOriginalException()));
            } else {
                re = new RemoteException("Error populating metadata",de);
            }
            throw re;
        }
        return fileInfoSet;
    }





    public Set  storeMetadataValuesFromPassedInfos(Set fileInfoSet )   throws RemoteException {
        Connection con=null;
        try {
            con = dbConnections.getDBConnection(true);
            Map collapsedMapByProject = MetadataQueryUtilities.collapseByProject(fileInfoSet);

            for (Iterator iter=collapsedMapByProject.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                String release = (String)entry.getKey();
                Set    infosSetByRelease =  (Set)entry.getValue();

                VersionPopulator vpop = new VersionPopulator(con);
                vpop.populate(release,infosSetByRelease);
                vpop.delete(fileInfoSet);
                MetadataPopulator populater = new MetadataPopulator(con);
                populater.create(fileInfoSet);

                EmailInfo emailInfo = new EmailInfo();
                emailInfo.setMailFromHost("CMVCServer");
                emailInfo.setMailFromUserName(processServerName);
                emailInfo.setSubject("Metadata inserted  for the following part ");
                //mail.addContent("\nMetadata replaced for the following criteria:" + criteria);  
                //mail.addContent("\nwith " + replace +"\n");
                emailInfo.addContent("_____________________________________________________________________\n");
                emailInfo.addContent("Library Pathname  Version   VersionSID MetadataKeyword MetadataValue \n");
                emailInfo.addContent("_____________________________________________________________________\n");
                //add the set stuff.
                // emailInfo.addContent("release ": + release);
                //add the set stuff.

                MetadataChangeNotifier.notify(emailInfo,fileInfoSet,con);
            }

            dbConnections.closeDBConnection(con);

        } catch (DatabaseException de) {
            activityLogger.log(Level.SEVERE, "Error storing metadata", de);
            RemoteException re = null;
            if (con != null) {
                re = new RemoteExceptionWithLog(de, con.toString() + "\n" + SQLUtilities.getSQLExceptionAsString((SQLException)de.getOriginalException()));
            } else {
                re = new RemoteException("Error storing metadata", de);
            }
            throw re;
        }
        return fileInfoSet;

    }


    public Set  updateMetadataValuesInStorageFromPassedInfos(Set fileInfoSet )   throws RemoteException  {
        Connection con=null;
        try {
            con = dbConnections.getDBConnection(true);
            MetadataPopulator populator = new MetadataPopulator(con);
            populator.update(fileInfoSet);

            EmailInfo emailInfo = new EmailInfo();
            emailInfo.setMailFromHost("CMVCServer");
            emailInfo.setMailFromUserName(processServerName);
            emailInfo.setSubject("Metadata replaced  for the following part ");
            //mail.addContent("\nMetadata replaced for the following criteria:" + criteria);  
            //mail.addContent("\nwith " + replace +"\n");
            emailInfo.addContent("_____________________________________________________________________\n");
            emailInfo.addContent("Library Pathname  Version   VersionSID MetadataKeyword MetadataValue \n");
            emailInfo.addContent("_____________________________________________________________________\n");

            MetadataChangeNotifier.notify(emailInfo,fileInfoSet,con);

            dbConnections.closeDBConnection(con);
        } catch (DatabaseException de) {
            activityLogger.log(Level.SEVERE, "Error updating metadata", de);
            RemoteException re = null;
            if (con != null) {
                re = new RemoteExceptionWithLog(de, con.toString() + "\n" + SQLUtilities.getSQLExceptionAsString((SQLException)de.getOriginalException()));
            } else {
                re = new RemoteException("Error updating metadata", de);
            }
            throw re;
        }
        return fileInfoSet;

    }
}
