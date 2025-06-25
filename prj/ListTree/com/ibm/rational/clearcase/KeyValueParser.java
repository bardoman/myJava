package com.ibm.rational.clearcase;
import java.util.*;

public class KeyValueParser
{
    String key="";
    String value="";

    public KeyValueParser(String lineStr)
    {
        parse(lineStr,":");
    }

    public KeyValueParser(String lineStr, String delim)
    {
        parse(lineStr,delim);
    }

    private void parse(String lineStr, String delim)
    {
        StringTokenizer token = new StringTokenizer(lineStr,delim);

        key = token.nextToken().trim();

        if(token.hasMoreTokens())
        {
            value = token.nextToken("\n").substring(1).trim();
        }
    }

    String getKey()
    {
        return key;
    }

    String getValue()
    {
        return value;
    }
}