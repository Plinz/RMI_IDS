import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registry_itf extends Remote {
	public void register(Accounting_itf client) throws RemoteException;
}