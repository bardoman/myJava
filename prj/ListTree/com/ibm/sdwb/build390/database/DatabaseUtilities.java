package com.ibm.sdwb.build390.database;

import java.sql.*;
import java.io.*;
import java.util.*;

public class DatabaseUtilities {

	private String dbUrl = null;
	private String dbUserId = null;
	private String dbPassword = null;
	private static String DB2TABLEDESCRIPTION = "SYSCAT.COLUMNS";
	private static String DB2TRIGGERS = "SYSCAT.TRIGGERS";


	static {
		refreshDBDriver();
	}

	private static void refreshDBDriver() {
		try {
			Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter exceptionHolder = new PrintWriter(new StringWriter());
			e.printStackTrace(exceptionHolder);
			exceptionHolder.flush();
			try {
				BufferedWriter logWrite = new BufferedWriter(new FileWriter("opServerErr.log"));
				logWrite.write(new java.util.Date().toString() + " " + exceptionHolder.toString());
				logWrite.newLine();
				logWrite.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			exceptionHolder.close();
		}
	}

	public DatabaseUtilities(String databaseName, String databaseUser, String databasePassword) {
		dbUserId = databaseUser;
		dbPassword = databasePassword;
		dbUrl = "jdbc:db2:"+databaseName;
		DriverManager.setLoginTimeout(60);
	}


	public Connection getDBConnection(boolean autoLogException) throws DatabaseException{
		try {
			Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
			
			Connection connection;

			if (autoLogException) {
				connection = new LoggingConnection(DriverManager.getConnection(dbUrl, dbUserId, dbPassword));
			} else {
				connection = DriverManager.getConnection(dbUrl, dbUserId, dbPassword);
			}
			connection.setTransactionIsolation(connection.TRANSACTION_READ_UNCOMMITTED);

			return connection;
		} catch (SQLException se) {
			throw new DatabaseException("An error occured connecting with the database.", se);
		} catch (ClassNotFoundException cnfe) {
			throw new DatabaseException("An error occured loading the database driver.", cnfe);
		} catch (IllegalAccessException iae) {
			throw new DatabaseException("An error occured instantiating the database driver.", iae);
		} catch (InstantiationException  ie) {
			throw new DatabaseException("An error occured instantiating the database driver.", ie);
		}

	}

    public void closeDBConnection(Connection con) throws DatabaseException{
        try {
            con.rollback();
            con.close();
        }
        catch(SQLException se) {
            throw new DatabaseException("An error occured closing the database connection.", se);
        }
    }

	public Statement getStatement(Connection con) throws DatabaseException{
		try {
			return con.createStatement();
		} catch (SQLException se) {
			throw new DatabaseException("An error occured creating a statement in the database.", se);
		}
	}

	public String createTemporaryTable(Connection con, String tableSpecification) throws DatabaseException{
		try {
			con.setAutoCommit(false);
			boolean done = false;
			Random randSource = new Random();
			String tableName = null;
			SQLException exceptionEncountered = null;
			for  (int i = 0; i < 20 & !done; i++) {
				done = true;
				exceptionEncountered = null;
				// determine the value for this keyword
				Statement configValueStatement = con.createStatement();
				tableName = "TEMP"+ Integer.toString(Math.abs(randSource.nextInt()));
				String valueQuery = "CREATE TABLE "+tableName+" ("+tableSpecification+")";
				try{
					configValueStatement.executeUpdate(valueQuery);
				}catch (SQLException se){
					done = false;
					exceptionEncountered = se;
				}
				configValueStatement.close();
			}
			if (exceptionEncountered != null) {
				throw exceptionEncountered;
			}
			return tableName;
		}catch (SQLException se){
			throw new DatabaseException ("An error occured creating the temporary table", se);
		}
	}

	private void removeTemporaryTable(Connection con)throws DatabaseException{
		try {
			con.rollback();
		}catch (SQLException se){
			throw new DatabaseException("An error occured removing the database changes.", se);
		}
	}

	public List getColumnsForTable(String tableName, String familyName, Connection con) throws SQLException{
		String query="SELECT COLNAME, COLNO FROM "+DB2TABLEDESCRIPTION+" WHERE tabname='"+tableName.toUpperCase()+"' and tabschema='"+familyName.toUpperCase()+"'";
		Statement tableColumnStatement = con.createStatement();
		ResultSet results = tableColumnStatement.executeQuery(query);
		ArrayList tempIndexs = new ArrayList();
		ArrayList tempCols = new ArrayList();
		while(results.next()) {
			String columnName = results.getString("COLNAME");
			int columnIndex = results.getInt("COLNO");
			tempIndexs.add(new Integer(columnIndex));
			tempCols.add(columnName);
		}
		List columnList = new ArrayList();
		for(int i = 0; i < tempIndexs.size(); i++) {
			Iterator indexIterator = tempIndexs.iterator();
			Iterator columnIterator = tempCols.iterator();
			while(((Integer)indexIterator.next()).intValue() != i ) {
				columnIterator.next();
			}
			columnList.add(columnIterator.next());

		}
                tableColumnStatement.close();
		return columnList;
	}

	public String getTriggerDefinition(String triggerName, String familyName, Connection con) throws SQLException{
		String query="SELECT TEXT FROM "+DB2TRIGGERS+" WHERE trigname='"+triggerName.toUpperCase()+"' and TRIGSCHEMA='"+familyName.toUpperCase()+"'";
		Statement triggerStatement = con.createStatement();
		ResultSet results = triggerStatement.executeQuery(query);
		String defString = null;
		if(results.next()) {
			defString = results.getString("TEXT");
		}
                triggerStatement.close();
		return defString;
	}

    public void createTriggerIfItHasChanged(String triggerName, String triggerCreationString, String famName, Connection con) throws SQLException{
		String triggerCommand = "CREATE TRIGGER "+triggerName+" "+triggerCreationString;
		String currentTriggerCommand = getTriggerDefinition(triggerName, famName, con);
		if(!triggerCommand.equals(currentTriggerCommand)) {
			if(currentTriggerCommand !=null) {
				String dropCommand = "DROP TRIGGER "+triggerName;
				con.createStatement().executeUpdate(dropCommand);
			}
			con.createStatement().executeUpdate(triggerCommand);
		}
    }

    public void createTableIfItDoesntExist(String tableName, String tableSpecification, String famName, Connection con) throws SQLException{
		List currentTableColumns = getColumnsForTable(tableName, famName, con);
		if(currentTableColumns.isEmpty()) {
			String tableCreateCommand = "CREATE TABLE "+tableName+" "+tableSpecification;
			con.createStatement().executeUpdate(tableCreateCommand);
		}
    }
}
