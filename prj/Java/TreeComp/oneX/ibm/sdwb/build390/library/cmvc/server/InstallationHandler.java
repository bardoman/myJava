package com.ibm.sdwb.build390.library.cmvc.server;

import java.sql.*;
import java.util.*;
import com.ibm.sdwb.build390.database.*;

class InstallationHandler{

	private DatabaseUtilities dbUtils = null;

	private static String METADATATABLENAME = "Build390Metadata";
    private static final String DEBUGTABLE = "KenDEBUG";

	InstallationHandler(DatabaseUtilities tempDb, String famName) throws SQLException, DatabaseException{
		Connection con = tempDb.getDBConnection(false);
        con.setAutoCommit(true);
		dbUtils = tempDb;
		handleTables(con, famName);
		handleTriggers(con, famName);
	}

	private void handleTriggers(Connection con, String famName) throws SQLException{
		if(dbUtils.getTriggerDefinition("B390VerInsert", famName, con)!=null) {
			String dropCommand = "DROP TRIGGER B390VerInsert";
			con.createStatement().executeUpdate(dropCommand);
		}

        dbUtils.createTriggerIfItHasChanged("B390ChangeInsert", "AFTER INSERT ON Changes REFERENCING NEW AS newRow FOR EACH ROW MODE DB2SQL WHEN (EXISTS (SELECT DISTINCT bmd.version FROM Build390Metadata bmd, FileView f WHERE bmd.VERSIONID = f.nuversionid and newrow.fileid=f.id UNION SELECT DISTINCT bmd.version FROM Build390Metadata bmd, ChangeView cv WHERE cv.fileid=newRow.fileid and bmd.versionid=cv.versionid and cv.versionSID LIKE 'v.%')) INSERT INTO Build390Metadata (VERSIONID, VERSION, METADATAKEYWORD, METADATAVALUE) SELECT DISTINCT newRow.versionid, VERSION, METADATAKEYWORD, METADATAVALUE FROM Build390Metadata bmd, FileView F WHERE bmd.VERSIONID = f.nuversionid and newrow.fileid=f.id and 0=(SELECT COUNT(cv.versionid) FROM ChangeView cv WHERE cv.versionSID LIKE 'v.%' and cv.versionid<>newRow.versionid and cv.fileid=newRow.fileid) UNION SELECT DISTINCT newRow.versionid, VERSION, METADATAKEYWORD, METADATAVALUE FROM Build390Metadata bmd, ChangeView cv WHERE cv.fileId=newRow.fileId and bmd.versionid=cv.versionid and cv.versionSID LIKE 'v.%'", famName, con); 
        dbUtils.createTriggerIfItHasChanged("B390VerCon1Update",getVersionRenamingConcurrentToConcurrentUpdateTrigger() , famName, con); 
        dbUtils.createTriggerIfItHasChanged("B390VerCon2Update",getVersionRenamingConcurrentToRealUpdateTrigger() , famName, con); 
        dbUtils.createTriggerIfItHasChanged("B390VerDelete",getVersionDeleteTrigger() , famName, con); 
	}

	private void handleTables(Connection con, String famName) throws SQLException{
        dbUtils.createTableIfItDoesntExist(METADATATABLENAME,"(versionID INTEGER NOT NULL, VERSION TIMESTAMP, METADATAKEYWORD CHAR(20), METADATAVALUE VARCHAR(255))", famName, con);
        dbUtils.createTableIfItDoesntExist(DEBUGTABLE,"(one INTEGER NOT NULL, VERSION TIMESTAMP, ID1 INTEGER,ID2 INTEGER, prev1 integer, prev2 integer, SID VARCHAR(47),SID2 VARCHAR(47))", famName, con);
	}

    private String getVersionRenamingConcurrentToConcurrentUpdateTrigger(){
        String versionRename = new String();
        versionRename +=    "AFTER UPDATE OF SID ON Versions "+    // when the SID field of the Versions table has been updated
                            "REFERENCING OLD AS oldRow "+
                                "NEW AS newRow "+
                            "FOR EACH ROW MODE DB2SQL "+
                            "WHEN (newRow.SID LIKE 'v.%' AND newRow.SID<>oldRow.SID) "+ // if the new SID is v.something (which means it's concurrent) 
                            "BEGIN ATOMIC " + 
                                "DELETE FROM "+METADATATABLENAME+" WHERE versionid=oldrow.id; "+// delete the stuff by the new row since it's actually an older version
                                "UPDATE "+METADATATABLENAME+" "+// modify the metadata table
                                    "SET versionid=newRow.id "+// set the versionid of applicable rows to the new row id (because the first concurrent version has it's SID renamed.  So we need to grab the source from the latest version record, which matches this new SID)
                                    "WHERE versionid in "+
                                    "(SELECT DISTINCT id from versions where SID=newRow.SID AND id<>newRow.id); "+  // grab the id of the original lastest SID
                            "END";
        return versionRename;
    }   

    private String getVersionRenamingConcurrentToRealUpdateTrigger(){
        String versionRename = new String();
        versionRename +=    "AFTER UPDATE OF ID ON Versions "+    // when the ID field of the Versions table has been updated
                            "REFERENCING OLD AS oldRow "+
                                "NEW AS newRow "+
                            "FOR EACH ROW MODE DB2SQL "+
                                "UPDATE "+METADATATABLENAME+" "+// modify the metadata table
                                    "SET versionid=newRow.id "+// set the versionid of applicable rows to the new row id (because the first concurrent version has it's SID renamed.  So we need to grab the source from the latest version record, which matches this new SID)
                                    "WHERE versionid=oldRow.id ";  // grab the id of the original lastest SID
        return versionRename;
    }   


    private String getVersionDeleteTrigger(){
        String deleteTrigger = new String();
        deleteTrigger +=    "AFTER DELETE ON Versions "+
                            "REFERENCING OLD AS oldRow "+
                            "FOR EACH ROW MODE DB2SQL "+
                            "DELETE FROM "+METADATATABLENAME+" WHERE VERSIONID=oldRow.ID";
        return deleteTrigger;
    }
}
