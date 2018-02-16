package gui;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import chat.Client;
import chat.ClientInterface;
import chat.Message;
import chat.ServerInterface;
import chat.Tuple;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


public class ClientGUI extends Application implements Observer{

	private Registry registry;
	private ServerInterface serverInterface;
	private Client client;
	private ClientInterface c_stub;
	private List<Tuple<String, Color>> usersList;
	private List<Color> colors;

	private ListView<RoomExpendable> roomsListView;

	private BorderPane root;
	private TextField chatMessage;
	private TextFlow chatRoom;
	private ScrollPane scrollPaneChat;
	private ContextMenu cm;


	public static void main(String[] args) {
		System.setProperty("sun.rmi.transport.tcp.localHostnameTimeOut", "1000000");
		launch(args);
	}

	private Stage stage;

	@Override
	public void start(Stage primaryStage) throws Exception {

		colors = new ArrayList<Color>(Arrays.asList(Color.AQUA, Color.BLUE, Color.BLUEVIOLET, Color.BROWN, Color.BURLYWOOD, Color.CADETBLUE, Color.CHARTREUSE,
				Color.CHOCOLATE, Color.CORAL, Color.CORNFLOWERBLUE, Color.CYAN, Color.DARKBLUE, Color.DARKCYAN, Color.DARKGOLDENROD, Color.DARKGREEN, 
				Color.DARKGREY, Color.DARKKHAKI, Color.DARKMAGENTA, Color.DARKOLIVEGREEN, Color.DARKORANGE, Color.DARKORCHID, Color.DARKRED, Color.DARKSEAGREEN, 
				Color.DARKSLATEBLUE, Color.DARKSLATEGREY, Color.DARKTURQUOISE, Color.DARKVIOLET, Color.DEEPPINK, Color.DEEPSKYBLUE, Color.DIMGREY, Color.DODGERBLUE,
				Color.FIREBRICK, Color.FORESTGREEN, Color.FUCHSIA, Color.GOLD, Color.GOLDENROD, Color.GREEN, Color.GREENYELLOW, Color.GREY, Color.HOTPINK,
				Color.INDIANRED, Color.INDIGO, Color.LAWNGREEN, Color.LIME, Color.LIMEGREEN, Color.MAGENTA, Color.MAROON, Color.MEDIUMBLUE, Color.MIDNIGHTBLUE,
				Color.NAVY, Color.OLIVE, Color.OLIVEDRAB, Color.ORANGERED, Color.PERU, Color.ROYALBLUE, Color.SADDLEBROWN, Color.SEAGREEN,
				Color.SIENNA, Color.SLATEBLUE, Color.SPRINGGREEN, Color.STEELBLUE, Color.TEAL, Color.TOMATO, Color.TURQUOISE, Color.YELLOWGREEN));

		usersList = new ArrayList<Tuple<String, Color>>();

		stage = primaryStage;
		Scene scene = Chat();
		primaryStage.setTitle("Client GUI");
		primaryStage.setWidth(1080);
		primaryStage.setHeight(800);
		primaryStage.setResizable(false);        
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	protected Scene Chat() {
		root = new BorderPane();
		scrollPaneChat = new ScrollPane();
		chatMessage = new TextField();
		chatRoom = new TextFlow();

		chatMessage.setPromptText("Enter Your Chat Message Here");
		chatMessage.setOnKeyPressed(e -> computeLine(e));

		roomsListView = new ListView<RoomExpendable>();
		roomsListView.setCellFactory(cf -> new RoomCell());
		
		stage.setOnCloseRequest(h -> {
			try {
				if (serverInterface != null && c_stub != null){
					serverInterface.leaveRoom(c_stub);
					serverInterface.leave(c_stub);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Platform.exit();
			System.exit(0);
		});

		scrollPaneChat.setContent(chatRoom);
		scrollPaneChat.setFitToWidth(true);
		scrollPaneChat.setVvalue(1);
		
		root.setCenter(scrollPaneChat);
		root.setLeft(roomsListView);
		root.setBottom(chatMessage);
		
		cm = new ContextMenu();
		MenuItem exit = new MenuItem();
		exit.setText("Déconnexion du serveur");
		exit.setOnAction(event -> {destroyRoom(new String[]{"/exit"});});
		MenuItem labelCreate = new MenuItem();
		MenuItem create = new MenuItem();
		TextField fieldNameRoom = new TextField();
		fieldNameRoom.setPromptText("Nom du salon");
		fieldNameRoom.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER){
				createRoom(new String[]{"/join", fieldNameRoom.getText()});
				cm.getItems().clear();
				cm.getItems().addAll(create, exit);
			}			
		});
		labelCreate.setGraphic(fieldNameRoom);
		create.setText("Créer un nouveau salon");
		create.setOnAction(event -> {
			cm.getItems().clear();
			cm.getItems().add(labelCreate);
			cm.show((Node) fieldNameRoom, cm.getAnchorX(), cm.getAnchorY());
		});
		cm.getItems().addAll(create, exit);
		help();

		return new Scene(root);
	}

	private void computeLine(KeyEvent ke) {
		String msg = chatMessage.getText();
		if(ke.getCode() == KeyCode.ENTER && !msg.trim().isEmpty()) {
			try {
				String[] tokens = msg.split(" ");
				switch (tokens[0]){
				case "/msg":
					sendPrivateMessage(tokens);
					break;
				case "/con":
					connexion(tokens);
					break;
				case "/exit":
					exit(tokens);
					break;
				case "/join":
					joinRoom(tokens);
					break;
				case "/leave":
					leaveRoom(tokens);
					break;
				case "/create":
					createRoom(tokens);
					break;
				case "/destroy":
					destroyRoom(tokens);
					break;
				case "/help":
					help();
					break;
				default:
					sendMessage(tokens);
				}
				chatMessage.clear();
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void help() {
		printRedMessage(
				"Liste des commandes :\n"
				+ "/con <Pseudo> <Serveur> <Port> Connexion à un serveur\n"
				+ "/con <Pseudo> Connexion au server local\n"
				+ "/exit Deconnexion du serveur\n"
				+ "/join <Salon> Rejoindre un salon de discussion\n"
				+ "/leave Quitter le salon de discussion\n"
				+ "/create <Salon> Créer un nouveau salon de discussion\n"
				+ "/destroy <Salon> Détruire un salon de discussion\n"
				+ "/help Affiche la liste des commandes\n");
	}
	
	private void createRoom(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else if (tokens.length != 2){
			printRedMessage("Usage :\n/create <Salon> Créer un nouveau salon de discussion\n");
		} else {
			try {
				if (serverInterface.createRoom(c_stub, tokens[1])){
					printRedMessage("La room " + tokens[1] + " a bien été créé\n");
				} else {
					printRedMessage("Impossible de créer cette room\n");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void destroyRoom(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else if (tokens.length != 2){
			printRedMessage("Usage :\n/destroy <Salon> Détruire un salon de discussion\n");
		} else {
			try {
				if (serverInterface.destroyRoom(c_stub, tokens[1])){
					printRedMessage("La room " + tokens[1] + " a bien été détruite\n");
				} else {
					printRedMessage("Impossible de détruire cette room\n");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void leaveRoom(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else if (tokens.length != 1){
			printRedMessage("Usage :\n/leave Quitter la room\n");
		} else if (client.room.equals("Accueil")){
			printRedMessage("Vous ne pouvez pas quitter la room Accueil\n");
		} else {
			try {
				serverInterface.leaveRoom(c_stub);
				chatRoom.getChildren().clear();
				printRedMessage("Vous avez quitté la room "+client.room+"\n");
				client.room = "";
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void joinRoom(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else if (tokens.length != 2){
			printRedMessage("Usage :\n/join <roomName> Joindre la room <roomName>\n");
		} else {
			try {
				if (serverInterface.joinRoom(c_stub, tokens[1])){
					chatRoom.getChildren().clear();
					client.room = tokens[1];
					serverInterface.getHistory(c_stub);
				} else {
					printRedMessage("Impossible de joindre cette room\n");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void exit(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else if (tokens.length != 1){
			printRedMessage("Usage :\n/exit Quitter le serveur\n");
		} else {
			try {
				serverInterface.leave(c_stub);
				chatRoom.getChildren().clear();
				roomsListView.getItems().clear();
				scrollPaneChat.setContextMenu(null);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void connexion(String[] tokens) throws NotBoundException {
		try{
			int portToExport = 0;
			if (tokens.length == 4){
				registry = LocateRegistry.getRegistry(tokens[2], Integer.parseInt(tokens[3]));
				portToExport = 2000;
			} else if (tokens.length == 3){
				registry = LocateRegistry.getRegistry(tokens[2], 1099);
			} else {
				registry = LocateRegistry.getRegistry("localhost", 1099);
			}
			serverInterface = (ServerInterface) registry.lookup("ServerInterface");
			client = new Client(tokens[1]);
			client.addObserverPostMessage(this);
			roomsListView.setItems(client.getRoomList());
			c_stub = (ClientInterface) UnicastRemoteObject.exportObject(client, portToExport);
			if (serverInterface.join(c_stub)){
				chatRoom.getChildren().clear();
				client.room = "Accueil";
				scrollPaneChat.setContextMenu(cm);
				printRedMessage("Vous êtes bien connecté sur le serveur\n");
			} else {
				printRedMessage("Impossible de rejoindre le serveur : nom déjà utilisé\n");
			}
		} catch(RemoteException e){
			printRedMessage("Connexion au serveur impossible\n");
			e.printStackTrace();
		} catch(NumberFormatException e){
			printRedMessage("Numéro de port invalide\n");
		}
		chatMessage.clear();
		help();
	}
	

	private void sendMessage(String[] tokens) {
		if(serverInterface == null){
			printRedMessage("Vous n'êtes connecté à aucun serveur\n");
			help();
		} else {
			try {
				serverInterface.sendMessage(c_stub, new Message(client.getName(), "all", String.join(" ", tokens), false));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendPrivateMessage(String[] tokens) throws RemoteException {
		if (client == null){
			chatMessage.clear();
			printRedMessage("Vous n'êtes connecté à aucun serveur\nCommande utiles :\n/help\t\tAide\n/connect <Pseudo> <AddressServer> <Port>\tConnexion à un nouveau"
					+ " serveur (par défaut AddressServer=localhost Port=2020)\n");
		} else if (tokens.length > 2){
			if (usersList.stream().anyMatch(t -> t.x.equals(tokens[1]))  && !tokens[1].equals(client.getName())){
				String privateMsg = String.join(" ", Arrays.copyOfRange(tokens, 2, tokens.length));
				serverInterface.sendMessage(c_stub, new Message(client.getName(), tokens[1], privateMsg, true));
				chatMessage.clear();
			} else {
				chatMessage.clear();
				chatRoom.requestFocus();
				if (tokens[1].equals(client.getName()))
					chatMessage.setPromptText("Erreur : Vous ne pouvez pas vous envoyer un message privé");
				else
					chatMessage.setPromptText("Erreur : "+ tokens[1] + " n'est pas un nom d'utilisateur valide");
			}
		}
	}

	private void printRedMessage(String message) {
		Text redMessage = new Text();
		redMessage.setFill(Color.RED);
		redMessage.setText(message);
		chatRoom.getChildren().add(redMessage);		
	}

	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Message message = (Message) arg;
				Text nameText = new Text();
				if (message.getFrom().equals("SERVER")){
					nameText.setFill(Color.RED);
					nameText.setText(message.getData()+'\n');
					chatRoom.getChildren().addAll(nameText);
				} else{
					if(usersList.stream().anyMatch(t -> t.x.equals(message.getFrom()))){
						nameText.setFill(usersList.stream().filter(t -> t.x.equals(message.getFrom())).findFirst().get().y);
					}
					nameText.setText(message.getFrom());
					if(message.isPrivate()){
						Text privateMessage; 
						if (message.getFrom().equals(client.name)){
							privateMessage = new Text(" [Privé à " + message.getTo() + "] ");
						} else {
							privateMessage = new Text(" [Privé] ");
						}
						privateMessage.setFill(Color.PURPLE);
						chatRoom.getChildren().addAll(new Text("<"), nameText, privateMessage, new Text(">" + message.getData() + '\n'));
					} else {
						chatRoom.getChildren().addAll(new Text("<"), nameText, new Text(">"+message.getData()+'\n'));
					}
				}
				scrollPaneChat.setVvalue(1);
			}
		});
	}
	
	private SVGPath createArrowPath(double height, boolean up) {
	    SVGPath svg = new SVGPath();
	    int width = (int) height / 4;
	 
	    if (up)
	        svg.setContent("M" + width + " 0 L" + (width * 2) + " " + width + " L0 " + width + " Z");
	    else
	        svg.setContent("M0 0 L" + (width * 2) + " 0 L" + width + " " + width + " Z");
	 
	    return svg;
	}
	
	public class UserCell extends ListCell<String> {
		@Override 
		protected void updateItem(String userName, boolean empty) {
			super.updateItem(userName, empty);

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (empty || userName == null || userName.equals("")){
						setGraphic(null);
						setText(null);
					} else {
						setText(userName);
						setStyle("-fx-font-weight: bold;");
						if (usersList.stream().noneMatch(t -> t.x.equals(userName))){
							Color c;
							c = colors.get((new Random()).nextInt(colors.size()));
							colors.remove(c);
							usersList.add(new Tuple<String, Color>(userName, c));
							ListIterator<Node> it = chatRoom.getChildren().listIterator(chatRoom.getChildren().size());
							Text n;
							while (it.hasPrevious() && !(((n = (Text) it.previous()).getText().equals(userName + " rentre dans la chat\n")
									|| n.getText().equals(userName + " quitte le chat\n"))
									&& n.getFill().equals(Color.RED))) {
								if(n.getText().equals(userName)){
									n.setFill(c);
									it.set(n);
								}
							}
						}
						usersList.removeIf(t -> !client.getUsersList().contains(t.x));
						setTextFill(usersList.stream().filter(t -> t.x.equals(userName)).findFirst().get().y);
					}
				}
			});

			ContextMenu contextMenu = new ContextMenu();
			MenuItem editColor = new MenuItem();
			editColor.setText("Changez la couleur");
			editColor.setOnAction(event -> {
				Tuple<String, Color> tuple = usersList.stream().filter(t -> t.x.equals(getText())).findFirst().get();
				Color oldColor = tuple.y;
				Color newColor = colors.get((new Random()).nextInt(colors.size()));
				colors.remove(newColor);
				colors.add(oldColor);
				tuple.y = newColor;
				setTextFill(newColor);
				ListIterator<Node> it = chatRoom.getChildren().listIterator(chatRoom.getChildren().size());
				Text n;
				while (it.hasPrevious() && !(((n = (Text) it.previous()).getText().equals(userName + " rentre dans la chat\n")
						|| n.getText().equals(userName + " quitte le chat\n"))
						&& n.getFill().equals(Color.RED))) {
					if(n.getText().equals(getText()) && !n.getFill().equals(Color.BLACK)){
						n.setFill(newColor);
						it.set(n);
					}
				}
			});
			contextMenu.getItems().add(editColor);
			emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					setContextMenu(null);
				} else {
					setContextMenu(contextMenu);
				}
			});
		}
	}
	
	public class RoomCell extends ListCell<RoomExpendable> {
		@Override
		protected void updateItem(final RoomExpendable item, boolean empty) {
			super.updateItem(item, empty);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					VBox vbox = new VBox();						
					if (item != null && getIndex() > -1 && item.getRoomName() != null) {
						final Label labelHeader = new Label(item.getRoomName());
						labelHeader.setGraphic(createArrowPath(20, false));
						labelHeader.setGraphicTextGap(10);
						labelHeader.setId("tableview-columnheader-default-bg");
						ListView<String> tmp = item.getUsersView();
						tmp.setCellFactory(lv -> new UserCell());
						tmp.setPrefWidth(roomsListView.getPrefWidth()-10);
						tmp.setMaxHeight(item.getUsersInRoom().size() * 26);
						labelHeader.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent me) {
								if (me.getButton() == MouseButton.PRIMARY){
									item.setHidden(item.isHidden() ? false : true);
									if (item.isHidden()) {
										labelHeader.setGraphic(createArrowPath(20, false));
										vbox.getChildren().remove(vbox.getChildren().size() - 1);
									}
									else {
										labelHeader.setGraphic(createArrowPath(20, true));
										vbox.getChildren().add(tmp);
									}
								}
							}
						});
						vbox.getChildren().add(labelHeader);
						if(!item.isHidden()){
							labelHeader.setGraphic(createArrowPath(20, true));
							vbox.getChildren().add(tmp);
						}
					}
					else {
						setGraphic(null);
					}
					setGraphic(vbox);
					
					ContextMenu contextMenu = new ContextMenu();
					MenuItem join = new MenuItem();
					join.setText("Joindre le salon");
					join.setOnAction(event -> {joinRoom(new String[]{"/join", item.getRoomName()});});
					MenuItem leave = new MenuItem();
					leave.setText("Quitter le salon");
					leave.setOnAction(event -> {leaveRoom(new String[]{"/leave"});});
					MenuItem labelCreate = new MenuItem();
					TextField fieldNameRoom = new TextField();
					fieldNameRoom.setPromptText("Nom du salon");
					labelCreate.setGraphic(fieldNameRoom);
					MenuItem create = new MenuItem();
					create.setText("Créer un nouveau salon");
					create.setOnAction(event -> {
						contextMenu.getItems().clear();
						contextMenu.getItems().add(labelCreate);
						contextMenu.show((Node) fieldNameRoom, contextMenu.getAnchorX(), contextMenu.getAnchorY());
					});
					MenuItem destroy = new MenuItem();
					destroy.setText("Détruire le salon");
					destroy.setOnAction(event -> {destroyRoom(new String[]{"/destroy", item.getRoomName()});});
					MenuItem exit = new MenuItem();
					exit.setText("Déconnexion du serveur");
					exit.setOnAction(event -> {destroyRoom(new String[]{"/exit"});});
					fieldNameRoom.setOnKeyPressed(e -> {
						if (e.getCode() == KeyCode.ENTER){
							createRoom(new String[]{"/join", fieldNameRoom.getText()});
							contextMenu.getItems().clear();
							if(!empty && item != null){
								contextMenu.getItems().add(client.room.equals(item.getRoomName())?leave:join);
							}
							contextMenu.getItems().addAll(create, exit);
						}
					});
					if(!empty && item != null){
						contextMenu.getItems().add(client.room.equals(item.getRoomName())?leave:join);
					}
					contextMenu.getItems().addAll(create, exit);
					setContextMenu(contextMenu);
				}
			});
		}
	}
}