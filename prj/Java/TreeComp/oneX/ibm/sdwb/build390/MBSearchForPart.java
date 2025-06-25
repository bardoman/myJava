
package com.ibm.sdwb.build390;
/*********************************************************************/
/* MBSearchForPart class for the Build/390 client                    */
/*  searches for a given Partname /part class in a given list        */
/*********************************************************************/
/*05/24/2000 searcher classEKM001 ..Birth of the class
/*********************************************************************/

import java.util.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;



import com.ibm.sdwb.build390.logprocess.LogEventProcessor ;

public class MBSearchForPart {
/**
    */
    private Vector RebuildDisplayPartVector       = new Vector();
    //private HashMap FindNextHashMap = new HashMap();
	private SortedMap FindNextHashMap =  new TreeMap();
    private String SearchTitle =  new String();
    private boolean isFirstHit = false;
    private MBStatus status;
    private String NameToFind; 
    private String PartClassToFind;
    private LogEventProcessor lep;

    private static final String DRIVERNAME="DRIVERNAME";
    private static final String PARTNAME="PARTNAME";
    private static final String PARTCLASS="PARTCLASS";
    private static final String FIRSTSEARCH="FIRSTSEARCH";
    private boolean isDriverSearch=false;

    public MBSearchForPart(Enumeration ToBeSearchedFromEnumKeys,MBInternalFrame thisFrame,MBStatus status,LogEventProcessor lep,boolean isDriverSearch) throws com.ibm.sdwb.build390.MBBuildException {
        this.status=status;
        this.lep=lep;
        this.isDriverSearch=isDriverSearch;

        try {
            //    MBLogRetrievePartFinder Find = new MBLogRetrievePartFinder(thisFrame,lep);
            //  Hashtable findHashMap=Find.getFindByName();
            HashMap findHashMap = getSearchDataHashMap(isDriverSearch,thisFrame);

            if (findHashMap!=null) {
                if (findHashMap.size()>0) {
                    if (isDriverSearch) {
                        NameToFind = (String)findHashMap.get(DRIVERNAME);
                        PartClassToFind = null;
                    } else {
                        NameToFind  = ((String)findHashMap.get(PARTNAME));
                        PartClassToFind = (String)findHashMap.get(PARTCLASS);
                    }

                    isFirstHit =   ((Boolean)findHashMap.get(FIRSTSEARCH)).booleanValue();
                    RebuildDisplayPartVector.removeAllElements();
                    FindNextHashMap.clear();
                   
                    int i=-1;


                    while (ToBeSearchedFromEnumKeys.hasMoreElements()) {
                        i++;
                        Vector PartVector =(Vector)ToBeSearchedFromEnumKeys.nextElement();
                        String  PartName =  ((String)PartVector.get(0)).trim();
                        String PartClass = isDriverSearch ? null : ((String)PartVector.get(1)).trim();

                        //status.updateStatus("Finding ..."+PartName,false);
                        status.updateStatus("Finding ...",false);

                        ArrayList passarraylist = new ArrayList();
                        passarraylist.clear();

                        if (NameToFind!=null) {
                            passarraylist.add(PartName);
                            passarraylist.add(NameToFind.trim());
                            passarraylist.add(PartClass);
                            passarraylist.add(PartClassToFind);
                            SearchTitle = (PartClassToFind !=null ? (NameToFind + PartClassToFind) : (NameToFind));
                        } else {
                            passarraylist.add(PartClass);
                            passarraylist.add(PartClassToFind.trim());
                            passarraylist.add(PartName);
                            passarraylist.add(NameToFind);
                            SearchTitle = PartClassToFind;

                        }
                        passarraylist.add(PartVector);
                        passarraylist.add(new Integer(i));
                        checkForMatch(passarraylist);

                    }

                }
            }
        } catch (MBBuildException e) {
            lep.LogException(e);
        }
    }




    private HashMap getSearchDataHashMap(boolean isDriverSearch,MBInternalFrame thisFrame) throws com.ibm.sdwb.build390.MBBuildException {
        MBLogRetrievePartFinder Find = new MBLogRetrievePartFinder(thisFrame,lep,isDriverSearch);
        return(Find.getFindByName());
    }



    //checks for match of partname + partclass
    private void checkForMatch(ArrayList tempArrayList){

        String CompareName         = (String)tempArrayList.get(0);
        String MatchName           = (String)tempArrayList.get(1);

        if (MatchName.indexOf("*") > -1) {
            if (isMatchAfterWildCard(MatchName,CompareName)) {
                checkForClassOrNameMatch(tempArrayList);
            }

        } else {
            if (matches(CompareName,MatchName)) {
                checkForClassOrNameMatch(tempArrayList);
            }
        }


    }

    private boolean matches(String input,String regex){
        regex = regex.replaceAll("^[ \\t]+|[ \\t]+$","");
        String[] temp= input.replaceAll("^[ \\t]+|[ \\t]+$","").split("\\s+");

        temp[0] = temp[0].replaceAll("^[ \\t]+|[ \\t]+$",""); /* trim starting and ending space and tab char */

        return temp[0].matches(regex);
    }

    //checks for match   of partname or partclass  and adds in the RebuildDisplayArrayList and FindNextHashMap if match is met
    private void checkForClassOrNameMatch(ArrayList temppassarraylist){
        String  CompareAnotherName  = (String)temppassarraylist.get(2);
        String  MatchAnotherName    = (String)temppassarraylist.get(3);
        Vector  PartVector          = (Vector)temppassarraylist.get(4);

        if (MatchAnotherName!=null) {
            if (isMatchAnotherNameAlso(CompareAnotherName,MatchAnotherName)) {
                RebuildDisplayPartVector.addElement(PartVector);
                FindNextHashMap.put((Integer)temppassarraylist.get(5),PartVector);
            }
        } else {
            RebuildDisplayPartVector.addElement(PartVector);
            FindNextHashMap.put((Integer)temppassarraylist.get(5),PartVector);
        }

    }
    //parses the name or class given and returns true if match is met
    private boolean isMatchAnotherNameAlso(String CompareAnotherName,String MatchAnotherName) {
        if (MatchAnotherName.indexOf("*") > -1) {
            return(isMatchAfterWildCard(MatchAnotherName,CompareAnotherName));
        } else {
            if (matches(CompareAnotherName,MatchAnotherName)) {
                return true;
            }
        }
        return false;
    }

    //if the string has got wild card this methods parses the tokens with * as delimiter and checks for the occurrence of the token.
    //the format supported are *A,A*A*A....,*A*A*A... 
    //the current stuff doesnt support wild character ?
    private boolean isMatchAfterWildCard(String MatchTo,String CompareWith){
        StringTokenizer strk = new StringTokenizer(MatchTo.trim(),"*");
        int noOfTokensfetched=0;
        int totalTokens = strk.countTokens();
        String nextMatchForToken = null;
        try {
            nextMatchForToken=strk.nextToken().trim();
            noOfTokensfetched++;
        } catch (NoSuchElementException nsme) {
            return true;
        }
        boolean isMatchOfAllTokens=false;
        int loopit=-1;
        int SearchedLength=0;
        String nextMatchToBeFoundInString = null;
        if (MatchTo.charAt(0)=='*') {
            if (totalTokens>0) {
                if ((CompareWith.substring(1)).indexOf(nextMatchForToken) >-1) {
                    int len = ((CompareWith.substring(1)).indexOf(nextMatchForToken))+nextMatchForToken.length()+1;
                    if ((len+1)<CompareWith.length()) {
                        nextMatchToBeFoundInString = (CompareWith.substring(1)).substring(len);
                        SearchedLength=CompareWith.length() - nextMatchToBeFoundInString.length();
                        isMatchOfAllTokens=true;
                        loopit = 1;
                    } else {
                        isMatchOfAllTokens=false;
                    }
                }
            } else {
                if (CompareWith.endsWith(nextMatchForToken)) {
                    isMatchOfAllTokens=true;
                }
            }
        } else {
            if (CompareWith.startsWith(nextMatchForToken)) {
                if (CompareWith.length() > (nextMatchForToken.length()+1)) {
                    nextMatchToBeFoundInString = CompareWith.substring(nextMatchForToken.length()+1);
                    SearchedLength=CompareWith.length() - nextMatchToBeFoundInString.length();
                    loopit=1;
                } else {
                    loopit=-1;
                }
                isMatchOfAllTokens=true;
                loopit=1;
            } else {
                loopit=-1;
            }

        }

        while (strk.hasMoreTokens()&(loopit>0)&nextMatchToBeFoundInString!=null) {
            nextMatchForToken=strk.nextToken().trim();
            noOfTokensfetched++;

            int nextMatchedIndex = nextMatchToBeFoundInString.indexOf(nextMatchForToken);
            if ((noOfTokensfetched==totalTokens)&(!MatchTo.endsWith("*"))) {
                if (nextMatchToBeFoundInString.endsWith(nextMatchForToken)) {
                    isMatchOfAllTokens=true;
                } else {
                    isMatchOfAllTokens=false;
                }
            } else {
                if (nextMatchedIndex > -1) {
                    int nextMatchToStartFromLength = nextMatchedIndex + nextMatchForToken.length() + 1;
                    SearchedLength= SearchedLength+nextMatchToStartFromLength;
                    if (SearchedLength<=CompareWith.length()) {
                        nextMatchToBeFoundInString = nextMatchToBeFoundInString.substring(nextMatchToStartFromLength);
                        isMatchOfAllTokens=true;
                    }
                } else {
                    isMatchOfAllTokens=false;
                    loopit=-1;
                }
            }
        }
        return isMatchOfAllTokens;

    }



    public Vector getMatchedPartsVector(){
        return RebuildDisplayPartVector;
    }

    public SortedMap getFindNextHashMap(){
        return FindNextHashMap;
    }

    public String getSearchTitle(){
        return SearchTitle;
    }

    public char getDisplayOption(){
        if (isFirstHit) {
            return 'F';
        } else {
            return 'R';
        }
    }

    public String getNameToFind(){
        return NameToFind;
    }

    public String getPartClassToFind(){
        return PartClassToFind;
    }
}










