package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;


/**
 * This is used to package several related arguments together.      
 */
public class MultipleAssociatedCommandLineArgument  extends CommandLineArgument {
    private Set commandLineArgumentsToGroup = null;
    private Set invalidArguments = new HashSet();
    private Map argumentsThatHaveBeenSet = null;
    private boolean satisfied = true;
    private boolean problemsFound = false;


    public MultipleAssociatedCommandLineArgument() {
        super("","Grouped arguments");
        commandLineArgumentsToGroup = new HashSet();

    }

    public void addCommandLineArgument(CommandLineArgument newArg) {
        commandLineArgumentsToGroup.add(newArg);
    }

    public final boolean isSatisfied() {
        return satisfied;
    }


    public final Set getCommandLineArgumentSet() {
        return commandLineArgumentsToGroup;
    }


    public String getReasonNotSatisfied() {
        String reason="";
        for (Iterator iter=invalidArguments.iterator();iter.hasNext();) {
            reason += ((CommandLineArgument)iter.next()).getReasonNotSatisfied();
        }
        return reason;
    }

    public Map getIndexToArgumentsMap() {
        return argumentsThatHaveBeenSet;
    }

    public String toString() {
        return "CommandLineArgument:"+commandLineArgumentsToGroup.toString()+"   ArgumentsThatAreSet:"+argumentsThatHaveBeenSet.toString();
    }

    private Set setOneArgumentGroup(Map argumentSetWithoutNumber) {
        boolean anyMatchesFound = false;
        boolean allMatchesFound = true;
        Set commandsThatHaveBeenSet = new HashSet();


        for (Iterator argsNeededIterator = commandLineArgumentsToGroup.iterator(); argsNeededIterator.hasNext();) {
            CommandLineArgument oneArgument = ((CommandLineArgument)argsNeededIterator.next()).copy();
            oneArgument.setValueFromMap(argumentSetWithoutNumber);

            if (oneArgument.isSatisfied()) {
                anyMatchesFound = true;
            } else {
                allMatchesFound = false;
                invalidArguments.add(oneArgument);
            }

            if (oneArgument.getValue()!=null) {
                commandsThatHaveBeenSet.add(oneArgument);
            }
        }

        if (anyMatchesFound & !allMatchesFound) {
            problemsFound = true;
        } else {
            problemsFound = commandsThatHaveBeenSet.isEmpty();
        }

        if (!commandsThatHaveBeenSet.isEmpty()) {
            super.setValue("ok.have.value"); //we need this line
        } else {
            super.setValue(null); //we need this as well. :)
        }

        return commandsThatHaveBeenSet;
    }

    public void setValues(Map fullArgumentMap) {
        problemsFound = false;
        Set processedNumbers = new HashSet();
        argumentsThatHaveBeenSet = new HashMap();
        for (Iterator keyIterator = fullArgumentMap.keySet().iterator(); keyIterator.hasNext();) {
            String testKey = (String) keyIterator.next();
            String keyIndex = getNumberString(testKey);
            if (keyIndex != null & !processedNumbers.contains(keyIndex)) {
                processedNumbers.add(keyIndex);
                Map singleArgumentMap = getAllArgumentsForIndex(keyIndex, fullArgumentMap);
                Set commandThatHaveBeenSet = setOneArgumentGroup(singleArgumentMap);

                if (!commandThatHaveBeenSet.isEmpty()) {
                    argumentsThatHaveBeenSet.put(keyIndex, commandThatHaveBeenSet);
                }
            }

        }

        satisfied = !problemsFound & !argumentsThatHaveBeenSet.isEmpty();

        if (!satisfied) {
            for (Iterator iter=commandLineArgumentsToGroup.iterator();iter.hasNext();) {
                CommandLineArgument argument = ((CommandLineArgument)iter.next());
                invalidArguments.add(argument);

            }
        }
    }

    private String getNumberString(String fullString) {
        StringBuffer returnString = null;
        boolean processingDigits = true;
        for (int characterIndex = fullString.length()-1; characterIndex >0 & processingDigits; characterIndex--) {
            if (Character.isDigit(fullString.charAt(characterIndex))) {
                if (returnString ==null) {
                    returnString = new StringBuffer();
                }
                returnString.append(fullString.charAt(characterIndex));
            } else {
                //Begin INT2395
                char ch = fullString.charAt(characterIndex);
                if (ch == '@') {
                    return null;
                }
                //End INT2395

                processingDigits = false;
            }
        }
        if (returnString!=null) {
            return returnString.reverse().toString(); /*PTM3465 for nos. > 10, it returns back 21 instead of 12*/
        } else {
            return null;
        }
    }

    private Map getAllArgumentsForIndex(String index, Map argumentMap) {
        Map oneIndexArgumentMap = new HashMap();
        for (Iterator keyIterator = argumentMap.keySet().iterator(); keyIterator.hasNext();) {
            String oneKey = (String) keyIterator.next();
            if (oneKey.endsWith(index)) {
                //Begin INT2395
                String tempStr = getNumberString(oneKey);
                if (tempStr != null) {
                    if (getNumberString(oneKey).equals(index)) {    // put this in there because even though a argument ends with a 1, it could be like 101 which isn't the same as 1
                        oneIndexArgumentMap.put(oneKey.substring(0, oneKey.length()-index.length()), argumentMap.get(oneKey));
                    }
                }
                //End INT2395
            }
        }
        return oneIndexArgumentMap;
    }
}
