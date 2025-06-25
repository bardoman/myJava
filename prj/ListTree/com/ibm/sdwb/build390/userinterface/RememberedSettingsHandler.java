package com.ibm.sdwb.build390.userinterface;

import java.util.*;
import java.io.*;
import com.ibm.sdwb.build390.user.Setup;

public class RememberedSettingsHandler implements java.io.Serializable {
    static final long serialVersionUID = 1111111111111111L;
    private Map defaultMap = new HashMap();
    private static RememberedSettingsHandler singletonHandler = null;
    private static final String DEFAULTSAVENAME = "defaults.ser";
    private static final String LISTKEYWORD = "LIST";
    private static final String RELEASE = "RELEASE";
    private static final String DRIVER = "DRIVER";
    private static final String BUILDTYPE = "BUILDTYPE";
    private static final int NUMBEROFDEFAULTSTOSAVE = 5;


    private RememberedSettingsHandler() {
    }

    public synchronized static RememberedSettingsHandler getInstance() {
        if (singletonHandler==null) {
            loadRememberedSettings();
        }
        return singletonHandler;
    }

    public synchronized static void saveRememberedSettings() throws java.io.IOException{
        if (singletonHandler!=null) {
            ObjectOutputStream saveStream = new ObjectOutputStream(new FileOutputStream(getSaveFile()));
            saveStream.writeObject(singletonHandler);
            saveStream.close();
        }
    }

    private static void loadRememberedSettings() {
        try {
            if (getSaveFile().exists()) {
                ObjectInputStream readStream = new ObjectInputStream(new FileInputStream(getSaveFile()));
                singletonHandler =(RememberedSettingsHandler) readStream.readObject();
                readStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load defaults", e);
        }
        if (singletonHandler==null) {
            singletonHandler = new RememberedSettingsHandler();
        }
    }

    public static File getSaveFile() {
        return new File(com.ibm.sdwb.build390.MBGlobals.Build390_path+"misc"+java.io.File.separator+DEFAULTSAVENAME);
    }

    public void addReleaseRememberedSetting(Setup setup, String releaseName) {
        if (!validInput(releaseName)) {
            return;
        }
        Map setupMap = getSetupMap(setup);
        synchronized(setupMap) {
            List relList = getReleaseRememberedSettingsList(setup);
            relList = moveToFrontOfList(releaseName, relList);
            setupMap.put(listKeyword(RELEASE), relList);
        }
    }

    public List getReleaseRememberedSettingsList(Setup setup) {
        Map setupMap = getSetupMap(setup);
        List releaseList = (List) setupMap.get(listKeyword(RELEASE));
        if (releaseList == null) {
            releaseList = new ArrayList();
        }
        return releaseList;
    }

    public void addDriverRememberedSetting(Setup setup, String release, String driver) {
        if (!validInput(release)) {
            return;
        }
        if (!validInput(driver)) {
            return;
        }
        Map releaseMap = getReleaseMap(setup,release);
        synchronized(releaseMap) {
            List driverList = getDriverRememberedSettingsList(setup,release);
            driverList = moveToFrontOfList(driver, driverList);
            releaseMap.put(listKeyword(DRIVER), driverList);
        }
    }

    public List getDriverRememberedSettingsList(Setup setup, String release) {
        Map releaseMap = getReleaseMap(setup,release);
        List driverList = (List) releaseMap.get(listKeyword(DRIVER));
        if (driverList==null) {
            driverList = new ArrayList();
        }
        return driverList;
    }

    public void addBuildtypeRememberedSetting(Setup setup, String release, String driver, String buildtype) {
        if (!validInput(release)) {
            return;
        }
        if (!validInput(driver)) {
            return;
        }
        if (!validInput(buildtype)) {
            return;
        }
        Map driverMap = getDriverMap(setup,release, driver);
        synchronized(driverMap) {
            List buildtypeList = getBuildtypeRememberedSettingsList(setup,release, driver);
            buildtypeList = moveToFrontOfList(buildtype, buildtypeList);
            driverMap.put(listKeyword(BUILDTYPE), buildtypeList);
        }
    }

    public List getBuildtypeRememberedSettingsList(Setup setup, String release, String driver) {
        Map driverMap = getDriverMap(setup,release, driver);
        List buildtypeList = (List) driverMap.get(listKeyword(BUILDTYPE));
        if (buildtypeList==null) {
            buildtypeList = new ArrayList();
        }
        return buildtypeList;
    }

    public void addPerSetupSetting(Setup setup,Object key, Object value) {
        if (!validInput(key)) {
            return;
        }
        if (value == null) {
            getSetupMap(setup).remove(key);
        } else {
            getSetupMap(setup).put(key, value);
        }
    }

    public Object getPerSetupSetting(Setup setup, Object key) {
        return getSetupMap(setup).get(key);
    }

    public void addPerReleaseSetting(Setup setup, String release, Object key, Object value) {
        if (!validInput(release)) {
            return;
        }
        if (!validInput(key)) {
            return;
        }

        Map releaseMap = getReleaseMap(setup,release);
        synchronized(releaseMap) {
            if (value == null) {
                releaseMap.remove(key);
            } else {
                releaseMap.put(key, value);
            }
        }
    }

    public Object getPerReleaseSetting(Setup setup, String release, Object key) {
        return getReleaseMap(setup,release).get(key);
    }

    public void addPerReleaseSettingList(Setup setup, String release, Object key, Object value) {
        if (!validInput(release)) {
            return;
        }
        if (!validInput(key)) {
            return;
        }
        Map releaseMap = getReleaseMap(setup,release);
        synchronized(releaseMap) {
            List generalList = getPerReleaseSettingList(setup,release, key);
            generalList = moveToFrontOfList(value, generalList);
            releaseMap.put(key, generalList);
        }
    }

    public List getPerReleaseSettingList(Setup setup, String release, Object key) {
        List returnList = (List) getReleaseMap(setup,release).get(key);
        if (returnList==null) {
            return new ArrayList();
        }
        return returnList;
    }

    public void addPerDriverSetting(Setup setup, String release, String driver, Object key, Object value) {
        if (!validInput(release)) {
            return;
        }
        if (!validInput(driver)) {
            return;
        }
        if (!validInput(key)) {
            return;
        }
        if (value == null) {
            getDriverMap(setup,release, driver).remove(key);
        } else {
            getDriverMap(setup,release, driver).put(key, value);
        }
    }

    public Object getPerDriverSetting(Setup setup, String release, String driver, Object key) {
        return getDriverMap(setup,release, driver).get(key);
    }

    private String listKeyword(String identifier) {
        return identifier+"|"+LISTKEYWORD;
    }

    private boolean validInput(Object input) {
        if (input == null) {
            return false; // don't mess with nulls
        }
        if (input instanceof String) {
            if (input.toString().trim().length()< 1) {
                return false; // don't mess with empty strings
            }
        }
        return true;
    }

    private Map getSetupMap(Setup setup) {
        Map setupMap = null;
        synchronized (defaultMap) {
            setupMap = (Map) defaultMap.get(setup.getIdentifyingStringForLibraryMainframePair());
            if (setupMap==null) {
                setupMap = new HashMap();
                defaultMap.put(setup.getIdentifyingStringForLibraryMainframePair(), setupMap);
            }
        }
        return setupMap;
    }

    private Map getReleaseMap(Setup setup, String release) {
        Map setupMap = getSetupMap(setup);
        synchronized(setupMap) {
            Map releaseMap = (Map)setupMap.get(release);
            if (releaseMap==null) {
                releaseMap = new HashMap();
                setupMap.put(release, releaseMap);
            }
            return releaseMap;
        }
    }

    private Map getDriverMap(Setup setup, String release, String driver) {
        Map releaseMap = getReleaseMap(setup, release);
        synchronized(releaseMap) {
            Map driverMap = (Map)releaseMap.get(driver);
            if (driverMap==null) {
                driverMap = new HashMap();
                releaseMap.put(driver, driverMap);
            }
            return driverMap;
        }
    }

    private List moveToFrontOfList(Object item, List list) {
        List newList = new ArrayList();
        newList.add(item);
        for (Iterator listIterator = list.iterator();listIterator.hasNext() & newList.size()< NUMBEROFDEFAULTSTOSAVE;) {
            String nextItem = (String) listIterator.next();
            if (nextItem==null) {
                nextItem = new String();
            }
            if (!nextItem.equals(item)) {
                newList.add(nextItem);
            }
        }
        return newList;
    }
}
