package com.ibm.sdwb.build390.metadata.filter;
/*********************************************************************/
/* CmvcMetadataFilterCriteria     class for the Build/390 client     */
/* A regular expression criteria to filter on cmvc metadata          */
/* keywords/value                                                    */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.util.*;

public abstract class AbstractMetadataFilterCriteria extends com.ibm.sdwb.build390.filter.criteria.RegularExpressionCriteria {

    private String metadataKey;

    public AbstractMetadataFilterCriteria(String metadataKey, String regex){
        super(regex);
        this.metadataKey = metadataKey;
    }

    String getKeyword(){
        return metadataKey;
    }

    

    public String toString(){
        return(metadataKey+"="+ getRegularExpression());
    }
}

