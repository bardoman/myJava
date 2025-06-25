package com.ibm.sdwb.build390.configuration;

import java.util.*;
import java.rmi.*;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.process.management.Haltable;
import com.ibm.sdwb.build390.LibraryError;
import java.io.*;

public class ConfigurationAccessImplementation implements ConfigurationAccess, Serializable {

    static final long serialVersionUID = 1111111111111111L;
    private Map savedValues = new HashMap();
/*	Map bufferedValues = new HashMap();
    Map timeOfBuffering = new HashMap();
// Set up a 10 minute expiration period for data.
    private static final long cachedDataExpirationLength = 600000;
*/    
    private LibraryInfo libInfo = null;
    private boolean useCachedValues = false;
    private String project = null;


    public ConfigurationAccessImplementation(LibraryInfo tempLib, String tempProject){
        libInfo = tempLib;
        project = tempProject;
    }

    public String getProjectConfigurationSetting(String section, String keyword)throws LibraryError{
/* lets not use cached values, just always get fresh unless we aren't realtime
        String keyWord = libInfo.getProcessServerName()+libInfo.getProcessServerAddress()+release+section+keyword;
        Date currentTime = new Date();
        Date cacheTime = (Date) timeOfBuffering.get(keyword);
        if (cacheTime !=null) {
            if ((currentTime.getTime()-cacheTime.getTime()) > cachedDataExpirationLength){
                return (String) bufferedValues.get(keyword);
            }
        }
*/        
        Map sourceMap = null;
        if (useCachedValues) {
            sourceMap = savedValues;
        } else {
            try {
                sourceMap =  libInfo.getConfigurationServer().getConfiguration(project,section,keyword);
                /*            timeOfBuffering.put(keyword, currentTime);
                            if (configResult != null) {
                                bufferedValues.put(keyword, configResult);
                            }else{
                                bufferedValues.remove(keyword);
                            }
                */            
            } catch (RemoteException re) {
                throw new LibraryError(re.getMessage(), (Exception) re.detail);
            }
        }
        Map projectRealm = (Map) sourceMap.get(DictionaryOfConfigOptions.PROJECTREALM);
        if (projectRealm != null) {
            Map sectionMap = (Map) projectRealm.get(section);
            if (sectionMap!=null) {
                return(String) sectionMap.get(keyword);
            }
        }
        return null;

    }

    public Map getAllConfigurationSettings() throws LibraryError{       
        try {
            savedValues = libInfo.getConfigurationServer().getConfiguration(project, null, null);
            return(Map) savedValues;
        } catch (java.rmi.RemoteException re) {
            throw new LibraryError("Problem getting the configuration settings from the configuration server.", re);
        }
    }

    public void setUseCachedValues(boolean temp) throws LibraryError{
        if (useCachedValues == false & temp == true) {// if we just turned on use cached
            getAllConfigurationSettings();  // then prime the cache so we have a coherent picture of all the settings
        }
        useCachedValues = temp;
    }
}
