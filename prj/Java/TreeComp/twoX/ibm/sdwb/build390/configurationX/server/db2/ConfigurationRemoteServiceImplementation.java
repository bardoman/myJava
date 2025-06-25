package com.ibm.sdwb.build390.configuration.server.db2;

import java.rmi.*;
import java.rmi.server.* ;
import java.rmi.registry.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.ibm.sdwb.build390.database.*;
import com.ibm.sdwb.build390.*;
import java.sql.*;
import com.ibm.sdwb.build390.utilities.SQLUtilities;
import com.ibm.sdwb.build390.user.authorization.*;

public class ConfigurationRemoteServiceImplementation extends UnicastRemoteObject implements ConfigurationRemoteServiceProvider {

	private static String CONFIGTABLENAME = "Build390Config";
    private Logger activityLogger = null;
    private String processServerName = null;
    private DatabaseUtilities dbUtils = null;

	public ConfigurationRemoteServiceImplementation(String tempProcessServerName, Logger tempLogger, DatabaseUtilities tempDb) throws RemoteException {
        activityLogger = tempLogger;
        dbUtils = tempDb;
		if (tempProcessServerName != null) {
			processServerName = tempProcessServerName.trim().toLowerCase();
		}
        activityLogger.info("Service activated");
        try {
            testAndHandleConfigurationTableExistance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
	}

	public Map getConfiguration(String tempProject, String tempSection, String tempKeyword) throws RemoteException {
        activityLogger.entering("ConfigurationSettingsRetrievalThread", "getProjectConfiguration");
        Map configurationSettings = new HashMap();
        String configQuery = "SELECT bc.realm, bc.section, bc.configKey, bc.configValue FROM Build390Config bc, releaseView rv WHERE bc.realm='RELEASE' and bc.link='"+tempProject+"' "+
                                "or bc.realm='COMPONENT' and bc.link=rv.compName and rv.name='"+tempProject+"' ";
        try {
            if (tempSection != null) {
                configQuery += " and bc.section='"+tempSection+"'";
            }
            if (tempKeyword != null) {
                configQuery += " and bc.configKey='"+tempKeyword+"'";
            }
            Connection configSettingsConnection = dbUtils.getDBConnection(true);
            try {
                Statement configSettingsStatement = configSettingsConnection.createStatement();
                ResultSet configSettingResultSet = configSettingsStatement.executeQuery(configQuery);
                int numberOfColumns = configSettingResultSet.getMetaData().getColumnCount();
                while (configSettingResultSet.next()) {
                    String realm = configSettingResultSet.getString(1);
                    String section = configSettingResultSet.getString(2);
                    String configKey = configSettingResultSet.getString(3);
                    String configValue = configSettingResultSet.getString(4);
                    HashMap realmHash = (HashMap) configurationSettings.get(realm);
                    if (realmHash == null) {
                        realmHash = new HashMap();
                        configurationSettings.put(realm, realmHash);
                    }
                    HashMap hashToUse = (HashMap) realmHash.get(section);
                    if (hashToUse == null) {
                        hashToUse = new HashMap();
                        realmHash.put(section, hashToUse);
                    }
                    hashToUse.put(configKey, configValue);
                }
            } finally {
                    configSettingsConnection.close();
            }
        } catch (DatabaseException de) {
            throw new RemoteException("Error getting the project configuration.\nDebug:Query:\n  " + configQuery);
        } catch (SQLException se2) {
            activityLogger.log(Level.WARNING, "Error getting project configuration", se2);
            throw new RemoteException("Error getting project configuration\nDebug:"+SQLUtilities.getSQLExceptionAsString(se2));
        }
        return configurationSettings;
    }

    public void setContiguration(String project, Map settingMap, com.ibm.sdwb.build390.user.authorization.AuthorizationCheck authCheck) throws RemoteException{

        try {
            Connection configSettingsConnection = dbUtils.getDBConnection(true);
            String component = getComponentForRelease(project);  

            if (authCheck instanceof com.ibm.sdwb.build390.library.cmvc.server.CMVCAuthorizationVerifier) {
                ((com.ibm.sdwb.build390.library.cmvc.server.CMVCAuthorizationVerifier) authCheck).setDatabaseUtilities(dbUtils);
            }

            if (!authCheck.isAuthorizedTo("S390Config", component)) {
                throw new RemoteException("User not authorized to update configuration information for project " + project);
            }
            // delete the old config info
            String deleteOrder = "DELETE FROM "+ CONFIGTABLENAME+" "+
                                 "WHERE realm='"+com.ibm.sdwb.build390.configuration.DictionaryOfConfigOptions.PROJECTREALM+"' "+
                                 "AND link='"+project+"'";

            Statement deleteStatement = configSettingsConnection.createStatement();
            deleteStatement.executeUpdate(deleteOrder);
            String configInsertCommand =  "INSERT INTO "+CONFIGTABLENAME+" (realm, link, section, configKey, configValue) "+
                                          "VALUES ";
            boolean valuesFound = false;
            for (Iterator realmKeys = settingMap.keySet().iterator(); realmKeys.hasNext();) {
                String realm = (String) realmKeys.next();
                Map realmMap = (Map) settingMap.get(realm);
                for (Iterator sectionKeys = realmMap.keySet().iterator(); sectionKeys.hasNext(); ) {
                    String section = (String) sectionKeys.next();
                    Map sectionMap = (Map) realmMap.get(section);
                    for (Iterator configKeys = sectionMap.keySet().iterator(); configKeys.hasNext(); ) {
                        String currKey = (String) configKeys.next();
                        String currVal = (String) sectionMap.get(currKey);
                        // format the config info to save it
                        String tempVal = new String();
                        for (int i = 0; i < currVal.length(); i++) {
                            String currentChar = currVal.substring(i, i+1);
                            if (currentChar.equals("'")) {
                                tempVal += "''";
                            } else {
                                tempVal += currentChar;
                            }
                        }
                        currVal = tempVal;
                        valuesFound = true;
                        configInsertCommand += "('"+com.ibm.sdwb.build390.configuration.DictionaryOfConfigOptions.PROJECTREALM+"','"+project+"','"+section+"','"+currKey.toUpperCase()+"','"+currVal+"')";
                        if (configKeys.hasNext()) {
                            configInsertCommand += ", ";
                        }
                    }
                    if (sectionKeys.hasNext()) {
                        configInsertCommand += ", ";
                    }
                }
            }
            if (valuesFound) {
                Statement insertStatement = configSettingsConnection.createStatement();
                insertStatement.executeUpdate(configInsertCommand);
                insertStatement.close();
            }
        }catch (AuthorizationException ae){
            activityLogger.log(Level.SEVERE, "Error authorizing the user",  ae);
            throw new RemoteException("Error authorizing the user\n"+ae);
        }catch (SQLException se){
            activityLogger.log(Level.SEVERE, "Error setting the project configuration",  se);
            throw new RemoteException("Error setting the project configuration\n"+SQLUtilities.getSQLExceptionAsString(se));
        }catch (DatabaseException de){
            activityLogger.log(Level.SEVERE, "Error connecting to the database",  de);
            throw new RemoteException("Error connecting to the database", de);
        }
    }

    public void testAndHandleConfigurationTableExistance() throws DatabaseException, SQLException{
        Connection con = dbUtils.getDBConnection(true);
        con.setAutoCommit(true);
        dbUtils.createTableIfItDoesntExist(CONFIGTABLENAME,"(realm VARCHAR(30) NOT NULL, link VARCHAR(40) NOT NULL, section VARCHAR(50) NOT NULL, configKey VARCHAR(100) NOT NULL, configValue VARCHAR(254), PRIMARY KEY (realm, link, section, configkey))", processServerName, con);
    }

    private String getComponentForRelease(String release) throws RemoteException{
        String component = null;
        String componentQuery = "SELECT compname FROM ReleaseView WHERE name='"+release+"'";
        try {
            Connection componentQueryConnection = dbUtils.getDBConnection(true);
            try {
                Statement componentQueryStatement = componentQueryConnection.createStatement();
                ResultSet componentQueryResultSet = componentQueryStatement.executeQuery(componentQuery);
                int numberOfColumns = componentQueryResultSet.getMetaData().getColumnCount();
                while (componentQueryResultSet.next()) {
                    component = componentQueryResultSet.getString(1);
                }
            } finally {
                    componentQueryConnection.close();
            }
        } catch (DatabaseException de) {
            throw new RemoteException("Error getting the project configuration.\nDebug:Query:\n  " + componentQuery);
        } catch (SQLException se2) {
            activityLogger.log(Level.WARNING, "Error getting project configuration", se2);
            throw new RemoteException("Error getting project configuration\nDebug:"+SQLUtilities.getSQLExceptionAsString(se2));
        }
        return component;
    }

}
