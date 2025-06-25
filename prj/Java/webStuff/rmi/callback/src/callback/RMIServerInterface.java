
package callback;

import java.rmi.*;
import java.util.*;

public interface RMIServerInterface extends Remote
{

	public void execute(String printStr,Notifier notifier) throws RemoteException;
}
