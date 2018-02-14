package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class RoomExpendable {
	private String roomName;
	private ObservableList<String> usersInRoom;
	private ListView<String> usersView;
	private boolean hidden = true;
	
	public RoomExpendable(String roomName, boolean hidden){
		this.roomName = roomName;
		this.hidden = hidden;
		this.usersInRoom = FXCollections.observableArrayList();
		this.usersView = new ListView<String>();
		this.usersView.setItems(usersInRoom);
		this.usersView.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
			}
		});
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public ObservableList<String> getUsersInRoom() {
		return usersInRoom;
	}

	public void setUsersInRoom(ObservableList<String> usersInRoom) {
		this.usersInRoom = usersInRoom;
	}

	public ListView<String> getUsersView() {
		return usersView;
	}
}
