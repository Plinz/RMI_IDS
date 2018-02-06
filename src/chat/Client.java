package chat;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class Client implements ClientInterface{
	
	private String name;
	private transient Observable observable = new Observable();
	private transient Set<Observer> observers = new HashSet<>();
	
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
		observers.stream().forEach(o -> o.update(observable, new Tuple<String, String>(name, msg)));
	}

	public synchronized void addObserver(Observer observer){
		observers.add(observer);
	}
	public synchronized void removeObserver(Observer observer){
		observers.remove(observer);
	}
}
