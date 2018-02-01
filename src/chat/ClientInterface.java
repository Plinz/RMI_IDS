package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote{
	void postMessage(String name,String msg)  throws RemoteException;
	public String getName() throws RemoteException;
}
