package com.ibm.sdwb.build390.userinterface.graphic.widgets;
/*********************************************************************/
/* UserSelection                 class for the Build/390 client      */
/* Stores the users find/replace selection string.                   */
/*********************************************************************/
//02/11/2005 SDWB2398  Replace metadata in cmvc.
/*********************************************************************/

import java.util.*;

public class UserSelection {

    private Collection findEntries =null;
    private Collection replaceEntries =null;

    private UserSelectionOptions options;

    public UserSelection(){
        options = new UserSelectionOptions();
    }

    public UserSelection(Collection findEntries,Collection replaceEntries){
        this.findEntries = findEntries;
        this.replaceEntries = replaceEntries;
        options = new UserSelectionOptions();
    }

    public void addFindEntry(String singleFindEntry){
        if (singleFindEntry!=null) {
            if (singleFindEntry.trim().length() > 0) {
                getFindEntries().add(singleFindEntry.trim());
            }
        }
    }

    public void addReplaceEntry(String singleReplaceEntry){
        if (singleReplaceEntry!=null) {
            if (singleReplaceEntry.trim().length() > 0) {
                getReplaceEntries().add(singleReplaceEntry.trim());
            }
        }
    }

    public String getFindString(){
        String temp =null;
        for (Iterator iter=getFindEntries().iterator();iter.hasNext();) {
            if (temp==null) {
                temp = new String();
            }
            temp += ((String)iter.next()).trim();
            if (iter.hasNext()) {
                temp += ", ";
            }

        }
        return temp;
    }


    public String getReplaceString(){
        String temp =null;
        for (Iterator iter=getReplaceEntries().iterator();iter.hasNext();) {
            if (temp==null) {
                temp = new String();
            }
            temp += ((String)iter.next()).trim();
            if (iter.hasNext()) {
                temp += ", ";
            }

        }
        return temp;
    }

    public Collection getFindEntries() {
        if (findEntries==null) {
            findEntries = new HashSet();
        }
        return findEntries;
    }


    public Collection getReplaceEntries(){
        if (replaceEntries==null) {
            replaceEntries = new HashSet();
        }
        return replaceEntries;
    }

    public void setFindEntries(Collection entries){
        this.findEntries = entries;
    }

    public void setReplaceEntries(Set entries){
        this.replaceEntries = entries;

    }


    public UserSelectionOptions getOptions(){
        return options;
    }

    public void setOptions(UserSelectionOptions options){
        this.options = options;
    }


}
