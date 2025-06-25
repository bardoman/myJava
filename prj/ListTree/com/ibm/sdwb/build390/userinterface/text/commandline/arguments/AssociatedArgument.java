
package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;

import java.util.*;
import java.util.Map.Entry;

import com.ibm.sdwb.build390.utilities.*;


/**
 * This is used to package several related arguments together.      
 */
public class AssociatedArgument  extends CommandLineArgument {

    private Set<IndexedArgument> commandLineArgumentsToGroup = null;
    private Map<String,Enum> invalidArgumentsMap  = null;
    private Set<String> ignoreNumbersToProcess = null;
    private Map<String, Set<CommandLineArgument>> indexToArgumentsMap =null;
    private boolean satisfied = true;
    private int maxIndex = 0;
    private int minIndex = 0;
    private int iterableMaxIndex = 0;
    private int iterableMinIndex = -1;
    private BooleanOperation parentOperation;
    private boolean isIgnoreInCompleteGroup = false;


    public AssociatedArgument(BooleanOperation tempParent) {
        super("","Associated Argument");
        this.parentOperation = tempParent;
        commandLineArgumentsToGroup = new HashSet<IndexedArgument>();
        invalidArgumentsMap = new LinkedHashMap<String,Enum>();
        ignoreNumbersToProcess = new HashSet<String>();
        indexToArgumentsMap = new HashMap<String, Set<CommandLineArgument>> ();
    }

    public void addIndexedArgument(IndexedArgument newArg) {
        commandLineArgumentsToGroup.add(newArg);
    }

    public Set<IndexedArgument> getCommandLineArgumentsSet() {
        return commandLineArgumentsToGroup;
    }

    public String getNameOfBoolean() {
        StringBuffer strbd = new StringBuffer();
        Formatter formatter = new Formatter(strbd);
        for (Iterator<IndexedArgument> iter=commandLineArgumentsToGroup.iterator(); iter.hasNext();) {
            IndexedArgument anArgument = iter.next();
            formatter.format("%-42s",indentedString(anArgument.getCommandLineName(),anArgument.getDescriptionOfBoolean()));
            if (iter.hasNext()) {
                formatter.format("%n");
            }
        }
        return  strbd.toString();
    }

    private String  indentedString(String tempCommand, String tempExplanation) {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        StringTokenizer strk = new StringTokenizer(tempExplanation,"\n");
        formatter.format("%-20s%-22s%n",tempCommand,strk.nextToken());
        while (strk.hasMoreTokens()) {
            String str = strk.nextToken();
            formatter.format("%-22s%-22s","", str);
            if (strk.hasMoreTokens()) {
                formatter.format("%n");
            }
        }
        return strbd.toString();
    }


    /**
     *  Checks if all  numeric groups are satisfied
     *  A  single numeric group includes arguments for a particular index.
     */
    public final boolean isSatisfied() {
        satisfied = true;
        if (parentOperation instanceof BooleanAnd) {
            satisfied = satisfied & conditionAnd();
        } else if (parentOperation instanceof BooleanExclusiveOr) {
            satisfied = satisfied & conditionExclusiveOr(true);
        } else if (parentOperation instanceof BooleanOr) {
            satisfied = satisfied & conditionExclusiveOr(false);
        }

        return satisfied;
    }

    /**
     *  Checks if a specific numeric is satisfied 
     *  A  single numeric group includes arguments for a particular index.
     */
    protected final boolean isSatisfied(int index) {
        ReasonNotSatisfiedEnum type = null;
        if (parentOperation instanceof BooleanAnd) {
            type =  conditionAndAt(index);
        } else if (parentOperation instanceof BooleanExclusiveOr) {
            type =  conditionExclusiveOrAt(index,true);
        } else if (parentOperation instanceof BooleanOr) {
            type = conditionExclusiveOrAt(index,false);
        }

        switch (type) {
        case NONE:
            return true;
        default:
            return false;
        }
    }

    /** 
     * figures out if atleast one  argument is available at a particular index.
     */
    protected boolean inputAvailableAt(int index) {
        boolean available = false;
        boolean isGroupAvailable = true;
        for (Iterator<IndexedArgument> iter=commandLineArgumentsToGroup.iterator();(iter.hasNext() & !available);) {
            IndexedArgument iarg = iter.next();
            if (parentOperation instanceof BooleanExclusiveOr) {
                available = iarg.inputAvailableAt(index);
            } else if (parentOperation instanceof BooleanAnd) {
                isGroupAvailable = isGroupAvailable & iarg.inputAvailableAt(index);
            }
        }
        if (parentOperation instanceof BooleanAnd) {
            return isGroupAvailable;
        }

        return available;
    }

    /** 
     * figures out if atleast one  group is available.
     */
    public boolean inputAvailable() {
        boolean available = false;
        for (Iterator<IndexedArgument> iter=commandLineArgumentsToGroup.iterator();(iter.hasNext() & !available);) {
            available = iter.next().inputAvailable();
        }
        return available;
    }




    protected  int getMaxIndex() {
        return maxIndex;
    }

    protected  int getMinIndex() {
        return minIndex;
    }

    protected  void setIterableMaxIndex(int tempIterableMaxIndex) {
        this.iterableMaxIndex= tempIterableMaxIndex;
    }

    protected int  getIterableMaxIndex() {
        if (iterableMaxIndex <=0) {
            return maxIndex;
        }
        return iterableMaxIndex;
    }

    protected  void setIterableMinIndex(int tempIterableMinIndex) {
        this.iterableMinIndex= tempIterableMinIndex;
    }

    protected int  getIterableMinIndex() {
        if (iterableMinIndex < 0) {
            return minIndex;
        }
        return iterableMinIndex;
    }

    private void  setDefaultsIfIndicesAreZeroes() {
        if (getIterableMinIndex() == 0 & getIterableMaxIndex() ==0) {
            setIterableMaxIndex(1);
            setIterableMinIndex(1);
        }
    }

    protected void setIgnoreNumbersToProcess(Set<String> tempIgnoreNumbers) {
        this.ignoreNumbersToProcess = tempIgnoreNumbers;
    }

    protected void setIgnoreInCompleteGroup(boolean tempIsIgnoreInCompleteGroup) {
        this.isIgnoreInCompleteGroup = tempIsIgnoreInCompleteGroup;
    }

    protected boolean isIgnoreInCompleteGroup() {
        return isIgnoreInCompleteGroup;
    }


    protected Map<String, Set<CommandLineArgument>> getIndexToArgumentsMap() {
        for (IndexedArgument anArgument : commandLineArgumentsToGroup) {
            String commandLineName = anArgument.getCommandLineName();
            for (Map.Entry<String,String> entry : anArgument.getIndexToValuesMap().entrySet()) {
                String key = entry.getKey();
                if (!ignoreNumbersToProcess.contains(key)) { //fix to ignoreNumbers
                    Set<CommandLineArgument> argumentsSet = indexToArgumentsMap.get(key);
                    if (argumentsSet == null) {
                        argumentsSet = new HashSet<CommandLineArgument>();
                        indexToArgumentsMap.put(key, argumentsSet);
                    }
                    anArgument.setValue(entry.getValue());
                    CommandLineArgument arg = anArgument.createArgument();
                    arg.setValue(anArgument.getValue());
                    argumentsSet.add(arg);
                }
            }
        }
        return indexToArgumentsMap;
    }



    private ReasonNotSatisfiedEnum conditionAndAt(int tempIndex) {
        String index = String.valueOf(tempIndex);

        ReasonNotSatisfiedEnum rtype = ReasonNotSatisfiedEnum.NONE;
        for (Iterator<IndexedArgument> iter = commandLineArgumentsToGroup.iterator();iter.hasNext();) {
            ReasonNotSatisfiedEnum type = ReasonNotSatisfiedEnum.NONE;
            IndexedArgument checkArgument = iter.next();
            String value = checkArgument.getIndexToValuesMap().get(index);
            if (!checkArgument.getIndexToValuesMap().containsKey(index)) {
                type = ReasonNotSatisfiedEnum.WAS_NOT_SPECIFIED;
            }

            if (checkArgument.isAllowOrderedIndexEntry() && checkArgument.getProcessedOutOfOrderNumbersSet().contains(index)) {
                type = ReasonNotSatisfiedEnum.WAS_NOT_IN_ORDER;
            }

            if (value!=null && checkArgument.getArgument()!=null) {
                checkArgument.getArgument().setValue(value);
                if (checkArgument.getArgument().inputAvailable() && !checkArgument.getArgument().isSatisfied()) {
                    String reason = (checkArgument.getArgument().getReasonNotSatisfied()).replace(checkArgument.getCommandLineName(), checkArgument.getCommandLineName() + index);
                    type = ReasonNotSatisfiedEnum.WRAPPED_ARGUMENT;
                    type.setDescription(reason);
                }
            }

            if ((type!=ReasonNotSatisfiedEnum.NONE) && (!invalidArgumentsMap.containsKey(index))) {
                invalidArgumentsMap.put(checkArgument.getCommandLineName()+index,type);
                rtype = type;
            }

        }
        return rtype;
    }


    private ReasonNotSatisfiedEnum conditionExclusiveOrAt(int tempIndex,boolean isMutuallyExclusive) {
        String index = String.valueOf(tempIndex);

        ReasonNotSatisfiedEnum type = ReasonNotSatisfiedEnum.NONE;

        boolean allExists = true;
        boolean atleastOneExists = false;
        boolean otherArgumentsSatisfied = true;

        Map<String,Enum> singleInvalidMap  = new LinkedHashMap<String,Enum>();

        for (Iterator<IndexedArgument> iter = commandLineArgumentsToGroup.iterator();iter.hasNext();) {
            IndexedArgument checkArgument = iter.next();

            if (!checkArgument.getIndexToValuesMap().containsKey(index)) {
                type = ReasonNotSatisfiedEnum.WAS_NOT_SPECIFIED;
                allExists =false;
            } else {
                atleastOneExists = true;
            }

            if (checkArgument.getArgument()!=null) {
                checkArgument.getArgument().setValue(checkArgument.getIndexToValuesMap().get(index));
                if (checkArgument.getArgument().inputAvailable() && !checkArgument.getArgument().isSatisfied()) {
                    String reason = (checkArgument.getArgument().getReasonNotSatisfied()).replace(checkArgument.getCommandLineName(), checkArgument.getCommandLineName() + index);
                    type = ReasonNotSatisfiedEnum.WRAPPED_ARGUMENT;
                    type.setDescription(reason);
                    otherArgumentsSatisfied = false;
                }
            }

            if (type!= ReasonNotSatisfiedEnum.NONE) {
                singleInvalidMap.put(checkArgument.getCommandLineName()+index,type);
            }
        }

        if ((allExists & isMutuallyExclusive) || !atleastOneExists || !otherArgumentsSatisfied) {
            if (singleInvalidMap.isEmpty()) {
                type = ReasonNotSatisfiedEnum.CUSTOM;
                singleInvalidMap.put(new String("You can only choose one of:\n"+ 
                                                String.format("%s", getIndentedDescription())), type);
            }
            invalidArgumentsMap.putAll(singleInvalidMap);
        } else {
            type = ReasonNotSatisfiedEnum.NONE; //reset it.
        }

        return type;
    }


    private boolean conditionAnd() {

        boolean tempSatisfied = true;
        boolean atleastOnce = false;
        int count=0;
        setDefaultsIfIndicesAreZeroes();
        for (int i= getIterableMinIndex();i <= getIterableMaxIndex(); i++) {
            String index = String.valueOf(i);
            if (!ignoreNumbersToProcess.contains(index)) {
                tempSatisfied =  tempSatisfied & isSatisfied(i);
                atleastOnce = true;
            }

        }

        if (isIgnoreInCompleteGroup() & !atleastOnce) {
            atleastOnce = true;
        }
        if ((!atleastOnce && (ignoreNumbersToProcess.size() < getIterableMaxIndex()))  ||
            (ignoreNumbersToProcess.size() > getIterableMaxIndex())) {
            isSatisfied(getIterableMinIndex()+1); //this  is only to record the error.
            tempSatisfied = atleastOnce & tempSatisfied;
        }

        return tempSatisfied;
    }

    private boolean conditionExclusiveOr(boolean isMutuallyExclusive) {
        boolean tempSatisfied = true;
        boolean allGroupsExists = true;
        boolean atleastOneGroupExists = false;


        setDefaultsIfIndicesAreZeroes();
        for (int i= getIterableMinIndex() ;i <= getIterableMaxIndex(); i++) {
            String index = String.valueOf(i);
            if (!ignoreNumbersToProcess.contains(index)) {
                tempSatisfied = tempSatisfied & isSatisfied(i);
            }

        }

        return tempSatisfied;
    }


    public String getReasonNotSatisfied() {
        StringBuilder reason = new StringBuilder();
        Formatter formatter = new Formatter(reason);
        for (Iterator<Map.Entry<String,Enum>> entries = invalidArgumentsMap.entrySet().iterator();entries.hasNext();) {
            Map.Entry<String,Enum> entry = entries.next();

            if (entry.getKey().trim().length() > 0) {
                String errorDescription = entry.getKey();
                switch ((ReasonNotSatisfiedEnum)entry.getValue()) {
                case WAS_NOT_SPECIFIED:
                    errorDescription = String.format("%-20s%-22s",entry.getKey(), "was   not  specified.");
                    break;
                case WAS_NOT_IN_ORDER:
                    errorDescription  = String.format("%-20s%-22s", entry.getKey(), "was  not  in   order.");
                    break;
                case WRAPPED_ARGUMENT:
                    errorDescription = ((ReasonNotSatisfiedEnum)entry.getValue()).getDescription();
                    break;
                default:
                    break;
                }
                if (!errorDescription.startsWith("=>") && !errorDescription.startsWith("You ")) {
                    formatter.format("%-2s%s","=>",errorDescription);
                } else {
                    formatter.format("%s",errorDescription);
                }
            }

            if (entries.hasNext()) {
                formatter.format("%n");
            }

        }
        return reason.toString();
    }




    public void setValues(Map fullArgumentMap) {
        for (CommandLineArgument argument : commandLineArgumentsToGroup) {
            if (argument instanceof IndexedArgument) {
                ((IndexedArgument)argument).setValues(fullArgumentMap);
                int tempMax = ((IndexedArgument)argument).getMaxIndex();
                int tempMin = ((IndexedArgument)argument).getMinIndex();

                if (tempMax > maxIndex) {
                    maxIndex = tempMax;
                }

                if (((IndexedArgument)argument).inputAvailable()) {
                    if (minIndex <=0) {
                        minIndex = tempMin;
                    } else if (tempMin < minIndex) {
                        minIndex = tempMin;
                    }
                }
            } else if (argument instanceof MultipleAssociatedCommandLineArgument) {
                ((MultipleAssociatedCommandLineArgument)argument).setValues(fullArgumentMap);
            } else {
                argument.setValueFromMap(fullArgumentMap);
            }
        }

    }


    public String toString() {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        formatter.format("%s%n%s%n","AssociatedArguments:","--------------------------");
        for (Map.Entry<String, Set<CommandLineArgument>> entry : getIndexToArgumentsMap().entrySet()) {
            String key = entry.getKey();
            for (CommandLineArgument singleEntry : entry.getValue()) {
                formatter.format("%d=>%-15s=%-15s%n",Integer.parseInt(key), singleEntry.getCommandLineName(), singleEntry.getValue());
            }
        }
        return strbd.toString();

    }

    private String getIndentedDescription() {
        StringBuilder strbd = new StringBuilder();
        for (Iterator<IndexedArgument> iter =  getCommandLineArgumentsSet().iterator();iter.hasNext();) {
            String  joinWord = "";
            String allFormat = "%s";
            if (iter.hasNext()) {
                joinWord = "  (or)";
                allFormat = "%-2s%s%n%-42s%n";
            };
            IndexedArgument indexedArg = iter.next();
            strbd.append(String.format(allFormat,"=>",indentedString(indexedArg.getCommandLineName(), indexedArg.getDescriptionOfBoolean()), joinWord));
        }
        return strbd.toString();
    }

    private  enum ReasonNotSatisfiedEnum {
        NONE, 
        WAS_NOT_SPECIFIED,  
        WAS_NOT_IN_ORDER,
        WRAPPED_ARGUMENT,
        CUSTOM;

        private String description  = "";

        public void setDescription(String temp) {
            switch (this) {
            case CUSTOM:
                description= temp;
                break;
            case WRAPPED_ARGUMENT:      
                description= temp;
                break;
            default:
                break;
            }
        }


        public String getDescription() {
            return description;
        }
    };

}
