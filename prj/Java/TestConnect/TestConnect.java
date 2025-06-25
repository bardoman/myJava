import java.net.*;
import java.util.*;

public class TestConnect
extends TimerTask
{
    String addr=null;
    int port=0;
    Socket sock;
    InetAddress inetAdr;
    Timer timer = new Timer();
    long timeout;

    TestConnect(String args[])
    {
        addr = args[0];

        port = new Integer(args[1]).intValue();

        timeout = Long.parseLong(args[2]);

        System.out.println("*****************************************");

        try
        {
            timer.schedule(this, timeout*1000 );

            sock = new Socket(addr,port);

            inetAdr = sock.getInetAddress();

            timer.cancel();

            System.out.println("Success");
            System.out.println("addr    ="+addr);
            System.out.println("port    ="+port);
            System.out.println("inetAdr ="+inetAdr);
        }

        catch(Exception e)
        {
            System.out.println("***Failed connection to "+addr+"@"+port+"***");

            System.out.println("Exeception");
            e.printStackTrace();

        }

        System.out.println("*****************************************");

    }

    public void run()
    {
        System.out.println("***Failed connection to "+addr+"@"+port+"***");
        System.out.println("*****************************************");
        System.exit(1);
    }


    public static void main(String args[])
    {  
        if(args.length != 3)
        {
            System.out.println("Socket connection tester. Usage=> TestConnect <addr> <port> <timeout_secs>");
            return;
        }

        new TestConnect(args);
    }

}
