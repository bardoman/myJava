package com.ibm.sdwb.build390.metadata.filter;

import java.util.*;

import com.ibm.sdwb.build390.MetadataType;

public class  MetadataExpression {

    private String[] input;

    public  static final int EQUALS = 0;
    public  static final int LESS_THAN = EQUALS+1;
    public  static final int GREATER_THAN = LESS_THAN + 1;
    public  static final int NOT_EQUALS = GREATER_THAN + 1;

    private int compareType  =-1;

    private MetadataValueExpression valueExpression;
    private String realName;


    public MetadataExpression(String[] input) {
        this.input = input;
        valueExpression = new MetadataValueExpression(input[2]);
    }


    public String getVirtualKeyword(){
        /* \w 	A word character: [a-zA-Z_0-9]
           \W 	A non-word character: [^\w] */
        return  input[0].replaceAll("[\\W]+","").toUpperCase();
    }

    public void setRealKeyword(String realName){
        this.realName = realName;
    }

    public String getRealKeyword(){
        return realName;
    }

    public String[] getInput(){ 
        return input;
    }


    public MetadataValueExpression getMetadataValueExpression() {
        return valueExpression;
    }


    public String getCompareTypeAsString(){
        return input[1];
    }

    public int  getCompareType(){
        return getCompareType(getCompareTypeAsString());
    }


    public int getCompareType(String type){
        type = type.trim();
        if (type.equalsIgnoreCase(MetadataType.COMPARITOR_EQUAL)) {
            return  EQUALS;
        }

        if (type.equalsIgnoreCase(MetadataType.COMPARITOR_LESS_THAN)) {
            return  LESS_THAN;
        }

        if (type.equalsIgnoreCase(MetadataType.COMPARITOR_MORE_THAN)) {
            return  GREATER_THAN;
        }

        if (type.equalsIgnoreCase(MetadataType.COMPARITOR_NOT_EQUAL)) {
            return  NOT_EQUALS;
        }
        return -1;
    }


    static final class WildCardChecker {

        //for now ignore this.
        String  allowableWildCards(){
            return "*";
        }

        //for now ignore this.but this should be used to validate wildcard strings
        //so we only all host supported wildcards.
        boolean validWildCard(String searchString){
            //need to fine tune the below regex.
            //searchString.matches(allowableWildCards());
            return true;
        }

        static String convert(String expressionString) {
            String search = new String(expressionString);
            if (search.indexOf(".*") >=0) {
                return search.replaceAll("\\.\\*","*");/*we the first slash acts as a escape char to second slash. The second slash make use to check for .(dot) */ 
            }
            return search;
        }

    }

    public String toString() {
        return "["+getVirtualKeyword() + " " + getCompareTypeAsString() + " " + getMetadataValueExpression().toString() +"]";
    }

}




