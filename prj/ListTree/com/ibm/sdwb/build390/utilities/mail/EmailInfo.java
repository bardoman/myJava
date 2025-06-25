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

public class EmailInfo {
    private StringBuffer subjectBuffer = null;
    private StringBuffer  contentBuffer  = null;
    private String mailFromUser;
    private String mailFromHost;
    private Vector mailToVector;

    public EmailInfo(){
        this.subjectBuffer = new StringBuffer("Subject: ");
        this.contentBuffer = new StringBuffer();
    }


    public void setSubject(String subject){
        subjectBuffer.append(subject);
        subjectBuffer.append("\n\n");
    }

    public void addContent(String tempContent){
        contentBuffer.append(tempContent);
        contentBuffer.append("\n");
    }

    public void clearContent(){
        if (contentBuffer !=null) {
            contentBuffer.delete(0,contentBuffer.length());
        }
    }

    public void setMailFromUserName(String mailFromUser){
        this.mailFromUser = mailFromUser;
    }

    public void setMailFromHost(String mailFromUser){
        this.mailFromHost = mailFromUser;
    }

    public void setMailToVector(Vector mailToVector){
        this.mailToVector = mailToVector;
    }


    public String getMailFromHost(){
        return mailFromHost;
    }

    public Vector getMailToVector(){
        return mailToVector;
    }


    public String getContent(){
        return contentBuffer.toString();
    }


    public String getSubject(){
        return subjectBuffer.toString();
    }

    public String getMailFromUserName(){
        return mailFromUser;
    }


    public String toString(){
        StringBuffer toStringBuffer = new StringBuffer();
        toStringBuffer.append(getSubject());
        toStringBuffer.append(getContent());
        toStringBuffer.append(getMailToVector());
        toStringBuffer.append(getMailFromUserName());
        toStringBuffer.append(getMailFromHost());
        return toStringBuffer.toString();

    }

}
