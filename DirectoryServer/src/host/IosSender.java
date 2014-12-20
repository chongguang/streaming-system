package host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class IosSender extends Thread {
	
	private static volatile IosSender instance = null;
	private static StringBuffer streamList = null; //The StringBuffer is Synchronised so mutual exclusion is guaranteed 
	private static boolean serverRunning = true;
	private static final int MAIN_IOS_PORT = 6666;
	
	
	public void run() {
		ServerSocket connectionServer = null;
        Socket clientSession = null;

   	 	System.out.println("iOS sender is running.");

   	 	try {
            connectionServer = new ServerSocket(MAIN_IOS_PORT);
            while (true && serverRunning) {
                clientSession = connectionServer.accept();
                new IosServerStream(clientSession, this).start();
            }

        } catch (IOException e) {
            System.out.println(e);
        }
	}
	
	protected synchronized String returnList() {
		return streamList.toString();
	}
	
	public void closeServer(){
		serverRunning = false;
	}
}

class IosServerStream extends Thread {

    Socket clientSession;
    IosSender server;
	private static boolean streamIsLive = true;

    IosServerStream(Socket clientSession, IosSender server) {
        this.clientSession = clientSession;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSession.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSession.getInputStream()));
            
            System.out.println("New client session opened.");
            while (streamIsLive) {
                System.out.println("Received request from: " + clientSession.getLocalAddress());
                out.println(server.returnList());
            }
            out.close();
            in.close();
            clientSession.close();
            System.out.println("Client session closed.");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void closeStream(){
		streamIsLive = false;
	}
}
