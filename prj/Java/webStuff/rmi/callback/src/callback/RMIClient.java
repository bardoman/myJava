package callback;   

import java.io.*; 
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.*;

public class  RMIClient
implements Notifier
{
	public RMIClient() 
	throws Exception 
	{
		RMIServerInterface remoteInterface = create();

		Class remoteClass=remoteInterface.getClass();

		Method methods[]=remoteClass.getMethods();

		for(int i=0;i!=methods.length;i++)
		{
			System.out.println("method name["+i+"]="+methods[i].getName());
		}

		System.out.println("*******Start processing ..please wait****");

		remoteInterface.execute("Howdy server ??? How are you ???",this);  

		System.out.println("********End processing...****************");
	}

	private RMIServerInterface create()
	throws Exception 
	{
		RMIServerInterface serverToUse=null;

		UnicastRemoteObject.exportObject(this);

		if(serverToUse ==null)
		{
			String processRMIServerURL = "rmi://hodori.boulder.ibm.com:1099/callbackserver";
			try
			{
				serverToUse = (RMIServerInterface) Naming.lookup(processRMIServerURL);
			}
			catch(java.net.MalformedURLException e)
			{
				e.printStackTrace();
				System.err.println("A bad URL error occurred while attempting to connect to the process rmi server at URL " + processRMIServerURL);
			}
			catch(NotBoundException nbe)
			{
				nbe.printStackTrace();
				System.err.println("An error occurred attempting to connect to the process rmi server because the service wasn't found");
			}
			catch(java.rmi.UnknownHostException uhe)
			{

				System.err.println("An error occurred attempting to connect to the process rmi server. The host localhost was not found.");
			}
			catch(RemoteException re)
			{
				((Exception) re.detail).printStackTrace();
				System.err.println("An error occurred while attempting to connect to the process rmi server. Check with your administrator.\n"+re.detail.getMessage());
			}
		}
		return serverToUse;
	}

	public void notify(String notifyString)
	throws RemoteException
	{
		System.out.println(notifyString);
	}

	public static void main(String args[])
	{
		try
		{
			RMIClient rmiClient = new RMIClient();
		}
		catch(Exception rme)
		{
			System.err.println("Something crapped out on the remote server end.");
			rme.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}

