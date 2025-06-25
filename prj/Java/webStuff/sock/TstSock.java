import java.net.*;
import java.io.*;

public class TstSock
{

  public TstSock(String port)
  {
  try
  {
    ServerSocket srv=new ServerSocket(Integer.parseInt(port));
    Socket incoming = srv.accept();
    BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
    PrintWriter out = new PrintWriter (incoming.getOutputStream(),true);
    out.println("hello! Enter BYE to exit.");
    boolean done=false;
    while(!done)
    {
      String line =in.readLine();
      if(line==null) done=true;
      else
      {
        out.println("Echo:"+line);
        if(line.trim().equals("BYE"))done=true;
      }
    }
    incoming.close();
   }
   catch(Exception e)
   {
     System.out.println(e);
   }

  }
  public static void main(String[] args)
  {
	if(args.length >= 1)
	{
	
    TstSock tstSock1 = new TstSock(args[0]);
	}
	else System.out.println("No port specified. Usage=> TstSock <port>");
  }
}