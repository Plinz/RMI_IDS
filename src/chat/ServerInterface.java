package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	void join(ClientInterface client)  throws RemoteException;
	void leave(ClientInterface client) throws RemoteException;
	void sendMessage(ClientInterface client,String message) throws RemoteException;
	void getHistory(ClientInterface client) throws RemoteException;
}
