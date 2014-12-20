package host;

import directoryserver.DirectoryServerException;
import directoryserver.DirectoryServerService;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 *
 * @author Group 5
 */
public class Host extends UnicastRemoteObject {
    
    public static final int MAIN_CLIENT_PORT = 3333;
    public static final String SERVER_ADRESSE = "localhost";
    private static DirectoryServerService calServ;
    private static List<List<String>> streams;
    
    //230.0.0.1
    //4446

    public Host() throws RemoteException, MalformedURLException, NotBoundException {
        String servName = "//" + SERVER_ADRESSE + ":" + MAIN_CLIENT_PORT + "/" + DirectoryServerService.serverName; //server name in URL format
        calServ = (DirectoryServerService) Naming.lookup(servName);	//get streaming server        
    } 
    
    public void addStream(String streamName, String ip, int port) throws RemoteException{
    	try {
            calServ.addStream(streamName, ip, port);
        } catch (DirectoryServerException e) {
            e.printStackTrace();
        }
    }
    
    public void refreshStream(String streamName) throws RemoteException{
    	try {
            calServ.refreshtream(streamName);
        } catch (DirectoryServerException e) {
            e.printStackTrace();
        }
    }
    
    public void removeStream(String streamName) throws RemoteException{
    	try {
            calServ.removeStream(streamName);
        } catch (DirectoryServerException e) {
            e.printStackTrace();
        }
    }  
    
    private void getStreamsFromServer() throws RemoteException{
    	streams = calServ.getStreams();
    }
    
    public List<List<String>> getStreams() throws RemoteException {
        getStreamsFromServer();
        return streams;
    }
}
