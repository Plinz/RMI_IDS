package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote{
	void postMessage(Message message)  throws RemoteException;
	public String getName() throws RemoteException;
	void userJoin(String name, String room) throws RemoteException;
	void userLeave(String name, String room) throws RemoteException;
	void roomCreated(String name) throws RemoteException;
	void roomDestroyed(String name) throws RemoteException;
	void disconnect() throws RemoteException;
}
