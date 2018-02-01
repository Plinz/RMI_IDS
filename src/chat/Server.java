package chat;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Server implements ServerInterface {
	private List<ClientInterface> clientList;
	private List<Tuple<String, String>> history;
	
	public Server(){
		clientList = new ArrayList<ClientInterface>();
		history = new ArrayList<Tuple<String, String>>();
	}
	@Override
	public void join(ClientInterface client) throws RemoteException {
		if(!clientList.contains(client)){
			clientList.add(client);
			client.postMessage("SERVER", "Vous êtes bien connecté ");
			for(Tuple<String, String> t : history){
				client.postMessage(t.x, t.y);
			}
			System.out.println("Join Client : "+client.getName());
		} else {
			client.postMessage("SERVER", "Vous êtes déjà connecté ");
		}
	}

	@Override
	public void leave(ClientInterface client) throws RemoteException {
		if(clientList.contains(client)){
			clientList.remove(client);
			client.postMessage("SERVER", "Vous êtes bien déconnecté");
			System.out.println("Leave Client : "+client.getName());
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
