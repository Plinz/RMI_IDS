package gui;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Observable;
import java.util.Observer;

import chat.Client;
import chat.ClientInterface;
import chat.ServerInterface;
import chat.Tuple;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ClientGUI extends Application implements Observer{
	
	private Registry registry;
	private ServerInterface serverInterface;
	private Client client;
	private ClientInterface c_stub;

	private Label nameL;
	private Label serverAdressL;
	private TextField nameTF;
	private TextField serverAdressTF;
	
	private Button connexion;
	
	private TextArea chatRoom;
	
	
	public static void main(String[] args) {
	    launch(args);
	}
	
    private Stage stage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
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
	    serverAdressTF.setEditable(true);
	
	    nameL = new Label("User Name");
	    nameTF = new TextField();
	    nameTF.setPromptText("User Name");
	    nameTF.setEditable(true);	
        
        connexion.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent event) {
	            try {
					registry = LocateRegistry.getRegistry(serverAdressTF.getText().trim());
	    			serverInterface = (ServerInterface) registry.lookup("ServerInterface");
	    			client = new Client(nameTF.getText().trim());
	    			c_stub = (ClientInterface) UnicastRemoteObject.exportObject(client, 0);
	    			serverInterface.join(c_stub);
	    			stage.setScene(Chat());
	            } catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
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
	    TextField chatMessage = new TextField();
	    chatRoom = new TextArea();
	    try {
			serverInterface.getHistory(c_stub);
		} catch (RemoteException e2) {
			e2.printStackTrace();
		}
	    chatMessage.setPromptText("Enter Your Chat Message Here");
	    chatRoom.setPromptText("Welcome to the Chat room");
	    chatRoom.setEditable(false);
	    chatMessage.setOnKeyPressed(e -> {
	    	 if(e.getCode() == KeyCode.ENTER) {
	    		 try {
	    			 String msg = chatMessage.getText();
	    			 serverInterface.sendMessage(c_stub, msg);
	    			 chatRoom.appendText("<"+client.getName()+">"+msg+'\n');
	    			 chatMessage.clear();
	    		 } catch (RemoteException e1) {
	    			 e1.printStackTrace();
	    		 }
	    	 }
	    });
	    
	    TableView<String> usersList = new TableView<String>(client.getUsersList());
	    client.listUsers();
	    
	    MenuBar menuBar = new MenuBar();
        Menu menuOption = new Menu("Option");
        MenuItem itemLeave = new MenuItem("Leave");
        menuOption.getItems().add(itemLeave);
        itemLeave.setOnAction(e -> {
        	System.out.println("FireAction");
        	try {
				serverInterface.leave(c_stub);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
        	stage.setScene(logInScene());
        });
        menuBar.getMenus().add(menuOption);
	    
        root.setCenter(chatRoom);
        root.setTop(menuBar);
        root.setRight(usersList);
        root.setBottom(chatMessage);
        return new Scene(root);
    }

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		Tuple<String, String> msg = (Tuple<String, String>) arg;
		chatRoom.appendText("<"+msg.x+">"+msg.y+'\n');	
	}

}