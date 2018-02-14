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
		boolean success = !clientInLobby.stream().filter(c -> {
			try {
				return c.getName().equals(client.getName());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return false;
		}).findAny().isPresent();
		if (success && !usersRooms.containsKey(client.getName())){
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

	@Override
	public boolean createRoom(ClientInterface client, String name) throws RemoteException {
		boolean success = false;
		if(!name.trim().isEmpty() && !rooms.containsKey(name)){
			rooms.put(name, new Room(name, client));
			rooms.forEach((n, r) -> r.roomCreated(name));
			clientInLobby.forEach(c -> {
				try {
					c.roomCreated(name);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			success = true;
		}
		return success;
	}
	
	@Override
	public void destroyRoom(ClientInterface client, String name) throws RemoteException {
		Room toDestroy = rooms.get(name);
		if (toDestroy.getOwner().getName().equals(client.getName())){
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
	}

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
	
	@Override
	public void sendMessage(ClientInterface client, Message message) throws RemoteException {
		String name = client.getName();
		String room = usersRooms.get(name);
		if (room != null){
			rooms.get(room).sendMessage(message);
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
