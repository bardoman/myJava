package com.ibm.sdwb.build390.mainframe;

public class MainframeOutputTraceOptions {

    private boolean holdSubServerJobOutput = false;
    private boolean traceVerbOnly    = false;
    private boolean traceVerbAndData = false;

    private static MainframeOutputTraceOptions traceInstance = null;

    private MainframeOutputTraceOptions(){

    }

    public static MainframeOutputTraceOptions getInstance(){
        if(traceInstance == null){
            traceInstance = new MainframeOutputTraceOptions();
        }
        return traceInstance;
    }


    public boolean isHoldSubServerJobOutput(){
        return holdSubServerJobOutput;
    }

    public boolean isTraceVerbOnly(){
        return traceVerbOnly;
    }

    public boolean isTraceVerbAndData(){
        return traceVerbAndData;
    }


    public void setHoldSubServerJobOutput(boolean holdSubServerJobOutput){
        this.holdSubServerJobOutput = holdSubServerJobOutput;
    }

    public void setTraceVerbOnly(boolean traceVerbOnly){
        this.traceVerbOnly = traceVerbOnly;
    }

    public void setTraceVerbAndData(boolean traceVerbAndData){
        this.traceVerbAndData = traceVerbAndData;
    }

}
