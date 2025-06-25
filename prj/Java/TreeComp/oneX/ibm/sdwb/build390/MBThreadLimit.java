package com.ibm.sdwb.build390;
/************************************************************************/
// 04/27/99 errorHandling       change LogException parms & add new error types
// 03/07/2000 reworklog         changes to implement the log stuff using listeners
/************************************************************************/

/**
*/
import com.ibm.sdwb.build390.logprocess.*;

public class MBThreadLimit {
    int current = 0;
    int limit = 0;
    private LogEventProcessor lep=null;

    public MBThreadLimit (int tempLimit,LogEventProcessor lep) {
        limit = tempLimit;
        this.lep=lep;
    }

    public synchronized void waitCounter() {
        while (current >= limit) {
            try {
                wait();
            } catch (InterruptedException ie) {
                lep.LogException("An interruption occurred while waiting on the thread counter", ie);
            }
        }
        current++;
    }

    public synchronized void notifyCounter() {
        current--;
        notifyAll();
    }
}
