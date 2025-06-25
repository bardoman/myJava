import java.net.*;

public class PortTestServer
{
    public static void main(String args[])
    {

        if(args.length == 0)
        {
            System.out.println("Usage => PortTestServer  <port>");

            System.exit(1);
        }

        try
        {
            while(true)
            {
                ServerSocket srv = new ServerSocket(Integer.valueOf(args[0]).intValue());

                System.out.println("Listening");

                srv.accept();

                System.out.println("***Connected***");

                srv.close();
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }
}
