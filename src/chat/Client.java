package chat;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import gui.RoomExpendable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client implements ClientInterface{
	
	public String name;
	public String room;
	private ObservableList<String> usersList;
	private ObservableList<RoomExpendable> roomList;
	private transient Observable observable = new Observable();
	private transient Set<Observer> observers = new HashSet<>();
	
	public Client(String n){
		name = n;
		room = "";
		usersList = FXCollections.observableArrayList();
		roomList = FXCollections.observableArrayList();
	}

	public String getName() throws RemoteException {
		return name;
	}
	
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	public synchronized void addObserverPostMessage(Observer observer){
		observers.add(observer);
	}
	
	public synchronized void removeObserverpostMessage(Observer observer){
		observers.remove(observer);
	}
	
	public ObservableList<String> getUsersList(){
		return usersList;
	}

	public ObservableList<RoomExpendable> getRoomList() {
		return roomList;
	}
	
	@Override
	public void postMessage(Message message) throws RemoteException {
		observers.stream().forEach(o -> o.update(observable, message));
	}

	@Override
	public void userJoin(String name, String room) throws RemoteException {
		usersList.add(name);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				roomList.stream().filter(r -> r.getRoomName().equals(room)).findFirst().get().getUsersInRoom().add(name);
			}
		});
		
	}
	
	@Override
	public void userLeave(String name, String room) throws RemoteException {
		usersList.remove(name);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				roomList.stream().filter(r -> r.getRoomName().equals(room)).findFirst().get().getUsersInRoom().remove(name);
			}
		});
	}
	
	@Override
	public void roomCreated(String name) throws RemoteException {
		roomList.add(new RoomExpendable(name, true));
	}

	@Override
	public void roomDestroyed(String name) throws RemoteException {
		roomList.remove(roomList.stream().filter(r -> r.getRoomName().equals(name)).findFirst().get());
	}

	@Override
	public void disconnect() throws RemoteException {
				
	}

	
}
