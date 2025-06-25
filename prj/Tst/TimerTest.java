
import java.util.*;

/**
 * Simple demo that uses java.util.Timer to schedule a task 
 * to execute once 5 seconds have passed.
 */

public class TimerTest
{
    static int MAX_TIME = 15000;

    Timer timer;
    Vector vect = new Vector();

    public TimerTest()
    {
        Random rand = new Random(new Date().getTime());


        timer = new Timer();

        long delay = (long) MAX_TIME;

        timer.schedule(new Task((int)delay, delay), delay);

        for (int i=0;i!=5;i++)
        {
            timer = new Timer();

            delay = rand.nextInt(9) * 1000;

            timer.schedule(new Task(i, delay), delay);

            vect.add(timer);
        }

    }

    class Task extends TimerTask
    {
        long delay;  
        int index;

        Task(int index, long delay)
        {
            this.delay = delay;
            this.index = index;

            System.out.println("Task="+index+", Delay="+delay);

        }

        public void run()
        {
            System.out.println("Time's up! Task="+index+", Delay="+delay);

            if (index == MAX_TIME)
            {
                System.exit(0);
            }

            if (delay == 0)
            {
                return;
            }
            else
            {
                timer.cancel(); 
            }
        }
    }

    public static void main(String args[])
    {

        new TimerTest();

    }
}
