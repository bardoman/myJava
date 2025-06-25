package com.ibm.sdwb.build390.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.sdwb.build390.MBBuild;
import com.ibm.sdwb.build390.MBBuildException;
import com.ibm.sdwb.build390.MBBuildLoader;
import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;



public class SerializedBuildsLister {

    private static SerializedBuildsLister serializedLister;
    private File build390HomeDirectory =null;

    private static final String MISC_DIRECTORY = "MISC";
    private static final String GENERICBLD_DIRECTORY = "GENERICBLD";

    public static FilterCriteria BUILD_DISPLAY_CRITERIA = new BuildDisplayCriteria();

    private SerializedBuildsLister(File build390HomeDirectory) {
        this.build390HomeDirectory = build390HomeDirectory;
    }

    public static SerializedBuildsLister getInstance(File build390HomeDirectory) {
        if (serializedLister ==null) {
            serializedLister = new SerializedBuildsLister(build390HomeDirectory);
        }
        return serializedLister;
    }

    private FileFilter createDefaultFilter() {
        return new SerializedBuildFilesFilter();
    }

    public File locateBuildId(String buildId) throws MBBuildException {
        java.util.List temp = listBUILDFilesTree(build390HomeDirectory.getAbsolutePath(),null);
        BuildDisplayCriteria criteria =  new BuildDisplayCriteria();
        for (Iterator iter = temp.iterator();iter.hasNext();) {
            File info = (File)iter.next();
            if (info.getParentFile().getName().equalsIgnoreCase(buildId)) { //if the file directory are same, compare the builid.
                MBBuild build = MBBuildLoader.loadBuild(info);

                if (build.get_buildid().equalsIgnoreCase(buildId) && criteria.passes(build)) {
                    return info;
                }
            }
        }
        return null;
    }

    public  java.util.List listBUILDFilesTree(String pathName,java.io.FileFilter fileFilter) {
        java.util.List outputList = new java.util.ArrayList();
        if (fileFilter==null) {
            fileFilter = createDefaultFilter();
        }
        FileSystem.listFilesTree(new File(pathName),outputList,fileFilter);
        return outputList;
    }

    public java.util.List loadBUILDObjectsFromFiles(java.util.List buildFilesTree,Map buildIdToDirectoryMapping) throws MBBuildException {

        BuildDisplayCriteria criteria =  new BuildDisplayCriteria();

        java.util.List buildsList = new java.util.ArrayList();

        com.ibm.sdwb.build390.utilities.MultipleConcurrentException allExceptions = new com.ibm.sdwb.build390.utilities.MultipleConcurrentException("Exceptions encountered during loading builds:!"); /*TST1828 */

        for (Iterator iter = buildFilesTree.iterator(); iter.hasNext();) {
            try {
                File testFile = (File)iter.next();
                if (testFile.exists()) {

                    MBBuild loadedBuild = MBBuildLoader.loadBuild(testFile);

                    if (criteria.passes(loadedBuild)) {
                        if (buildIdToDirectoryMapping!=null) {
                            buildIdToDirectoryMapping.put(loadedBuild.get_buildid(), testFile);
                        }
                        buildsList.add(loadedBuild);
                    }
                }
            } catch (com.ibm.sdwb.build390.MBBuildException mbe) {
                allExceptions.addException(mbe);
            }
        }

        if (allExceptions.getExceptionSet().size() > 0) {
            throw allExceptions;
        }

        return buildsList;
    }

    private class SerializedBuildFilesFilter implements FileFilter {
        private   Set foundSet = new HashSet();
        public boolean accept(File file) {
            if (file.getName().toUpperCase().equals(MISC_DIRECTORY) | file.getName().toUpperCase().equals(GENERICBLD_DIRECTORY)) {
                return false;
            }

            if (!file.isDirectory()) {
                boolean isBUILDSERFILE = file.getName().toUpperCase().equalsIgnoreCase(MBConstants.BUILDSAVEFILE);
                if (isBUILDSERFILE && belongsToValidBuildGroup(file)) {
                    boolean parentAlreadyFound = parentAlreadyFound(file);
                    if (parentAlreadyFound) {
                        return false;
                    } else {
                        foundSet.add(file.getParent());
                        return true;
                    }
                }
                return false;   
            }
            return(file.isDirectory());
        }

        private boolean belongsToValidBuildGroup(File file) {
            for (Iterator iter = MBConstants.BuildDirectories.iterator();iter.hasNext();) {
                String directoryGroup = (String)iter.next();
                if (file.getAbsolutePath().indexOf(directoryGroup) > 0) {
                    return true;
                }
            }
            return false;
        }

        private boolean parentAlreadyFound(File file) {
            File parent = file.getParentFile();
            if (parent!=null && !parent.getAbsolutePath().equals(build390HomeDirectory.getAbsolutePath())) {
                if (foundSet.contains(parent.getAbsolutePath())) {
                    return true;
                } else {
                    boolean temp =  parentAlreadyFound(parent);
                    return temp;
                }
            }
            return false;
        }

    }




    private static  class BuildDisplayCriteria implements FilterCriteria {
        public boolean passes(Object o) {
            MBBuild build = (MBBuild)o;

            if (!MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
                return !build.getLibraryInfo().isFakeInfo();
            }

            return build.getLibraryInfo().isFakeInfo();
        }
    }


    public static void main(String[] args) {
        /*  java.util.List fileSet = SerializedBuildsLister.listBUILDFilesTree("C:\\kishore\\work\\build390\\client\\6.0\\",SerializedBuildsLister.SERIALIZED_BUILD_FILES_FILTER);
          for (Iterator iter=fileSet.iterator();iter.hasNext();) {
              FileSystemInfo obj = (FileSystemInfo)iter.next();
              System.out.println(obj.toString());
          }
          System.exit(0);
          */
    }
}
