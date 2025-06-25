package com.ibm.rational.clearcase;

public class ViewInfo
{
    private String name="";
    private String globalPath="";
    private String serverHost="";
    private String region="";
    private boolean active=false;
    private String viewTagUuid="";
    private String viewOnHost="";
    private String viewServerAccessPath="";
    private String viewUuid="";
    private String viewAttributes="";
    private String viewOwner="";

    public ViewInfo()
    {
    }

    public String getName()
    {
        return name;
    }

    public String getGlobalPath()
    {
        return globalPath;
    }

    public String getServerHost()
    {
        return serverHost;
    }

    public String getRegion()
    {
        return region;
    }

    public boolean isActive()
    {
        return active;
    }

    public String setViewTagUuid()
    {
        return viewTagUuid;
    }

    public String getViewOnHost()
    {
        return viewOnHost;
    }

    public String getViewServerAccessPath()
    {
        return viewServerAccessPath;
    }

    public String getViewUuid()
    {
        return viewUuid;
    }

    public String getViewAttributes()
    {
        return viewAttributes;
    }

    public String getViewOwner()
    {
        return viewOwner;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setGlobalPath(String globalPath)
    {
        this.globalPath = globalPath;
    }

    public void setServerHost(String serverHost)
    {
        this.serverHost = serverHost;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public void setActive(boolean active )
    {
        this.active = active;
    }

    public void setViewTagUuid(String viewnameUuid)
    {
        this.viewTagUuid=viewnameUuid;
    }

    public void setViewOnHost(String viewOnHost)
    {
        this.viewOnHost=viewOnHost;
    }

    public void setViewServerAccessPath(String viewServerAccessPath)
    {
        this.viewServerAccessPath=viewServerAccessPath;
    }

    public void setViewUuid(String viewUuid)
    {
        this.viewUuid=viewUuid;
    }

    public void setViewAttributes(String viewAttributes)
    {
        this.viewAttributes=viewAttributes;
    }

    public void setViewOwner(String viewOwner)
    {
        this.viewOwner=viewOwner;
    }

    public String toString()
    {
        String str = 
        "View Information=>\n"+
        "name:"+name+"\n"+                   
        "GlobalPath:"+ globalPath+"\n"+            
        "ServerHost:"+ serverHost+"\n"+            
        "Region:"+ region+"\n";

        if(active == true)
        {
            str+="Active:true\n";
        }
        else
        {
            str+="Active:false\n";
        }

        str+= "ViewTagUuid:"+ viewTagUuid+"\n"+     
              "ViewOnHost:"+ viewOnHost+"\n"+             
              "ViewServerAccessPath:"+ viewServerAccessPath+"\n"+   
              "ViewUuid:"+ viewUuid+"\n"+         
              "ViewAttributes:"+ viewAttributes+"\n"+        
              "ViewOwner:"+ viewOwner+"\n"; 

        return str;
    }
}
