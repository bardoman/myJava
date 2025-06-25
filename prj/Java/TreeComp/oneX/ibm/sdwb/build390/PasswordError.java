package com.ibm.sdwb.build390;
/*********************************************************************/
/* Exception  thrown when invalid password is entered                */
/*                         no password is entered                    */
/*                         general password error                    */
/*********************************************************************/
// Changes
// Date       Defect                 Reason
// 04/10/07                          Birth of Class  
/*********************************************************************/

public class PasswordError extends MBBuildException {

    public PasswordError(String emsg){
        super("Password Error",emsg,MBConstants.INVALIDPASSWORDERROR);
    }

    public PasswordError(String emsg, int rc){
        super("Password Error",emsg,rc);
    }

        
}
