package directoryserver;

import java.util.Date;

/**
 *
 * @author Group 5
 * 
 * Classe représentant le host avec ses principales fonctions.
 */
public class HostInstance {

    private Date lastUpdate;
    
    private final String title;
    
    private final String multicastAdresse;
    
    private final int multicastPort;
    
    public HostInstance(Date creationTime, String title, String multicastAdresse, int multicastPort){
    	
        this.lastUpdate = creationTime;
        this.title = title;
        this.multicastAdresse = multicastAdresse;
        this.multicastPort = multicastPort;
    }

    public String getTitle() {
        return title;
    }

    public String getMulticastAdresse() {
        return multicastAdresse;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void refresh() {
        lastUpdate = new Date();
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
    
}
