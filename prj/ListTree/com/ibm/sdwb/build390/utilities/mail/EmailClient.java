package com.ibm.sdwb.build390.utilities.mail;
/*********************************************************************/
/* EmailClient                   class for the Build/390 client      */
/*  SMTP client.                                                     */
/*  when the javax.mail packages gets absorbed into j2sdk ,          */
/*  we should start using that one.                                  */
/*********************************************************************/
//02/11/2005 SDWB2395  Display filter result in multiple tabs.
/*********************************************************************/
import java.util.*;

public class EmailClient {


    public static void sendEmail(EmailInfo emailInfo){
        String message = emailInfo.getSubject() + emailInfo.getContent();
        com.ibm.sdwb.build390.MBUtilities.SendMail(message , emailInfo.getMailToVector(), emailInfo.getMailFromUserName(), emailInfo.getMailFromHost()); 

    }


}
