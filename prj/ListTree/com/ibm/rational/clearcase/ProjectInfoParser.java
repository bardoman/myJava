package com.ibm.rational.clearcase;

import java.util.*;
import java.util.logging.*;

public class ProjectInfoParser 
{
    private static String NAME_KEY = "project ";
    private static String BY_KEY = "by";

    private static String OWNER_KEY = "owner";
    private static String GROUP_KEY = "group";
    private static String FOLDER_KEY = "folder";
    private static String INTEGRATION_STREAM_KEY = "integration stream";
    private static String MODIFIABLE_COMPONENTS_KEY = "modifiable components";
    private static String DEFAULT_REBASE_PROMOTION_LEVEL_KEY = "default rebase promotion level";
    private static String RECOMMENDED_BASELINES_KEY = "recommended baselines";
    private static String MODEL_KEY = "model";
    private static String POLICIES_KEY = "policies";
    private static String BASELINE_NAMEING_TEMPLATE_KEY = "baseline naming template";


    private String inStr = null;
    private Logger logger = null;

    public ProjectInfoParser(String inStr, Logger logger)
    {
        this.inStr = inStr;
        this.logger = logger;
    }

    public ProjectInfo [] getInfo()
    throws CTAPIException
    {
        logger.entering("ProjectInfoParser","getInfo");

        StringTokenizer lineToken = new StringTokenizer(inStr,"\n\r");

        Vector infoVect = new Vector();

        Vector utilVect;

        ProjectInfo info = null;

        int lineCnt=0;

        while(lineToken.hasMoreElements())
        {
            String lineStr = lineToken.nextToken();

            if(lineStr.trim().startsWith(NAME_KEY))
            {
                if(info != null)
                {
                    infoVect.add(info);
                }

                info = new ProjectInfo();

                String name = lineStr.substring(NAME_KEY.length());

                name = name.replace('"',' ').trim();

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

                    lineCnt=2;
                }
            }
            else
                if(lineCnt==2)
            {
                String description = lineStr.replace('"',' ').trim();

                info.setDescription(description);

                lineCnt=3;
            }
            else
            {
                KeyValueParser keyValue = new KeyValueParser(lineStr);
                String key = keyValue.getKey();
                String value = keyValue.getValue();

                if(key.equals(OWNER_KEY))
                {
                    info.setOwner(value);
                }
                else
                    if(key.equals(GROUP_KEY))
                {
                    info.setGroup(value);
                }
                else
                    if(key.equals(FOLDER_KEY))
                {
                    info.setFolder(value);
                }
                else
                    if(key.equals(INTEGRATION_STREAM_KEY))
                {
                    info.setIntegrationStream(value);
                }
                else
                    if(key.equals(MODIFIABLE_COMPONENTS_KEY))
                {
                    utilVect = new Vector();

                    lineStr = lineToken.nextToken().trim();

                    key = new KeyValueParser(lineStr).getKey();

                    while(!key.equals(DEFAULT_REBASE_PROMOTION_LEVEL_KEY))
                    {
                        utilVect.add(lineStr);

                        lineStr = lineToken.nextToken().trim();

                        keyValue = new KeyValueParser(lineStr);

                        key = keyValue.getKey();

                        value = keyValue.getValue();
                    }

                    info.setModifiableComponents(VectToStringArray(utilVect));

                    if(key.equals(DEFAULT_REBASE_PROMOTION_LEVEL_KEY))
                    {
                        info.setDefaultRebasePromotionLevel(value);
                    }
                    else
                    {
                        throw new CTAPIException("Unknown token=>"+key,logger);
                    }
                }
                else
                    if(key.equals(RECOMMENDED_BASELINES_KEY))
                {
                    utilVect = new Vector();

                    lineStr = lineToken.nextToken().trim();

                    key = new KeyValueParser(lineStr).getKey();

                    while(!key.equals(MODEL_KEY))
                    {
                        utilVect.add(lineStr);

                        lineStr = lineToken.nextToken().trim();

                        keyValue = new KeyValueParser(lineStr);

                        key = keyValue.getKey();

                        value = keyValue.getValue();
                    }

                    info.setRecommendedBaselines(VectToStringArray(utilVect));

                    if(key.equals(MODEL_KEY))
                    {
                        info.setModel(value);
                    }
                    else
                    {
                        throw new CTAPIException("Unknown token=>"+key,logger);
                    }
                }
                else
                    if(key.equals(POLICIES_KEY))
                {
                    utilVect = new Vector();

                    lineStr = lineToken.nextToken().trim();

                    key = new KeyValueParser(lineStr).getKey();

                    while(!key.equals(BASELINE_NAMEING_TEMPLATE_KEY))
                    {
                        utilVect.add(lineStr);

                        lineStr = lineToken.nextToken().trim();

                        keyValue = new KeyValueParser(lineStr);

                        key = keyValue.getKey();

                        value = keyValue.getValue();
                    }

                    info.setPolicies(VectToStringArray(utilVect));

                    if(key.equals(BASELINE_NAMEING_TEMPLATE_KEY))
                    {
                        info.setBaselineNamingTemplate(value);
                    }
                    else
                    {
                        throw new CTAPIException("Unknown token=>"+key,logger);
                    }
                }
                else
                {
                 //   throw new CTAPIException("Unknown token=>"+key,logger);
                }
            }
        }

        infoVect.add(info);

        ProjectInfo infoList[] = new ProjectInfo[infoVect.size()];

        infoList = (ProjectInfo []) infoVect.toArray(infoList);

        logger.exiting("ProjectInfoParser","getInfo");

        return infoList;
    }

    String [] VectToStringArray(Vector vect)
    {
        String strAray[] = new String[vect.size()];

        strAray = (String []) vect.toArray(strAray);

        return strAray;
    }
}
