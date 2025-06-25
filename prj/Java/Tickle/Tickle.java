import java.net.*;
import java.io.*;
import java.util.*;

public class Tickle
{
    public Tickle(String addressStr, String portStr, String valueStr)
    {
        try
        {
            StringTokenizer typeTokenizer = new  StringTokenizer(valueStr, "=/");
            StringTokenizer byteTokenizer = null;
            String typeTok = null;
            String byteTok = null;
            Vector tempVect = new Vector();

            while(typeTokenizer.hasMoreTokens())
            {
                typeTok = typeTokenizer.nextToken();

                byteTokenizer = new  StringTokenizer(typeTokenizer.nextToken(), ",");

                if(typeTok.startsWith("O"))
                {
                    while(byteTokenizer.hasMoreTokens())
                    {
                        byteTok = byteTokenizer.nextToken();

                        tempVect.add(Byte.valueOf(byteTok,8));
                    }
                }
                else
                    if(typeTok.startsWith("X"))
                {
                    while(byteTokenizer.hasMoreTokens())
                    {
                        byteTok = byteTokenizer.nextToken();

                        tempVect.add(Byte.valueOf(byteTok,16));
                    }
                }
                else
                    if(typeTok.startsWith("D"))
                {
                    while(byteTokenizer.hasMoreTokens())
                    {
                        byteTok = byteTokenizer.nextToken();

                        tempVect.add(Byte.valueOf(byteTok,10));
                    }
                }
                else
                    if(typeTok.startsWith("A"))
                {
                    while(byteTokenizer.hasMoreTokens())
                    {
                        byteTok = byteTokenizer.nextToken();

                        byte byteAry[] = byteTok.getBytes();

                        tempVect.add(new Byte(byteAry[0]));
                    }
                }
            }

            Byte byteAray[] = new Byte[tempVect.size()];

            byteAray = (Byte[]) tempVect.toArray(byteAray);


            int port = Integer.valueOf(portStr).intValue();

            System.out.println("Address="+addressStr+"@"+port);

            Socket sock = new Socket(addressStr, port);

            OutputStream out = sock.getOutputStream();


            System.out.print("Sending=>");

            for(int i=0;i!=byteAray.length;i++)
            {
                out.write(byteAray[i].byteValue());

                System.out.print(byteAray[i].byteValue()+",");
            }  
            System.out.println();

            out.close();

            System.out.println("***Done***");

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }

    public static void main(String args[]) 
    {
        if(args.length == 3)
        {
            new Tickle(args[0],args[1],args[2]);
        }
        else
        {
            System.out.println("'TICKLE'=>sends a user specified series of bytes to a network address@port");

            System.out.println("Usage => Tickle <address> <port> <value_string>");

            System.out.println("The value_string parameter must be enclosed in ");
            System.out.println("double quotes and entered in the following format");

            System.out.println("/X=n,n,... for hex,");
            System.out.println("/A=c,c,... for ascii,");
            System.out.println("/D=n,n,... for decimal");
            System.out.println("/O=n,n,... for octal");

            System.out.println("Number values must be restricted to a byte range.");
            System.out.println("No whitespace characters are permitted in the String.");

            System.out.println("Example => Tickle snjeds3.sanjose.ibm.com 999 \"/A=H,E,L,P,/D=13,10,\"");

            System.out.println("Sends the value =>HELP<CR><LF> to the host snjeds3 at port 999");
        }
    }
}
