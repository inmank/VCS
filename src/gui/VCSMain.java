package gui;

/**
 * The class contains the main method and other Constants.
 */
import java.awt.Color;

public class VCSMain {

	public static final String 	TITLE = "Visual CryptoGraphy";
	
	public static final String	KEY_SECRET 		= "Secert";
	public static final String	KEY_COVER 		= "Cover";
	public static final String	KEY_THRESHOLD 	= "Threshold";
	public static final String	KEY_DITHER 		= "Dither";
	public static final String	KEY_SHARE 		= "Share";
	public static final String	KEY_EMBEDED 	= "Embeded";
	
	public static final int RGB_BLACK 		= Color.BLACK.getRGB();
	public static final int RGB_WHITE 		= Color.WHITE.getRGB();

	protected static String[] supportedExtensions = {"jpg", "bmp", "gif", "png"};
	
	public static void main(String[] args) {
		VCSMainFrame eevcsMainFrame = new VCSMainFrame(TITLE);
		eevcsMainFrame.setVisible(true);
	}
}
