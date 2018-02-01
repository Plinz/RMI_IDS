package chat;

import java.rmi.RemoteException;

public class Client implements ClientInterface{
	
	private String name;
	
	public Client(String n){
		name = n;
	}

	public String getName() throws RemoteException {
		return name;
	}
	
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	@Override
	public void postMessage(String name, String msg) throws RemoteException {
		System.out.println(name+">"+msg);
	}
}
