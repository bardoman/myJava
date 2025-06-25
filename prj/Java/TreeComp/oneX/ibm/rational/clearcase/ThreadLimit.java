package com.ibm.rational.clearcase;


public class ThreadLimit
{
    int current = 0;
    int limit = 0;

    ThreadLimit (int tempLimit) {
        limit = tempLimit;
    }

    public synchronized void waitCounter() {
        while(current >= limit)
        {
            try
            {
                wait();
            }
            catch(InterruptedException ie)
            {

                System.out.print("An interruption occurred while waiting on the thread counter");
            }
        }
        current++;
    }

    public synchronized void notifyCounter() {
        current--;
        notifyAll();
    }
}
