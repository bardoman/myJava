import java.io.*;
import java.net.*;

public class UrlTest
{
    public static void main(String args[]) {

        try
        {
            URL url = new URL("http://www.prasadyoga.net/Prasad.php");

            LineNumberReader rd = new LineNumberReader(new InputStreamReader(url.openStream()));

            while(rd.ready())
            {
                System.out.println(rd.readLine());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



    }
}
