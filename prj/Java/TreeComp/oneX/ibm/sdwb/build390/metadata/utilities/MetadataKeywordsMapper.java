package com.ibm.sdwb.build390.metadata.utilities;

import java.io.Serializable;
import java.util.*;


import com.ibm.sdwb.build390.*;


public class MetadataKeywordsMapper {

    private MetadataType[] metaTypes;
    private HashMap metaTypeMap;

    public MetadataKeywordsMapper(){
        metaTypeMap = new HashMap();
    }

    MetadataKeywordsMapper(MetadataType[] metaTypes){
        this.metaTypes = metaTypes;
        metaTypeMap = new HashMap();
        mapVirtualToRealKeyword();
    }

    public MetadataType[] getMetadataTypes(){
        return metaTypes;
    }

    public void setMetadataTypes(MetadataType[] metaTypes){
        this.metaTypes = metaTypes;
        mapVirtualToRealKeyword();
    }

    void mapVirtualToRealKeyword(){
        for (int i=0;i<metaTypes.length;i++) {
            metaTypeMap.put(metaTypes[i].getKeyword(),metaTypes[i]);
        }
    }


   public  String[] getVirtualKeywordsArray(){
        String[] keywordsArray = new String[metaTypes.length];
        for (int i=0;i<metaTypes.length;i++) {
            keywordsArray[i] = metaTypes[i].getKeyword();
        }
        return keywordsArray;
    }

    MetadataType getMetadataType(String virtualName){
        return((MetadataType)metaTypeMap.get(virtualName));
    }

    public String getRealKeyword(String virtualName) {
        if (getMetadataType(virtualName) == null) {
            return null;
        }
        return((MetadataType)metaTypeMap.get(virtualName)).getRealKeyword();
    }

    /** the suffix is actually numeric.
     * ie. 
     * DSC1
     * DNM1
    **/
    public String getRealKeywordWithSuffix(String virtualName,int suffix)  {
        MetadataType type = getMetadataType(virtualName);
        if (type == null) {
            return null; 
        }
        String realkeyword = type.getRealKeyword();
        if (type.getType().equalsIgnoreCase(MetadataType.MULT_ENTRY_TYPE)) {
            realkeyword += suffix;
        }
        return realkeyword;
    }




}

