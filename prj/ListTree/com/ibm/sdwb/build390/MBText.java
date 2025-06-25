package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBText class for the Build390 java client                    */
/*********************************************************************/
/* Updates:                                                          */
// 05/07/99 #322    Add rc help
// 06/10/99         Fixed MBClient_Log1 error msg
// 03/23/01         Defect 297 . the RC 8.7.9 were not documented
/*********************************************************************/

/** MBText contains the text messages that are used by the program so they can be centrally managed.
*   Each message should be titled <class>_name, where class is the using class */
public class MBText {

    /** MBClient help data */
    public static final String MBClient_Help = "Usage: Build390 <command> <keyword=value> </option> parmfile=<file_path>\n"+
                                               "Where:- command is the command to be executed.\n"+
                                               "      - keyword=value is a command parameter and it's value. Any number of\n"+
                                               "        these may be specified.\n"+
                                               "        The pw=<password> pair is the password for the mvs host session to be\n"+
                                               "        used, and is required.\n"+
                                               "      - /option is an optional keyword such as /debug, /trace\n"+
                                               "      - parmfile=<file_path> is an optional keyword that defines the path to a\n"+
                                               "        local file that contains keyword=value pairs and options.\n"+
                                               "        Values specified on the command line override those in the file.\n\n"+
                                               "      - use Build390 <command> /help to get help for a particular command\n"+
                                               "      - use Build390 helprc to get a list of return code definitions\n"+
                                               "      - use Build390 version to see the version and build date of the client";

    /** MBClient help data //#322 */
    public static final String MBClient_RC_Help = "These are the return codes that the client specifies on termination.\n"+
                                                  "0  - Successful completion.\n"+
                                                  "1  - An error occurred and the process was terminated, review the log file in\n"+
                                                  "    the 'misc' directory for details.\n"+
                                                  "2  - Syntax error, review the log file for details.\n"+
                                                  "3  - Password was not entered but is required. \n"+
                                                  "   - An invalid host password was entered.\n"+
                                                  "    WARNING: If you specify the password incorrectly 3 times in a row, \n"+
                                                  "    the MVS userid will be revoked.\n"+
                                                  "33 - An invalid library password was entered.\n"+
                                                  "4  - Setup information is not complete. Enter setup information.\n"+
                                                  "5  - An error occurred during a MVS host operation.\n"+
                                                  "6  - An error occurred during a source library operation.\n"+
                                                  "7  - An error occurred during a ftp operation.\n"+
                                                  "8  - An error occurred during a bps or service operation.\n"+
                                                  "9  - An error occurred during a registration operation.\n\n"+
                                                  "Trace and debug information is kept in the build390.log file in the 'misc' directory.";

    /** MBClient new setup msg */
    public static final String MBClient_Setup = "Creating new Setup";

    /** MBClient error msg */
    public static final String MBClient_Log1 = "Syntax error. You entered more than one command on the input line";

    /** MBClient error msg */
    public static final String MBClient_Log2 = "No setup information found";

    /** MBClient error msg */
    public static final String MBClient_Log3 = "No setup information found, you must run the gui and enter setup information";

    /** MBClient error msg */
    public static final String MBClient_Log4 = "Class not found exception occurred when trying to create the Setup object";

    /** MBClient error msg */
    public static final String MBClient_Log5 = "When trying to call the create an instance of the class ";

    /** MBClient error msg */
    public static final String MBClient_Log6 = "When trying to access an instance of the class";

    /** MBClient rc msg */
    public static final String MBClient_rc = "RC=";
}
