package com.ibm.rational.clearcase;

public class VobInfo
{
    private String name="";
    private String description="";
    private String globalPath="";
    private String serverHost="";
    private String access="";
    private String mountOptions="";
    private String region="";
    private boolean active=false;
    private String vobTagReplicaUuid="";
    private String vobOnHost="";
    private String vobServerAccessPath="";
    private String vobFamilyUuid="";
    private String vobReplicaUuid="";
    private String vobRegistryAttributes="";

    public VobInfo()
    {
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getGlobalPath()
    {
        return globalPath;
    }

    public String getServerHost()
    {
        return serverHost;
    }

    public String getAccess()
    {
        return access;
    }

    public String getMountOptions()
    {
        return mountOptions;
    }

    public String getRegion()
    {
        return region;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getVobTagReplicaUuid()
    {
        return vobTagReplicaUuid;
    }

    public String getVobOnHost()
    {
        return vobOnHost;
    }

    public String getVobServerAccessPath()
    {
        return vobServerAccessPath;
    }

    public String getVobFamilyUuid()
    {
        return vobFamilyUuid;
    }

    public String getVobReplicaUuid()
    {
        return vobReplicaUuid;
    }

    public String getVobRegistryAttributes()
    {
        return vobRegistryAttributes;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setGlobalPath(String globalPath)
    {
        this.globalPath = globalPath;
    }

    public void setServerHost(String serverHost)
    {
        this.serverHost = serverHost;
    }

    public void setAccess(String access)
    {
        this.access = access;
    }

    public void setMountOptions(String mountOptions)
    {
        this.mountOptions = mountOptions;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public void setActive(boolean active )
    {
        this.active = active;
    }

    public void setVobTagReplicaUuid(String vobTagReplicaUuid)
    {
        this.vobTagReplicaUuid = vobTagReplicaUuid;
    }

    public void setVobOnHost(String vobOnHost)
    {
        this.vobOnHost = vobOnHost;

    }

    public void setVobServerAccessPath(String vobServerAccessPath)
    {
        this.vobServerAccessPath = vobServerAccessPath;
    }

    public void setVobFamilyUuid(String vobFamilyUuid)
    {
        this.vobFamilyUuid = vobFamilyUuid;
    }

    public void setVobReplicaUuid(String vobReplicaUuid)
    {
        this.vobReplicaUuid = vobReplicaUuid;
    } 

    public void setVobRegistryAttributes(String vobRegistryAttributes)
    {
        this.vobRegistryAttributes = vobRegistryAttributes;
    } 

    public String toString()
    {
        String str = 
        "Vob Information=>\n"+
        "name:"+name+"\n"+
        "description:"+description+"\n"+
        "GlobalPath:"+ globalPath+"\n"+            
        "ServerHost:"+ serverHost+"\n"+            
        "Access:"+ access+"\n"+                
        "MountOptions:"+ mountOptions+"\n"+          
        "Region:"+ region+"\n";

        if(active == true)
        {
            str+="Active:true\n";
        }
        else
        {
            str+="Active:false\n";
        }

        str+= "VobTagReplicaUuid:"+ vobTagReplicaUuid+"\n"+     
              "VobOnHost:"+ vobOnHost+"\n"+             
              "VobServerAccessPath:"+ vobServerAccessPath+"\n"+   
              "VobFamilyUuid:"+ vobFamilyUuid+"\n"+         
              "VobReplicaUuid:"+ vobReplicaUuid+"\n"+        
              "VobRegistryAttributes:"+ vobRegistryAttributes+"\n"; 

        return str;
    }

}
