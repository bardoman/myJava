package com.ibm.sdwb.build390.library;


import java.util.*;
import com.ibm.sdwb.build390.*;
import java.io.*;

public class LocalSourceInfo implements SourceInfo, Serializable
{
    private String name;

    private Set parts;

    private MBUBuild build = null;

    public LocalSourceInfo(Set parts, String name)
    {
        this.parts = parts;
        this.name = name;
    }

    public MBUBuild getBuild(){
        return build;
    }

    public void setBuild(MBUBuild tempBuild){
        build = tempBuild;
    }

    public SourceInfo getClone()
    {
        try
        {
            return(SourceInfo) clone();
        }
        catch(CloneNotSupportedException cnse)
        {
            throw new RuntimeException(cnse);
        }
    }

    public Set getSetOfParts()
    throws MBBuildException
    {
        return parts;
    }

    public Set getListOfMissingRequisites(){
        return new HashSet();
    }

    public Set getChangesetsInSource()//***BE
    throws MBBuildException
    {
        Set results = new HashSet();
        return results;
    }

    public boolean isValidSource()
    throws MBBuildException
    {
        if(parts!=null)
        {
            return true;
        }
        return false;
    }

    public String getSourceIdentifyingStringForMVS()
    {
        return "";
    }

    public String getState(){
        throw new RuntimeException("Get state not supported");
    }

    public void setIncludingCommittedBase(boolean temp){
        // nothing to do
    }

    public boolean isIncludingCommittedBase(){
        return false;
    }

    /**
     * returns the type of source object this is (bob, jill, etc)
     * 
     * @return String sourceType
     */
    public String getName()
    {
        return name;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public String getProject(){
        return new String();
    }
}
