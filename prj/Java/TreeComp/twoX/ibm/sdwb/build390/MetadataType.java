package com.ibm.sdwb.build390;
/*********************************************************************/
/* Java MetadataType class for the Build/390 client                          */
/*  contains metadata type info      */
/*********************************************************************/
// 04/17/2001 Creation
//

/**  
 * <br>The MetadataType class  contains metadata type info
  */
public class MetadataType {
    public static final String BOOLEAN_TYPE            = "BOOLEAN_TYPE";
    public static final String NUMERICAL_TYPE          = "NUMERICAL_TYPE";
    public static final String SINGLE_ENTRY_TYPE       = "SINGLE_ENTRY_TYPE";
    public static final String MULT_ENTRY_TYPE         = "MULT_ENTRY_TYPE";
    public static final String COMPARITOR_EQUAL        = "EQ";
    public static final String COMPARITOR_NOT_EQUAL    = "NE";
    public static final String COMPARITOR_LESS_THAN    = "LT";
    public static final String COMPARITOR_MORE_THAN    = "GT";
    public static final String ASSIGNMENT_EQUAL        = "=";
    public static final String BOOLEAN_TRUE            = "ON";
    public static final String BOOLEAN_FALSE           = "OFF";
    public static final String LOGICAL_AND             = ",";
    public static final String LOGICAL_OR              = "|";

    String keyword=null;
    String realName=null;
    String type=null;
    String description=null;
    int maxLength=0;
    int maxCnt=0;

    public MetadataType(String keyword,String realName, String type, String description, int maxLength, int maxCnt){
        this.keyword=keyword;
        this.realName=realName; 
        this.type=type;
        this.description=description;
        this.maxLength=maxLength;
        this.maxCnt=maxCnt;
    }

    public MetadataType(String keyword,String type, String description, int maxLength, int maxCnt)    {
        this.keyword=keyword;
        this.type=type;
        this.description=description;
        this.maxLength=maxLength;
        this.maxCnt=maxCnt;
    }

    public MetadataType(String keyword,String type, String description, int maxLength)    {
        this.keyword=keyword;
        this.type=type;
        this.description=description;
        this.maxLength=maxLength;
    }

    public MetadataType(String keyword,String type, String description)    {
        this.keyword=keyword;
        this.type=type;
        this.description=description;
    }

    public MetadataType()    {
    }

    public String getKeyword()    {
        return keyword;
    } 

    public void setKeyword(String keyword)    {
        this.keyword=keyword;
    }

    public String getType()    {
        return type;
    }

    public void setType(String type)    {
        this.type=type;
    }

    public String getDescription()    {
        return description;
    }

    public String getRealKeyword(){
        return realName;
    }

    public void setDescription(String description)    {
        this.description=description;
    }

    public int getMaxLength()    {
        return maxLength;
    }

    public void setMaxLength(int maxLength)    {
        this.maxLength=maxLength;
    }

    public int getMaxCnt()    {
        return maxCnt;
    }

    public void setMaxCnt(int maxCnt)    {
        this.maxCnt=maxCnt;
    }

    public void setRealKeyword(String realName){
        this.realName = realName;
    }


    public String toString() {
        return "[keyword="+getKeyword() +",realname="+getRealKeyword()+",type="+getType() + ",description="+ getDescription() +"]\n"; 
    }
}
