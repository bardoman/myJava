package com.ibm.sdwb.build390.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.sdwb.build390.MBClient;
import com.ibm.sdwb.build390.MBConstants;
import com.ibm.sdwb.build390.MBGlobals;
import com.ibm.sdwb.build390.MBMainframeInfo;
import com.ibm.sdwb.build390.filter.DefaultFilter;
import com.ibm.sdwb.build390.filter.criteria.FilterCriteria;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.library.cmvc.CMVCLibraryInfo;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.userinterface.graphic.MainInterface;


public class SetupManager implements java.io.Serializable {


    static final long serialVersionUID = 1111111111111111L;

    private boolean useDefaultEditor = true;                                                // EditorSelect

    private SortedSet librarySettings     = new TreeSet(new LibraryInfoComparator());           // Contains LibraryInfo for all libraries
    private SortedSet mainframeSettings   = new TreeSet(new MainframeInfoComparator());         // Contains MBMainframeInfo for all libraries

    private LibraryInfo currentLibraryPointer         = null;
    private LibraryInfo currentFakeLibraryPointer         = null;
    private MBMainframeInfo currentMainframePointer       = null;
    private String editor        = new String();                    // Path to editor

    private transient LogEventProcessor lep=null;

    private static SetupManager setupManager;

    private static String CLIENT_SETUP_FILE_STRING = "misc" + java.io.File.separator + "client.ser";



    private SetupManager() {
        initializeLogProcessor();
    }


    /* public static SetupManager getSetupManager(File setupFile) {
         if (setupFile == null) {
             setupManager = new SetupManager();
         } else if (setupManager ==null) {
             load(setupFile);
         }
         return setupManager;
     }*/


    public static SetupManager getSetupManager() {
        if (setupManager == null) {
            load();
        }
        return setupManager;
    }

    public static void newInstance() {
        setupManager = new SetupManager();
    }

    public SetupModificationManager getModificationManager() {
        return SetupModificationManager.getSetupModificationManager();
    }

    public  Setup createSetupInstance() {
        return new Setup(getCurrentLibraryInfo().cloneLibraryInfo() , getCurrentMainframeInfo().cloneMainframeInfo() ,getEditorPath(), IsDefaultEditorSelected());
    }

    /*The is just to make sure, only the SetupManager knows how to make Setup objects */
    public  Setup createSetupInstance(LibraryInfo tempLibInfo, MBMainframeInfo tempMainInfo, String tempEditorPath, boolean tempIsDefaultEditor) {
        return new Setup(tempLibInfo, tempMainInfo,tempEditorPath, tempIsDefaultEditor);
    }


    private static  void load() {
        try {
            File setupFile = new File(MBGlobals.Build390_path+CLIENT_SETUP_FILE_STRING);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(setupFile));
            setupManager = (com.ibm.sdwb.build390.user.SetupManager)ois.readObject();
        } catch (ClassNotFoundException cnfe) {
            newInstance();
        } catch (IOException ioe) {
            newInstance();
        }
    }






    private void initializeLogProcessor() {
        if (lep == null) {
            lep = new LogEventProcessor();
            lep.addEventListener(MBClient.getGlobalLogFileListener());
            if (MainInterface.getInterfaceSingleton()!=null) {
                lep.addEventListener(MBClient.getGlobalLogGUIListener());
            }
        }
    }

    public boolean hasSetup() {

        DefaultFilter filter = new DefaultFilter(new DisplayeableInfoCriteria());
        filter.filter(Arrays.asList((Object[])SetupManager.getSetupManager().getLibraryInfoSet().toArray().clone()));
        return(!getMainframeInfoSet().isEmpty() && !filter.matched().isEmpty());
    }


    /** removes the family
    * @param s int containing the family */
    public void removeLibraryInfo(LibraryInfo delInfo) {
        getLibraryInfoSet().remove(delInfo);
        if (delInfo.equals(currentLibraryPointer)) {
            // this should never happen.  Set to null so we throw a big fat exception if it does and we try to use it
            currentLibraryPointer=null;
        }
        if (delInfo.equals(currentFakeLibraryPointer)) {
            // this should never happen.  Set to null so we throw a big fat exception if it does and we try to use it
            currentFakeLibraryPointer=null;
        }
    }

    /** removes the mainframe
    * @param s int containing the mainframe */
    public void removeMainframeInfo(MBMainframeInfo delInfo) {
        getMainframeInfoSet().remove(delInfo);
        if (delInfo.equals(currentMainframePointer)) {
            // this should never happen.  Set to null so we throw a big fat exception if it does and we try to use it
            currentMainframePointer=null;
        }
    }

    /** adds a family name
    * @param s String containing the family name */
    public void addLibraryInfo(LibraryInfo newInfo) {
        if (getLibraryInfoSet().isEmpty()) {
            setCurrentLibrary(newInfo);
        }
        getLibraryInfoSet().add(newInfo);


    }

    /** adds a mainframe name
    * @param s String containing the mainframe name */
    public void addMainframeInfo(MBMainframeInfo tempInfo) {
        if (getMainframeInfoSet().isEmpty()) {
            setCurrentMainframe(tempInfo);
        }
        getMainframeInfoSet().add(tempInfo);
    }

    public void setCurrentLibrary(LibraryInfo tempLib) {
        if (!getLibraryInfoSet().contains(tempLib)) {
            getLibraryInfoSet().add(tempLib);
        }
        // we should clean this up.  For starters go into Mode and add a isFakeLibrary() method to hide the id implementation
        if (MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
            currentFakeLibraryPointer = tempLib;
        } else {
            // stick this in an else so it's obvious we can't do both
            currentLibraryPointer = tempLib;
        }


    }

    public void setCurrentMainframe(MBMainframeInfo tempMain) {
        if (!mainframeSettings.contains(tempMain)) {
            mainframeSettings.add(tempMain);
        }
        currentMainframePointer = tempMain;
    }


    /** Sets the view selection EditorSelect
    * @param boolean containing the browser selected */
    public void setDefaultEditorSelected(boolean tempSelection) {
        useDefaultEditor = tempSelection;
    }

    /** Sets the browser path EditorSelect
    * @param s String containing the browser path */
    public void setEditorPath(String s) {
        editor = s.trim();
    }

    public MBMainframeInfo getCurrentMainframeInfo() {
        return currentMainframePointer;
    }

    /** Gets the editor selection EditorSelect
    * @return A boolean containing the editor selected */
    public boolean IsDefaultEditorSelected() {
        return(useDefaultEditor);
    }

    public LibraryInfo getCurrentLibraryInfo() {
        LibraryInfo libInfo = null;
        if (MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
            libInfo = currentFakeLibraryPointer;
        } else {
            libInfo = currentLibraryPointer;
        }
        if (libInfo != null) {
            try {
                if (!MBClient.getCommandLineSettings().isCommandLine()) {
                    com.ibm.sdwb.build390.help.HelpController.getInstance().setHelpLoaderInterface(libInfo.getHelpLoaderInterface());
                }
            } catch (com.ibm.sdwb.build390.help.HelpException hpe) {
                hpe.printStackTrace();
            }
            return libInfo;
        }
        return null;
    }

    public Set getLibraryInfoSet() {
        if (librarySettings == null) {
            librarySettings = new TreeSet(new LibraryInfoComparator());
        }
        return librarySettings;
    }

    public Set getMainframeInfoSet() {
        if (mainframeSettings == null) {
            mainframeSettings = new TreeSet(new MainframeInfoComparator());
        }
        return mainframeSettings;
    }

    public String getEditorPath() {
        return(editor);
    }



    /** Saves the serialized object file. */
    public void saveSetup() {
        saveToFile(MBGlobals.Build390_path + CLIENT_SETUP_FILE_STRING);
    }

    /** Saves the serialized object file to a given file */
    // 1/7/99, chris, added
    //         share the same method to avoid dual maintenance
    public void saveSetup(String fileName) {
        saveToFile(fileName);
    }

    /** Saves the serialized object file to a file */
    private void saveToFile(String aFile) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(aFile));
            oos.writeObject(this);
            oos.close();
        } catch (IOException ioe) {
            lep.LogException("An error occurred while trying to save file " + aFile , ioe);
            ioe.printStackTrace(System.err);
        }

    }

    /* This method is called to deserialize SetupManager object.  If changes are made to SetupManager, this
     * method should be changed to handle the incompatibles, like initializing fields that didn't previously
     * exist or otherwise making sure the SetupManager object that results from deserialization can be used.
     * If some of the fields in the new SetupManager need to be manually initialized, this sets a flag that is
     * checked after deserialization.  If the flag is true, the user is told he must fill in some Setup
     * information before the client can be used
     */
    private void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lep = new LogEventProcessor();
        lep.addEventListener(MBClient.getGlobalLogFileListener());
        if (MainInterface.getInterfaceSingleton()!=null) {
            lep.addEventListener(MBClient.getGlobalLogGUIListener());
        }
    }



    public String toString() {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        formatter.format("%n%n%s%n","#---------------------------------------------------------------#");
        formatter.format("%1s%21s%s%21s%1s%n","#","","SETUP     INFORMATION","","#");
        formatter.format("%s%n","#---------------------------------------------------------------#");

        String mainframeInfoString = "#   =>NO BUILDSERVER SETUP FOUND";
        String libraryInfoString   = "#   =>NO LIBRARY     SETUP FOUND";
        if (getCurrentMainframeInfo()!=null) {
            mainframeInfoString = getCurrentMainframeInfo().toCompleteString();
        }

        if (getCurrentLibraryInfo()!=null) {
            libraryInfoString = getCurrentLibraryInfo().toCompleteString();
        }

        formatter.format("%n%-28s%1s%n","#BUILD390 BUILDSERVER SETUP",":");
        formatter.format("%s%n",mainframeInfoString);
        formatter.format("%-28s%1s%n","#BUILD390 LIBRARY     SETUP",":");
        formatter.format("%s%n",libraryInfoString);
        formatter.format("%-28s =%s%n","#User Editor Path",getEditorPath());
        formatter.format("%s%n","#---------------------------------------------------------------#");
        return strbd.toString();
    }

    private class DisplayeableInfoCriteria implements FilterCriteria {
        public boolean passes(Object o) {
            LibraryInfo libInfo = (LibraryInfo)o;

            if (!MBClient.getCommandLineSettings().getMode().isFakeLibrary()) {
                return !libInfo.isFakeInfo();
            }

            return libInfo.isFakeInfo();
        }
    }

    private static final class LibraryInfoComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            String processServerName1 = ((LibraryInfo) o1).getProcessServerName();
            String processServerName2 = ((LibraryInfo) o2).getProcessServerName();
            String processServerAddress1 = ((LibraryInfo) o1).getProcessServerAddress();
            String processServerAddress2 = ((LibraryInfo) o2).getProcessServerAddress();
            String  processServerPort1 = Integer.toString(((LibraryInfo) o1).getProcessServerPort());
            String  processServerPort2 = Integer.toString(((LibraryInfo) o2).getProcessServerPort());

            int comparedName = processServerName1.compareTo(processServerName2);
            int comparedAddress = processServerAddress1.compareTo(processServerAddress2);


            if (comparedName ==0) {
                if (comparedAddress ==0) {
                    if (processServerPort1 !=null && processServerPort2 !=null) {
                        return processServerPort1.compareTo(processServerPort2);
                    }
                }
                return comparedAddress;
            } else {
                return comparedName;
            }
        }
    };

    private static final class MainframeInfoComparator implements Comparator, Serializable {
        public int compare(Object o1, Object o2) {
            String o1M = ((MBMainframeInfo) o1).getMainframeAddress() +"@"+((MBMainframeInfo) o1).getMainframePort()+"@"+((MBMainframeInfo) o1).getMainframeUsername();
            String o2M = ((MBMainframeInfo) o2).getMainframeAddress() +"@"+((MBMainframeInfo) o2).getMainframePort()+"@"+((MBMainframeInfo) o2).getMainframeUsername();
            return(o1M.compareTo(o2M));
        }
    };

}
