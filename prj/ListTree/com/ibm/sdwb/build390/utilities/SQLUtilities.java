package com.ibm.sdwb.build390.utilities;
/*********************************************************************/
/* SQLUtilties                   class for the Build/390 client      */
/* Helper class for database operations.                             */
/*********************************************************************/
//02/11/2005 SDWB2395  Display filter pages in tab.
/*********************************************************************/
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.Calendar;

public class SQLUtilities {


    public static String getDatabaseInformation(Connection con) throws SQLException {
        StringBuffer info = new StringBuffer();
        if (con!=null) {
            DatabaseMetaData dbMetaData = con.getMetaData();
            info.append("Database    Name   : " + dbMetaData.getDatabaseProductName()+"\n");
            info.append("Database    Version: " + dbMetaData.getDatabaseProductVersion()+"\n");
        } else {
            info.append("Warning!:Unable to connect to database.\n");
        }
        return info.toString();

    }

    public static String getSQLExceptionAsString(SQLException ex) {
        StringBuffer err = new StringBuffer();
        if (ex!=null) {
            err.append("Message:"+ ex.getMessage ()+" SQLState:"+ ex.getSQLState () + " ErrorCode  :"+ ex.getErrorCode () +"\n");
            ByteArrayOutputStream exceptionBytes = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(exceptionBytes));
            err.append(exceptionBytes.toString());
        }
        return err.toString();

    }

    /** Just an easier way to not to repeat the below code everywhere to use timestamps 
     * This is compatible with DB2 's TIMESTAMP type.
     * The output eg:2005-02-24 19:53:20.251
     **/
    public static Timestamp getTimeStamp() {
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
        return timestamp;
    }


    // format the metadata string so it will fit and be processed by the SQL command properly
    public  static String padWithQuotes(String rawString) {
        String formattedValue  = new String();
        for (int i = 0; i < rawString.length(); i++) {
            String currentChar = rawString.substring(i, i+1);
            if (currentChar.equals("'")) {
                formattedValue += "''";
            } else {
                formattedValue += currentChar;
            }
        }
        return formattedValue;

    }
}

