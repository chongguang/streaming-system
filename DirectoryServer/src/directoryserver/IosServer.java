package directoryserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public final class IosServer extends Thread {
	
	private static volatile IosServer instance = null;
	private static StringBuffer streamList = null; //The StringBuffer is Synchronised so mutual exclusion is guaranteed 
	private static boolean serverRunning;
	private static final int MAIN_IOS_PORT = 5555;
	
	private IosServer(){
		super();
		serverRunning = true;
	}

	public final static IosServer getInstance() {
		
		if (IosServer.instance == null)   {
           synchronized(IosServer.class) {
             if (IosServer.instance == null) {
            	 IosServer.instance = new IosServer();
            	 System.out.println("Creating iOS server.");
             } else {
            	 System.out.println("Returning iOS server.");
             }
           }
        }
        return IosServer.instance;
    }
	
	public void run() {
		ServerSocket connectionServer = null;
        Socket clientSession = null;

   	 	System.out.println("iOS server is running.");

   	 	try {
            connectionServer = new ServerSocket(MAIN_IOS_PORT);
            while (true && serverRunning) {
                clientSession = connectionServer.accept();
                new ClientSessionThread(clientSession, this).start();
            }

        } catch (IOException e) {
            System.out.println(e);
        }
	}
	
	public synchronized void updateList(HashMap<String, HostInstance> StreamList) {
		streamList = new StringBuffer();
        for (String k : StreamList.keySet()){
        	streamList.append(k + ";");
        	streamList.append(StreamList.get(k).getMulticastAdresse() + ";");
        	streamList.append(StreamList.get(k).getMulticastPort() + ";");
        }
	}
	
	protected synchronized String returnList() {
		return streamList.toString();
	}
	
	public void closeServer(){
		serverRunning = false;
	}
}

class ClientSessionThread extends Thread {

    Socket clientSession;
    IosServer server;

    ClientSessionThread(Socket clientSession, IosServer server) {
        this.clientSession = clientSession;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSession.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSession.getInputStream()));
            
            System.out.println("New client session opened.");
            while ((in.readLine()) != null) {
                System.out.println("Received request from: " + clientSession.getLocalAddress());
                out.println(server.returnList());
            }
            out.close();
            in.close();
            clientSession.close();
            System.out.println("Client session closed.");
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}
