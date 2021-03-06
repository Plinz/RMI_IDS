package chat;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerMain {
	  public static void  main(String [] args) {
		  try {	
			ServerInterface serverInterface = new Server();
			ServerInterface s_stub = (ServerInterface) UnicastRemoteObject.exportObject(serverInterface, 1099);
			
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind("ServerInterface", s_stub);
			System.out.println ("Server ready");

		  } catch (Exception e) {
			  System.err.println("Error on server :" + e) ;
			  e.printStackTrace();
		  }
	  }
}
