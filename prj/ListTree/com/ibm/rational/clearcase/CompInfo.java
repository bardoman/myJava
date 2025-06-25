package com.ibm.rational.clearcase;

public class CompInfo
{
    private String name="";
    private String createDate="";
    private String createBy="";
    private String rootDirectory="";
    private String owner="";
    private String group="";
   
    public CompInfo()
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
    public String getRootDirectory()
    {
        return rootDirectory;
    }
    public void setRootDirectory(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
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
    

    public String toString()
    {
        String str = 
        "Component Information=>\n"+
        "Name:"+name+"\n"+                   
        "createDate:"+createDate +"\n"+            
        "createBy:"+createBy +"\n"+            
        "rootDirectory:"+rootDirectory +"\n"+                
        "owner:"+owner+"\n"+          
        "group:"+group +"\n";       
                
        return str;
    }

}     











