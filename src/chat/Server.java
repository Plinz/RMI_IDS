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
		System.out.println("Leave Room "+name+ " "+room);
		if (room != null){
			rooms.get(room).leave(client);
			usersRooms.remove(name);
			rooms.forEach((n, r) -> r.userLeave(name, room));
		}
	}

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
		usersRooms.forEach((n,r) -> {System.out.println("Join fin "+n+" "+r);});
		return success;
	}

	@Override
	public void leaveRoom(ClientInterface client) throws RemoteException {
		String name = client.getName();
		String room = usersRooms.get(name);
		System.out.println("Leave Room "+name+ " "+room);
		if (room != null && !room.equals("Accueil")){
			rooms.forEach((n, r) -> r.userLeave(name, room));
			rooms.get(room).leave(client);
			usersRooms.remove(name);
			if (!room.equals("Accueil"))
				joinRoom(client, "Accueil");
		}
		usersRooms.forEach((n,r) -> {System.out.println("Leave fin "+n+" "+r);});
	}
	
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

	@Override
	public void getHistory(ClientInterface client) throws RemoteException {
		String room = usersRooms.get(client.getName());
		if (room != null){
			rooms.get(room).getHistory(client);
		}	
	}
}
