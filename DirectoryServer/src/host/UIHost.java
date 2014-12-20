package host;

import client.UIClient;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import static host.ImageSender.DATAGRAM_MAX_SIZE;
import static host.ImageSender.HEADER_SIZE;
import static host.ImageSender.MAX_PACKETS;
import static host.ImageSender.MAX_SESSION_NUMBER;
import static host.ImageSender.OUTPUT_FORMAT;
import static host.ImageSender.SCALING;
import static host.ImageSender.SESSION_END;
import static host.ImageSender.SESSION_START;
import static host.ImageSender.SHOW_MOUSEPOINTER;
import static host.ImageSender.SLEEP_MILLIS;
import static host.ImageSender.bufferedImageToByteArray;
import static host.ImageSender.getScreenshot;
import static host.ImageSender.shrink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import static javax.swing.JOptionPane.showMessageDialog;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Cette classe permet de construire l'interface utilisateur du Host, producteur de streams.
 * Il permet de définir l'IP multicast du Host, le port et le nom du stream à publier.
 * On peut à ce niveau soit le bureau soit la capture de la webcam.
 * La liste des streams disponibles pour les clients est aussi affichée et peut ètre mise
 * à jour par le bouton refresh.
 * 
 * @author group 
 */
public class UIHost extends JPanel implements ListSelectionListener {

	private JList list;
	private DefaultListModel listModel;
	private JButton refreshButton;
	private JButton createButton;
	private Host host;
	private static final String refreshString = "Refresh";
	private static final String createString = "Create";
	private JTextField ipInput;
	private JTextField portInput;
	private JTextField streamNameInput;
	private JComboBox modeList;

	private static final String ipString = "IP Address";
	private static final String portString = "Port";
	private static final String streamNameString = "Stream Name";
	private static final String modeString = "Mode";
	private static final String modeDesktopString = "Desktop";
	private static final String modeWebCamString = "WebCam";
	private static final String[] modeStrings = { modeDesktopString,
			modeWebCamString };

	private boolean isWebCamUsed;
	private Webcam webcam;

        /**
        * 
        * @author group 5
        * @throws RemoteException
        * @throws MalformedURLException
        * @throws NotBoundException
        */
	public UIHost() throws RemoteException, MalformedURLException,
			NotBoundException {
		super(new BorderLayout());

		this.isWebCamUsed = false;
		host = new Host(); // création de l'instance du Host
		List<List<String>> streams = host.getStreams(); // récupération de la liste des listes contenants le titre, le port et l'adresse du stream
		listModel = new DefaultListModel();
		for (List<String> s : streams) {
			listModel.addElement("Title: " + s.get(0) + "; IP Address:"
					+ s.get(1) + "; Port:" + s.get(2));
		}
                // Gestion de la liste à afficher
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.setVisibleRowCount(25);
		JScrollPane listScrollPane = new JScrollPane(list);
                
                // Bouton refresh du pannel
		refreshButton = new JButton(refreshString);
		refreshButton.setActionCommand(refreshString);
		refreshButton.addActionListener(new UIHost.RefreshListener());

                // Bouton Create du pannel
		createButton = new JButton(createString);
		createButton.setActionCommand(createString);
		createButton.addActionListener(new UIHost.CreateListener());

                // Dessin et dimensions des boutons
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(refreshButton);
		buttonPane.add(createButton);
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ipInput = new JTextField(10);
		portInput = new JTextField(10);
		streamNameInput = new JTextField(10);
		modeList = new JComboBox(modeStrings);
		JPanel inputPane = new JPanel();
		inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.LINE_AXIS));
		JLabel ipLabel = new JLabel(ipString);
		JLabel portLabel = new JLabel(portString);
		JLabel nameLabel = new JLabel(streamNameString);
		JLabel modeLabel = new JLabel(modeString);
		inputPane.add(ipLabel);
		inputPane.add(ipInput);
		inputPane.add(portLabel);
		inputPane.add(portInput);
		inputPane.add(nameLabel);
		inputPane.add(streamNameInput);
		inputPane.add(modeLabel);
		inputPane.add(modeList);

		add(listScrollPane, BorderLayout.PAGE_START);
		add(inputPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.PAGE_END);
	}

	class RefreshListener implements ActionListener {
            
            /**
             *  Cette méthode permet de rafraichir l'interface si une nouvelle action est faite.
             * 
             * @param e
            */
		public void actionPerformed(ActionEvent e) {
			List<List<String>> streams = new ArrayList<List<String>>();
			try {
				streams = host.getStreams(); // récupération de la liste du Host
			} catch (RemoteException ex) {
				Logger.getLogger(UIClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			listModel.clear(); // efface la liste
			for (List<String> s : streams) { // repeuplement de la liste
				listModel.addElement("Title: " + s.get(0) + "; IP Address:"
						+ s.get(1) + "; Port:" + s.get(2));
			}
		}
	}

	class CreateListener implements ActionListener {            
              /**
                * @param e
                */
		public void actionPerformed(ActionEvent e) {
			String ip = ipInput.getText();
			String portString = portInput.getText();
			String streamName = streamNameInput.getText();
			String mode = modeList.getSelectedItem().toString(); // mode Desktop ou Webcam
			List<List<String>> streams = new ArrayList<List<String>>();

			if (mode.equals(modeWebCamString) && isWebCamUsed == true) {
				showMessageDialog(null, "Webcam is used. You can't use webcam twice in a UI Host.");
				return;
			}

			try {
				streams = host.getStreams(); // récupération des streams
			} catch (RemoteException ex) {
				Logger.getLogger(UIHost.class.getName()).log(Level.SEVERE,
						null, ex);
			}
			for (List<String> s : streams) {
				if (s.get(0).equals(streamName)) {// controle existence stream
					showMessageDialog(null, "Stream name already exists!");
					return;
				}
				if (s.get(1).equals(ip) && s.get(2).equals(portString)) {// controle existence stream sur le port et l'adresse IP
					showMessageDialog(null, "Address already used!");
					return;
				}
			}
			try {
				host.addStream(streamName, ip, Integer.parseInt(portString)); // ajout du nouveau stream au Host
				MyRunnable myRunnable = new MyRunnable(
						Integer.parseInt(portString), ip, streamName, mode);
				Thread t = new Thread(myRunnable);
				t.start(); // lancement du thread fournissant les streams au followers.
				if (mode.equals(modeWebCamString)) {
					isWebCamUsed = true;
				}
				host.refreshStream(streamName);
			} catch (RemoteException ex) {
				Logger.getLogger(UIHost.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (IOException ex) {
				Logger.getLogger(UIHost.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
	}
        /**
         *@author group 5
         */
	public class MyRunnable implements Runnable {

		private int port;
		private String ip;
		private String streamName;
		private String mode;
		private volatile boolean stopped = false;

		public MyRunnable(int port, String ip, String streamName, String mode) {
			this.port = port;
			this.ip = ip;
			this.streamName = streamName;
			this.mode = mode;
		}

		public void run() {
                    ImageSender sender = new ImageSender(ip, port);
                    int sessionNumber = 0;

                    // Create Frame
                    final JFrame frame = new JFrame("Multicast Image Sender");
                    JLabel label = new JLabel();
                    String labelString;
                    if (mode.equals(modeDesktopString)) {
                            labelString = "Multicasting screenshots...";
                    } else {
                            labelString = "Multicasting webcam...";
                            Webcam.setAutoOpenMode(true);
                            webcam = Webcam.getDefault();
                            webcam.setViewSize(WebcamResolution.VGA.getSize());
                    }
                    label.setText(labelString);
                    frame.getContentPane().add(label);
                    frame.setVisible(true);

                    frame.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                                    if (JOptionPane.showConfirmDialog(frame,
                                                    "Are you sure to stop this streaming?",
                                                    "Really Closing?", JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                                            try {
                                                    if (mode.equals(modeWebCamString)) {
                                                            webcam.close();
                                                    }
                                                    stopped = true;
                                                    UIHost.this.host.removeStream(streamName);
                                            } catch (RemoteException ex) {
                                                    Logger.getLogger(UIHost.class.getName()).log(
                                                                    Level.SEVERE, null, ex);
                                            }
                                    }
                            }
                    });

                    frame.pack();

                    try {
                        /* Continuously send images */
                        while (!stopped) {
                            BufferedImage image;
                            System.out.print(mode);
                            if (mode.equals(modeDesktopString)) {
                                    image = getScreenshot();
                            } else {
                                    image = ImageSender.getWebcamImage(webcam);
                            }

                            /* Draw mousepointer into image */
                            if (SHOW_MOUSEPOINTER && mode.equals(modeDesktopString)) {
                            PointerInfo p = MouseInfo.getPointerInfo();
                            int mouseX = p.getLocation().x;
                            int mouseY = p.getLocation().y;

                            Graphics2D g2d = image.createGraphics();
                            g2d.setColor(Color.red);
                            Polygon polygon1 = new Polygon(new int[] { mouseX,
                                            mouseX + 10, mouseX, mouseX }, new int[] {
                                            mouseY, mouseY + 10, mouseY + 15, mouseY }, 4);

                            Polygon polygon2 = new Polygon(new int[] { mouseX + 1,
                                            mouseX + 10 + 1, mouseX + 1, mouseX + 1 },
                                            new int[] { mouseY + 1, mouseY + 10 + 1,
                                                            mouseY + 15 + 1, mouseY + 1 }, 4);
                            g2d.setColor(Color.black);
                            g2d.fill(polygon1);

                            g2d.setColor(Color.red);
                            g2d.fill(polygon2);
                            g2d.dispose();
                            }

                            /* Scale image */
                            image = shrink(image, SCALING);

                            byte[] imageByteArray = bufferedImageToByteArray(image,
                                            OUTPUT_FORMAT);
                            int packets = (int) Math.ceil(imageByteArray.length
                                            / (float) DATAGRAM_MAX_SIZE);

                            /* If image has more than MAX_PACKETS slices -> error */
                            if (packets > MAX_PACKETS) {
                                System.out.println("Image is too large to be transmitted!");
                                continue;
                            }

                            /* Loop through slices */
                            for (int i = 0; i <= packets; i++) {
                                int flags = 0;
                                flags = i == 0 ? flags | SESSION_START : flags;
                                flags = (i + 1) * DATAGRAM_MAX_SIZE > imageByteArray.length ? flags
                                                | SESSION_END
                                                : flags;

                                int size = (flags & SESSION_END) != SESSION_END ? DATAGRAM_MAX_SIZE
                                                : imageByteArray.length - i * DATAGRAM_MAX_SIZE;

                                /* Set additional header */
                                byte[] data = new byte[HEADER_SIZE + size];
                                data[0] = (byte) flags;
                                data[1] = (byte) sessionNumber;
                                data[2] = (byte) packets;
                                data[3] = (byte) (DATAGRAM_MAX_SIZE >> 8);
                                data[4] = (byte) DATAGRAM_MAX_SIZE;
                                data[5] = (byte) i;
                                data[6] = (byte) (size >> 8);
                                data[7] = (byte) size;

                                /* Copy current slice to byte array */
                                System.arraycopy(imageByteArray, i * DATAGRAM_MAX_SIZE,
                                                data, HEADER_SIZE, size);
                                /* Send multicast packet */
                                sender.sendImage(data, sender.getIp(), sender.getPort());

                                /* Leave loop if last slice has been sent */
                                if ((flags & SESSION_END) == SESSION_END)
                                        break;
                            }
                            /* Sleep */
                            Thread.sleep(SLEEP_MILLIS);

                            /* Increase session number */
                            sessionNumber = sessionNumber < MAX_SESSION_NUMBER ? ++sessionNumber: 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() throws RemoteException,
			MalformedURLException, NotBoundException {
		// Create and set up the window.
		JFrame frame = new JFrame("Host");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new UIHost();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		
		//Center GUI
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);

		frame.setResizable(false);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (RemoteException ex) {
					Logger.getLogger(UIClient.class.getName()).log(
							Level.SEVERE, null, ex);
				} catch (MalformedURLException ex) {
					Logger.getLogger(UIHost.class.getName()).log(Level.SEVERE,
							null, ex);
				} catch (NotBoundException ex) {
					Logger.getLogger(UIHost.class.getName()).log(Level.SEVERE,
							null, ex);
				}
			}
		});
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
	}

}
