package com.ibm.rational.clearcase;

public class HostInfo
{
    private String client="";
    private String product="";
    private String operatingSystem="";
    private String hardwareType="";
    private String registryHost="";
    private String registryRegion="";
    private String licenseHost="";

    public HostInfo()
    {
    }

    public String getClient()
    {
        return client;
    }

    public void setClient(String client)
    {
        this.client = client;
    }

    public String getproduct()
    {
        return product;
    }

    public void setProduct(String product)
    {
        this.product = product;
    }

    public String getOperatingSystem()
    {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem)
    {
        this.operatingSystem = operatingSystem;
    }

    public String getHardwareType()
    {
        return hardwareType;
    }

    public void setHardwareType(String hardwareType)
    {
        this.hardwareType = hardwareType;
    }

    public String getRegistryHost()
    {
        return registryHost;
    }

    public void setRegistryHost(String registryHost)
    {
        this.registryHost = registryHost;
    }

    public String getRegistryRegion()
    {
        return registryRegion;
    }

    public void setRegistryRegion(String registryRegion)
    {
        this.registryRegion = registryRegion;
    }

    public String getLicenseHost()
    {
        return licenseHost;
    }

    public void setLicenseHost(String licenseHost)
    {
        this.licenseHost = licenseHost;
    }

    public String toString()
    {
        String str = 
        "Host Information=>\n"+
        "client:"          +client+"\n"+         
        "product:"         +product+"\n"+       
        "operatingSystem:" +operatingSystem+"\n"+
        "hardwareType:"    +hardwareType+"\n"+   
        "registryHost:"    +registryHost+"\n"+   
        "registryRegion:"  +registryRegion+"\n"+ 
        "licenseHost:"     +licenseHost+"\n";    

        return str;
    }
}     











