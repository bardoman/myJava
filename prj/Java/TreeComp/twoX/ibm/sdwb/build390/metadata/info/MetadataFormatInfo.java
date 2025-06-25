package com.ibm.sdwb.build390.metadata.info;
/*********************************************************************/
/* Java MetadataFormatInfo class for the Build390 java client      */
/* this object stores information associated with metadata such as formating, */
/* type, number of possible entries, etc.                            */
/*********************************************************************/
/* Updates:                                                          */
/*********************************************************************/
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JInternalFrame;

import com.ibm.sdwb.build390.AlphabetizedVector;

/** <br>MetadataFormatInfo provides support for Metadata formatting.
*/
public class MetadataFormatInfo {

    private Vector switchFieldList = null;
    private Vector accumFieldList = null;
    private Vector singleEntryFieldList = null;
    private Vector multipleEntryFieldList = null;
    private Hashtable fieldInfo = null;

    public MetadataFormatInfo() {
        switchFieldList = new AlphabetizedVector();
        accumFieldList = new AlphabetizedVector();
        singleEntryFieldList = new AlphabetizedVector();
        multipleEntryFieldList = new AlphabetizedVector();
        fieldInfo = new Hashtable();
    }

// add a switch field 	
    public void addSwitchField(String englishName, String realName, String keyword, boolean tempRead, boolean tempCase, boolean tempQuote){
        fieldInfo.put(realName, new switchEntry(englishName, realName, keyword, tempRead, tempCase, tempQuote));
        switchFieldList.addElement(realName);
    }

// add an accumulator field
    public void addAccumField(String englishName, String realName, String keyword, boolean tempRead, boolean tempCase, boolean tempQuote){
        fieldInfo.put(realName, new accumulatorEntry(englishName, realName, keyword, tempRead, tempCase, tempQuote));
        accumFieldList.addElement(realName);
    }

// add an entry field, multiple or single.	
    public void addEntryField(String englishName, String realName, String keyword, int maxEntries, int entryLength, boolean tempRead, boolean tempCase, boolean tempQuote){
        if (maxEntries< 2) {
            fieldInfo.put(realName, new singleEntry(englishName, realName, keyword, entryLength, tempRead, tempCase, tempQuote));
            singleEntryFieldList.addElement(realName);
        } else {
            fieldInfo.put(realName, new multipleEntry(englishName, realName, keyword, entryLength, maxEntries, tempRead, tempCase, tempQuote));
            multipleEntryFieldList.addElement(realName);
        }
    }

// getSwitchFieldList - get the list of switch fields for the metadata	
    public Vector getSwitchFieldList(){
        return switchFieldList;
    }

// getAccumFieldList - get the list of accumulator fields for the metadata	
    public Vector getAccumFieldList(){
        return accumFieldList;
    }

// getSingleEntryFieldList - get the list of single entry fields for the metadata	
    public Vector getSingleEntryFieldList(){
        return singleEntryFieldList;
    }

// getMultipleFields - get the list of multiple entry fields for the metadata	
    public Vector getMultipleFields(){
        return multipleEntryFieldList;
    }

// getFieldInfo - get the hashtable of detailed field info	
    public Hashtable getFieldInfo() {
        return fieldInfo;
    }

//	toString - dump a String version of data contained in an instance of this object.  
//			used mainly for debugging.
    public String toString() {
        String returnString = new String();
        returnString += "Metadata Format Info:\n";
        returnString += "    Switch Fields:  "+switchFieldList.toString()+"\n";
        returnString += "    Accum Fields:  "+accumFieldList.toString()+"\n";
        returnString += "    Single Entry Fields:  "+singleEntryFieldList.toString()+"\n";
        returnString += "    Multiple Entry Fields:  "+multipleEntryFieldList.toString()+"\n";
        returnString += "    Field specifics:  "+fieldInfo.toString()+"\n";
        return returnString;
    }

// class to hold info that is common to all metadata types
    public class universalFormatInfo {
        private String englishName = null;
        private String realName = null;
        private String keyword = null;
        private boolean readOnly = false;
        private boolean caseSensitive = true;
        private boolean quoted = false;

        universalFormatInfo(String tempEngName, String tempRealName, String tempKey, boolean tempRead, boolean tempCase, boolean tempQuote){
            englishName = new String(tempEngName);
            realName = new String(tempRealName);
            keyword = new String(tempKey);
            readOnly = tempRead;
            caseSensitive = tempCase;
            quoted = tempQuote;
        }

        public String getEnglishName(){
            return englishName;
        }

        public String getRealName(){
            return realName;
        }

        public String getKeyword(){
            return keyword;
        }

        public boolean isReadOnly(){
            return readOnly;
        }

        public boolean isCaseSensitive(){
            return caseSensitive;
        }

        public boolean isQuoted(){
            return quoted;
        }

        public String toString() {
            String returnString = new String();
            returnString +="EnglishName="+englishName+", RealName="+realName+", Keyword="+keyword+", ReadOnly="+readOnly+", CaseSensitive="+caseSensitive+", Quoted="+quoted;
            return returnString;
        }
    }

// class that extends universalFormatInfo with extentions specific to switch fields	
    public class switchEntry extends universalFormatInfo {

        switchEntry(String tempEngName, String tempRealName, String tempKey, boolean tempRead, boolean tempCase, boolean tempQuote){
            super(tempEngName, tempRealName, tempKey, tempRead, tempCase, tempQuote);
        }

        public String toString() {
            String returnString = super.toString();
            return "SwitchEntry=["+returnString+"]\n";
        }
    }

// class that extends universalFormatInfo with extentions specific to accumulator fields	
    public class accumulatorEntry extends universalFormatInfo {
        accumulatorEntry(String tempEngName, String tempRealName, String tempKey, boolean tempRead, boolean tempCase, boolean tempQuote){
            super(tempEngName, tempRealName, tempKey, tempRead, tempCase, tempQuote);
        }

        public String toString() {
            String returnString = super.toString();
            return "AccumulatorEntry=["+returnString+"]\n";
        }
    }

// class that extends universalFormatInfo with extentions specific to single entry fields	
    public class singleEntry extends universalFormatInfo {
        private int length = -1;

        singleEntry(String tempEngName, String tempRealName, String tempKey, int tempLength, boolean tempRead, boolean tempCase, boolean tempQuote){
            super(tempEngName, tempRealName, tempKey, tempRead, tempCase, tempQuote);
            length = tempLength;
        }

        public int getLength(){
            return length;
        }

        public String toString() {
            String returnString = super.toString()+" length="+length;
            return "SingleEntry=["+returnString+"]\n";
        }
    }


// class that extends universalFormatInfo with extentions specific to multiple entry fields	
    public class multipleEntry extends singleEntry {
        private int maxEntries = -1;

        multipleEntry(String tempEngName, String tempRealName, String tempKey, int length, int tempMaxEntries, boolean tempRead, boolean tempCase, boolean tempQuote){
            super(tempEngName, tempRealName, tempKey, length, tempRead, tempCase, tempQuote);
            maxEntries = tempMaxEntries;
        }

        public int getMaximumEntries(){
            return maxEntries;
        }

        public String toString() {
            String returnString = super.toString()+" maxEntries="+maxEntries;
            return "MultipleEntry=[" + returnString +"]\n";
        }
    }
}
