package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class VobInfoParser 
{
    private static String TAG_KEY = "Tag";
    private static String GLOBAL_PATH_KEY = "Global path";
    private static String SERVER_H0ST_KEY = "Server host";
    private static String ACCESS_KEY = "Access";
    private static String MOUNT_OPTIONS_KEY = "Mount options";
    private static String REGION_KEY = "Region";
    private static String ACTIVE_KEY = "Active";
    private static String VOB_TAG_REPLICA_UUID_KEY = "Vob tag replica uuid";
    private static String VOB_ON_HOST_KEY = "Vob on host";
    private static String VOB_SERVER_ACCESS_PATH_KEY = "Vob server access path";
    private static String VOB_FAMILY_UUID_KEY = "Vob family uuid";
    private static String VOB_REPLICA_UUID_KEY = "Vob replica uuid";
    private static String VOB_REGISTRY_ATTRIBUTES_KEY = "Vob registry attributes";


    private String inStr = null;
    private Logger logger = null;

    public VobInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public VobInfo [] getInfo()
    throws CTAPIException
    {
        logger.entering("VobInfoParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector vobInfoVect = new Vector();

        VobInfo vobInfo = null;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();
            KeyValueParser keyValue = new KeyValueParser(lineStr);
            String key = keyValue.getKey();
            String value = keyValue.getValue();

            if(key.equals(TAG_KEY))
            {
                if(vobInfo != null)
                {
                    vobInfoVect.add(vobInfo);
                }

                vobInfo = new VobInfo();

                int pos=0;

                String description="";

                String name="";

                if((pos=value.indexOf('\"'))!=-1)
                {
                    name = value.substring(0,pos);

                    description = value.substring(pos).replace('"',' ').trim();
                }
                else
                {
                    name = value;
                }

                vobInfo.setName(name.trim());

                vobInfo.setDescription(description);
            }
            else
                if(key.equals(GLOBAL_PATH_KEY))
            {
                vobInfo.setGlobalPath(value);
            }
            else
                if(key.equals(SERVER_H0ST_KEY))
            {
                vobInfo.setServerHost(value);
            }
            else
                if(key.equals(ACCESS_KEY))
            {
                vobInfo.setAccess(value);
            }
            else
                if(key.equals(MOUNT_OPTIONS_KEY))
            {
                vobInfo.setMountOptions(value);
            }
            else
                if(key.equals(REGION_KEY))
            {
                vobInfo.setRegion(value);
            }
            else
                if(key.equals(ACTIVE_KEY))
            {
                if(value.equals("YES"))
                {
                    vobInfo.setActive(true);
                }
                else
                {
                    vobInfo.setActive(false);
                }
            }
            else
                if(key.equals(VOB_TAG_REPLICA_UUID_KEY))
            {
                vobInfo.setVobTagReplicaUuid(value);
            }
            else
                if(key.equals(VOB_ON_HOST_KEY))
            {
                vobInfo.setVobOnHost(value);
            }
            else
                if(key.equals(VOB_SERVER_ACCESS_PATH_KEY))
            {
                vobInfo.setVobServerAccessPath(value);
            }
            else
                if(key.equals(VOB_FAMILY_UUID_KEY))
            {
                vobInfo.setVobFamilyUuid(value);
            }
            else
                if(key.equals(VOB_REPLICA_UUID_KEY))
            {
                vobInfo.setVobReplicaUuid(value);
            }
            else
                if(key.equals(VOB_REGISTRY_ATTRIBUTES_KEY))
            {
                vobInfo.setVobRegistryAttributes(value);
            }
            else
            {
                throw new CTAPIException("Unknown token=>"+key,logger);
            }
        }

        vobInfoVect.add(vobInfo);

        VobInfo vobInfoList[] = new VobInfo[vobInfoVect.size()];

        vobInfoList = (VobInfo []) vobInfoVect.toArray(vobInfoList);

        logger.exiting("VobInfoParser","getInfo");

        return vobInfoList;
    }

}
