package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class ActivityInfoParser 
{
    private String inStr = null;
    private Logger logger = null;

    public ActivityInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public ActivityInfo [] getInfo()
    throws CTAPIException
    {
        logger.entering("ActivityInfoParser","getInfo");

        StringTokenizer lineTokenizer = new StringTokenizer(inStr,"\n\r");

        Vector infoVect = new Vector();

        Vector utilVect;

        ActivityInfo info = null;

        while(lineTokenizer.hasMoreElements())
        {
            info = new ActivityInfo();

            String lineStr = lineTokenizer.nextToken();

            StringTokenizer elemTokenizer = new StringTokenizer(lineStr," ");

            String createDate = elemTokenizer.nextToken();

            info.setCreateDate(createDate);

            String name = elemTokenizer.nextToken();

            info.setName(name);

            String createBy = elemTokenizer.nextToken();

            info.setCreateBy(createBy);

            elemTokenizer.nextToken("\"");

            String title = elemTokenizer.nextToken("\"");

            info.setTitle(title);

            infoVect.add(info);
        }

        ActivityInfo infoList[] = new ActivityInfo[infoVect.size()];

        infoList = (ActivityInfo []) infoVect.toArray(infoList);

        logger.exiting("ActivityInfoParser","getInfo");

        return infoList;
    }

    String [] VectToStringArray(Vector vect)
    {
        String strAray[] = new String[vect.size()];

        strAray = (String []) vect.toArray(strAray);

        return strAray;
    }
}
