package com.ibm.rational.clearcase;

public class ProjectInfo
{
    private String name="";
    private String createDate="";
    private String createBy="";
    private String description="";
    private String owner="";
    private String group="";
    private String folder="";
    private String integrationStream="";
    private String modifiableComponents[]=new String[0];
    private String defaultRebasePromotionLevel="";
    private String recommendedBaselines[]=new String[0];
    private String model="";
    private String policies[]=new String[0];
    private String baselineNamingTemplate="";

    public ProjectInfo()
    {
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getCreateDate()
    {
        return createDate;
    }
    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    public String getCreateBy()
    {
        return createBy;
    }
    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getOwner()
    {
        return owner;
    }
    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    public String getGroup()
    {
        return group;
    }
    public void setGroup(String group)
    {
        this.group = group;
    }
    public String getFolder()
    {
        return folder;
    }
    public void setFolder(String folder)
    {
        this.folder = folder;
    }
    public String getIntegrationStream()
    {
        return integrationStream;
    }
    public void setIntegrationStream(String integrationStream)
    {
        this.integrationStream = integrationStream;
    }
    public String [] getModifiableComponents()
    {
        return modifiableComponents;
    }
    public void setModifiableComponents(String modifiableComponents[])
    {
        this.modifiableComponents = modifiableComponents;
    }
    public String getDefaultRebasePromotionLevel()
    {
        return defaultRebasePromotionLevel;
    }
    public void setDefaultRebasePromotionLevel(String defaultRebasePromotionLevel)
    {
        this.defaultRebasePromotionLevel = defaultRebasePromotionLevel;
    }
    public String [] getRecommendedBaselines()
    {
        return recommendedBaselines;
    }
    public void setRecommendedBaselines(String recommendedBaselines[])
    {
        this.recommendedBaselines = recommendedBaselines;
    }
    public String getModel()
    {
        return model;
    }
    public void setModel(String model)
    {
        this.model = model;
    }
    public String [] getPolicies()
    {
        return policies;
    }
    public void setPolicies(String policies[])
    {
        this.policies = policies;
    }
    public String getBaselineNamingTemplate()
    {
        return baselineNamingTemplate;
    }
    public void setBaselineNamingTemplate(String baselineNamingTemplate)
    {
        this.baselineNamingTemplate = baselineNamingTemplate;
    }


    public String toString()
    {
        String str = 
        "Project Information=>\n"+
        "Name:"+name+"\n"+                   
        "createDate:"+createDate +"\n"+            
        "createBy:"+createBy +"\n"+            
        "description:"+description +"\n"+                
        "owner:"+owner+"\n"+          
        "group:"+group +"\n"+       
        "folder:"+folder +"\n"+     
        "integrationStream:"+integrationStream +"\n"+
        "modifiableComponents:\n";
        for(int i=0;i!=modifiableComponents.length;i++)
        {
            str+="  "+modifiableComponents[i]+"\n";
        }

        str+=
        "defaultRebasePromotionLevel:"+ defaultRebasePromotionLevel+"\n"+
        "recommendedBaselines:\n";
        for(int i=0;i!=recommendedBaselines.length;i++)
        {
            str+="  "+recommendedBaselines[i]+"\n";
        }

        str+=
        "model:"+model +"\n"+ 
        "policies:\n";
        for(int i=0;i!=policies.length;i++)
        {
            str+="  "+policies[i]+"\n";
        }

        str+=
        "baselineNamingTemplate:"+baselineNamingTemplate +"\n";        

        return str;
    }

}     











