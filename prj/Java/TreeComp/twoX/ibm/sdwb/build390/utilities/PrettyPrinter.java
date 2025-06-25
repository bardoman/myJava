
package com.ibm.sdwb.build390.utilities;

import java.util.*;

import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.*;

/**
 * This class is designed to print out boolean structures
 * in a pretty fashion.
 */
public class PrettyPrinter {
    private static final int numberOfSpacesToIndent = 3;
    private static final int defaultIndentLevel     = 1;
    private static final int maximumLineLength = 70;
    private static  int indentColumn = 0;

    public static final String handleBooleanInterface(BooleanInterface oneBoolean) {
        return handleBooleanInterface(oneBoolean,0);
    }
    private static final String handleBooleanInterface(BooleanInterface oneBoolean, int indentLevel) {

        if (oneBoolean instanceof BooleanOperation) {
            return handleBooleanOperator((BooleanOperation)oneBoolean,indentLevel);
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument) {
            return handleMultipleAssociatedArguments((com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument) oneBoolean, indentLevel);
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation) {
            return handleAssociativeBooleanOperation((com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation) oneBoolean, indentLevel);
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociatedArgument) {
            return handleAssociatedArguments((com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociatedArgument) oneBoolean, indentLevel);
        } else {
            if (indentLevel==0) {
                indentLevel=defaultIndentLevel;
            }



            String slash ="";
            if (oneBoolean instanceof CommandLineSwitch) {
                slash = "/";
            }



            int spaced = 20;
            if (indentLevel > 1) {
                spaced = spaced - ((indentLevel -1)*numberOfSpacesToIndent);
            }

            String   commandHelpString = "";
            if (oneBoolean instanceof EnumeratedCommandLineArgument) {
                EnumeratedCommandLineArgument oneEnumArgument = (EnumeratedCommandLineArgument)oneBoolean;
                commandHelpString  += printStringAtIndent(indentedEnumString(spaced,oneBoolean.getNameOfBoolean(),oneBoolean.getDescriptionOfBoolean(),oneEnumArgument.getDefaultValue()),indentLevel);
            } else {
                commandHelpString = indentedString(spaced,(slash+oneBoolean.getNameOfBoolean()),oneBoolean.getDescriptionOfBoolean());
                commandHelpString =  printStringAtIndent(commandHelpString,indentLevel);
            }


            if (oneBoolean instanceof IndexedArgument) {
                String commandName = oneBoolean.getNameOfBoolean();
                IndexedArgument iArg = (IndexedArgument)oneBoolean;
                int startIndex = iArg.getStartingIndex();
                String aop1 = printStringAtIndent(String.format("%-"+spaced +"s %s","",commandName + " must be indexed, starting at " + iArg.getStartingIndex() + " (such as " + commandName +  startIndex +", "+commandName +  ++startIndex +")."),indentLevel);
                commandHelpString +=  "\n"+aop1;
            }

            return commandHelpString;
        }
    }


    private static final String handleBooleanOperator(BooleanOperation oneOp, int indentLevel) {
        String outputString = new String();
        String operationName = oneOp.getOperationName();
        if (oneOp instanceof BooleanExclusiveOr) {
            if (indentLevel==0) {
                indentLevel = 1;
            }
            outputString += printIndentedString("You must choose one of", indentLevel)+"\n";
            operationName = "or";
        }
        int tempLevel = indentLevel;
        if (oneOp instanceof BooleanAnd) {
            indentLevel++; 
            tempLevel++;
        } else {
            tempLevel++;
        }

        for (Iterator operandIterator = oneOp.getOperandSet().iterator(); operandIterator.hasNext();) {
            outputString += handleBooleanInterface((BooleanInterface) operandIterator.next(), tempLevel);
            if (operandIterator.hasNext()) {
                outputString+="\n"+ printIndentedString("("+operationName.toLowerCase()+")", tempLevel)+"\n";
            }
        }
        indentLevel--;
        return outputString;
    }

    private static final String handleAssociativeBooleanOperation(com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation groupedArg, int indentLevel) {
        if (indentLevel ==0) {
            indentLevel  = 1;
        }
        String outputString = printStringAtIndent("The following arguments must be treated as a group (all specified or none)\n", indentLevel)+"\n";
        outputString += handleBooleanInterface(groupedArg.getBooleanOperation(), indentLevel);
        return outputString;
    }


    private static final String handleMultipleAssociatedArguments(com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument argSet, int indentLevel) {
        if (indentLevel ==0) {
            indentLevel  = 1;
        }
        String outputString = printStringAtIndent("The following arguments must be treated as a group (all specified or none)\nand each group must be numbered, starting at 1 (such as a1, b1, a2, b2)", indentLevel)+"\n";
        int tempIndent = indentLevel +1;
        for (Iterator argumentIterator = argSet.getCommandLineArgumentSet().iterator(); argumentIterator.hasNext();) {
            outputString += handleBooleanInterface((BooleanInterface) argumentIterator.next(), tempIndent);
            if (argumentIterator.hasNext()) {
                outputString+="\n";
            }
        }
        return outputString;
    }


    private static final String handleAssociatedArguments(com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociatedArgument argSet, int indentLevel) {
        if (indentLevel ==0) {
            indentLevel  = 1;
        }
        String outputString ="";
        for (Iterator argumentIterator = argSet.getCommandLineArgumentsSet().iterator(); argumentIterator.hasNext();) {
            outputString += handleBooleanInterface((BooleanInterface) argumentIterator.next(), indentLevel);
            if (argumentIterator.hasNext()) {
                outputString+="\n";
            }
        }
        return outputString;

    }



    private static final String printIndentedString(String inputString, int indentLevel) {
        String processedIndentedString = new String();
        String workingString = String.format(" %-"+(indentLevel*numberOfSpacesToIndent)+"s",""); // start off with an indent

        for (StringTokenizer tokenizedInputString = new StringTokenizer(inputString,"\t\r\f\n"); tokenizedInputString.hasMoreTokens() ; ) {
            String nextToken = tokenizedInputString.nextToken();
            // do this so if somehow we end up with one HUGE word to add we don't get in an infinite loop
            if (workingString.trim().length()>0 
                & (workingString.length()+nextToken.length() > maximumLineLength)) {
                processedIndentedString+=workingString+"\n";
                indentColumn = (indentLevel+5)*numberOfSpacesToIndent;
                workingString = String.format(" %-"+(indentColumn)+"s"+"%s","",nextToken);
            } else {
                workingString += String.format("%s",nextToken);
            }
        }
        processedIndentedString+=workingString;
        return processedIndentedString;
    }

    private static final String printStringAtIndent(String inputString, int indentLevel) {
        String processedIndentedString = new String();
        String workingString = String.format(" %-"+(indentLevel*numberOfSpacesToIndent)+"s",""); // start off with an indent 
        for (StringTokenizer tokenizedInputString = new StringTokenizer(inputString,"\t\r\f\n"); tokenizedInputString.hasMoreTokens() ; ) {
            String nextToken = tokenizedInputString.nextToken();
            // do this so if somehow we end up with one HUGE word to add we don't get in an infinite loop
            if (workingString.trim().length()>0 
                & (workingString.length()+nextToken.length() > maximumLineLength)) {
                processedIndentedString+=workingString+"\n";
                workingString = String.format(" %-"+(indentLevel*numberOfSpacesToIndent)+"s"+"%s","",nextToken);
            } else {
                workingString += String.format("%s",nextToken);
            }
        }
        processedIndentedString+=workingString;
        return processedIndentedString;
    }

    private static String  indentedString(int tempSpaced, String tempCommand, String tempExplanation) {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        StringTokenizer strk = new StringTokenizer(tempExplanation,"\n");
        formatter.format("%-"+tempSpaced + "s %s%n",tempCommand,strk.nextToken());
        while (strk.hasMoreTokens()) {
            String str = strk.nextToken();
            formatter.format("%-"+tempSpaced + "s %s","", str);
            if (strk.hasMoreTokens()) {
                formatter.format("%n");
            }
        }
        return strbd.toString();
    }



    private static String  indentedEnumString(int tempSpaced, String tempCommand, String tempExplanation,String defaultValue) {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        StringTokenizer strk = new StringTokenizer(tempExplanation,"\n");
        String tempToken = strk.nextToken();
        if (tempToken.contains("<") && tempToken.contains(">")) {
            tempToken = tempToken.toUpperCase();
        }
        formatter.format("%-"+tempSpaced + "s %s%n",tempCommand,tempToken);
        while (strk.hasMoreTokens()) {
            String str = strk.nextToken();
            String[] splitString = str.split("-",2);
            if (splitString!=null && splitString.length >=2) {
                String first  = splitString[0];
                String second = splitString[1];
                if (first!=null && second !=null) {
                        first = first.toUpperCase();
                    formatter.format("%-"+tempSpaced + "s %-12s -%s","",first,second);
                } else {
                    formatter.format("%-"+tempSpaced + "s %s","", str);
                }
            } else {
                formatter.format("%-"+tempSpaced + "s %s","", str);
            }
            if (strk.hasMoreTokens()) {
                formatter.format("%n");
            }
        }
        if (defaultValue!=null) {
            formatter.format("%n%-"+tempSpaced + "s %-12s - %s%n","", "default",defaultValue);
        }
        return strbd.toString();
    }

}
