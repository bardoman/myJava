package com.ibm.sdwb.build390.retain;
/***************************************************************************/
/* Java FmidFactory  
This is a HashMap that stores a key value pair in the following formats for fmid
<Key as  cmvcrelease|fullfamily name> <value> or
<Key as  retainrelease|retaincompid|fullfamily name> <value>
eg:
 case  1:      cmvcrelease         = HBLD120
      fullfamily address  = b390dev@b390dev.cmvc.com@1111
      case 1: the key would be  HBLD120|b390dev@b390dev.cmvc.com@1111
 case  2: retain release = R01
         retain compid  = VSE01
         key would be R01|VSE01|b390dev@b390dev.cmvc.com@1111
/***************************************************************************/
/* chgdate  chg description
/* 09/29/2001 DEF:#HoldCleanup :  birth of the class in 40 release
/***************************************************************************/
import java.lang.*;
import java.util.*;
import com.ibm.sdwb.build390.logprocess.LogEventProcessor;
import com.ibm.sdwb.build390.library.LibraryInfo;
import com.ibm.sdwb.build390.*;

public class FmidFactory {

    public static Hashtable fmidCmvcRelMap = null;
    private static FmidFactory singletonFMID =null;
    private static LogEventProcessor lep =null;

    private FmidFactory(){
        fmidCmvcRelMap = new Hashtable();
    }

    public static FmidFactory getInstance(LogEventProcessor lep){
        if (singletonFMID ==null) {
            singletonFMID  = new FmidFactory();
        }
        FmidFactory.lep =lep;
        return(singletonFMID);
    }

    private java.util.Map get(String key){
        if (fmidCmvcRelMap.containsKey(key)) {
            return(Map)fmidCmvcRelMap.get(key);
        }
        return null;
    }

    public java.util.Map get(String cmvcRelease , LibraryInfo libInfo){
        return get(cmvcRelease.trim() + "|"+ libInfo.getDescriptiveString());
    }

    public java.util.Map get(String retainRelease ,String retainComponent, LibraryInfo libInfo){
        return(get(retainRelease.trim() + "|"+retainComponent+"|"+ libInfo.getDescriptiveString()));
    }

    private void put(String key,java.util.Map realProxyFMIDMap){
        if (!fmidCmvcRelMap.containsKey(key)) {
            fmidCmvcRelMap.put(key,realProxyFMIDMap);
        }
    }

    public void put(String cmvcRelease , LibraryInfo libInfo,java.util.Map realProxyFMIDMap){
        put(cmvcRelease.trim() + "|"+libInfo.getDescriptiveString(),realProxyFMIDMap);
    }

    public void put(String retainRelease , String retainComponent, LibraryInfo libInfo, java.util.Map  realProxyFMIDMap){
        put(retainRelease.trim() + "|" + retainComponent + libInfo.getDescriptiveString(),realProxyFMIDMap);
    }

}
