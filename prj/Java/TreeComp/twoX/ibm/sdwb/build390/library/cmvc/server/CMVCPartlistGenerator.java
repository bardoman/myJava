package com.ibm.sdwb.build390.library.cmvc.server;

import java.sql.*;
import java.rmi.*;
import java.util.*;
import com.ibm.sdwb.build390.*;
import com.ibm.sdwb.build390.info.FileInfo;
import com.ibm.sdwb.build390.utilities.*;
import com.ibm.sdwb.build390.database.*;
import com.ibm.sdwb.build390.library.cmvc.ComponentAndPathRestrictions;

public class CMVCPartlistGenerator {

	private DatabaseUtilities dbUtils = null;
	private static Random randomSource = new Random();

	CMVCPartlistGenerator(DatabaseUtilities tempUtils){
		dbUtils = tempUtils;
	}

/* here we declare the table we will be storing the partlist in.
Since we shall have to make many alterations to it, we just store the minimum information necessary
to identify a file/version set.   Then, once we have massaged the set of file/version numbers into
just what we want, we can run queries on the whole set to get the information about them we require.
*/
    private String createPartlistTable(Connection con)throws DatabaseException{
        String tableSpecification = "fileName VARCHAR(195), fileId INTEGER, versionId INTEGER, changeDate VARCHAR(25) ";
        return dbUtils.createTemporaryTable(con, tableSpecification);
    }

    public String populatePartlistTable(Connection con, boolean includeCommittedBase, boolean levelQuery, String levelOrTrack, String release, ComponentAndPathRestrictions restrictions) throws DatabaseException{
// Ken 4/23/01  Don't bother synchronization.  Yes, several people could hit this at once.  They will all get the same answer, so the overwrites won't
// hurt anything.  Worse case, you get a little extra work on first batch of queries.
        String tableName = createPartlistTable(con);
        addDeltaPartsToTable(tableName, con, levelQuery, levelOrTrack, release);
        if (includeCommittedBase) {
            addCommittedPartsToTable(tableName, con, levelQuery, levelOrTrack, release);
        }
        cleanUnusedPartsFromTable(tableName, con, restrictions);
        return tableName;
    }

    public Set getPartlist(Connection con, String tableName, boolean includeScode, boolean includeDisttype, boolean includeCodepage)throws DatabaseException{
        String partlistQueryString = "SELECT DISTINCT myTab.fileName as pathname, P.name as extractName, V.SID as SID, CP.name as component, C.type as changeType, V.verDate as changeDate, F.type as filetype, partOwner.address as OwnerEmail, lastChanger.address as ChangerEmail, v.versize as fileSize, c.releaseName as releaseName ";
        if (includeScode) {
            partlistQueryString += ", F.scode as scode";
        }
        if (includeDisttype) {
            partlistQueryString += ", F.disttype as disttype";
        }
        if (includeCodepage) {
            partlistQueryString += ", F.codepage as codepage";
        }
        partlistQueryString +=  " FROM Versions V, Components CP, Path P, ChangeView C, Files F, Users partOwner, Users lastChanger,  "+tableName+" myTab "+
                                "WHERE F.id=myTab.fileId "+
                                "and C.fileId=F.id "+
                                "and P.id=F.nuPathId "+
                                "and C.addDate=myTab.changeDate "+
                                "and myTab.changeDate != '0' " +
                                "and V.id=myTab.versionId " +
                                "and CP.id=F.compId " +
                                "and lastChanger.id=C.userId " +
                                "and partOwner.id=F.ownerId " +

                                "UNION "+
                                "SELECT DISTINCT myTab.fileName as pathname, P.name as extractName, V.SID as SID, CP.name as component, 'UNTYPED' as changeType, V.verDate as changeDate, F.type as fileType, partOwner.address as OwnerEmail, '' as ChangerEmail, v.versize as fileSize, F.releaseName as releaseName ";
        if (includeScode) {
            partlistQueryString += ", F.scode as scode";
        }
        if (includeDisttype) {
            partlistQueryString += ", F.disttype as disttype";
        }
        if (includeCodepage) {
            partlistQueryString += ", F.codepage as codepage";
        }
        partlistQueryString +=  " FROM FileView F, Versions V, Path P, Components CP,  Users partOwner, "+tableName+" myTab "+
                                "WHERE F.id=myTab.fileId "+
                                "and P.id=F.nuPathId "+
                                "and myTab.changeDate = '0' " +
                                "and V.id=myTab.versionId " +
                                "and CP.id=F.compId " +
                                "and partOwner.id=F.ownerId ";
        Set partlistSet = new HashSet();
		Map metadataMap = getMetadata(con,tableName);
		Set usedMainframeNames = new HashSet();
        try {
            Statement partlistStatement = con.createStatement();
            ResultSet partsFound = partlistStatement.executeQuery(partlistQueryString);
            while (partsFound.next()) {
                String dir;
                String baseName;
                String pathName = partsFound.getString("pathname");
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
                String scode = null;
                String distType = null;
                String codePage = null;
                if (includeScode) {
                    scode = partsFound.getString("scode");
                }
                if (includeDisttype) {
                    distType = partsFound.getString("disttype");
                }
                if (includeCodepage) {
                    codePage = partsFound.getString("codepage");
                }
                FileInfo newPartInfo = new FileInfo(dir,baseName);
				newPartInfo.setProject(partsFound.getString("releaseName"));
				newPartInfo.setVersion(partsFound.getString("SID"));
				newPartInfo.setDate(partsFound.getString("changeDate"));
				newPartInfo.setTypeOfChange(partsFound.getString("changeType"));
				newPartInfo.setFileType(partsFound.getString("filetype"));
				newPartInfo.setCodePage(codePage);
				newPartInfo.setDistType(distType);
				newPartInfo.setSCode(scode);
                newPartInfo.setOwnerEmail(partsFound.getString("OwnerEmail"));
                newPartInfo.setUpdaterEmail(partsFound.getString("ChangerEmail"));
                newPartInfo.setSize(partsFound.getInt("fileSize"));

				String tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
				while (usedMainframeNames.contains(tempMainframeName)) {
					tempMainframeName = "C" + (new String(Math.abs(randomSource.nextLong()) + "0000000")).substring(0, 7);
				};
				newPartInfo.setMainframeFilename(tempMainframeName);
				usedMainframeNames.add(tempMainframeName);
				
				Map onePartMap = (Map) metadataMap.get(pathName);
				if (onePartMap!=null) {
					Map oneVersionMap = (Map) onePartMap.get(newPartInfo.getVersion());
					if (oneVersionMap!=null) {
						newPartInfo.getMetadata().putAll(oneVersionMap);
					}
				}
                partlistSet.add(newPartInfo);

            }
            partlistStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred getting the list of parts.", se);
        }
        return partlistSet;

    }

    private Map getMetadata(Connection con, String tableName)throws DatabaseException{
        String metadataQueryString = "SELECT myTab.fileName as pathName, V.SID, md.metadatakeyword, md.metadatavalue, md.version as mdVersion "+
                                     "FROM  Versions V, Files F, Build390Metadata  md,"+tableName+" myTab "+
                                     "WHERE F.id=myTab.fileId "+
                                     "and V.id=myTab.versionId " +
                                     "and md.versionId=V.id";

        Map metadataHash = new HashMap();
        try {
            Statement metadataStatement = con.createStatement();
            ResultSet metadataFound = metadataStatement.executeQuery(metadataQueryString);
            while (metadataFound.next()) {
                String pathName = metadataFound.getString("pathname");
                String version = metadataFound.getString("SID");
                String mdKey = metadataFound.getString("metadatakeyword");
                String mdValue = metadataFound.getString("metadataValue");
                String mdVersion = metadataFound.getString("mdVersion");
                Hashtable fileHash = (Hashtable) metadataHash.get(pathName);
                if (fileHash == null) {
                    fileHash=new Hashtable();
                    metadataHash.put(pathName, fileHash);
                }
                Hashtable verHash = (Hashtable) fileHash.get(version);
                if (verHash == null) {
                    verHash=new Hashtable();
                    fileHash.put(version, verHash);
                }
                String newVersion = ParsingFunctions.stripNonNumeric(mdVersion);

                if (!verHash.containsKey(mdKey.trim())) {
                    verHash.put(mdKey.trim(), mdValue.trim());           
                }
// This code lets us get the latest metadata version and use it as the version for all the metadata
                if (verHash.get(MBConstants.METADATAVERSIONKEYWORD)!=null) {
                    String oldVersion = (String) verHash.get(MBConstants.METADATAVERSIONKEYWORD);
                    if (newVersion.compareTo(oldVersion) < 0) {
                        newVersion = oldVersion;
                    }
                }
                verHash.put(MBConstants.METADATAVERSIONKEYWORD, newVersion);

            }
            metadataStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred getting the metadata for the parts.", se);
        }
        return metadataHash;
    }


    private void addDeltaPartsToTable(String tableName, Connection con, boolean levelQuery, String levelOrTrack, String release) throws DatabaseException{
        String deltaPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate) ";
        deltaPartsInsertCommand += "SELECT DISTINCT C.pathname, Ftab.fileID, C.versionId, C.addDate ";
        deltaPartsInsertCommand += "FROM ChangeView C, ";
/* 	We use the add date here instead of Version.changeDate because some cahnges, like renames and
    deletes, don't change the version, so there's no way to know in what order they occurred.
*/
        deltaPartsInsertCommand +=   "(SELECT DISTINCT max(C2.addDate) AS addDate, C2.fileid AS fileId ";
        deltaPartsInsertCommand +=    "FROM ChangeView C2 ";
        if (levelQuery) {
            deltaPartsInsertCommand +=  ", LevelMemberView LM " +
                                        "WHERE LM.levelName='"+levelOrTrack+"' "+
                                        "and LM.releasename='"+release+"' "+
                                        "and LM.trackId=C2.trackId ";
        } else {
            deltaPartsInsertCommand += "WHERE C2.defectname='"+levelOrTrack+"' "+
                                       "and C2.releasename='"+release+"' ";
        }
        deltaPartsInsertCommand +=  "GROUP BY Fileid";
        deltaPartsInsertCommand += ") AS FTab ";
        deltaPartsInsertCommand +=  "WHERE FTab.addDate=C.addDate and C.fileId=FTab.fileId ";
        try {
            Statement deltaPartsInsertStatement = con.createStatement();
            deltaPartsInsertStatement.executeUpdate(deltaPartsInsertCommand);
            deltaPartsInsertStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred inserting the delta parts into the partlist table.", se);
        }
    }


    private void addCommittedPartsToTable(String tableName, Connection con, boolean levelQuery, String levelOrTrack, String release) throws DatabaseException{
        if (isCommittedOrCompleteLevel(levelQuery, levelOrTrack, release, con)) {
/*
add all parts that are in a prior committed level.
pick the latest one in each track so we keep the table smaller.
We'll strip down the table to the latest when they've all been added.

*/          
            String versionedCommittedPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate)  ";
            versionedCommittedPartsInsertCommand += "SELECT P.name, F.ID, C.versionId, C.addDate ";
            versionedCommittedPartsInsertCommand += "FROM Changes C, Path P, Releases R, Files F, LevelMembers LM, Levels L1, Levels L2 ";
            versionedCommittedPartsInsertCommand += "WHERE L1.name='"+levelOrTrack+"' "+
                                                    "and R.name='"+release+"' "+
                                                    "and L1.releaseid=R.id "+
                                                    "and L2.releaseid=R.id "+
                                                    "and L1.commitDate>L2.commitDate "+
                                                    "and L2.id=LM.levelId "+
                                                    "and LM.trackId=C.trackId "+
                                                    "and F.releaseId=R.id "+
                                                    "and F.id=C.fileId "+
                                                    "and P.id=C.pathId "+
                                                    "and C.addDate=	(SELECT max(addDate) " +
                                                    "FROM Changes, LevelMembers " +
                                                    "WHERE Changes.trackId=LevelMembers.trackId " +
                                                    "and Changes.fileId=F.id "+
                                                    "and LevelMembers.levelId=L2.id)";

            try {
                Statement versionedCommittedPartsInsertStatement = con.createStatement();
                versionedCommittedPartsInsertStatement.executeUpdate(versionedCommittedPartsInsertCommand);
                versionedCommittedPartsInsertStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred inserting the versioned committed parts into the partlist table.", se);
            }

/*
Add the files that are in the Changes table, but were created before the track/level process was
turned on.
*/
            String preversionedCommittedPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate) ";
            preversionedCommittedPartsInsertCommand += "SELECT P.name, F.ID, V.Id, '0' ";
            preversionedCommittedPartsInsertCommand += "FROM Changes C, Path P, Releases R, Versions V, Versions V2, Files F, Levels L, Tracks T ";
            preversionedCommittedPartsInsertCommand += "WHERE F.releaseId=R.id "+
// make sure we don't have this in the table already.
//															"and 0 = (SELECT COUNT(*) FROM "+tableName+" myTab WHERE myTab.fileId=F.id) "+
                                                       "and L.name='"+levelOrTrack+"' "+
                                                       "and L.releaseId=R.id "+
                                                       "and R.name='"+release+"' "+
                                                       "and T.releaseId=R.id " +
                                                       "and C.fileId=F.id " +
                                                       "and P.id=F.pathId "+
                                                       "and C.trackId=T.id " +
                                                       "and V2.id=C.versionId "+
                                                       "and (F.DropDate is null or F.DropDate>L.commitDate) "+
// get the appropriate version number for the pre track/level version based on the change type.
                                                       "and ((C.type in ('rename', 'delete','recreate') and V.id=C.versionId) "+
                                                       "or (C.type not in ('rename', 'delete', 'recreate') and V.id=V2.previousId)) "+
// make sure we have the oldest change
                                                       "and C.addDate =  (SELECT MIN(C11.addDate) "+
                                                       "FROM Tracks T11, Changes C11 "+
                                                       "WHERE T11.releaseId=R.id "+
                                                       "and T11.id=C11.trackId "+
                                                       "and C11.fileId=F.id) "+
// make sure this file was created before track/level was turned on
                                                       "and 0= (SELECT COUNT(*) " +
                                                       "FROM Changes, Tracks " +
                                                       "WHERE Changes.fileId=F.id "+
                                                       "and Tracks.id=Changes.trackId "+
                                                       "and Tracks.releaseId=R.id "+
                                                       "and Changes.type in ('create', 'link')) ";

            try {
                Statement preversionedCommittedPartsInsertStatement = con.createStatement();
                preversionedCommittedPartsInsertStatement.executeUpdate(preversionedCommittedPartsInsertCommand);
                preversionedCommittedPartsInsertStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred inserting the preversioned committed parts into the partlist table.", se);
            }

/*
Add the files that are not in the Changes table.
*/
            String nonversionedCommittedPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate) ";
            nonversionedCommittedPartsInsertCommand += "SELECT P.name, F.ID, V.Id, '0' ";
            nonversionedCommittedPartsInsertCommand += "FROM Releases R, Versions V, Files F, Path P, Levels L ";
            nonversionedCommittedPartsInsertCommand += "WHERE F.releaseId=R.id "+
                                                       "and R.name='"+release+"' "+
                                                       "and L.name='"+levelOrTrack+"' "+
                                                       "and L.releaseId=R.id "+
                                                       "and P.id=F.pathId "+
// make sure we don't have this in the table already.
                                                       "and 0 = (SELECT COUNT(*) FROM "+tableName+" myTab WHERE myTab.fileId=F.id) "+
                                                       "and (F.DropDate is null or F.DropDate>L.commitDate) "+
                                                       "and V.id=F.versionId "+
                                                       "and 0= (SELECT COUNT(*) " +
                                                       "FROM Changes, Tracks " +
                                                       "WHERE Changes.fileId=F.id "+
                                                       "and Tracks.id=Changes.trackId "+
                                                       "and Tracks.releaseId=R.id) ";
            try {
                Statement nonversionedCommittedPartsInsertStatement = con.createStatement();
                nonversionedCommittedPartsInsertStatement.executeUpdate(nonversionedCommittedPartsInsertCommand);
                nonversionedCommittedPartsInsertStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred inserting the unversioned committed parts into the partlist table.", se);
            }
        } else {
/*
if we are building a committed base for a track or a non  committed/complete level, add all the committed files.
This gets the files that are in the changes table.
*/
            String versionedCommittedPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate) ";
            versionedCommittedPartsInsertCommand += "SELECT P.name, Ftab.fileID, C.versionId, C.addDate ";
            versionedCommittedPartsInsertCommand += "FROM Changes C, Path P, ";
            versionedCommittedPartsInsertCommand +=     "(SELECT max(tempC.addDate) AS addDate, F.id AS fileId ";
            versionedCommittedPartsInsertCommand +=     "FROM Changes tempC, Releases R, Files F, Tracks T "+
                                                        "WHERE R.name='"+release+"' "+
                                                        "and F.releaseId=R.id " +
                                                        "and F.id=tempC.fileId " +
                                                        "and tempC.trackId=T.id " +
                                                        "and T.releaseId=R.id " +
                                                        "and T.state in ('commit', 'complete') " +
                                                        "GROUP BY F.id) AS FTab ";
            versionedCommittedPartsInsertCommand += "WHERE C.fileId=ftab.fileid "+
                                                    "and P.id=C.pathId "+
// make sure we don't have this in the table already.
                                                    "and 0 = (SELECT COUNT(*) FROM "+tableName+" myTab WHERE myTab.fileId=Ftab.fileid) "+
                                                    "and C.addDate=Ftab.addDate ";
            try {
                Statement versionedCommittedPartsInsertStatement = con.createStatement();
                versionedCommittedPartsInsertStatement.executeUpdate(versionedCommittedPartsInsertCommand);
                versionedCommittedPartsInsertStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred inserts the versioned committed parts into the partlist table.", se);
            }
/*
Add the files that are not in the Changes table.
*/
            String nonversionedCommittedPartsInsertCommand = "INSERT INTO "+tableName+" (fileName, fileId, versionId, changeDate) ";
            nonversionedCommittedPartsInsertCommand += "SELECT P.name, F.ID, V.Id, '0' ";
            nonversionedCommittedPartsInsertCommand += "FROM  Releases R, Files F, Versions V, Path P ";
            nonversionedCommittedPartsInsertCommand += "WHERE F.releaseId=R.id "+
                                                       "and R.name='"+release+"' "+
// make sure we don't have this in the table already.
                                                       "and 0 = (SELECT COUNT(*) FROM "+tableName+" myTab WHERE myTab.fileId=F.id) "+
                                                       "and V.SID is not null " +
                                                       "and F.pathId=P.id "+
                                                       "and F.DropDate is null " +
                                                       "and F.versionid not in (select versionid from changes) " +
                                                       "and F.versionId=V.id ";
            try {
                Statement versionedCommittedPartsInsertStatement = con.createStatement();
                versionedCommittedPartsInsertStatement.executeUpdate(versionedCommittedPartsInsertCommand);
                versionedCommittedPartsInsertStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred inserts the versioned committed parts into the partlist table.", se);
            }
        }
    }

/*
    Unused parts are old version, wrong component, wrong directory, etc.
*/  
    private void cleanUnusedPartsFromTable(String tableName, Connection con, ComponentAndPathRestrictions restrictions) throws DatabaseException{
        List components = null;
		boolean includeComponents = false;
		List directories = null;
		boolean includeDirectories = false;
		if (restrictions!=null) {
			components = restrictions.getComponentList();
			includeComponents = restrictions.isComponentsIncluded();
			directories = restrictions.getPathList();
			includeDirectories = restrictions.isPathsIncluded();
		}
// remove the old versions from the table.
        try {
            String deletionString =  "DELETE FROM "+tableName+
                                     " WHERE CHAR(versionId) CONCAT changeDate in   (SELECT CHAR(T1.versionId) CONCAT T1.changeDate "+
                                     "FROM "+tableName+" T1 "+
                                     "WHERE T1.changeDate!= "+
                                     "(SELECT max(T2.changeDate) FROM "+tableName+" T2 " +
                                     "WHERE T2.fileId=T1.fileId)"+
                                     ")";
            Statement deleteOldVersionsStatement = con.createStatement();
            deleteOldVersionsStatement.executeUpdate(deletionString);
            deleteOldVersionsStatement.close();
        } catch (SQLException se) {
            throw new DatabaseException("An error occurred deleting the old version parts from the partlist table.", se);
        }
// restrict based on components
        if (components != null) {
            if (components.size() > 0) {
                try {
                    String componentRestriction = " (";
                    for (int i = 0; i < components.size(); i++) {
                        componentRestriction += "'"+(String) components.get(i)+"'";
                        if ((i+1) < components.size()) {
                            componentRestriction +=", ";
                        }
                    }
                    componentRestriction +=") ";

                    if (includeComponents) {
                        componentRestriction = " not in "+componentRestriction;
                    } else {
                        componentRestriction = " in "+componentRestriction;
                    }
                    String componentString =  "DELETE FROM "+tableName+
                                              " WHERE CHAR(versionId) CONCAT changeDate in   (SELECT CHAR(T1.versionId) CONCAT T1.changeDate "+
                                              "FROM "+tableName+" T1, Files F, Components C "+
                                              "WHERE T1.fileid=F.id and C.id=F.compId "+
                                              "and C.name "+componentRestriction+
                                              ")";
                    Statement deleteWrongComponentStatement = con.createStatement();
                    deleteWrongComponentStatement.executeUpdate(componentString);
                    deleteWrongComponentStatement.close();
                } catch (SQLException se) {
                    throw new DatabaseException("An error occurred deleting the incorrect component parts from the partlist table.", se);
                }
            }
        }
// restrict based on directories
        if (directories != null) {
            if (directories.size() > 0) {
                try {
                    String directoryRestriction = " (";
                    for (int i = 0; i < directories.size(); i++) {
                        String tempDirectory = ((String) directories.get(i)).trim();
                        tempDirectory = tempDirectory.replace('\\', '/');
                        if (!tempDirectory.endsWith("/")) {
                            tempDirectory += "/";
                        }
                        directoryRestriction += "(P.name like '" + tempDirectory +"%') ";
                        if ((i+1) < directories.size()) {
                            directoryRestriction +="OR ";
                        }
                    }
                    directoryRestriction +=") ";

                    if (includeDirectories) {
                        directoryRestriction = " not "+directoryRestriction;
                    }
                    String directoryString =  "DELETE FROM "+tableName+
                                              " WHERE CHAR(versionId) CONCAT changeDate in   (SELECT CHAR(T1.versionId) CONCAT T1.changeDate "+
                                              "FROM "+tableName+" T1, Files F, Path P "+
                                              "WHERE T1.fileid=F.id and F.pathId=P.id "+
                                              "and "+directoryRestriction+
                                              ")";
                    Statement deleteWrongDirectoriesStatement = con.createStatement();
                    deleteWrongDirectoriesStatement.executeUpdate(directoryString);
                    deleteWrongDirectoriesStatement.close();
                } catch (SQLException se) {
                    throw new DatabaseException("An error occurred deleting the incorrect directory parts from the partlist table.", se);
                }
            }
        }
    }

    private boolean isCommittedOrCompleteLevel(boolean levelQuery, String level, String release, Connection con) throws DatabaseException{
        if (!levelQuery) {
            return false;
        } else {
            boolean returnVal = false;
            String committedQueryString =   "SELECT COUNT(*) FROM LevelView L "+
                                            "WHERE L.name='"+level+"' "+
                                            "AND L.releaseName='"+release+"' "+
                                            "AND L.state in ('commit', 'complete')";
            try {
                Statement committedQueryStatement = con.createStatement();
                ResultSet levelsFound = committedQueryStatement.executeQuery(committedQueryString);
                if (levelsFound.next()) {
                    if (levelsFound.getInt(1) > 0) {
                        returnVal=true;
                    }
                }
                committedQueryStatement.close();
            } catch (SQLException se) {
                throw new DatabaseException("An error occurred determining the state of the target level.", se);
            }
            return returnVal;
        }
    }
}
