import java.net.*;
import java.lang.*;

public class SocketTestConnect
{
    public static void main(String args[]) {
        try
        {
            if(args.length <2)
            {
                System.out.println("SocketTestConnect <address> <port>");
                System.exit(1);
            }

            Socket sock = new Socket(args[0], Integer.valueOf(args[1]).intValue());

            System.out.println("testing socket connect for address="+args[0]+", port="+ args[1]);

            if (sock.isConnected())
            {

                System.out.println("Socket connected");

            }
            else
            {

                System.out.println("Socket not connected");
            }
            sock.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
