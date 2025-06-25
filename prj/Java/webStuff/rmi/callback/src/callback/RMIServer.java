
package callback;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.rmi.*;
import java.rmi.server.* ;
import java.rmi.registry.*;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface
{



	public RMIServer() throws RemoteException {
		super();
		if(System.getSecurityManager()==null)
		{
			System.setSecurityManager(new RMISecurityManager());
		}
		System.out.println("callbackserver"+" service started.");
	}


	public void execute(String tempString,Notifier notifier) throws RemoteException {
		System.out.println("** Executing At the Server End \n " + tempString );
		notifier.notify("Howdy Client , This is a callback sent from the SERVER ... ");
	}


	public static void main(String args[])
	{

		String port = "1099";

		try
		{
			int portNum = Integer.parseInt(port);

			Registry registry = LocateRegistry.getRegistry(portNum);

			// start rmiregistry and bind to the given port
			try
			{
				registry = LocateRegistry.createRegistry(portNum);
			}
			catch(ExportException ex)
			{
				registry = LocateRegistry.getRegistry(portNum);
			}

			RMIServer thisServer = new RMIServer();
			registry.rebind("callbackserver", thisServer);
		}
		catch(Exception e)
		{
			System.out.println("An error occurred created and registering the server ");
			e.printStackTrace();

		}
	}
}


