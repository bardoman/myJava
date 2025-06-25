package com.ibm.sdwb.build390;

import java.util.Vector;

class MBSchedulerInfo {
    public String groupName=new String();
    public String eventName=new String();
    public String interval=new String();
    public String start=new String();
    public String stop=new String();
    public boolean[] daysToRun = new boolean[7];
    public String conditionCode=new String();
    public boolean doConditionCheck=false;
    public boolean active = true;
    public String stepName = new String();
    public String dataSetName = new String();
    public String dateAdded = new String();
    public String dateChanged = new String();
    public Vector contactList = new Vector();

    public String toString() {
        String stringForm = "{"+groupName+","+eventName+","+interval+","+
                            start+","+stop+","+daysToRun[0]+","+daysToRun[1]+","+daysToRun[2]+","+daysToRun[3]+","+daysToRun[4]+","+daysToRun[5]+","+daysToRun[6]+
                            conditionCode+","+active+","+stepName+","+dataSetName+","+contactList+"}";
        return stringForm;
    }
}
