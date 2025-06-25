import java.net.Socket;
public class test
{
    public static void main(String args[]) {
        try
        {
            Socket sock = new Socket("buddy.storage.tucson.ibm.com", 60012);

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
