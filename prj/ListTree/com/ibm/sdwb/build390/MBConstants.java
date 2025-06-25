package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MBConstants class for the Build390 java client               */
/*********************************************************************/
/* Updates:                                                          */
// 03/17/99 Defect_247          Add site specific help hook
// 04/27/99 registration        Add rc for registration
// 04/29/99 UsersGuideName      Change users guide name for 2.2
// 05/24/99 service process     uncomment out ptf build type
// 01/17/2000 build.1M.log      Add archive filepath ,logfile extension variables.
// 10/14/2000 $ODECHG dummy     family/address  - doesnt exist in cmvc
// 05/12/2000 $ODECHG dummy     Add a variable INVALIDPASSWORDERROR
// 02/01/2001 $ODECHG dummy     make the family name as UpperCase
// 02/06/2001 $ODECHG dummy     make the family name as back to lower case
//03/26/2001 #AddDelta:  add delta driver type 
//11/02/02  Feat.SDWB1776:      BLD390 should use disttype and scode values from CMVC.
//09/23/04  PTM3735             make the programversion accessible via a Getter
/*********************************************************************/
import java.util.Vector;

/** MBConstants contains the constants that are used by the program so
* they can be centrally managed */
public class MBConstants {

    private static final String PROGRAMVERSION = "SDWB6.0.2.0"; 
    private static final String BUILDDATE  = "02/01/2008";

    public static final String MASCOTFILE = "doggy.gif";
    public static final String productName = "Build/390";






    public static final String COPYRIGHT1="(c) Copyright International Business Machines Corporation 1999-2007."; 
    public static final String COPYRIGHT2="All rights reserved."; 
    public static final String COPYRIGHT3="U.S. Government Users restricted rights"; 
    public static final String COPYRIGHT4="-- Use, duplication or disclosure is subject to restrictions set forth";
    public static final String COPYRIGHT5="in GSA ADP Schedule contract with IBM Corp."; 

    // ToolCount data
    public static final String Toolset  = "SDWB";
    public static final String Toolname = "Build390";

    // Return Codes
    /** RC for TCPIP connect failure */
    public  static final int Error_connect = 1001;
    /** RC for TCPIP unknown error */
    public  static final int Error_unknown = 1002;

    // Java VM Return Codes
    /** RC for successful completion  */
    public  static final int EXITSUCCESS = 0;
    /** RC for general error */
    public  static final int GENERALERROR = 1;
    /** RC for syntax error */
    public  static final int SYNTAXERROR = 2;
    /** RC for no password entered */
    public  static final int NOPASSWORDERROR = 3;
    /** RC for no password entered */
    public  static final int LIBRARY_PASSWORDERROR = 33;
    /** RC for no password entered */
    public  static final int INVALIDPASSWORDERROR = 3;
    /** RC for setup information not present, or not complete */
    public  static final int SETUPERROR = 4;
    /** RC for error that occured communicating with, or on, host */
    public  static final int HOSTERROR = 5;
    /** RC for setup information not present, or not complete */
    public  static final int LIBRARYERROR = 6;
    /** RC for errors in the ftp process */
    public  static final int FTPERROR = 7;
    /** RC for errors in the bps process */
    public  static final int SERVICEERROR = 8;
    /** RC for database problem */
    public  static final int DATABASEERROR = 10;

    /** RC for invalid prompt in NOPROMPT mode */
    public  static final int INVALIDPROMPTERROR = 11;

    /** Debug setting for user level */
    public static final byte DEBUG_USER = 0x01;
    /** Debug setting for developer level */
    public static final byte DEBUG_DEV  = 0x02;
    /** Debug setting for all */
    public static final byte DEBUG_ALL  = 0x04;

    /** Maximum number of concurrent connections to a mainframe */
    public static final int MAINFRAMETHREADLIMIT = 5;

    // Character constants
    public static final String NEWLINE = "\r\n";

    // Build types
    /** setting for a driver build */
    public static Vector BuildDirectories = new Vector();
    public static Vector BuildTypes = new Vector();

    //Build directories
    public static final String DRIVERBUILDDIRECTORY= "drvrbld"+java.io.File.separator;
    public static final String GENERICBUILDDIRECTORY= "genericbld"+java.io.File.separator;
    public static final String USERBUILDDIRECTORY= "userbld"+java.io.File.separator;
    public static final String USERMODBUILDDIRECTORY= "usrmdbld"+java.io.File.separator;
    public static final String METADATABUILDDIRECTORY= "metadata"+java.io.File.separator;

    /** path to the APAR directory */
    public static final String APARBUILDDIRECTORY = "apar"+java.io.File.separator;

    /** path to the PTF directory */
    public static final String PTFBUILDDIRECTORY = "ptf"+java.io.File.separator;

    // Build Types
    public static final String DRIVERBUILD= "Driver";
    public static final String GENERICBUILD= "Generic";
    public static final String USERBUILD= "User";
    public static final String USERMODBUILD= "Usermod";
    public static final String METADATABUILD= "Metadata";
    public static final String APARBUILD = "Apar";
    public static final String PTFBUILD = "Ptf";
    public static final String DELTADRIVERTYPE="DELTADRIVERTYPE";

    // hashtable designations
    public static final String RELEASETRACKSKEY = "RELEASETRACKSKEY";
    public static final String RELEASEHASHKEY = "RELEASEHASHKEY";
    public static final String DRIVERHASHKEY = "DRIVERHASHKEY";
    public static final String BUILDTYPEHASHKEY = "BUILDTYPEHASHKEY";
    public static final String MVSRELKEYWORD = "MVSREL";
    public static final String MVSHLQKEYWORD = "MVSHLQ";

    // Specific strings
    /** signify that a comment follows */
    public static final String COMMENTSTRING = "//";
    public static final String FILENAMEPLACEHOLDER ="filenamegoeshere";
    public static final String BUILDCOMPLETE = "buildcomplete";
    public static final String PROCESSBUILDPHASE = "Process MVS Build Phase";
    public static final String METADATAVERSIONKEYWORD = "BUILD390METADATAVERSION";

    // Configuration file section names
    //#DEF.INT1768:
    public static final String REQUIREDMETADATASECTION = "VERIFY_MD_SETTINGS";



    public static final String METADATASECTION = "METADATA";
    public static final String GENERALSECTION = "GENERAL";

    // File names
    /** name of build help file */
    public static final String SITE_HELP_FILE = "misc"+java.io.File.separator+"B390SiteHelp.html"; // Defect_247
    public static final String SITE_HELP_BOOKMARKS_FILE = "misc"+java.io.File.separator+"B390SiteHelpBookmarks.list"; // Defect_247
    public static final String USERSGUIDEFILENAME = "B390ClientUsersGuide60.html"; // UsersGuideName
    public static final String USERSGUIDEFILE = "misc"+java.io.File.separator+USERSGUIDEFILENAME; // UsersGuideName
    public static final String HELPFILE = "misc"+java.io.File.separator+"main-frame.html";
    /** name of build save file */
    public static final String BUILDSAVEFILE = "build.ser";
    /** name of driver report file */
    public static final String DRVRRPTFILE = "drvrrpt";
    /** name of job scheduling report file */
    public static final String SCHEDULEFILE = "schedule";
    /** name of PTF report file */
    public static final String PDTREPORTFILE = "pdtrpt";
    /** name of driver report file */
    public static final String SHADOWRPTFILE = "shadrpt";
    /** print file extension */
    public static final String PRINTFILEEXTENTION = ".prt";
    /** clear output file extension */
    public static final String CLEARFILEEXTENTION = ".out";
    /** name of ser file indicating to not show about dialog */
    public static final String dontshowfile= "misc"+java.io.File.separator+"dontshow.ser";

    /** log output file extension */
    public static final String LOGFILEEXTENTION = ".log";

    /** path to log file for the client */
    public static final String LOGFILEPATH = "misc" + java.io.File.separator + "build390.log";
    /** path to archive file  to archive the build390.log if >1m  for the client */
    public static final String ARCHIVEFILEPATH = "misc" + java.io.File.separator + "archive"+java.io.File.separator;

    /** archive build 390 log filename  */
    public static final String ARCHIVELOG = "build390arc";

    /** service jar  build 390 filename  */
    public static final String BPSSERVICE390JARFILE = "bpsService390.jar";


    /** subdirectory of build directory that files will be extracted to/uploaded from */
    public static final String EXTRACTDIRECTORY = "extract";

    /** Output files of MBGenerateLoadAndCheckVerbFiles */
    public static final String PARTLISTLOAD = "partlist.load";
    public static final String PARTLISTDCHK = "partlist.dchk";
    public static final String ORDERFILE       = "partlist.order";
    public static final String QUERYFILE       = "partlist.query";
    public static final String TRACKSFILE      = "partlist.tracks";
    public static final String PARTSFILE       = "partlist.parts";

    /** Output files of MBGenerateBuildVerbFile */
    public static final String DRIVERCHKVRB = "partlist.verb";
    public static final String BUILDORDERFILE = "BLDORDER.ORD";

    static {
        BuildDirectories.addElement(DRIVERBUILDDIRECTORY);
        BuildDirectories.addElement(GENERICBUILDDIRECTORY);
        BuildDirectories.addElement(USERBUILDDIRECTORY);
        BuildDirectories.addElement(USERMODBUILDDIRECTORY);
        BuildDirectories.addElement(APARBUILDDIRECTORY);
        BuildDirectories.addElement(METADATABUILDDIRECTORY);
// 5/12/99, chris, uncomment out
        BuildDirectories.addElement(PTFBUILDDIRECTORY);
    }

    static {
        BuildTypes.addElement(DRIVERBUILD);
        BuildTypes.addElement(GENERICBUILD);
        BuildTypes.addElement(USERBUILD);
        BuildTypes.addElement(USERMODBUILD);
        BuildTypes.addElement(APARBUILD);
        BuildTypes.addElement(METADATABUILD);
// 5/12/99, chris, uncomment out
        BuildTypes.addElement(PTFBUILD);
    }

    public static final String getProgramVersion() {
        return PROGRAMVERSION;
    }

    public static final String getBuildDate() {
        return BUILDDATE;
    }
}
