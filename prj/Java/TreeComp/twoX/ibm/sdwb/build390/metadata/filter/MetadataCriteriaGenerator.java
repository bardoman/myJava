package com.ibm.sdwb.build390.metadata.filter;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

import com.ibm.sdwb.build390.filter.criteria.*;
import com.ibm.sdwb.build390.metadata.utilities.MetadataKeywordsMapper;
import com.ibm.sdwb.build390.MBBuildException;

public class MetadataCriteriaGenerator {


    private MetadataKeywordsMapper keywordsMapper;
    private boolean useRegexEngine = false;
    private StringBuffer  nonMetadataKeywords = new StringBuffer();

    public MetadataCriteriaGenerator(MetadataKeywordsMapper keywordsMapper){
        this.keywordsMapper = keywordsMapper;
    }

    public MetadataCriteriaGenerator(MetadataKeywordsMapper keywordsMapper,boolean useRegexEngine){
        this.keywordsMapper = keywordsMapper;
        this.useRegexEngine = useRegexEngine;
    }


    public   MultiFilterCriteria generateCriteria(Collection unparsedExpressions)   {
        AndOperationCriteria globalCriteria = new AndOperationCriteria();

        for (Iterator iter = unparsedExpressions.iterator();iter.hasNext();) {
            FilterCriteria criteria =  create(iter);
            globalCriteria.addFilterCriteria(criteria);
        }
        return globalCriteria;
    }

    public  static String generateCriteriaAsString(Collection unparsedExpressions) {
        Iterator criteriaIterator = unparsedExpressions.iterator();
        String cmd = "";
        int index = 0;
        do {
            String criteriaString = (String)criteriaIterator.next();
            if (criteriaString.trim().length() > 0) {
                criteriaString = criteriaString.replaceAll("\"","");
                criteriaString = criteriaString.replaceAll("="," EQ ");
                criteriaString = MetadataExpression.WildCardChecker.convert(criteriaString); 
                if (!(criteriaString.endsWith(",") || criteriaString.endsWith("|"))) {
                    criteriaString += ",";
                }
                cmd+= ", CR"+index+"=\""+criteriaString.trim()+"\"";
                index++;
            }

        }while (criteriaIterator.hasNext());
        return cmd;

    }

    public String  foundNonMetadataKeywords(){
        return nonMetadataKeywords.toString().trim();
    }

    private MetadataExpression createExpression(String expressionString)  {
        String[] tokenizedArray = expressionString.trim().split("\\s+"); /* split on one or more space */
        MetadataExpression expression = new  MetadataExpression(tokenizedArray);
        String realKeyword =    keywordsMapper.getRealKeyword(expression.getVirtualKeyword());

        if (realKeyword==null) {
            nonMetadataKeywords.append(expression.getVirtualKeyword() +"\n");
            realKeyword = expression.getVirtualKeyword();
        }
        expression.setRealKeyword(realKeyword);
        return expression;
    }


    private MetadataExpressionCriteria createExpressionCriteria(String expressionString)  {
        if (expressionString.endsWith(",")) {
            expressionString = expressionString.substring(0,expressionString.length()-1).trim();
        }
        MetadataExpression expression = createExpression(expressionString);
        MetadataExpressionCriteria singleCriteria = new MetadataExpressionCriteria(expression);
        if (!useRegexEngine) {
            singleCriteria.usePatternAsStringLiteral();
        }
        singleCriteria.compile();
        return singleCriteria;
    }


    private FilterCriteria create(Iterator iter)  {
        MetadataExpressionCriteria singleCriteria = createExpressionCriteria((String)iter.next());
        if (iter.hasNext()) {
            if (nextBooleanOr(singleCriteria.getMetadataExpression())) {
                OrOperationCriteria  singleAggregatedOr = new OrOperationCriteria();
                singleAggregatedOr.addFilterCriteria(singleCriteria);     
                return handleOr(singleAggregatedOr,iter);
            }
            AndOperationCriteria singleAggregatedAnd =  new AndOperationCriteria();
            singleAggregatedAnd.addFilterCriteria(singleCriteria);     
            return handleAnd(singleAggregatedAnd,iter);
        }
        return singleCriteria;
    }

    private boolean nextBooleanOr(MetadataExpression value){
        return value.getMetadataValueExpression().endsWithBooleanOr();
    }


    private BooleanOperationCriteria handleOr(OrOperationCriteria mainOr,Iterator iter)  {
        MetadataExpressionCriteria singleOrCriteria = createExpressionCriteria((String)iter.next());    
        mainOr.addFilterCriteria(singleOrCriteria);
        if (iter.hasNext()) {
            if (nextBooleanOr(singleOrCriteria.getMetadataExpression())) {
                handleOr(mainOr,iter);
            }
        }
        return mainOr;
    }

    private BooleanOperationCriteria handleAnd(AndOperationCriteria mainAnd,Iterator iter)  {
        MetadataExpressionCriteria singleAndCriteria = createExpressionCriteria((String)iter.next());    
        mainAnd.addFilterCriteria(singleAndCriteria);
        if (iter.hasNext()) {
            if (!nextBooleanOr(singleAndCriteria.getMetadataExpression())) {
                handleAnd(mainAnd,iter);
            }
        }
        return mainAnd;
    }


    public static void main(String[] args) throws Exception {
        MetadataCriteriaGenerator generator = new MetadataCriteriaGenerator(null);
        test0(generator);
        test1(generator);
        test2(generator);
        test3(generator);
        test4(generator);
        test5(generator);
        test6(generator);
        test7(generator);

    }


    static void test0(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO");
        System.out.println ("start test 0.");
        dumpCriteria(gen.generateCriteria(test));
        gen.generateCriteria(test);
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 0.");

    }

    static void test1(MetadataCriteriaGenerator gen)  {
        Vector test = new Vector();
        test.addElement("APARM EQ DO|");
        System.out.println ("start test 1.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 1.");


    }

    static void test2(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO|");
        test.addElement("SOTYPE EQ PLX");
        System.out.println ("start test 2.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 2.");

    }

    static void test3(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO|");
        test.addElement("SOTYPE EQ PLX|");
        System.out.println ("start test 3.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 3.");

    }


    static void test4(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO");
        test.addElement("SOTYPE EQ PLX");
        System.out.println ("start test 4.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 4.");

    }


    static void test5(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO|");
        test.addElement("NOSYM LT OFF|");
        test.addElement("DESC  GT BABA");
        test.addElement("SOTYPE EQ PLX");
        System.out.println ("start test 5.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 5.");

    }


    static void test6(MetadataCriteriaGenerator gen)  {
        Vector test = new Vector();
        test.addElement("APARM EQ DO|");
        test.addElement("NOSYM LT OFF|");
        test.addElement("DESC  GT BABA");
        test.addElement("SOTYPE EQ PLX|");
        System.out.println ("start test 6.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 6.");

    }


    static void test7(MetadataCriteriaGenerator gen) {
        Vector test = new Vector();
        test.addElement("APARM EQ DO");
        test.addElement("NOSYM LT OFF");
        test.addElement("DESC  GT BABA");
        test.addElement("SOTYPE EQ PLX");
        System.out.println ("start test 7.");
        dumpCriteria(gen.generateCriteria(test));
        System.out.println(gen.generateCriteriaAsString(test));
        System.out.println ("end   test 7.");

    }

    static void dumpCriteria(MultiFilterCriteria criteria) {
        System.out.println(criteria);

    }


}
