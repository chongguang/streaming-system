package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;

import static javax.swing.JOptionPane.showMessageDialog;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author chongguang
 */
public class UIClient extends JPanel implements ListSelectionListener {
    
    private JList list;
    private DefaultListModel listModel;
    private JButton refreshButton;
    private JButton playButton;
    private Client client;
    private List<List<String>> streams;
    private static final String refreshString = "Refresh";
    private static final String playString = "Play";
    
    public UIClient() throws RemoteException {        
        super(new BorderLayout());
        client = new Client();
        streams = client.getStreams();
        listModel = new DefaultListModel();
        for (List<String> s : streams) {
            listModel.addElement(s.get(0));
        }
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(25);
        JScrollPane listScrollPane = new JScrollPane(list);       

        // bouton Refresh de l'Interface Client    
        refreshButton = new JButton(refreshString);
        refreshButton.setActionCommand(refreshString);
        refreshButton.addActionListener(new RefreshListener());      

        // Bouton Play de l'Interface Client    
        playButton = new JButton(playString);
        playButton.setActionCommand(playString);
        playButton.addActionListener(new PlayListener());
        
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(refreshButton);
        buttonPane.add(playButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    class RefreshListener implements ActionListener {
        
        /**
         * Cette méthode met à jour la liste de streams coté client
         *@param e
         */
        public void actionPerformed(ActionEvent e) {
            try {
                streams = client.getStreams(); // liste de streams
            } catch (RemoteException ex) {
                Logger.getLogger(UIClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            listModel.clear();// efface la liste
            for (List<String> s : streams) {// repeuple la liste de streams
                listModel.addElement(s.get(0));
            }
        }        
    }     

    class PlayListener implements ActionListener {
        
        /**
         * Cette méthode met à jour la liste de streams coté client
         * @param e
         */
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex(); // sélection d'un stream de la liste
            if ( index >= 0){                
                String streamName = (String) listModel.get(index);
                int port = 0;
                String ip = null;
                for (List<String> s : streams) {
                    if (s.get(0).equals(streamName)) { // récupération du port et de l'IP du stream sélectionné
                        port = Integer.parseInt(s.get(2));
                        ip = s.get(1);
                    }
                }
                if (port != 0 && ip != null) { // Controle du port et de l'adresse IP
                    MyRunnable myRunnable = new MyRunnable(port, ip);
                    Thread t = new Thread(myRunnable);
                    t.start(); // Lancement du thread pour le stream
                } 
            } else {
                showMessageDialog(null, "Please select a stream!");
            }          
        }        
    } 
    
    
    public class MyRunnable implements Runnable {

        private int port;
        private String ip;

        public MyRunnable(int port, String ip) {
            this.port = port;
            this.ip = ip;
        }
	        
		public void run() {
	
	            ImageReceiver receiver = new ImageReceiver( ip, port);
	            receiver.receiveImages(receiver.getIp(), receiver.getPort()); // reception de l'image
		}
   
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() throws RemoteException {
        //Create and set up the window.
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new UIClient();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
        //Center GUI
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		
		frame.setResizable(false);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (RemoteException ex) {
                    Logger.getLogger(UIClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    	
    }
    
}
