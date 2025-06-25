package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class HostInfoParser 
{
    private static String CLIENT_KEY          = "Client";
    private static String PRODUCT_KEY         = "Product";
    private static String OS_KEY              = "Operating system";
    private static String HARDWARE_TYPE_KEY   = "Hardware type";
    private static String REGISTRY_HOST_KEY   = "Registry host";
    private static String REGISTRY_REGION_KEY = "Registry region";
    private static String LICENSE_HOST_KEY    = "License host";

    private String inStr = null;
    private Logger logger = null;

    public HostInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public HostInfo getInfo()
    throws CTAPIException
    {
        logger.entering("HostInfoParser","getInfo");

        StringTokenizer lineTokenizer = new StringTokenizer(inStr,"\n\r");

        Vector infoVect = new Vector();

        HostInfo info = new HostInfo();

        while(lineTokenizer.hasMoreElements())
        {
            String lineStr = lineTokenizer.nextToken();

            KeyValueParser keyValue = new KeyValueParser(lineStr);
            String key = keyValue.getKey();
            String value = keyValue.getValue();

            if(key.equals(CLIENT_KEY))
            {
                info.setClient(value);
            }
            else
                if(key.equals(PRODUCT_KEY))
            {
                info.setProduct(value);
            }
            else
                if(key.equals(OS_KEY))
            {
                info.setOperatingSystem(value);
            }
            else
                if(key.equals(HARDWARE_TYPE_KEY))
            {
                info.setHardwareType(value);
            }
            else
                if(key.equals(REGISTRY_HOST_KEY))
            {
                info.setRegistryHost(value);
            }
            else
                if(key.equals(REGISTRY_REGION_KEY))
            {
                info.setRegistryRegion(value);
            }
            else
                if(key.equals(LICENSE_HOST_KEY))
            {
                info.setLicenseHost(value);
            }
        }

        logger.exiting("HostInfoParser","getInfo");

        return info;
    }
}