package com.ibm.sdwb.build390.metadata.filter;

import java.util.*;
import com.ibm.sdwb.build390.info.*;

public class MetadataExpressionCriteria extends AbstractMetadataFilterCriteria {

    private MetadataExpression context;

    private final Equals       equalsComparator      = new Equals();
    private final LessThan     lessThanComparator    = new LessThan();
    private final GreaterThan  greaterThanComparator = new GreaterThan();
    private final NotEquals    notEqualsComparator   = new NotEquals();
    private final Null         nullComparator        = new Null();



    public MetadataExpressionCriteria(MetadataExpression  context) {
        super(context.getRealKeyword(), context.getMetadataValueExpression().getSearchValue());
        this.context = context;
    }

    public boolean passes(Object obj) {
        boolean isMatch = false;
        FileInfo info = (FileInfo)obj;
        Set  entries  = info.getMetadata().entrySet();
        for (Iterator iter = entries.iterator();iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (((String)entry.getKey()).startsWith(getKeyword())) {

                boolean temp = getComparator(context.getCompareType()).compare(getRegularExpression(),entry.getValue())==0 ? true : false;

                if (temp) {
                    isMatch = true;
                } else {
                    iter.remove();
                }
            }

        }

        return isMatch;


    }

    public MetadataExpression getMetadataExpression(){
        return context;
    }
    //move the comparator code into here .
    //when equals comparator
    //and if its a regex then use EqualsComparator. say have a method - forceEquals() 
    //move the call hasMatch(...) into the EqualsComparator.
    //for NE may be use the negation operator ? 

    //try and break the Context class - like Parser or something else.  and have a class to formulate BooleanExpressionCriteria objects.

    //How are the parts going to be sent back from library ? as MetadataReplaceResultElement array ?


    Comparator getComparator(final int compareType){

        switch (compareType) {
        case MetadataExpression.EQUALS:
            return equalsComparator;
        case MetadataExpression.LESS_THAN:
            return lessThanComparator;
        case MetadataExpression.GREATER_THAN:
            return greaterThanComparator;
        case MetadataExpression.NOT_EQUALS:
            return notEqualsComparator;
        default:
            return nullComparator;
        }
    }

    /* always a match ie. input equals output */
    class Null implements Comparator {

        public int compare(Object o1,Object o2){
            return 0;

        }


        public boolean equals(Object o1){
            return true;

        }
    }

    class Equals implements Comparator {

        public int compare(Object o1,Object o2){
            String value   = (String) o2; /* this is melement.getValue() */
            if (hasMatch(value)) {
                return 0;
            }
            return -1;
        }


        public boolean equals(Object o1){
            return false;

        }
    }


    class LessThan implements Comparator {

        public int compare(Object o1,Object o2){
            String compareString = (String)o1;
            String criteriaString = (String)o2;
            return compareString.compareTo(criteriaString) < 0 ? 0 : -1;

        }


        public boolean equals(Object o1){
            return false;

        }
    }


    class GreaterThan implements Comparator {

        public int compare(Object o1,Object o2){
            String compareString = (String)o1;
            String criteriaString = (String)o2;
            return compareString.compareTo(criteriaString) > 0 ? 0 : -1;

        }


        public boolean equals(Object o1){
            return false;


        }
    }

    /* we probably have to tweak a bit using the regex Negation ^  thing. 
    eg:  
    [\w^_] 	       This matches any alphanumeric character but not the underscore.
    /"[^"]*"/          This example matches text sequences that are contained between quote marks. 
                       Note the use of the negation caret (^) to define a character class of all characters 
                       other than the quote mark.
     
    so basically the regex should be "[^" + getRegularExpression() + "]". We'll have to compile the pattern again.
    The current code should work though.
    */                   
    class NotEquals implements Comparator {

        public int compare(Object o1,Object o2){
            String value  = (String)o2;
            if (!hasMatch(value)) {
                return 0;
            }

            return -1;
        }


        public boolean equals(Object o1){
            return false;

        }
    }




    public String toString(){
        return context.toString();
    }

}