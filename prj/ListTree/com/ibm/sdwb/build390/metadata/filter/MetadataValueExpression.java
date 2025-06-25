package com.ibm.sdwb.build390.metadata.filter;

public class MetadataValueExpression {

    private String metadataValue ="";

    private boolean booleanOr =false;

    public MetadataValueExpression(String metadataValue){
        this.metadataValue =metadataValue; 
        if (metadataValue.endsWith("|")) {
            booleanOr = true;
        }
    }

    public String getSearchValue() {
        return metadataValue.trim();
    }

    public boolean endsWithBooleanOr() {
        return booleanOr;
    }

    public String toString(){
        return getSearchValue();
    }

}
