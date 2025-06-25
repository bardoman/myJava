package com.ibm.sdwb.build390.configuration;

//**************************************************************************************
// 03/21/2000 pjs - add passticketacct keyword
// 02/07/2000 kkk - add NOTIFICATION keyword
//11/02/02 Feat.SDWB1776:      BLD390 should use disttype and scode values from CMVC.
//**************************************************************************************

import java.util.*;
import com.ibm.sdwb.build390.*;

public class DictionaryOfConfigOptions
{
    public static Hashtable realmHash = new Hashtable();
    public static String PROJECTREALM = "RELEASE";
    public static String COMPONENTREALM = "COMPONENT";

    public static String ALLOWDYNAMICAESWITCH = "ALLOWDYNAMICAESWITCH";


    static{

        Hashtable releaseKeywordHash = new Hashtable();
        realmHash.put(PROJECTREALM, releaseKeywordHash);

        AlphabetizedVector tempKeywords = new AlphabetizedVector();
        tempKeywords.addElement("SCODE");
        releaseKeywordHash.put("VERIFY_MD_SETTINGS", tempKeywords);

        tempKeywords = new AlphabetizedVector();
        tempKeywords.addElement(com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUsermodSourceSelection.STAGINGLEVEL);
        tempKeywords.addElement(com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUsermodSourceSelection.STAGINGLEVELEDITABLE);
        tempKeywords.addElement(com.ibm.sdwb.build390.userinterface.graphic.widgets.UsermodBuildSettingsPanel.BUNDLEDPROCESSING);
        tempKeywords.addElement(com.ibm.sdwb.build390.userinterface.graphic.widgets.UsermodBuildSettingsPanel.BUNDLEDPROCESSINGEDITABLE);
        releaseKeywordHash.put(com.ibm.sdwb.build390.library.cmvc.userinterface.CMVCUsermodSourceSelection.USERMODCONFIGSECTIONKEY, tempKeywords);


        tempKeywords = new AlphabetizedVector();
        tempKeywords.addElement("BUILD_FAILURE_NOTIFICATION");
        tempKeywords.addElement("NOTIFY_OWNER_OF_PART");
        releaseKeywordHash.put("NOTIFICATION",tempKeywords);

    }

    public static Vector getAllRealms(){
        AlphabetizedVector realmVector = new AlphabetizedVector();
        Enumeration realms = realmHash.keys();
        while(realms.hasMoreElements())
        {
            realmVector.addElement((String) realms.nextElement());
        }
        return realmVector;
    }

    public static Vector getSectionsForRealm(String realm){
        Hashtable oneRealm = (Hashtable) realmHash.get(realm);
        if(oneRealm != null)
        {
            AlphabetizedVector sectionVector = new AlphabetizedVector();
            Enumeration sections = oneRealm.keys();
            while(sections.hasMoreElements())
            {
                sectionVector.addElement((String) sections.nextElement());
            }
            return sectionVector;
        }
        return null;
    }

    public static Vector getKeywordsForSectionInRealm(String realm, String section){
        Hashtable oneRealm = (Hashtable) realmHash.get(realm);
        return(Vector)oneRealm.get(section);
    }
}
