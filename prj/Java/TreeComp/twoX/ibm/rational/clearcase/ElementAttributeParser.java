package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class ElementAttributeParser 
{
    private static String ATTRIBUTES_KEY = "Attributes";

    private String inStr  = null;
    private Logger logger = null;

    public ElementAttributeParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public Hashtable getInfo()
    throws CTAPIException
    {
        Hashtable attrib = new Hashtable();

        logger.entering("ElementAttributeParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        int lineCnt=0;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();

            KeyValueParser keyValue = new KeyValueParser(lineStr, ":");

            String key = keyValue.getKey();

            String value = keyValue.getValue();

            if(lineCnt==0)
            {
                lineCnt=1;
            }
            else
                if(key.equals(ATTRIBUTES_KEY))
            {
                while(lineToken.hasMoreElements())
                {
                    lineStr = lineToken.nextToken();

                    keyValue = new KeyValueParser(lineStr, "=");
                    key = keyValue.getKey();
                    value = keyValue.getValue();

                    attrib.put(key, value.replace('\"',' ').trim());
                } 
            }
        }

        logger.exiting("ElementAttributeParser","getInfo");

        return attrib;
    }
}
