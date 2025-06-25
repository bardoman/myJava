package com.ibm.sdwb.build390.logprocess;               

/*****************************************************************************************************/
/* Java LogExceptinEvent for Build/390 client                                                                */
/* This is the LogExceptionEvent class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogExceptionEvent extends LogEvent class sends the LogExceptionEvents to the LogListeners
/* has  get methods to get the Stacktrace
/* invokes super(..) to send in the parameters to its super class.
/* getStackTrace = returns the StackTrace string decrypted from the ExceptionMessageDecrypter
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                   Birth of the class
/****************************************************************************************************/


class LogExceptionEvent extends LogEvent {

    String stackstr = new String();


    // stackstr = get the stack infor stored in exception decrypter
    LogExceptionEvent(String eventType, String eventInfo,Object extraInfo, String stackstr){
        super(eventType, eventInfo, extraInfo, LogEvent.LOGEXCEPTION_EVT_LEVEL);
        this.stackstr=stackstr;

    }

    String getStackTrace(){
        return stackstr;
    }

}


