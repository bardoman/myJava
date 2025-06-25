package com.ibm.sdwb.build390;


/*********************************************************************/
/* Java Syntax error for the Build390 java client                    */
/*********************************************************************/
// Changes
// Date     Defect/Feature      Reason
// 03/10/2000                Changes for the Log stuff ,make the class public since accessed in pkg LogProcess
/*************************************************************************************/


public class SyntaxError extends MBBuildException {

    public SyntaxError(String errorMessage) {
        super("Syntax Error", errorMessage, MBConstants.SYNTAXERROR);
    }
}


