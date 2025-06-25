
package callback;

import java.rmi.*;
import java.util.*;

public interface Notifier extends Remote
{

	public void notify(String printStr) throws RemoteException;
}
