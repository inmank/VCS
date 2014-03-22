package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import modules.GenerateShares;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class VCSMainFrame extends JFrame{

	private final String[] ACTION_ITEMS = {"Spliting", "Stacking"};
	private JComboBox actionComboBox;
	private JLabel	actionLabel;
	private ImageSelGroup	secretImage;
	private ImageSelGroup	thresholdImage;
	private ImageSelGroup	sharesImage;

	private HashMap<String, BufferedImage>	secretImgMap 	= new HashMap<String, BufferedImage>();
	private HashMap<String, BufferedImage>	shareImgMapInt 	= new HashMap<String, BufferedImage>();
	private HashMap<String, BufferedImage>	thresholdImgMap = new HashMap<String, BufferedImage>();
	private HashMap<String, BufferedImage>	shareImgMapExt 	= new HashMap<String, BufferedImage>();
	private HashMap<String, BufferedImage>	selShareImgMap 	= new HashMap<String, BufferedImage>();

	private VCSMainFrame mainFrame;

	public static final Integer[] K_VALUES = {2, 3};
	public static final Integer[] DITHER_VALUES = {4, 9, 16};
	public static final int N_LIMIT	= 10;

	private JLabel		vcsModeLabel = new JLabel("VCS Mode (k, n) : ");
	private JComboBox	shareKComboBox	= new JComboBox(K_VALUES);
	private JComboBox	shareNComboBox	= new JComboBox();

	//private JLabel 	ditherModeLabel	= new JLabel("Dither Matrix Level : ");
	//private JComboBox	ditherComboBox	= new JComboBox(DITHER_VALUES);

	private JLabel 		outPathLabel	= new JLabel("Output Path : ");
	private JTextField	outPathField	= new JTextField(20);
	private JButton		outPathButton;

	private JButton		splitButton;
	private JButton		stackButton;
	private JButton 	secretImgButton;
	private JButton 	shareImgButton;
	private JButton 	clearShareButton;

	private JCheckBox[]	shareCheckBoxes		= new JCheckBox[9];
	private JLabel		outImageArea		= new JLabel();
	private JCheckBox	useInternalShare	= new JCheckBox("Use Internal Shares Map");
	private JCheckBox	checkAllShares		= new JCheckBox("Check All");

	private JPanel 	splitPanel;
	private JPanel 	stackPanel;

	/**
	 * Constructor
	 * @param titleIn = Title for the Application
	 */
	public VCSMainFrame(String titleIn){
		mainFrame = this;
		setTitle(titleIn);
		try {
			for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(laf.getName())) {
					UIManager.setLookAndFeel(laf.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to Set Nimbus Look and Feel");
		}
		addComponents();
	}

	/**
	 * This method will return the corresponding Image map for given title.
	 * @param keyIn = the type of the Image map
	 * @return
	 */
	public HashMap<String, BufferedImage> getImageMap(String keyIn) {

		HashMap<String, BufferedImage> retValue = null;

		if (VCSMain.KEY_SECRET.equals(keyIn))
			retValue = secretImgMap;
		else if (VCSMain.KEY_THRESHOLD.equals(keyIn))
			retValue = thresholdImgMap;
		else if (VCSMain.KEY_SHARE.equals(keyIn))
			retValue = shareImgMapInt;

		return retValue;
	}

	/**
	 * To add and render the GUI components.
	 */
	public void addComponents() {
		actionLabel 	= new JLabel("Select Action: ");
		actionComboBox 	= new JComboBox(ACTION_ITEMS);

		secretImage 	= new ImageSelGroup(VCSMain.KEY_SECRET, this);
		thresholdImage	= new ImageSelGroup(VCSMain.KEY_THRESHOLD, this);
		sharesImage		= new ImageSelGroup(VCSMain.KEY_SHARE, this);

		splitButton 		= new JButton(new SplittingAction());
		stackButton 		= new JButton(new StackingAction());
		secretImgButton		= new JButton(new SelectionAction("Select Secret Image", true, false));
		shareImgButton  	= new JButton(new SelectionAction("Select Share Images", true, true));
		clearShareButton 	= new JButton(new ClearShareAction());
		outPathButton		= new JButton(new SelectionAction("Browse", false, false));

		actionComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String mode = (String) e.getItem();
					if (mode.equals("Spliting")) {
						splitPanel.setVisible(true);
						stackPanel.setVisible(false);
					} else {
						outImageArea.setBackground(Color.white);
						stackPanel.setVisible(true);
						splitPanel.setVisible(false);

						if (useInternalShare.isSelected())
							selShareImgMap = shareImgMapInt;
						else 
							selShareImgMap = shareImgMapExt;

						updateShareCheckBox(selShareImgMap);
					}
					mainFrame.pack();
					mainFrame.validate();
					mainFrame.repaint();
				}
			}
		});

		useInternalShare.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					selShareImgMap = shareImgMapInt;
					clearShareButton.setEnabled(false);
				} else {
					selShareImgMap = shareImgMapExt;
					clearShareButton.setEnabled(true);
				}
				updateShareCheckBox(selShareImgMap);
			}
		});

		checkAllShares.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					for (int i=0; i < shareCheckBoxes.length; i++) {
						if (shareCheckBoxes[i].isEnabled()) {
							shareCheckBoxes[i].setSelected(true);
						}
					}
				} else {
					for (int i=0; i < shareCheckBoxes.length; i++) {
						if (shareCheckBoxes[i].isEnabled()) {
							shareCheckBoxes[i].setSelected(false);
						}
					}
				}
			}
		});

		JPanel 	sharePanel = new JPanel();
		sharePanel.add(useInternalShare);
		sharePanel.add(checkAllShares);

		JPanel	shareCheckBoxPanel	= new JPanel();
		for (int j=0; j < shareCheckBoxes.length; j++) {
			shareCheckBoxes[j] = new JCheckBox(VCSMain.KEY_SHARE + (j+1));
			shareCheckBoxPanel.add(shareCheckBoxes[j]);
		}

		JScrollPane outImagePane = new JScrollPane(outImageArea);
		outImagePane.setPreferredSize(new Dimension(600, 400));

		JPanel	vcsPanel = new JPanel();
		vcsPanel.add(shareKComboBox);
		vcsPanel.add(shareNComboBox);

		//JPanel	ditherPanel = new JPanel();
		//ditherPanel.add(ditherComboBox);

		JPanel	outPathPanel = new JPanel();
		outPathPanel.add(outPathField);
		outPathPanel.add(outPathButton);

		GridBagConstraints gbc = new GridBagConstraints();

		JPanel actionPanel = new JPanel();
		actionPanel.add(actionLabel);
		actionPanel.add(actionComboBox);

		JPanel splitButtonPanel = new JPanel();
		splitButtonPanel.add(splitButton);
		splitButtonPanel.add(secretImgButton);

		JPanel stackButtonPanel = new JPanel();
		stackButtonPanel.add(stackButton);
		stackButtonPanel.add(shareImgButton);
		stackButtonPanel.add(clearShareButton);

		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(new TitledBorder("Settings"));
		settingsPanel.setLayout(new GridBagLayout());
		settingsPanel.add(vcsModeLabel, gbc);

		settingsPanel.add(vcsPanel, gbc);
		gbc.gridy++;
		//settingsPanel.add(ditherModeLabel, gbc);
		//settingsPanel.add(ditherPanel, gbc);
		//gbc.gridy++;
		settingsPanel.add(outPathLabel, gbc);
		settingsPanel.add(outPathPanel, gbc);
		gbc.gridy++;

		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = 0;
		JPanel 	imagePanel = new JPanel();
		imagePanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		imagePanel.add(secretImage, gbc);
		gbc.gridx++;
		imagePanel.add(thresholdImage, gbc);
		gbc.gridx++;
		imagePanel.add(sharesImage, gbc);

		//Splitting Panel
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		splitPanel = new JPanel();
		splitPanel.setBorder(new TitledBorder("Splitting"));
		splitPanel.setLayout(new GridBagLayout());
		splitPanel.add(imagePanel, gbc);
		splitPanel.add(settingsPanel, gbc);
		splitPanel.add(splitButtonPanel, gbc);

		//Stacking Panel
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		stackPanel = new JPanel();
		stackPanel.setBorder(new TitledBorder("Stacking"));
		stackPanel.setLayout(new GridBagLayout());
		stackPanel.add(sharePanel, gbc);
		stackPanel.add(shareCheckBoxPanel, gbc);
		stackPanel.add(outImagePane, gbc);
		stackPanel.add(stackButtonPanel, gbc);

		//Main Panel
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.add(actionPanel, gbc);
		mainPanel.add(splitPanel, gbc);
		mainPanel.add(stackPanel, gbc);


		try {
			initialize();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		JScrollPane mainPane = new JScrollPane(mainPanel);
		mainPane.setAutoscrolls(true);
		getContentPane().add(mainPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(150, 50);
		pack();
		validate();
	}

	/**
	 * Set default values to the components in the screen.
	 * @throws IOException
	 */
	void initialize() throws IOException {
		for (int i=2; i<N_LIMIT; i++) {
			shareNComboBox.addItem(i);
		}

		actionComboBox.setSelectedIndex(0);

		selShareImgMap = shareImgMapExt;
		updateShareCheckBox(selShareImgMap);

		String mode = (String) actionComboBox.getSelectedItem();
		if (mode.equals("Spliting")) {
			stackPanel.setVisible(false);
		} else {
			splitPanel.setVisible(false);
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime()));

		outPathField.setText(new File("").getAbsolutePath());
		updateUI(); 
	}

	/**
	 * Save the selected and generated images, 
	 * to the user specified output directory.
	 */
	private void writeImages() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

		String imgPath = outPathField.getText() + File.separator + dateFormat.format(Calendar.getInstance().getTime());
		String name = "Image";
		String format = "jpg";

		if (imgPath.isEmpty() || !new File(imgPath).mkdir())
			imgPath = ".";

		try {
			if (secretImgMap.size() > 0) {
				ImageIO.write(secretImgMap.get(VCSMain.KEY_SECRET), format, getFileStream(imgPath, name + "_In", format));
			}

			if (thresholdImgMap.size() > 0) {
				ImageIO.write(thresholdImgMap.get(VCSMain.KEY_THRESHOLD), format, getFileStream(imgPath, name + "_trh", format));
			}

			if (shareImgMapInt.size() > 0) {
				Iterator<String> keyItr = shareImgMapInt.keySet().iterator();
				int c = 0;
				while (keyItr.hasNext()) {
					ImageIO.write(shareImgMapInt.get(keyItr.next()), format, getFileStream(imgPath, name + "_Share"+c, format));
					c++;
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save the reconstructed output image to the use specified directory.
	 * @param outImage = Image to be saved.
	 */
	private void writeOutImage(BufferedImage outImage) {
		String imgPath = outPathField.getText();
		String name = "Image";
		String format = "jpg";

		if (imgPath.isEmpty())
			imgPath = ".";

		try {
			if (outImage != null) {
				ImageIO.write(outImage, format, getFileStream(imgPath, name + "_Out", format));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To get the OutStream for the selected image file.
	 * @param path = Location of the Image file
	 * @param name = Name of the Image file
	 * @param format = Image file format(jpg, gif, bmp)
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private ImageOutputStream getFileStream(String path, String name, String format) throws FileNotFoundException, IOException {
		String fullName = path + "\\" + name + "." + format;
		FileImageOutputStream fr = new FileImageOutputStream(new File(fullName));
		return fr;
	}

	/**
	 * To refresh the Image selection group values after Generate action.
	 */
	private void updateUI() {
		secretImage.initialize();
		thresholdImage.initialize();
		sharesImage.initialize();
	}

	/**
	 * Action to generate the shares.
	 * 
	 */
	private class StackingAction extends AbstractAction {

		public StackingAction() {
			super("Reconstruct");
		}

		public void actionPerformed(ActionEvent arg0) {
			ArrayList<BufferedImage> gridImageList = new ArrayList<BufferedImage>();

			for (int i=0; i < shareCheckBoxes.length; i++) {
				if (shareCheckBoxes[i].isSelected()) {
					String key = shareCheckBoxes[i].getText().replace(" ", "");
					gridImageList.add(selShareImgMap.get(key));
				}
			}

			if (!isValidForProcessing(gridImageList))
				return;

			int width 	= gridImageList.get(0).getWidth();
			int height 	= gridImageList.get(0).getHeight();

			BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

			for (int w=0; w < width; w++) {
				for (int h=0; h < height; h++) {
					if (isTransparent(w, h, gridImageList)) {
						outImage.setRGB(w, h, VCSMain.RGB_WHITE);	
					} else {
						outImage.setRGB(w, h, VCSMain.RGB_BLACK);
					}
				}
			}

			writeOutImage(outImage);
			outImageArea.setIcon(new ImageIcon(outImage));
			mainFrame.validate();
			mainFrame.repaint();
		}

		private boolean isTransparent(int width, int height, ArrayList<BufferedImage> list) {
			boolean retValue = false;
			int countWhite = 0;
			for (BufferedImage gridImage: list) {
				int rgbValue = gridImage.getRGB(width, height);
				if (rgbValue == VCSMain.RGB_WHITE)
					countWhite++;
			}

			if (countWhite == 1)
				retValue = false;
			else
				retValue = true;

			return retValue;
		}
	}

	/**
	 * Action to Reconstruct the output from the selected shares. 
	 *
	 */
	private class SplittingAction extends AbstractAction {

		public SplittingAction() {
			super("Generate");
		}

		public void actionPerformed(ActionEvent arg0) {
			int k = (Integer) shareKComboBox.getSelectedItem();
			int n = (Integer) shareNComboBox.getSelectedItem();
			if (!isValidForProcessing(null))
				return;

			thresholdImgMap.clear();
			shareImgMapInt.clear();
			
			GenerateShares genShare = new GenerateShares(k, n, secretImgMap.get(VCSMain.KEY_SECRET), mainFrame);
			genShare.process();

			updateUI();
			writeImages();
			JOptionPane.showMessageDialog(mainFrame, "Share generation completed.");
		}
	}

	/**
	 * General Selection Action for Buttons used.
	 * The process can be varied by checking the action command.
	 *
	 */
	private class SelectionAction extends AbstractAction {
		boolean isFileMode;
		boolean isMultiSelect;

		public SelectionAction(String nameIn, boolean fileMode, boolean multiSelect) {
			super(nameIn);
			isMultiSelect 	= multiSelect;
			isFileMode 		= fileMode;
		}

		public void actionPerformed(ActionEvent arg0) {
			String selAction = ((JButton) arg0.getSource()).getActionCommand();
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(isMultiSelect);
			if (!isFileMode) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			} else {
				chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
				chooser.setFileFilter(new FileFilter() {

					public String getDescription() {
						return "Select Image Files Only";
					}

					public boolean accept(File fileIn) {
						return (fileIn.isDirectory() 
								|| FilenameUtils.isExtension(fileIn.getName().toLowerCase(), VCSMain.supportedExtensions));
					}
				});
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			}

			if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
				File selFile = chooser.getSelectedFile();
				if ("Browse".equals(selAction) && !isFileMode) {
					outPathField.setText(selFile.getAbsolutePath());
				} else if ("Select Secret Image".equals(selAction)) {

					BufferedImage brIn;
					try {
						brIn = ImageIO.read(selFile);
					} catch (IOException ioe) {
						System.out.println(ioe.getMessage());
						JOptionPane.showMessageDialog(mainFrame, "Please select a valid Image file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if (brIn == null) {
						JOptionPane.showMessageDialog(mainFrame, "Please select a valid Image file.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					secretImgMap.put(VCSMain.KEY_SECRET, brIn);
					updateUI();
				} else if ("Select Share Images".equals(selAction)) {
					File[] selFiles = chooser.getSelectedFiles();
					for (File file: selFiles) {
						BufferedImage brIn;
						try {
							brIn = ImageIO.read(file);
						} catch (IOException ioe) {
							System.out.println(ioe.getMessage());
							JOptionPane.showMessageDialog(mainFrame, "Unable to read Share Image " + file.getName() , "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}

						if (brIn == null) {
							JOptionPane.showMessageDialog(mainFrame, "Please select a valid Image file.", "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}

						String key = VCSMain.KEY_SHARE + (shareImgMapExt.size()+1);
						shareImgMapExt.put(key, getThresholdImage(brIn));
					}
					updateShareCheckBox(shareImgMapExt);
				}
			}
		}
	}

	private BufferedImage getThresholdImage(BufferedImage brIn) {
		int width 	= brIn.getWidth();
		int height 	= brIn.getHeight();
		BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		outImage.createGraphics().drawImage(brIn, 0, 0, null);

		return outImage;
	}

	public void updateShareCheckBox(HashMap<String, BufferedImage> imageMap) {
		for (int i=0; i < shareCheckBoxes.length; i++) {
			if (i  < imageMap.size()) {
				shareCheckBoxes[i].setEnabled(true);
			} else {
				checkAllShares.setSelected(false);
				shareCheckBoxes[i].setSelected(false);
				shareCheckBoxes[i].setEnabled(false);
			}
		}
	}

	private class ClearShareAction extends AbstractAction {

		public ClearShareAction() {
			super("Clear Share Images");
		}

		public void actionPerformed(ActionEvent arg0) {
			shareImgMapExt.clear();
			updateShareCheckBox(shareImgMapExt);
		}
	}

	public boolean isValidForProcessing(ArrayList<BufferedImage> gridImageList) {
		int k = (Integer) shareKComboBox.getSelectedItem();
		int n = (Integer) shareNComboBox.getSelectedItem();

		File outPath = new File(outPathField.getText());

		if (gridImageList == null) {
			if (secretImgMap.size() == 0) {
				JOptionPane.showMessageDialog(this, "Please Select Secret Image", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if (k > n) {
				JOptionPane.showMessageDialog(this, "VCS Mode Minimum Share (k) should be less tha or equal Total Share (n)", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if (!outPath.isDirectory()) {
				JOptionPane.showMessageDialog(this, "Please select valid output Directory", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

		} else {
			if (gridImageList.size() == 0) {
				JOptionPane.showMessageDialog(mainFrame, "Please Select Minimum Shares for Reconstruction", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		return true;
	}
}
