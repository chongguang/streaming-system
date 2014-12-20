/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package directoryserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  <p>
 * Cette classe représente le serveur qui permet de distribuer la liste de streams.
 * Le producteur de streams s'y connecte comme Host.
 * </p>
 * 
 * @author Group 5
 */
public class DirectoryServer extends UnicastRemoteObject implements DirectoryServerService {
	
	public static IosServer iOSserver = null;    
	
    public static final int MAIN_CLIENT_PORT = 3333;
    
    private static HashMap<String, HostInstance> StreamList; //The HashMap is Synchronised so mutual exclusion is guaranteed 
    
    /**
     * @throws RemoteException
     */
    public DirectoryServer() throws RemoteException {
        StreamList = new HashMap<String, HostInstance>();
    }

    /**
     * @param args the command line arguments
     * @throws MalformedURLException 
     * @throws RemoteException
     * @throws DirectoryServerException 
     * 
     */
    public static void main(String[] args) throws RemoteException, MalformedURLException, DirectoryServerException {

        try {
            //Crée et exporte une instance du Registre sur le localhost qui accepte les requêtes sur le port spécifique.
            // 1099 est le port par défaut du registre RMI.
            java.rmi.registry.LocateRegistry.createRegistry(MAIN_CLIENT_PORT);
            System.out.println("RMI registry ready.");
        } catch (Exception e) {
            System.out.println("Exception starting RMI registry:");
            e.printStackTrace();
        }
        //Création de l'instance du DirectoryServer
        DirectoryServer s = new DirectoryServer();
        Naming.rebind("//localhost:" + MAIN_CLIENT_PORT + "/" + DirectoryServerService.serverName, s);//Enregistrement du serveur Local de Naming
        
        //Création de l'instance du serveur IOS qui lancera le Thread qui permet de mettre à jour la liste
        //de streams disponibles sur le client IOS.
        iOSserver = IosServer.getInstance();
        System.out.println("iOS Server ready.");
        iOSserver.run();
    }
    
    /*
     * Cette méthode retourne la liste des streams disponibles tirés de la HashMap contenant le nom ou URL du stream
     * et l'instance du Host associée
     *
     * @return la liste de la liste contenant le titre, l'adresse multicast et le port du stream 
     */
    @Override
    public List<List<String>> getStreams() {
        
        // Création de la Liste de Streams
        List<List<String>> streams = new ArrayList<List<String>>();
        // Itération pour remplir la liste
        for (String k : StreamList.keySet()){
                List<String> tmpList = new ArrayList<String>();
                tmpList.add(k);
                tmpList.add(StreamList.get(k).getMulticastAdresse());
                tmpList.add(Integer.toString(StreamList.get(k).getMulticastPort()));
                streams.add(tmpList);
        }
        return streams;
    }
    
   /*
    * Méthode permettant d'ajouter un nouveau stream,prenant comme paramètres son titre, l'adresse multicast et le port multicast
    * 
    * @param title titre du stream
    * @param multicastAdresse adresse multicat du stream
    * @param multicastPort port multicast du stream
    */
    @Override
    public synchronized void addStream(String title, String multicastAdresse, int multicastPort) throws DirectoryServerException {
    	if (title.contains(";") || multicastAdresse.contains(";")) {
            throw new DirectoryServerException("The character \";\" is illegal in name and adress!");
    	}
        
        Iterator it = StreamList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            //controle si le titre a déjà utilisé
            if (pairs.getKey().equals(title)){
                throw new DirectoryServerException("The title is already used!");
            }
            HostInstance h = (HostInstance) pairs.getValue();
            //controle si l'adresse multicast et ce port ont déjà été utilisé. Ici on vérifie simultanéement l'adresse et le port
            if (h.getMulticastAdresse().equals(multicastAdresse) && h.getMulticastPort() == multicastPort){
                throw new DirectoryServerException("The Adresse is already used!");
            }
        }
        // Insertion du titre et de l'instance du Host dans la liste
        StreamList.put(title, new HostInstance(new Date(), title, multicastAdresse, multicastPort));
        iOSserver.updateList(StreamList); // Mise à jour du server IOS avec le nouveau titre de stream
        System.out.println("Stream \"" + title + "\" was added");
    }
    
    /*
    * Méthode permettant d'enlever le stream de la liste en considérant son titre
    * 
    * @param title titre du stream
    */
    @Override
    public synchronized void removeStream(String title) throws DirectoryServerException {
    	if (!StreamList.containsKey(title)) { // controle existence du titre
    		throw new DirectoryServerException("Unnable to find stream with title: \""+ title +" \"");
    	}
        StreamList.remove(title);
        System.out.println("Stream \"" + title + "\" was removed");
        iOSserver.updateList(StreamList);
    }
    /*
    * Méthode permettant de mettre à jour la liste des streams dans la HashMap
    * 
    * @param title titre du stream
    */
    
    @Override
    public synchronized void refreshtream(String title) throws DirectoryServerException {
    	if (!StreamList.containsKey(title)) {//controle existence du stream
    		throw new DirectoryServerException("Unnable to find stream with title: \""+ title +" \"");
    	}
        StreamList.get(title).refresh();
        System.out.println("Stream \"" + title + "\" was refreshed");
    }

}
