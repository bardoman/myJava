package com.ibm.sdwb.build390.logprocess;

import java.text.DateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
/*****************************************************************************************************/
/* Java LogEvent for Build/390 client                                                                */
/* This is the LogEvent class for the build/390 LogProcessing                                        */
/****************************************************************************************************/
/* The LogEvent class sends the LogEvents to the LogListeners
/* has  get methods to get the Events
/* getEventLevel = returns the EventLeveL
/* getEventInfo  = return the EventInfo(basically the getUserMessagefromDecrypter)
/* getEventTitle = returns the EventTitle(basically getMessageFromDecrypter or the title passed)
/* getExtraInfo  = return the extra info stuff - is an object
/* toLoggableString = contains the String that is logged into the Log File
/****************************************************************************************************/
/* Changes
/* Date     Defect/Feature      Reason
/* 03/07/2000                   Birth of the class
/* 05/02/2000  eventDisplay     Additon of EventDisplay to control just traceit or traceit + display
/****************************************************************************************************/



class LogEvent {

    private String  eventType = new String();
    private String  eventInfo = new String();
    private Object  extraInfo = null;
    private int     eventLevel=-1;
    private boolean eventDisplay=true;
    //final vars for the event levels
    static final int LOGEXCEPTION_EVT_LEVEL =1;
    static final int LOGPRMYINFO_EVT_LEVEL  =2;
    static final int LOGSECYINFO_EVT_LEVEL  =3;

    //(0,1,2 logevent level ==  0 - logexception, 1 - logprmyinfo - always , 2 = logsecyinfo - uses debug_level
    // eventType = would be the title - Error,Warning etc
    // eventInfo = Has the error info
    // eventLevel = 1,2,3 
    LogEvent(String eventType,String eventInfo,int eventLevel,boolean eventDisplay){
        this.eventType    =eventType;
        this.eventInfo    =eventInfo;
        this.eventLevel   =eventLevel;
        this.eventDisplay =eventDisplay;
    }

    
    
    //(0,1,2 logevent level ==  0 - logexception, 1 - logprmyinfo - always , 2 = logsecyinfo - uses debug_level
    // eventType = would be the title - Error,Warning etc
    // eventInfo = Has the error info
    // eventLevel = 1,2,3 
    // 
    LogEvent(String eventType,String eventInfo,int eventLevel){
        this.eventType    =eventType;
        this.eventInfo    =eventInfo;
        this.eventLevel   =eventLevel;
    }
    // eventType  = would be the title - Error,Warning etc
    // eventInfo  = would be the exception parsed by the exceptionDecryptor = getUserException();
    // eventLevel = 1,2,3 
    // extraInfo  = to pass in any object like for db stuff
    LogEvent(String eventType,String eventInfo,Object extraInfo,int eventLevel){
        this.eventType    = eventType;
        this.eventInfo    = eventInfo;
        this.extraInfo    = extraInfo;
        this.eventLevel   = eventLevel;
    }                                  

    String getEventType(){
        return eventType;
    }
    String getEventInfo(){
        return eventInfo;
    }
    Object getExtraInformation(){
        return extraInfo;
    }
    int getEventLevel(){
        return eventLevel;
    }
    boolean getEventDisplay(){
        return eventDisplay;
    }


    String toLoggableString(){
        return(DateFormat.getDateTimeInstance().format(new Date())+" : "+getEventType() + " : " +  getEventInfo());
    }

}

