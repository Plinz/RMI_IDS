package gui;

import java.lang.reflect.Field;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import chat.Client;
import chat.ClientInterface;
import chat.ServerInterface;
import chat.Tuple;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
	
	
	public static void main(String[] args) {
	    launch(args);
	}
	
    private Stage stage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    	
    	colors = new ArrayList<Color>();
	    Class<?> clazz;
		try {
			clazz = Class.forName("javafx.scene.paint.Color");
		    if (clazz != null) {
		        Field[] field = clazz.getFields();
		        for (int i = 0; i < field.length; i++) {
		            Field f = field[i];                
		            Object obj = f.get(null);
		            if(obj instanceof Color){
		                colors.add(((Color) obj));
		            }
	
		        }
		    }
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		colors.remove(Color.RED.toString());
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
	    chatRoom = new TextFlow();

	    chatMessage.setPromptText("Enter Your Chat Message Here");
	    
	    chatMessage.setOnKeyPressed(e -> {
	    	 if(e.getCode() == KeyCode.ENTER) {
	    		 try {
	    			 String msg = chatMessage.getText();
	    			 serverInterface.sendMessage(c_stub, msg);
	    			 Text nameText = new Text();
	    			 nameText.setFill(usersList.stream().filter(t -> t.x.equals(client.name)).findFirst().get().y);
	    			 nameText.setText(client.getName());
	    			 chatRoom.getChildren().addAll(new Text("<"), nameText, new Text(">"+msg+'\n'));
	    			 chatMessage.clear();
	    		 } catch (RemoteException e1) {
	    			 e1.printStackTrace();
	    		 }
	    	 }
	    });
	    
	    usersListView = new ListView<String>();
	    usersListView.setItems(client.getUsersList());
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
	    	    
	    MenuBar menuBar = new MenuBar();
        Menu menuOption = new Menu("Option");
        MenuItem itemLeave = new MenuItem("Leave");
        menuOption.getItems().add(itemLeave);
        itemLeave.setOnAction(e -> {
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
        root.setRight(usersListView);
        root.setBottom(chatMessage);
        
        try {
			serverInterface.getHistory(c_stub);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
        
        return new Scene(root);
    }

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Tuple<String, String> msg = (Tuple<String, String>) arg;
				Text nameText = new Text();
				nameText.setFill(usersList.stream().filter(t -> t.x.equals(msg.x)).findFirst().get().y);
				nameText.setText(msg.x);
				chatRoom.getChildren().addAll(new Text("<"), nameText, new Text(">"+msg.y+'\n'));
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
				        if (usersList.stream().noneMatch(t -> t.x.equals(userName))){
				        	Color c;
				        	c = colors.get((new Random()).nextInt(colors.size()));
				        	colors.remove(c);
				        	usersList.add(new Tuple<String, Color>(userName, c));
				        }
				        usersList.removeIf(t -> !client.getUsersList().contains(t.x));
				        setTextFill(usersList.stream().filter(t -> t.x.equals(userName)).findFirst().get().y);
				        //setBackground(new Background(new BackgroundFill(usersList.stream().filter(t -> t.x.equals(userName)).findFirst().get().y, null, null)));
					}
				}
			});
	    }
	}

}