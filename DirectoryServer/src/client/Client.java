package client;

import directoryserver.DirectoryServerService;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Group 5
 */
public class Client extends UnicastRemoteObject {
    
    public static final int MAIN_CLIENT_PORT = 3333;
    public static final String SERVER_ADRESSE = "localhost";
    private static List<List<String>> streams;
    private static DirectoryServerService calServ;

    public Client() throws RemoteException {
        streams = new ArrayList<List<String>>();      
        String servName = "//" + SERVER_ADRESSE + ":" + MAIN_CLIENT_PORT + "/" + DirectoryServerService.serverName;
        streams = new ArrayList<List<String>>();
        try {
            calServ = (DirectoryServerService) Naming.lookup(servName);//get streaming server
            getStreamsFromServer();
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
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
