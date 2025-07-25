package com.ibm.sdwb.build390.database;

import java.util.*;
import java.sql.*;
import java.rmi.*;
import java.io.*;

public class LoggingStatement implements java.sql.Statement {
    private Statement realStatement = null;
    BufferedWriter logOfStatement = null;

    LoggingStatement(Statement tempRealStatement, BufferedWriter tempLogOfStatement){
        realStatement = tempRealStatement;
        logOfStatement = tempLogOfStatement;
    }

    public void cancel() throws SQLException{
        realStatement.cancel();
    }

    public ResultSet executeQuery(String sql) throws SQLException{
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.executeQuery(sql);
    }

    public boolean execute(String sql) throws SQLException{
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.execute(sql);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.execute(sql,columnNames);

    }

    public boolean execute(String sql,
                       int autoGeneratedKeys) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.execute(sql,autoGeneratedKeys);

    }

    public boolean execute(String sql,
                       int[] columnIndexes) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.execute(sql,columnIndexes);

    }




    public int executeUpdate(String sql) throws SQLException{
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.executeUpdate(sql);
    }

    public int executeUpdate(String sql,int autoGeneratedKeys) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.executeUpdate(sql,autoGeneratedKeys);

    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            logOfStatement.write(sql);
            logOfStatement.newLine();
        } catch (IOException ioe) {
// Ken we aren't going to do anything here, because 1, it's just logging, and 2, there's no place to put it
        }
        return realStatement.executeUpdate(sql, columnNames);
    }

    public void close() throws SQLException{
        realStatement.close();
    }

    public int getMaxFieldSize() throws SQLException{
        return realStatement.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException{
        realStatement.setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException{
        return realStatement.getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException{
        realStatement.setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException{
        realStatement.setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException{
        return realStatement.getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException{
        realStatement.setQueryTimeout(seconds);
    }

    public SQLWarning getWarnings() throws SQLException{
        return realStatement.getWarnings();
    }

    public void clearWarnings() throws SQLException{
        realStatement.clearWarnings();
    }

    public void setCursorName(String name) throws SQLException{
        realStatement.setCursorName(name);
    }

    public ResultSet getResultSet() throws SQLException{
        return realStatement.getResultSet();
    }

    public int getUpdateCount() throws SQLException{
        return realStatement.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException{
        return realStatement.getMoreResults();
    }
    //jdk1.2.2
    public void setFetchDirection(int A){
    }
    public int getFetchDirection(){
        return 0;
    }

    public void setFetchSize(int B){

    }
    public int getFetchSize(){
        return 0;
    }

    public int getResultSetConcurrency(){
        return 0;
    }
    public int getResultSetType(){
        return 0; 
    }

    public void addBatch(String Q){
    }

    public void clearBatch(){
    }

    public int[] executeBatch(){
        return null;
    }

    public Connection getConnection(){
        return null;
    }

    public boolean getMoreResults(int current) throws java.sql.SQLException {
        return realStatement.getMoreResults(current);
    } 

    public ResultSet getGeneratedKeys() throws java.sql.SQLException {
        return realStatement.getGeneratedKeys();
    }

    public int getResultSetHoldability() throws SQLException {
        return realStatement.getResultSetHoldability();
    }


}
