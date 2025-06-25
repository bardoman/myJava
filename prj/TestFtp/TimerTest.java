import org.apache.commons.net.ftp.*;
import java.io.*;
import java.util.*;


public class TimerTest 
{
    boolean timeUp=false;
    long timeout = 5000;
    Timer timer = new Timer();

    public TimerTest()
    {
        timer.schedule(new myTimerTask(), timeout);

        while(!timeUp);

        System.out.println("****");

        timer.cancel();

    }


    public class myTimerTask extends TimerTask
    {
        public myTimerTask()
        {
            timeUp=false;
        }
        public void run()
        {
            timeUp=true;
        }
    }

    public static void main(String args[]) 
    {  
        new TimerTest();
    }
}
