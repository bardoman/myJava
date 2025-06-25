package com.ibm.sdwb.build390.filter.criteria;
/*********************************************************************/
/* RegularExpressionCriteria      class for the Build/390 client     */
/* A  modified filtercriteria  class which allows  matching to be    */
/* perfomed  based on regularexpression.                             */
/*********************************************************************/
//02/11/2005 SDWB2398 Filter/Replace by metadata in cmvc 
/*********************************************************************/
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

public abstract class RegularExpressionCriteria  implements FilterCriteria {

    private Pattern pattern = null;
    private String regularExpression  = null;

    public RegularExpressionCriteria(String regularExpression) {
        this.regularExpression = regularExpression;

    }  

    public void usePatternAsStringLiteral(){
        /* this.regularExpression = Pattern.quote(regularExpression);  available in 1.5 only
        ^ and $ match the start/ending characters of a line.*/
        this.regularExpression = "^\\Q" + regularExpression + "\\E$";
    }

    public void compile()  {
        pattern = Pattern.compile(getRegularExpression(),Pattern.CASE_INSENSITIVE);
    }


    public  final boolean hasMatch(Object obj) {
        Matcher matcher = pattern.matcher((String)obj);
        boolean findo = matcher.find();
        return(findo); 
    }

    public final Pattern getPattern(){
        return pattern;
    }


    public final String getRegularExpression(){
        return regularExpression;
    }

    public String toString(){
        return("Regular expression is: " + getRegularExpression());
    }

}
