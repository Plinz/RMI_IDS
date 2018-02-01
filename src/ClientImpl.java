import java.rmi.*;

public class ClientImpl implements Info_itf, Accounting_itf{
	
	private String name;
	private int nbCall;
	
	public ClientImpl(String n) throws RemoteException {
		name = n;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public void numberOfCalls(int number) throws RemoteException {
		nbCall = number;
		System.out.println("Nombre d'appel : "+nbCall);
	}

}
