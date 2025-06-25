package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class ViewInfoParser 
{
    private static String TAG_KEY = "Tag";
    private static String GLOBAL_PATH_KEY = "Global path";
    private static String SERVER_H0ST_KEY = "Server host";
    private static String REGION_KEY = "Region";
    private static String ACTIVE_KEY = "Active";
    private static String VIEW_TAG_UUID_KEY = "View tag uuid";
    private static String VIEW_ON_HOST_KEY = "View on host";
    private static String VIEW_SERVER_ACCESS_PATH_KEY = "View server access path";
    private static String VIEW_UUID_KEY = "View uuid";
    private static String VIEW_ATTRIBUTES_KEY = "View attributes";
    private static String VIEW_OWNER_KEY = "View owner";


    private String inStr = null;
    private Logger logger = null;

    public ViewInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public ViewInfo [] getInfo()
    throws CTAPIException
    {
        logger.entering("ViewInfoParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector viewInfoVect = new Vector();

        ViewInfo viewInfo = null;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();
            KeyValueParser keyValue = new KeyValueParser(lineStr);
            String key = keyValue.getKey();
            String value = keyValue.getValue();

            if(key.equals(TAG_KEY))
            {
                if(viewInfo != null)
                {
                    viewInfoVect.add(viewInfo);
                }

                viewInfo = new ViewInfo();

                int pos=0;

                if((pos=value.indexOf('\"'))!=-1)
                {
                    value = value.substring(0,pos);
                }

                viewInfo.setName(value);
            }
            else
                if(key.equals(GLOBAL_PATH_KEY))
            {
                viewInfo.setGlobalPath(value);
            }
            else
                if(key.equals(SERVER_H0ST_KEY))
            {
                viewInfo.setServerHost(value);
            }
            else
                if(key.equals(REGION_KEY))
            {
                viewInfo.setRegion(value);
            }
            else
                if(key.equals(ACTIVE_KEY))
            {
                if(value.equals("YES"))
                {
                    viewInfo.setActive(true);
                }
                else
                {
                    viewInfo.setActive(false);
                }
            }
            else
                if(key.equals(VIEW_TAG_UUID_KEY))
            {
                viewInfo.setViewTagUuid(value);
            }
            else
                if(key.equals(VIEW_ON_HOST_KEY))
            {
                viewInfo.setViewOnHost(value);
            }
            else
                if(key.equals(VIEW_SERVER_ACCESS_PATH_KEY))
            {
                viewInfo.setViewServerAccessPath(value);
            }
            else
                if(key.equals(VIEW_UUID_KEY))
            {
                viewInfo.setViewUuid(value);
            }
            else
                if(key.equals(VIEW_ATTRIBUTES_KEY))
            {
                viewInfo.setViewAttributes(value);
            }
            else
                if(key.equals(VIEW_OWNER_KEY))
            {
                viewInfo.setViewOwner(value);
            }
            else
            {
                throw new CTAPIException("Unknown token=>"+key,logger);
            }
        }

        viewInfoVect.add(viewInfo);

        ViewInfo viewInfoList[] = new ViewInfo[viewInfoVect.size()];

        viewInfoList = (ViewInfo []) viewInfoVect.toArray(viewInfoList);

        logger.exiting("ViewInfoParser","getInfo");

        return viewInfoList;
    }
}
