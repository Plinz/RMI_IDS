

import java.rmi.*;
import java.util.HashMap;

public class HelloImpl implements Hello, Registry_itf, Hello2 {

	private String message;
	static private HashMap<Accounting_itf, Integer> map;
 
	public HelloImpl(String s) {
		message = s ;
		map = new HashMap<Accounting_itf, Integer>();
	}

	public String sayHello(Info_itf client) throws RemoteException {
		System.out.println(client.getName());
		return message ;
	}

	@Override
	public void register(Accounting_itf client) throws RemoteException {
		map.put(client, 0);
	}

	@Override
	public String sayHello(Accounting_itf client) throws RemoteException {
		if (!map.containsKey(client)){
			return "Erreur le client n'est pas enregistré sur le serveur";
		}
		int nbCall = map.get(client)+1;
		map.put(client, nbCall);
		client.numberOfCalls(nbCall);
		return message;
	}
}

