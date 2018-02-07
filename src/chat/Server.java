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

public class Server implements ServerInterface {
	private List<ClientInterface> clientList;
	private List<Tuple<String, String>> history;
	private File historyFile;
	
	@SuppressWarnings("unchecked")
	public Server(){
		clientList = new ArrayList<ClientInterface>();
		history = new ArrayList<Tuple<String, String>>();

		FileInputStream fi;
		try {
			historyFile = new File("historyFile");
			if(!historyFile.exists() && !historyFile.isDirectory()){
				historyFile.createNewFile();
			} else if (historyFile.length() != 0) {
				fi = new FileInputStream(historyFile);
				ObjectInputStream oi = new ObjectInputStream(fi);
				Tuple<String, String> hist;
				while((hist = ((Tuple<String, String>)oi.readObject())) != null){
					history.add(hist);
				}
				oi.close();
				fi.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	void writeInFile(File file, Object obj){
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
			o.writeObject(o);
			o.close();
			f.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}
	}
	
	@Override
	public void join(ClientInterface client) throws RemoteException {
		if(!clientList.contains(client)){
			clientList.add(client);
			history.forEach(t -> {
				try {
					client.postMessage(t.x, t.y);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			clientList.forEach(c -> {
				try {
					client.userJoin(c.getName());
					if (!client.getName().equals(c.getName()))
						c.userJoin(client.getName());
					c.postMessage("SERVER", client.getName()+" entre dans la room");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			System.out.println("Join Client : "+client.getName());
		} else {
			client.postMessage("SERVER", "Vous êtes déjà connecté ");
		}
	}

	@Override
	public void leave(ClientInterface client) throws RemoteException {
		if(clientList.contains(client)){
			String name = client.getName();
			clientList.remove(client);
			clientList.forEach(c -> {
				try {
					c.userLeave(name);
					c.postMessage("SERVER", name+" a quitté dans la room");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			System.out.println("Leave Client : "+name);
		} else {
			client.postMessage("SERVER", "Vous êtes déjà déconnecté");
		}
	}

	@Override
	public void sendMessage(ClientInterface client, String message) throws RemoteException {
		if (clientList.contains(client)){
			System.out.println("Nouveau Message de "+client.getName()+">"+message);
			String name = client.getName();
			history.add(new Tuple<String, String>(name, message));
			for(ClientInterface c : clientList){
				if(!c.equals(client))
					c.postMessage(name, message);
			}
		}
	}
	
	@Override
	public void getHistory(ClientInterface client) throws RemoteException {
		for(Tuple<String, String> t : history){
			client.postMessage(t.x, t.y);
		}	
	}

}
