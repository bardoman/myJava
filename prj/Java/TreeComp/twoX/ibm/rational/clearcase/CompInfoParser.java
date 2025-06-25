package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class CompInfoParser 
{
    private static String NAME_KEY = "project ";
    private static String BY_KEY = "by";
    private static String ROOT_DIRECTORY_KEY = "root directory";

    private static String OWNER_KEY = "owner";
    private static String GROUP_KEY = "group";
    private static String FOLDER_KEY = "folder";


    private String inStr = null;
    private Logger logger = null;

    public CompInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public CompInfo [] getInfo()
    throws CTAPIException
    {
        logger.entering("CompInfoParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector infoVect = new Vector();

        CompInfo info = null;

        int lineCnt=0;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();

            if(lineCnt==0)
            {
                if(info != null)
                {
                    infoVect.add(info);
                }

                info = new CompInfo();

                String name = lineStr.replace('"',' ').trim();

                info.setName(name);

                lineCnt=1;
            }
            else
                if(lineCnt==1)
            {
                int index=lineStr.indexOf(BY_KEY);

                if(index != -1)
                {
                    String createDate = lineStr.substring(0,index).trim();

                    info.setCreateDate(createDate);

                    String createBy = lineStr.substring((index+BY_KEY.length())).trim();

                    info.setCreateBy(createBy);

                    lineCnt++;
                }
            }
            else
            {
                KeyValueParser keyValue = new KeyValueParser(lineStr);
                String key = keyValue.getKey();
                String value = keyValue.getValue();


                if(key.equals(ROOT_DIRECTORY_KEY))
                {
                    info.setRootDirectory(value);
                }
                else

                    if(key.equals(OWNER_KEY))
                {
                    info.setOwner(value);
                }
                else
                    if(key.equals(GROUP_KEY))
                {
                    info.setGroup(value);

                    lineCnt=0;
                }
                else
                {
                    throw new CTAPIException("Unknown token=>"+key,logger);
                }
            }
        }

        infoVect.add(info);

        CompInfo infoList[] = new CompInfo[infoVect.size()];

        infoList = (CompInfo []) infoVect.toArray(infoList);

        logger.exiting("CompInfoParser","getInfo");

        return infoList;
    }

    String [] VectToStringArray(Vector vect)
    {
        String strAray[] = new String[vect.size()];

        strAray = (String []) vect.toArray(strAray);

        return strAray;
    }
}
