package com.ibm.sdwb.build390.utilities;

import java.util.*;



/**
 * To perform a logical XOR on BooleanInferface objects
 * passed in.
 */
public class BooleanExclusiveOr extends BooleanOperation {

    private boolean multipleTrueFound = false;
    private boolean trueFound = false;

    public boolean isSatisfied() {
        boolean trueFound = false;
        boolean available = false;
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            boolean temp = false;
            if (oneBoolean.isSatisfied()) {
                temp = oneBoolean.isSatisfied();
            }
            if (temp) {
                if (trueFound) {
                    // we found more than one true, which is not allowed
                    return false;  
                } else {
                    trueFound = true;
                }
            } else {
                available = available || oneBoolean.inputAvailable(); //track incomplete set.
            }
        }

        if (trueFound && available) { //we found an incomplete set, which is not allowed."
            trueFound = false;
        }

        return trueFound;
    }

    public final boolean inputAvailable() {
        boolean available = true;
        int i=0;
        for (Iterator boolIterator = booleanInterfaceSet.iterator(); boolIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) boolIterator.next();
            if (!oneBoolean.inputAvailable()) {
                i++;
            }
            available = available || oneBoolean.inputAvailable();
        }
        if (i==booleanInterfaceSet.size()) {
            available = false;
        }
        return available;
    }

    protected String getOperationName() {
        return "XOR";
    }

    public String getReasonNotSatisfied() {
        trueFound = false;
        multipleTrueFound = false;
        String trueDescriptions = new String();
        trueDescriptions = handleReason(booleanInterfaceSet,trueDescriptions);
        String reasonNotSatisfied = "";
        boolean inputAvailable = inputAvailable();
        if (!trueFound) {
            int i=0;
            for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
                BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
                if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument) {
                    com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument msa =
                    (com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument)oneBoolean;
                    if (!msa.isSatisfied()) {
                        i++;
                        reasonNotSatisfied = appendORword(i,reasonNotSatisfied);
                        reasonNotSatisfied += "\n"+msa.getReasonNotSatisfied();
                    }
                } else {
                    if (inputAvailable) {
                        if (!oneBoolean.isSatisfied() && oneBoolean.inputAvailable()) {
                            i++;
                            reasonNotSatisfied = appendORword(i,reasonNotSatisfied);
                            reasonNotSatisfied += "\n"+oneBoolean.getReasonNotSatisfied();
                        }
                    } else {
                        i++;
                        reasonNotSatisfied = appendORword(i,reasonNotSatisfied);
                        reasonNotSatisfied += "\n"+oneBoolean.getReasonNotSatisfied();
                    }
                }

            }

            String temp  = String.format("%-42s","===========================================");
            if (i >=2) {
                if (!reasonNotSatisfied.contains("You ")) {
                    temp += "\nYou must choose one of:";
                }
            }
            reasonNotSatisfied = temp + reasonNotSatisfied;
        } else if (multipleTrueFound) {
            reasonNotSatisfied = String.format("%-42s%n","===========================================");
            reasonNotSatisfied += "You can only choose one of:\n"+trueDescriptions;

        }

        reasonNotSatisfied += String.format("%n%-42s","===========================================");
        return reasonNotSatisfied;
    }


    public String handleReason(Set  booleanInterfaceSet,String trueDescriptions) {
        boolean available = false;
        Set<BooleanInterface> multipleDescriptionsSet =  new HashSet<BooleanInterface>();
        for (Iterator booleanIterator = booleanInterfaceSet.iterator(); booleanIterator.hasNext();) {
            BooleanInterface oneBoolean = (BooleanInterface) booleanIterator.next();
            if (oneBoolean.isSatisfied()) {
                if (trueFound) {
                    multipleTrueFound = true;
                }
                trueFound = true;
            } else {
                if (oneBoolean.inputAvailable() && available) {
                    trueFound = true; // track two or more incomplete sets
                }
                available = available || oneBoolean.inputAvailable(); //track incomplete set.
            }
            multipleDescriptionsSet.add(oneBoolean);
        }
        if ((trueFound && available) || (multipleTrueFound)) {
            multipleTrueFound = true;
            for (Iterator<BooleanInterface> describeIterator =multipleDescriptionsSet.iterator();describeIterator.hasNext();) {
                trueDescriptions = handleDescription(describeIterator.next(),trueDescriptions);
                if (describeIterator.hasNext()) {
                    trueDescriptions += "  (or)"; 
                }
            }
        } else {
            trueFound = false;
        }

        return trueDescriptions.trim();
    }

    private String  handleDescription(BooleanInterface oneBoolean, String trueDescriptions) {
        if (oneBoolean instanceof BooleanOperation) {
            for (Iterator booleanIterator = ((BooleanOperation)oneBoolean).getOperandSet().iterator(); booleanIterator.hasNext();) {
                BooleanInterface aBoolean = (BooleanInterface) booleanIterator.next();
                trueDescriptions = handleDescription(aBoolean,trueDescriptions);
                if (booleanIterator.hasNext()) {
                    trueDescriptions += "\n("+((BooleanOperation)oneBoolean).getOperationName().toLowerCase() + ")"; 
                }
            }
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument) {
            com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument msa =
            (com.ibm.sdwb.build390.userinterface.text.commandline.arguments.MultipleAssociatedCommandLineArgument)oneBoolean;
            trueDescriptions += "\n"+ msa.getReasonNotSatisfied();
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation) {
            com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation msg =
            (com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation)oneBoolean;
            trueDescriptions = handleDescription(((com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociativeBooleanOperation)oneBoolean).getBooleanOperation(),trueDescriptions);
        } else if (oneBoolean instanceof com.ibm.sdwb.build390.userinterface.text.commandline.arguments.AssociatedArgument) {
            trueDescriptions += String.format("%n=>%-42s",oneBoolean.getNameOfBoolean());
        } else {
            trueDescriptions += String.format("%n=>%-42s",indentedString(oneBoolean.getNameOfBoolean(),oneBoolean.getDescriptionOfBoolean()));
        }
        return trueDescriptions;
    }

    private String  indentedString(String tempCommand, String tempExplanation) {
        StringBuilder strbd = new StringBuilder();
        Formatter formatter = new Formatter(strbd);
        StringTokenizer strk = new StringTokenizer(tempExplanation,"\n");
        formatter.format("%-20s%-22s%n",tempCommand,strk.nextToken());
        while (strk.hasMoreTokens()) {
            String str = strk.nextToken();
            formatter.format("%-22s%-22s","", str);
            formatter.format("%n");
        }
        return strbd.toString();
    }

    private String appendORword(int errorIndex, String reason) {
        if ((errorIndex  > 0 && (errorIndex%2)==0)) {
            reason += "\n  (or)";
        }
        return reason;
    }

}
