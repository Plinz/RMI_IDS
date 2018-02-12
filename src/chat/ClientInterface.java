package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote{
	void postMessage(Message message)  throws RemoteException;
	public String getName() throws RemoteException;
	void userJoin(String name) throws RemoteException;
	void userLeave(String name) throws RemoteException;
}
