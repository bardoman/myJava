package com.ibm.sdwb.build390;

import java.util.*;

public class SmartStringTokenizer{
    String delimString = new String();
    String stringToParse = new String();

    public SmartStringTokenizer(String tempStringToParse, String delims){
        stringToParse = tempStringToParse;
        delimString = delims;
    }

    public String nextToken(){
        if (stringToParse == null) {
            return null;
        }
        int nextIndex = stringToParse.indexOf(delimString);
        if (nextIndex > -1) {
            String returnString = stringToParse.substring(0, nextIndex);
            stringToParse = stringToParse.substring(nextIndex+1);
            return returnString;
        }else {
            String returnString = stringToParse;
            stringToParse = null;
            return returnString;
        }

    }

    public boolean hasMoreTokens(){
        if (stringToParse == null) {
            return false;
        }
        return stringToParse.length() > 0;
    }

    public static void main (String args[]){
        SmartStringTokenizer temp = new SmartStringTokenizer("|test||of some|weird", "|");
        while (temp.hasMoreTokens()) {
            System.out.println(temp.nextToken());
        }
        temp = new SmartStringTokenizer("|test||of some|weird2|", "|");
        while (temp.hasMoreTokens()) {
            System.out.println(temp.nextToken());
        }
    }

}
