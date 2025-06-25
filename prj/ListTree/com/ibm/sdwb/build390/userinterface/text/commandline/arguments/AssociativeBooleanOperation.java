package com.ibm.sdwb.build390.userinterface.text.commandline.arguments;


import java.util.*;

import com.ibm.sdwb.build390.utilities.*;


public class AssociativeBooleanOperation implements BooleanInterface {
    private BooleanInterface booleanOperation  = null;
    private int iterableMaxIndex = 0;
    private int iterableMinIndex = 0;
    private Set<AssociatedArgument> groupedArgumentSet = null; 
    private Map<String, Set<CommandLineArgument>> indexToArgumentsMap =null;
    private boolean isIgnoreInCompleteGroup = false;

    public AssociativeBooleanOperation(BooleanInterface tempBooleanOperation) {
        this.booleanOperation = tempBooleanOperation;
        groupedArgumentSet = new HashSet<AssociatedArgument> ();
        indexToArgumentsMap = new HashMap<String, Set<CommandLineArgument>> ();
    }

    public BooleanInterface getBooleanOperation() {
        return  booleanOperation;
    }


    /** 
     * What is an incomplete group ? 
     * A a 
     * An example group MODELNAME/(MODELCLASS or MODELROOT).
     *If the group doesnot contain any one of the keywords then the group is incomplete.
     * eg: 1.MODELNAME1 
     *     2.MODELCLASS1
     *     3.MODELROOT1 MODELCLASS1
     *     4.MODELNAME2 
     * How do you figure out an incomplete group ? 
     *     A method which would tell us if  inputAvailableAt a particular index
     */
    public void setIgnoreInCompleteGroup() {
        this.isIgnoreInCompleteGroup = true;
    }

    protected boolean isIgnoreInCompleteGroup() {
        return isIgnoreInCompleteGroup;
    }


    public boolean isSatisfied() {
        /** before we process isSatisfied, get the dependent indices that should be processed, 
         * and the rest should be ignored.
         * and added to the ignoreNumberToProcessGroup.
        **/
        boolean temp = booleanOperation.isSatisfied();
        return temp;
    }

    public boolean inputAvailableAt(int index,boolean isVerifyGroup) {
        boolean available = false;
        boolean isGroupAvailable  = true;
        for (Iterator<AssociatedArgument> iter = groupedArgumentSet.iterator(); (iter.hasNext() & !available);) {
            AssociatedArgument gArgument = iter.next();
            boolean temp = gArgument.inputAvailableAt(index); 
            if (isVerifyGroup) {
                isGroupAvailable = isGroupAvailable & temp;
            } else if (temp) {
                available = true; //atleast one is available. 
            }

        }

        if (isVerifyGroup) { // all arguments for a particular index in the group are verified
            available = isGroupAvailable;
        }
        return available;
    }


    protected int getIterableMaxIndex() {
        return iterableMaxIndex;
    }

    protected int getIterableMinIndex() {
        return iterableMinIndex;
    }


    public Map<String, Set<CommandLineArgument>> getIndexToArgumentsMap() {
        for (AssociatedArgument anArgument : groupedArgumentSet) {
            for (Map.Entry<String,Set<CommandLineArgument>> entry : anArgument.getIndexToArgumentsMap().entrySet()) {
                Set <CommandLineArgument> argumentsSet = indexToArgumentsMap.get(entry.getKey());
                if (argumentsSet == null) {
                    argumentsSet = new HashSet<CommandLineArgument>();
                    indexToArgumentsMap.put(entry.getKey(), argumentsSet);
                }
                argumentsSet.addAll(entry.getValue());
            }
        }
        return indexToArgumentsMap;
    }

    public  void setValues() {
        if (booleanOperation instanceof BooleanOperation) {
            handleBooleanOperation((BooleanOperation)booleanOperation);
            if (iterableMaxIndex > 0) {
                //figure out the indexes to ignore. and set it into the grouped arguments.
                Set<String> ignoreNumbersToProcess = new HashSet<String> ();
                for (int i=getIterableMinIndex();i<=getIterableMaxIndex();i++) {
                    String numString = String.valueOf(i);
                    boolean available =  inputAvailableAt(i,false);
                    boolean ia = inputAvailableAt(i,true);
                    // If there aren't any argument available then we ignore those.
                    if (!available || (isIgnoreInCompleteGroup() & !ia)) {
                        ignoreNumbersToProcess.add(numString);
                    }
                }
                for (AssociatedArgument gArgument : groupedArgumentSet) {
                    gArgument.setIterableMaxIndex(iterableMaxIndex);
                    gArgument.setIterableMinIndex(iterableMinIndex);
                    gArgument.setIgnoreNumbersToProcess(ignoreNumbersToProcess);
                    gArgument.setIgnoreInCompleteGroup(isIgnoreInCompleteGroup());
                }
            }
        }


    }

    private void handleBooleanOperation(BooleanOperation operation) {
        for (Iterator  iter = operation.getOperandSet().iterator(); iter.hasNext();) {
            BooleanInterface argument = (BooleanInterface)iter.next();
            if (argument instanceof AssociatedArgument) {
                int tempMax = ((AssociatedArgument)argument).getMaxIndex();
                int tempMin = ((AssociatedArgument)argument).getMinIndex();

                if (tempMax > iterableMaxIndex) {
                    iterableMaxIndex = tempMax;
                }

                if (iterableMinIndex <=0) {
                    iterableMinIndex = tempMin;
                } else if (tempMin < iterableMinIndex) {
                    iterableMinIndex = tempMin;
                }
                groupedArgumentSet.add((AssociatedArgument)argument);
            } else if (argument instanceof BooleanOperation) {
                handleBooleanOperation((BooleanOperation)argument);
            }
        }
    }


    public String getReasonNotSatisfied() {
        return booleanOperation.getReasonNotSatisfied();
    }

    public String getNameOfBoolean() {
        return booleanOperation.getNameOfBoolean();
    }

    public String getDescriptionOfBoolean() {
        return  booleanOperation.getDescriptionOfBoolean();
    }

    public boolean inputAvailable() {
        return booleanOperation.inputAvailable();
    }

    public String toString() {
        getIndexToArgumentsMap();
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        formatter.format("%s%n%s%n","MultipleAssociatedGroupedIndexedArguments:","------------------------------------------------------------------------------------------------------");
        for (Map.Entry<String, Set<CommandLineArgument>> entry : indexToArgumentsMap.entrySet()) {
            String key = entry.getKey();
            for (CommandLineArgument singleEntry : entry.getValue()) {
                formatter.format("%d=>%-15s=%-15s%n",Integer.parseInt(key), singleEntry.getCommandLineName(), singleEntry.getValue());
            }
        }
        return strbd.toString();

    }

}
