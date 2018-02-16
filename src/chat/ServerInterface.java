package chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	boolean join(ClientInterface client)  throws RemoteException;
	void leave(ClientInterface client) throws RemoteException;
	boolean createRoom(ClientInterface client, String name) throws RemoteException;
	boolean destroyRoom(ClientInterface client, String name) throws RemoteException;
	boolean joinRoom(ClientInterface client, String room) throws RemoteException;
	void leaveRoom(ClientInterface client) throws RemoteException;
	void sendMessage(ClientInterface client, Message message) throws RemoteException;
	void getHistory(ClientInterface client) throws RemoteException;
}
