package com.ibm.sdwb.build390.userinterface.text.commandline;

import java.util.*;
import com.ibm.sdwb.build390.utilities.BooleanInterface;
import com.ibm.sdwb.build390.utilities.BooleanOperation;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.NoArguments;
import com.ibm.sdwb.build390.userinterface.text.commandline.arguments.CommandLineArgument;

public class RequiredAndOptionalArguments implements BooleanInterface {
    private BooleanInterface requiredArguments = null;
    private ArrayList options = null;

    public RequiredAndOptionalArguments() {
        options = new ArrayList();
    }

    public BooleanInterface getRequiredPart() {
        return requiredArguments;
    }

    public List getOptions() {
        return new ArrayList(options);
    }

    public void setRequiredPart(BooleanInterface tempRequired) {
        requiredArguments = tempRequired;
    }

    public void addOption(BooleanInterface oneOption) {
        options.add(oneOption);
    }

    public boolean isSatisfied() {
        boolean satisfied = true;
        if (requiredArguments!=null) {
            if (!(requiredArguments instanceof NoArguments)) {
                satisfied = requiredArguments.isSatisfied();
            }
            //this is to ensure we check optional parameters when there are no req.args.
            //eg: LISTPROCESSES command
            for (Iterator iter = options.iterator();iter.hasNext();) {
                BooleanInterface Ibool = (BooleanInterface)iter.next();
                if (Ibool.inputAvailable()) {
                    satisfied = satisfied && Ibool.isSatisfied();
                    if (!satisfied) {
                        break;

                    }
                }
            }

        }
        return satisfied;

    }


    public String getReasonNotSatisfied() {
        StringBuffer reasonNotSatisfiedBuffer = new StringBuffer();
        if (requiredArguments.getReasonNotSatisfied()!=null) {
            reasonNotSatisfiedBuffer.append(requiredArguments.getReasonNotSatisfied()+"\n");
        }
        for (Iterator iter = options.iterator();iter.hasNext();) {
            Object obj = iter.next();
            BooleanInterface Ibool = (BooleanInterface)obj;
            if (Ibool.inputAvailable()) {
                if (!Ibool.isSatisfied()) {
                    reasonNotSatisfiedBuffer.append(Ibool.getReasonNotSatisfied() +"\n");
                }
            }
        }
        return reasonNotSatisfiedBuffer.toString();
    }



    public String getNameOfBoolean() {
        return "Required and Optional Arguments";
    }

    public String getDescriptionOfBoolean() {
        return toString();
    }
    public boolean inputAvailable() {
        return true;
    }

    public String toString() {
        String returnString = new String();
        if (requiredArguments!=null) {
            returnString+= "Required:\n";
            returnString+=requiredArguments.toString();
        }
        returnString += "Options:\n"+options.toString();
        return returnString;
    }

}
