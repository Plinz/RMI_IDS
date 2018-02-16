package chat;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server implements ServerInterface {
	private List<ClientInterface> clientInLobby;
	private HashMap<String, Room> rooms;
	private HashMap<String, String> usersRooms;

	public Server(){
		rooms = new HashMap<String, Room>();
		usersRooms = new HashMap<String, String>();
		clientInLobby = new ArrayList<ClientInterface>();
	}

	@Override
	public boolean join(ClientInterface client) throws RemoteException {
		System.out.println("JOIN");
		String name = client.getName();
		System.out.println(name);
		boolean success = !clientInLobby.stream().filter(c -> {
			try {
				return c.getName().equals(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return false;
		}).findAny().isPresent();
		if (success && !usersRooms.containsKey(name)){
			clientInLobby.add(client);
			rooms.keySet().forEach(r -> {
				try {
					client.roomCreated(r);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			success = true;
			usersRooms.forEach((u, r) -> {
				try {
					client.userJoin(u, r);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
		} else {
			success = false;
		}
		return success;
	}

	@Override
	public void leave(ClientInterface client) throws RemoteException {
		if(clientInLobby.contains(client)){
			clientInLobby.remove(client);
		}
	}


	/**
	 * Créer un salon de discution de nom name
	 * @param client L'object client créant la discution
	 * @param name Le nom du salon de discution
	 */
	@Override
	public boolean createRoom(ClientInterface client, String name) throws RemoteException {
		boolean success = false;
		if(!name.trim().isEmpty() && !rooms.containsKey(name)){
			success = true;
			rooms.put(name, new Room(name, client));
			rooms.forEach((n, r) -> r.roomCreated(name));
			clientInLobby.forEach(c -> {
				try {
					c.roomCreated(name);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			
		}
		return success;
	}
	
	@Override
	public boolean destroyRoom(ClientInterface client, String name) throws RemoteException {
		Room toDestroy = rooms.get(name);
		boolean success = false;
		if (toDestroy.getOwner().equals(client.getName())){
			success = true;
			toDestroy.destroy();
			rooms.forEach((n, r) -> r.roomDestroyed(name));
			clientInLobby.forEach(c -> {
				try {
					c.roomDestroyed(name);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			rooms.remove(name);
		}
		return success;
	}

	/**
	 * Permet à un client de rejoindre un salon de discution existant dont le nom room est passé en
	 * paramètre. Le client à ajouter de doit pas être déjà présent dans un salon de discution
	 * @param client l'object client joignant le salon
	 * @param room le nom du salon à rejoindre
	 */
	@Override
	public boolean joinRoom(ClientInterface client, String room) throws RemoteException {
		String name = client.getName();
		boolean success = false;
		if (rooms.containsKey(room) && !usersRooms.getOrDefault(name, "").equals(room)){
			if (usersRooms.containsKey(name)){
				leaveRoom(client);
			}
			rooms.get(room).join(client);
			usersRooms.put(client.getName(), room);
			clientInLobby.remove(client);
			rooms.forEach((n, r) -> r.userJoin(name, room));
			clientInLobby.forEach(c -> {
				try {
					c.userJoin(name, room);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			success = true;
		}
		return success;
	}

	/**
	 * Permet à un client de quitter un salon de discution
	 * @param client l'object client quittant le salon
	 */
	@Override
	public void leaveRoom(ClientInterface client) throws RemoteException {
		String name = client.getName();
		String room = usersRooms.get(name);
		System.out.println("Leave Room "+name+ " "+room);
		if (room != null){
			rooms.get(room).leave(client);
			usersRooms.remove(name);
			clientInLobby.add(client);
			rooms.forEach((n, r) -> r.userLeave(name, room));
			clientInLobby.forEach(c -> {
				try {
					c.userLeave(name, room);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * Le client envoi un message public ou privé. Si le message est public alors il est envoyé
	 * à tout le salon. Sinon le message comporte le nom de l'utilisateur à qui envoyer le message en privé
	 * @param client L'object client quittant le salon
	 * @param message Le message a envoyer
	 */
	@Override
	public void sendMessage(ClientInterface client, Message message) throws RemoteException {
		String name = client.getName();
		String room = usersRooms.get(name);
		if (!message.isPrivate() && room != null){
			rooms.get(room).sendMessage(message);
		} else if (message.isPrivate()){
			String roomTo = usersRooms.get(message.getTo());
			if (roomTo != null && room != null){
				rooms.get(room).sendMessage(message);
				rooms.get(roomTo).sendMessage(message);
			}
		}
	}

	/**
	 * Envoi à un client tout l'historique du salon qu'il a rejoint
	 * @param client L'object client dans le salon
	 */
	@Override
	public void getHistory(ClientInterface client) throws RemoteException {
		String room = usersRooms.get(client.getName());
		if (room != null){
			rooms.get(room).getHistory(client);
		}	
	}
}
