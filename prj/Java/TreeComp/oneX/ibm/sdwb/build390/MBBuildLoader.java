package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java Build Loader for MVS Build server                            */
/* This is responsible for creating builds                           */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/12/2004   PTM3767       Increment build.ser backup
/*********************************************************************/
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.ibm.sdwb.build390.logprocess.*;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


/** MBBuildLoader provides support for loading builds & making them from arguments.
* It can deserialize a build, create a build from a file of name=value pairs, or create
* a build from command line arguments.  All methods are static, so you never need instantiate
* MBBuildLoader.  It dynamically determines the type of build to load and/or instantiate
* by looking for build fields in the command the build is going to.*/
public class MBBuildLoader {

    private static boolean runningnutsy = false;
    private static LogEventProcessor lep=null;
    public static int NUM_OF_BACKUP_BUILDS = 1;


    public static MBBuild loadBuild(String buildToLoad)throws com.ibm.sdwb.build390.MBBuildException{
        File build390Home = new File(MBGlobals.Build390_path);
        File directory = null;
        if (buildToLoad.startsWith("MULTIPTF")) {
            directory = new File(build390Home, MBConstants.PTFBUILDDIRECTORY);
        } else if (buildToLoad.startsWith("F") | buildToLoad.startsWith("D")) {
            directory = new File(build390Home, MBConstants.DRIVERBUILDDIRECTORY);
        } else if (buildToLoad.startsWith("A")) {
            directory = new File(build390Home, MBConstants.APARBUILDDIRECTORY);
        } else if (buildToLoad.startsWith("M")) {
            directory = new File(build390Home, MBConstants.USERMODBUILDDIRECTORY);
        } else if (buildToLoad.startsWith("U")) {
            directory = new File(build390Home, MBConstants.USERBUILDDIRECTORY);
        } else {
            throw new GeneralError("Unknown type of build specified for load.  Build="+buildToLoad);
        }
        return loadBuild(buildToLoad, directory.getAbsolutePath());

    }


    /**
     * Build file to load should be full path to the build.ser file.
     * eg:
     * c:/work/6.0/drvrbld/D8989898/build.ser
     * 
     * @param buildFileToLoad
     * 
     * @return 
     * @exception com.ibm.sdwb.build390.MBBuildException
     */
    public static MBBuild loadBuild(File buildFileToLoad)throws com.ibm.sdwb.build390.MBBuildException {
        String buildFileName = buildFileToLoad.getAbsolutePath();
        File build390Home = new File(MBGlobals.Build390_path);
        String b390path = build390Home.getAbsolutePath();
        String stripGlobalPath = buildFileName.substring(b390path.length());
        int indexOfBuildFileName = stripGlobalPath.indexOf(MBConstants.BUILDSAVEFILE);
        String stripBuildFileName = stripGlobalPath.substring(0,indexOfBuildFileName);

        StringTokenizer tokenizedString = new StringTokenizer(stripBuildFileName,java.io.File.separator);
        String buildId = "";
        while(tokenizedString.hasMoreTokens()){
            buildId = tokenizedString.nextToken(); //the last one contains the buildid.
        }

        String stripBuildIdDirectory = stripBuildFileName.substring(0,stripBuildFileName.indexOf(buildId));
        return loadBuild(buildId,new File(build390Home,stripBuildIdDirectory).getAbsolutePath());
    }


    /** loadBuild - returns a deserialized build
    * @param String buildToLoad  the buildid of the build to deserialize*/
    public static MBBuild loadBuild(String buildToLoad, String dirType) throws com.ibm.sdwb.build390.MBBuildException {
        if (lep==null) {
            createLogListeners();
        }
        MBBuild build;
        File workingDirectory = new File(dirType);
        String directories[] = workingDirectory.list(new DirectoryFilter(buildToLoad));
        if (directories.length > 0) {
            String buildPath = (new File(workingDirectory, directories[0])).getAbsolutePath();
            // Read build file
            try {
                if (!buildPath.endsWith(File.separator)) {
                    buildPath = buildPath +File.separator;
                }
                buildPath = buildPath + MBConstants.BUILDSAVEFILE;
                // if the file does not exist, get out
                File tf = new File(buildPath);
                if (!tf.exists()) {
                    throw new GeneralError("Build "+buildToLoad+" not found");
                }
                build = (MBBuild)com.ibm.sdwb.build390.utilities.BackupBuilds.readObject(tf,NUM_OF_BACKUP_BUILDS);

                return build;
            } catch (ClassNotFoundException cnfe) {
                throw new GeneralError("* ERROR :  Unable to load  build "+buildPath + "\n\n" + 
                                       "REASON:\n"+
                                       "1.The " + buildPath +" requires a class, which is not available in this release.\n" +
                                       "2.The " + buildPath +" was built, using an earlier version.\n" +
                                       "3.The " + buildPath +" file is corrupted.\n" +
                                       "4.The " + buildPath +" contains a class that has a mismatched serialVersionUID.\n\n" +
                                       "FIX:\n"+
                                       "1.Please try loading the " + buildPath +" in an earlier version.\n" +
                                       "2.If this build was performed on the current version, please contact " +
                                       "Build/390 support. with the " + MBGlobals.Build390_path + MBConstants.LOGFILEPATH +" file.\n"+
                                       "                          *****           ", cnfe); //TST1828
            } catch (Exception oce) { /*TST1828 */
                throw new GeneralError("* ERROR : Unable to load  build "+buildPath + "\n\n" + 
                                       "REASONS:\n"+
                                       "1.The " + buildPath +" was built, using an earlier version.\n" +
                                       "2.The " + buildPath +" file is corrupted.\n" +
                                       "3.The " + buildPath +" contains a class that has a mismatched serialVersionUID.\n\n" +
                                       "FIX:\n"+
                                       "1.Please try loading the " + buildPath +" in an earlier version.\n" +
                                       "2.If this is build was performed on the current version, please contact " +
                                       "Build/390 support. with the " + MBGlobals.Build390_path + MBConstants.LOGFILEPATH +" file.\n"+
                                       "                          *****           ", oce); //TST1828
            }
        } else {
            // directory not found
            throw new GeneralError("Build "+buildToLoad+" not found");
        }
    }

    private static void createLogListeners() {

        if (lep==null) {
            lep=new LogEventProcessor();
            lep.addEventListener(MBClient.getGlobalLogFileListener());
            if (MainInterface.getInterfaceSingleton() != null) {
                lep.addEventListener(MBClient.getGlobalLogGUIListener());
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        if (lep == null) {
            lep=new  LogEventProcessor();
        }
    }

}

/** The DirectoryFilter class creates a list of directories that start with
 * a given name */
class DirectoryFilter implements FilenameFilter {
    String buildId;

    public DirectoryFilter(String dirName) {
        buildId = dirName;
    }

    public boolean accept(File dir, String name) {
        if (name.toUpperCase().startsWith(buildId.toUpperCase()) && (new File(dir,name)).isDirectory()) {
            return true;
        } else return false;
    }
}
