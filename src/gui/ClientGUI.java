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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;


public class ClientGUI extends Application implements Observer{

	private Registry registry;
	private ServerInterface serverInterface;
	private Client client;
	private ClientInterface c_stub;
	private List<Tuple<String, Color>> usersList;
	private List<Color> colors;

	private Label nameL;
	private Label serverAdressL;
	private TextField nameTF;
	private TextField serverAdressTF;
	private ListView<String> usersListView;

	private Button connexion;

	private TextFlow chatRoom;
	private ScrollPane scrollPaneChat;


	public static void main(String[] args) {
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
		Scene scene = logInScene();
		primaryStage.setTitle("Client GUI");
		primaryStage.setWidth(740);
		primaryStage.setHeight(500);
		primaryStage.setResizable(false);        
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public Scene logInScene(){
		BorderPane root = new BorderPane();
		connexion = new Button("Connexion");
		serverAdressL = new Label("Server Address");
		serverAdressTF = new TextField();
		serverAdressTF.setPromptText("Server Address");
		serverAdressTF.setText("localhost");
		serverAdressTF.setEditable(true);

		nameL = new Label("User Name");
		nameTF = new TextField();
		nameTF.setPromptText("User Name");
		nameTF.setEditable(true);	
		root.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.ENTER)
				connexion.fire();
		});
		connexion.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					registry = LocateRegistry.getRegistry(serverAdressTF.getText().trim(), 2020);
					serverInterface = (ServerInterface) registry.lookup("ServerInterface");
					client = new Client(nameTF.getText().trim());
					c_stub = (ClientInterface) UnicastRemoteObject.exportObject(client, 0);
					if (serverInterface.join(c_stub)){
						stage.setScene(Chat());
					} else {
						nameTF.setText("ERROR");
					}
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		});
		root.setCenter(new VBox(30, new HBox(10, serverAdressL, serverAdressTF), new HBox(10, nameL, nameTF), connexion));
		root.setPadding(new Insets(60));


		return new Scene(root);
	}

	protected Scene Chat() {
		client.addObserverPostMessage(this);
		BorderPane root = new BorderPane();
		scrollPaneChat = new ScrollPane();
		TextField chatMessage = new TextField();
		chatRoom = new TextFlow();

		chatMessage.setPromptText("Enter Your Chat Message Here");

		chatMessage.setOnKeyPressed(e -> {
			String msg = chatMessage.getText();
			if(e.getCode() == KeyCode.ENTER && !msg.isEmpty()) {
				try {

					String[] tokens = msg.split(" ");
					if (tokens[0].equals("/msg")){
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
					} else {
						serverInterface.sendMessage(c_stub, new Message(client.getName(), "all", msg, false));
						chatMessage.clear();
					}
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});

		usersListView = new ListView<String>();
		usersListView.setItems(client.getUsersList());
		usersListView.setCellFactory(lv -> new UserNameCell());
		usersListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {			
				return new UserNameCell();
			}
		});
		usersListView.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
			}
		});;

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					serverInterface.leave(c_stub);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				Platform.exit();
				System.exit(0);
			}
		});

		scrollPaneChat.setContent(chatRoom);
		scrollPaneChat.setFitToWidth(true);
		scrollPaneChat.setVvalue(1);
		root.setCenter(scrollPaneChat);

		root.setRight(usersListView);
		root.setBottom(chatMessage);

		try {
			serverInterface.getHistory(c_stub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return new Scene(root);
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

	public class UserNameCell extends ListCell<String> {
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
							while(it.hasPrevious() && !((n = (Text) it.previous()).getText().equals(userName+" rentre dans le chat\n") && n.getFill().equals(Color.RED))){
								System.out.println(n.getText());
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
				while(it.hasPrevious() && !((n = (Text) it.previous()).getText().equals(getText()+" rentre dans le chat\n") && n.getFill().equals(Color.RED))){
					if(n.getText().equals(getText()) && !n.getFill().equals(Color.BLACK)){
						n.setFill(newColor);
						it.set(n);
					}
				}
			});
			contextMenu.getItems().addAll(editColor);
			emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					setContextMenu(null);
				} else {
					setContextMenu(contextMenu);
				}
			});
		}
	}

}