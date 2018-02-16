package chat;

import java.rmi.RemoteException;
import java.util.HashMap;

public class Server implements ServerInterface {
	private HashMap<String, Room> rooms;
	private HashMap<String, String> usersRooms;

	public Server(){
		rooms = new HashMap<String, Room>();
		rooms.put("Accueil", new Room("Accueil", null));
		usersRooms = new HashMap<String, String>();
	}

	@Override
	public boolean join(ClientInterface client) throws RemoteException {
		String name = client.getName();
		boolean success = false;
		if (!usersRooms.containsKey(name)){
			rooms.keySet().forEach(r -> {
				try {
					client.roomCreated(r);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			usersRooms.forEach((u, r) -> {
				try {
					client.userJoin(u, r);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			success = joinRoom(client, "Accueil");
		}
		return success;
	}

	@Override
	public void leave(ClientInterface client) throws RemoteException {
		String name = client.getName();
		String room = usersRooms.get(name);
		if (room != null){
			rooms.get(room).leave(client);
			usersRooms.remove(name);
			rooms.forEach((n, r) -> r.userLeave(name, room));
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
				rooms.forEach((n, r) -> r.userLeave(name, usersRooms.get(name)));
				rooms.get(usersRooms.get(name)).leave(client);
				usersRooms.remove(name);
			}
			rooms.get(room).join(client);
			usersRooms.put(client.getName(), room);
			rooms.forEach((n, r) -> r.userJoin(name, room));
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
		if (room != null && !room.equals("Accueil")){
			rooms.forEach((n, r) -> r.userLeave(name, room));
			rooms.get(room).leave(client);
			usersRooms.remove(name);
			if (!room.equals("Accueil"))
				joinRoom(client, "Accueil");
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
