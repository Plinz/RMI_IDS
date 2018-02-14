package chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Room {

	private List<ClientInterface> clientList;
	private ClientInterface owner;
	private List<Message> history;
	private File historyFile;
	private FileOutputStream fo;
	private ObjectOutputStream oo;

	private String name;

	public Room(String name, ClientInterface owner) {
		this.name = name;
		this.owner = owner;

		clientList = new ArrayList<ClientInterface>();
		history = new ArrayList<Message>();

		FileInputStream fi;
		try {
			historyFile = new File("history/historyFile_"+name);
			fo = new FileOutputStream(historyFile, true);
			if (historyFile.length() != 0) {
				fi = new FileInputStream(historyFile);
				ObjectInputStream oi = new ObjectInputStream(fi);
				Message hist;
				while(fi.available() > 0 && (hist = ((Message)oi.readObject())) != null){
					history.add(hist);
				}
				oi.close();
				fi.close();
				oo = new ObjectOutputStream(fo) {
					@Override
					protected void writeStreamHeader() throws IOException {
					}
				};
			} else {
				oo = new ObjectOutputStream(fo);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void destroy() {
		clientList.forEach(c -> {
			try {
				c.disconnect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
		try {
			fo.close();
			oo.close();
			historyFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void join(ClientInterface client) {
		
		try {
			String userName = client.getName();
			Message serverMsg = new Message("SERVER", null, userName+" rentre dans le chat", false);
			writeInFile(historyFile, serverMsg);
			history.add(serverMsg);
			clientList.add(client);
			clientList.forEach(c -> {
				try {
					c.postMessage(serverMsg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void leave(ClientInterface client) {
		String userName;
		try {
			userName = client.getName();
			Message serverMsg = new Message("SERVER", null, userName+" quitte le chat", false);
			writeInFile(historyFile, serverMsg);
			history.add(serverMsg);
			clientList.remove(client);
			clientList.forEach(c -> {
				try {
					c.postMessage(serverMsg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(Message message) throws RemoteException {
		if(!message.isPrivate()){
			writeInFile(historyFile, message);
			history.add(message);
		}
		for(ClientInterface c : clientList){
			if(!message.isPrivate() || ((c.getName().equals(message.getTo()) || c.getName().equals(message.getFrom())) && message.isPrivate()))
				c.postMessage(message);
		}
	}
	
	public void getHistory(ClientInterface client) throws RemoteException {
		for(Message m : history){
			client.postMessage(m);
		}	
	}

	public ClientInterface getOwner() {
		return owner;
	}

	public void setOwner(ClientInterface owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	void writeInFile(File file, Object obj){
		try {
			oo.writeObject(obj);
			oo.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void roomCreated(String roomName) {
		clientList.forEach(c -> {
			try {
				c.roomCreated(roomName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void roomDestroyed(String roomName) {
		clientList.forEach(c -> {
			try {
				c.roomDestroyed(roomName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}

	public void userJoin(String userName, String roomName) {
		clientList.forEach(c -> {
			try {
				c.userJoin(userName, roomName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void userLeave(String userName, String roomName) {
		clientList.forEach(c -> {
			try {
				c.userLeave(userName, roomName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
	}

}
