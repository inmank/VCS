package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class ImageDisplayPane extends JPanel{

	private HashMap<String, BufferedImage> imageMap;
	private JComboBox 	imageNameList = new JComboBox(); 
	private	JLabel	displayArea;

	public ImageDisplayPane(HashMap<String, BufferedImage> mapIn) {
		imageMap = mapIn;

		if (imageMap != null)
			imageNameList = new JComboBox(imageMap.keySet().toArray());
		displayArea = new JLabel();

		displayArea.setBackground(Color.white);
		setLayout(new BorderLayout());
		JScrollPane displayPane = new JScrollPane(displayArea);
		displayPane.setPreferredSize(new Dimension(600, 400));
		displayPane.setAutoscrolls(true);
		add(imageNameList, BorderLayout.PAGE_START);
		add(displayPane, BorderLayout.LINE_START);
		RenderImageInPanel(imageNameList.getSelectedItem());
		
		imageNameList.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent arg0) {
				if(arg0.getStateChange() == ItemEvent.SELECTED) {
					RenderImageInPanel(imageNameList.getSelectedItem());
				}
				
				validate();
				repaint();
			}
		});
	}

	private boolean RenderImageInPanel(Object imageNameIn) {
		BufferedImage br = imageMap.get(imageNameIn);
		boolean isRendered = false;
		
		if (br != null) {
			displayArea.setIcon(new ImageIcon(br));
			isRendered = true;
		}
		return isRendered;
	}
}
 