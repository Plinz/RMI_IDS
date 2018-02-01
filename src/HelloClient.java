import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HelloClient{
  
	public static void main(String [] args) {
		try {
		  if (args.length < 1) {
		   System.out.println("Usage: java HelloClient <rmiregistry host>");
		   return;}
		
		String host = args[0];
		
		// Get remote object reference
		Registry registry = LocateRegistry.getRegistry(host); 
		Hello h = (Hello) registry.lookup("Hello");
		Hello2 h2 = (Hello2) registry.lookup("Hello2");
		Registry_itf r = (Registry_itf) registry.lookup("Registry_itf");
		
		Info_itf client = new ClientImpl("Client_info_itf");
		Info_itf i_stub = (Info_itf) UnicastRemoteObject.exportObject(client, 0);
		
		Accounting_itf client2= new ClientImpl("Client_accounting_itf");
		Accounting_itf a_stub = (Accounting_itf) UnicastRemoteObject.exportObject(client2, 0);

		// Remote method invocation
		String res = h.sayHello(i_stub);
		System.out.println("Hello=\""+res+"\"");
		r.register(a_stub);
		String res2;
		for(int i =0; i < 12; i++){
			res2 = h2.sayHello(a_stub);
			Thread.sleep(1000);
		}
		
		//System.out.println("Hello2=\""+res2+"\"");
		
		} catch (Exception e)  {
			System.err.println("Error on client: " + e);
		}
  }

}