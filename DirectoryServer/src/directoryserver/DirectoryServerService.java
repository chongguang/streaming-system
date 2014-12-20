package directoryserver;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
* Interface utilisée pour le DirectoryServer
* 
* @author Group 5
*/
public interface DirectoryServerService extends Remote {
    static final String serverName = "StreamServer";//stream serve name (for naming registery server)
    public List<List<String>> getStreams() throws RemoteException;//get List of Streams with connection informations
    public void addStream(String title, String multicastAdresse, int multicastPort) throws RemoteException, DirectoryServerException;//add a stream to the list
    public void removeStream(String title) throws RemoteException, DirectoryServerException;//remove stream from the list
    public void refreshtream(String title) throws RemoteException, DirectoryServerException;//refresh stream before timeout
}
