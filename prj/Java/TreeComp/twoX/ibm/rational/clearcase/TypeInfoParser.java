package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class TypeInfoParser 
{
    private String inStr = null;
    private Logger logger = null;
    private String TYPE_TOKEN = "attribute type \"";

    public TypeInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public String [] getInfo()
    throws CTAPIException
    {
        logger.entering("TypeInfoParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector infoVect = new Vector();

        String tmpStr="";
        int index;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();

            index = lineStr.lastIndexOf(TYPE_TOKEN);

            if(index != -1)
            {
                tmpStr = lineStr.substring(index+TYPE_TOKEN.length());

                tmpStr = tmpStr.replace('"',' ').trim(); 

                infoVect.add(tmpStr);
            }
        }

        logger.exiting("TypeInfoParser","getInfo");

        return VectToStringArray(infoVect);
    }

    String [] VectToStringArray(Vector vect)
    {
        String strAray[] = new String[vect.size()];

        strAray = (String []) vect.toArray(strAray);

        return strAray;
    }
}
