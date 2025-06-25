package com.ibm.sdwb.build390.library.cmvc.server;

import java.rmi.*;
import java.sql.*;
import java.net.*;
import com.ibm.sdwb.build390.database.*;
import com.ibm.sdwb.build390.user.authorization.AuthorizationException;


public class CMVCAuthorizationVerifier implements com.ibm.sdwb.build390.user.authorization.AuthorizationCheck{

    private transient DatabaseUtilities dbUtils = null;
    private String userName = null;
    private String userAddress = null;
    private String userLogin = null;
    private String userPassword = null;
    private boolean usePasswordAuthentication = true;

	public CMVCAuthorizationVerifier(String tempUserName, String tempUserAddress, String tempUserPassword, boolean tempUsePasswordAuthentication) {
        userName = tempUserName;
        userAddress = tempUserAddress;
        userLogin = System.getProperty("user.name");
        userPassword = tempUserPassword;
        usePasswordAuthentication = tempUsePasswordAuthentication;
	}

    public void setDatabaseUtilities(DatabaseUtilities tempDB){
        dbUtils = tempDB;
    }

	public boolean isAuthorizedTo(String authorityToCheck, String entityToCheckAgainst) throws AuthorizationException{

		Connection con = null;
		try {
			// connect with default id/password
			con = dbUtils.getDBConnection(true);

			boolean isAuthorized = checkAuthorization(authorityToCheck, entityToCheckAgainst, con);
			con.close();
			return isAuthorized;
		} catch (SQLException sql) {
			throw new AuthorizationException("Error completing user authorization check.",sql);
		} catch (DatabaseException db) {
			throw new AuthorizationException("Error processing user authentication routine.",db);
		}finally{
			try {
				if (con!=null) {
					con.close();
				}
			}catch (SQLException se){
// don't do anything here, this is just disaster recovery, if it doesn't work, no biggy.
			}
		}
	}

	private boolean checkAuthorization(String authorityToCheck, String component, Connection con) throws AuthorizationException{

		if (!checkValidUser(con)) {
			return false;
		}

		try {
			CMVCLibraryClientCalls libClient = new CMVCLibraryClientCalls();
			return  libClient.checkForAuthority(authorityToCheck, component, userName);
		} catch (com.ibm.sdwb.build390.GeneralError re) {
			throw new AuthorizationException("Error processing user authentication routine.",re);
		}
	}

	public boolean checkValidUser(Connection con) throws AuthorizationException{
		if (usePasswordAuthentication) {
			try {
				String encryptedPassword = null;
				
				Statement tempStatement = con.createStatement();
				String tempQuery = "SELECT UserPass.CMVCpw "+
									 "FROM UserPass, Users "+
									 "WHERE Users.login='"+userName+"' "+
									 "AND Users.id=UserPass.id " ;
				ResultSet tempRes = tempStatement.executeQuery(tempQuery);
				if (tempRes.next()) {
					String gotPass = tempRes.getString(1);
					tempStatement.close();
                    encryptedPassword = com.ibm.sdwb.cmvc.crypt.Crypt.encryptPassword(userName, userPassword); /* using cmvc's crypt api thing  */
				}else {
					tempStatement.close();
				}

				
				
				Statement accessCheckingStatement = con.createStatement();
				String accessQuery = "SELECT count(*) "+
									 "FROM UserPass, Users "+
									 "WHERE Users.login='"+userName+"' "+
									 "AND Users.id=UserPass.id " +
									 "AND UserPass.iteration = 0 "+
									 "AND CMVCpw='"+encryptedPassword+"'";
				int matchingPasswords = 0;
				ResultSet access = accessCheckingStatement.executeQuery(accessQuery);
				if (access.next()) {
					matchingPasswords = access.getInt(1);
				}
				accessCheckingStatement.close();
				if (matchingPasswords < 1) {
					throw new AuthorizationException(userName + " entered an invalid password.");
				}
			} catch (SQLException sql) {
				throw new AuthorizationException("An error occurred during user authorization.", sql);
			}
		}else {
			try {
				InetAddress clientAddress = InetAddress.getByName(userAddress);

				// determine if there is a host list entry for the user
				Statement accessCheckingStatement = con.createStatement();
				String accessQuery = "SELECT count(*) "+
									 "FROM Hosts, Users "+
									 "WHERE Users.login='"+userName+"' "+
									 "AND Users.id=Hosts.userId " +
									 "AND Hosts.name='"+clientAddress.getHostName()+"' "+
									 "AND Hosts.login='"+userLogin+"'";
				int hostListEntries = 0;
				ResultSet access = accessCheckingStatement.executeQuery(accessQuery);
				if (access.next()) {
					hostListEntries = access.getInt(1);
				}
				accessCheckingStatement.close();
				if (hostListEntries < 1) {
					throw new AuthorizationException(userName + " does not have a host list entry for "+userLogin +" from " + userAddress);
				}
			} catch (java.net.UnknownHostException uhe) {
				throw new AuthorizationException("An error occurred during user authorization.", uhe);
			} catch (SQLException sql) {
				throw new AuthorizationException("An error occurred during user authorization.", sql);
			}
		}
		return true;
	}
}
