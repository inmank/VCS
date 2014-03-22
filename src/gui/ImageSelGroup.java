package gui;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Class: ImageSelGroup
 * This is a custom GUI component(Panel). It contains a button to launch the Image display panel,
 * and displays current availability of the images.
 *
 */
@SuppressWarnings("serial")
public class ImageSelGroup extends JPanel {

	private JButton imageButton;
	private JLabel	imgNameLabel;

	private HashMap<String, BufferedImage>	imageMap;
	private VCSMainFrame mainFrame;

	private JDialog	imageDisplayDialog;
	private String key;
	
	public ImageSelGroup(String titleIn, VCSMainFrame frameIn) {
		mainFrame 	= frameIn;
		key = titleIn;

		imgNameLabel = new JLabel();
		imageButton  = new JButton(new LaunchImageAction(new ImageIcon("resources\\imageIcon.gif")));

		setBorder(new TitledBorder(titleIn));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(imageButton);
		add(Box.createVerticalStrut(5));
		add(imgNameLabel);
		add(Box.createVerticalStrut(5));
	}

	public void initialize() {
		imageMap = mainFrame.getImageMap(key);
		
		if (imageDisplayDialog != null && !imageDisplayDialog.isVisible()) {
			imageDisplayDialog = null;
		}
		
		if (imageMap.size() > 0)
			imgNameLabel.setText("Available");
		else
			imgNameLabel.setText("Not Available");
	}

	private class LaunchImageAction extends AbstractAction {
		public LaunchImageAction(ImageIcon imageIcon) {
			super("", imageIcon);
		}

		public void actionPerformed(ActionEvent arg0) {
			if (imageMap.size() == 0)
				return;

			if (imageDisplayDialog == null) {
				imageDisplayDialog = new JDialog(mainFrame);
				imageDisplayDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			} 

			if (!imageDisplayDialog.isVisible()) {
				imageDisplayDialog.add(new ImageDisplayPane(imageMap));
				imageDisplayDialog.setTitle("Image Display Pane");
				imageDisplayDialog.pack();
				imageDisplayDialog.setVisible(true);
			} else {
				imageDisplayDialog.setVisible(true);
			}
		}
	}
}
