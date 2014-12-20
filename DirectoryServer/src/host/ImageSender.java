package host;

import com.github.sarxos.webcam.Webcam;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;
import javax.imageio.ImageIO;


/**
 * Multicast Image Sender
 * Version: 0.1
 * 
 * @author Jochen Luell
 *
 */
public class ImageSender {
    /* Flags and sizes */
    public static int HEADER_SIZE = 8;
    public static int MAX_PACKETS = 255;
    public static int SESSION_START = 128;
    public static int SESSION_END = 64;
    public static int DATAGRAM_MAX_SIZE = 65507 - HEADER_SIZE;
    public static int MAX_SESSION_NUMBER = 255;
    /*
     * The absolute maximum datagram packet size is 65507, The maximum IP packet
     * size of 65535 minus 20 bytes for the IP header and 8 bytes for the UDP
     * header.
     */
    public static String OUTPUT_FORMAT = "jpg";
    public static int COLOUR_OUTPUT = BufferedImage.TYPE_INT_RGB;

    /* Default parameters */
    public static double SCALING = 0.5;
    public static int SLEEP_MILLIS = 0;
    public static boolean SHOW_MOUSEPOINTER = true;
    
    /*socket paramters*/
    private final String ip;
    private final int port; 
    
    public String getIp(){
        return this.ip;
    }
    
    public int getPort(){
        return this.port;
    }
    
    public ImageSender(String ip, int port){
        this.ip = ip;
        this.port = port;
    }    

    /**
     * Takes a screenshot (fullscreen)
     * 
     * @return Sreenshot
     * @throws AWTException
     * @throws IOException
     */
    public static BufferedImage getScreenshot() throws AWTException,
                    IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);

        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRect);

        return image;
    }    
    
    public static BufferedImage getWebcamImage(Webcam webcam) {
        BufferedImage bufferedImage = webcam.getImage();
        System.out.println(bufferedImage);
        return bufferedImage;
    }

    /**
     * Converts BufferedImage to byte array
     * 
     * @param image Image to convert
     * @param format Image format (JPEG, PNG or GIF)
     * @return Byte Array
     * @throws IOException
     */
    public static byte[] bufferedImageToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    /**
     * Scales a bufferd image 
     * 
     * @param source Image to scale
     * @param w Image widht
     * @param h Image height
     * @return Scaled image
     */
    public static BufferedImage scale(BufferedImage source, int w, int h) {
        Image image = source.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
        BufferedImage result = new BufferedImage(w, h, COLOUR_OUTPUT);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    /**
     * Shrinks a BufferedImage
     * 
     * @param source Image to shrink
     * @param factor Scaling factor
     * @return Scaled image
     */
    public static BufferedImage shrink(BufferedImage source, double factor) {
        int w = (int) (source.getWidth() * factor);
        int h = (int) (source.getHeight() * factor);
        return scale(source, w, h);
    }

    /**
     * Copies a BufferedImage
     * 
     * @param image Image to copy
     * @return Copied image
     */
    public static BufferedImage copyBufferedImage(BufferedImage image) {
            BufferedImage copyOfIm = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g = copyOfIm.createGraphics();
        g.drawRenderedImage(image, null);
        g.dispose();
        return copyOfIm;
    }
    
    /**
     * Sends a byte array via multicast
     * Multicast addresses are IP addresses in the range of 224.0.0.0 to
     * 239.255.255.255.
     * 
     * @param imageData Byte array
     * @param multicastAddress IP multicast address
     * @param port Port
     * @return <code>true</code> on success otherwise <code>false</code>
     */

    public boolean sendImage(byte[] imageData, String multicastAddress, int port) {
        InetAddress ia;
        boolean ret = false;
        int ttl = 2;
        
        try {
            ia = InetAddress.getByName(multicastAddress); // récupération de l'adresse multicast
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return ret;
        }

        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(); // création du Socket Multicast
            ms.setTimeToLive(ttl);
            DatagramPacket dp = new DatagramPacket(imageData, imageData.length, ia, port); // création du pacquet contenant l'image
            ms.send(dp);// envoie du paquet en multicast
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (ms != null) {
                ms.close();
            }
        }
        return ret;
    }
}

/**
 * File filter class
 * 
 * @author luelljoc
 *
 */
class ImageFileFilter implements FilenameFilter
{
    public boolean accept( File dir, String name )
    {
      String nameLc = name.toLowerCase();
      return nameLc.endsWith(".jpg") ? true : false;
    }
}