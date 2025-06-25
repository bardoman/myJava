package com.ibm.sdwb.build390.database;

public class DatabaseException extends com.ibm.sdwb.build390.MBBuildException {

    public DatabaseException(String errorMessage) {
        super("Database Error", errorMessage, com.ibm.sdwb.build390.MBConstants.DATABASEERROR);
    }

    public DatabaseException(String errorMessage, Exception e) {
        super("Database Error", errorMessage, e, com.ibm.sdwb.build390.MBConstants.DATABASEERROR);
    }
}
