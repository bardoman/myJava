package com.ibm.sdwb.build390.logprocess;

import com.ibm.sdwb.build390.*;
import java.io.*;

public class ExceptionMessageDecrypter {
/****************************************************************************************************/
/* Java Decrypter for Build/390 client                                                              */
/* This is the Exception Decrypter class for the build/390 LogProcessing                            */
/****************************************************************************************************/
/* The Exception Decrypter  class. Would be instantiated in the method that needs the Exception or Throwable 
/* to be decrypted in User/Message
/* methods returned getUserMessageFromDecrypter() - returns String,- from method getUserMessage() - from MBBuildException 
/*                                                         or null - if Throwable is passed                        
/*                  getMessageFromDecrypter()     - returns String,-from method getMessage() of MBBuildexception or Throwable
/*                  getStackTraceFromDecrypter()  - return String, -from method e.printStackTrace() 
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                   Birth of the class
/****************************************************************************************************/

    private Throwable e;
    private MBBuildException mbe;
    private int i=0;
    private boolean isThrowable = false;
    private String UserMessageStr= null;
    private String MessageStr= null;
    private String exceptionDump= null;

    public ExceptionMessageDecrypter(Throwable e){
        this.e=e;
        isThrowable = true;
        setMessageFromDecrypter();
        UserMessageStr = getUserMessageFromDecrypter(e);
        exceptionDump = getStackTraceFromDecrypter(e);

    }

    public ExceptionMessageDecrypter(MBBuildException mbe){
        this.mbe=mbe;
        isThrowable = false;
        setMessageFromDecrypter();
        UserMessageStr = getUserMessageFromDecrypter(mbe);
        exceptionDump = getStackTraceFromDecrypter(mbe);
    }

    private void setMessageFromDecrypter(){
        MessageStr=null;
        if (isThrowable) {
            MessageStr =e.getMessage();
        } else {
            MessageStr = mbe.getMessage();
            if (MessageStr.equals(null)) {
                MessageStr= "Error !";
            }

        }
        if (MessageStr==null) {
            MessageStr = new String();
        }
    }
    //gets shown as title on the MBMsgBox display.
    public String getMessageFromDecrypter(){
        return(MessageStr.indexOf(":") > 0 ? MessageStr.substring(0,MessageStr.indexOf(":")) : MessageStr);
    }

    private String getUserMessageFromDecrypter(Throwable t){
        String userMessage=new String();
        if (t instanceof MBBuildException) {
            MBBuildException mbe = (MBBuildException) t;
            userMessage=getNonNullVersion(mbe.getUserMessage());
            if (mbe.getOriginalException()!=null) {
                String subUserMessage = getUserMessageFromDecrypter(mbe.getOriginalException());
                if (subUserMessage.length() > 0) {
                    userMessage +="\n"+subUserMessage;
                }
            }
        }
        if (userMessage ==null) {
            userMessage = new String();
        }
        return userMessage;
    }

    //this is displayed on the Text Area of the MBMsgBox display.
    //user message got from MBBuildException class


    public String getUserMessageFromDecrypter(){
        return UserMessageStr;
    }

    private String getStackTraceFromDecrypter(Throwable t){
        StringWriter stackTraceWriter = new StringWriter();
        digOutOriginalThrowable(t).printStackTrace(new PrintWriter(stackTraceWriter));
        return stackTraceWriter.toString();
    }

    public static Throwable digOutOriginalThrowable(Throwable t){
        if (t==null) {
            System.out.println("Exception is NULL.");
        }
        if (t instanceof MBBuildException) {
            MBBuildException mbe = (MBBuildException) t;
            if (mbe.getOriginalException()!=null) {
                return digOutOriginalThrowable(mbe.getOriginalException());
            }
        } else if (t instanceof java.rmi.RemoteException) {
            java.rmi.RemoteException re = (java.rmi.RemoteException) t;
            if (re.detail!=null) {
                return digOutOriginalThrowable(re.detail);
            }
        }
        return t;
    }
    //stacktrace for logging in Build.log file or ind.build.log

    public String getStackTraceFromDecrypter(){
        return exceptionDump;

    }

    private String getNonNullVersion(String temp){
        if (temp!=null) {
            return temp;
        }
        return new String();
    }

}              
