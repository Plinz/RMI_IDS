import java.rmi.server.*; 
import java.rmi.registry.*;

public class HelloServer {

  public static void  main(String [] args) {
	  try {
	    HelloImpl h = new HelloImpl ("Hello");
	    HelloImpl h2 = new HelloImpl ("Hello2");
	    HelloImpl r = new HelloImpl ("Registry");
	    
	    Hello h_stub = (Hello) UnicastRemoteObject.exportObject(h, 0);
	    Hello2 h2_stub = (Hello2) UnicastRemoteObject.exportObject(h2, 0);
	    Registry_itf r_stub = (Registry_itf) UnicastRemoteObject.exportObject(r, 0);

	    Registry registry= LocateRegistry.getRegistry(); 
	    registry.bind("Hello", h_stub);
	    registry.bind("Hello2", h2_stub);
	    registry.bind("Registry_itf", r_stub);

	    System.out.println ("Server ready");

	  } catch (Exception e) {
		  System.err.println("Error on server :" + e) ;
		  e.printStackTrace();
	  }
  }
}
