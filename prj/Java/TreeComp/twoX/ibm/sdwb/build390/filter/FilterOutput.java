package com.ibm.sdwb.build390.filter;
/*********************************************************************/
/* Java FilterOutput class for storing filtered outputs              */
/* An internal structure to store the matched/unmatched filter output*/
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 11/25/04 SDWB2398            Birth of the class
/*********************************************************************/
import java.util.*;

public class FilterOutput {


    private Collection matchedCollection;
    private Collection unMatchedCollection;

    
    FilterOutput(){
        this.matchedCollection  = new LinkedList();
        this.unMatchedCollection  = new LinkedList();
    }


    public void addMatchedEntry(Object output){
        matchedCollection.add(output);
    }

    public void addUnMatchedEntry(Object output){
         unMatchedCollection.add(output);
    }

    public Collection getMatched(){
        return matchedCollection;
    }


    public Collection getUnMatched(){
        return unMatchedCollection;
    }

    public void clearAll(){
        getMatched().clear();
        getUnMatched().clear();
    }


}



