package org.mp2dbuilder.viewer;

/*
 * ToolBarDemo.java requires the following addditional files:
 * images/Back24.gif
 * images/Forward24.gif
 * images/Up24.gif
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.io.ReaccsFileEndedException;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.mp2dbuilder.renderer.generators.ReactionCentreGenerator;
import org.mp2dbuilder.renderer.generators.SmartHitsGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class MoleculeViewer extends JPanel implements ActionListener {
	protected static ILoggingTool logger = LoggingToolFactory
			.createLoggingTool(MoleculeViewer.class);
	protected JTextArea textArea;
	protected String newline = "\n";
	static final protected String PREVIOUS = "previous";
	protected JTextArea text;
	final protected JLabel riregNoLabel = new JLabel("null");;
	static final protected String NEXT = "next";
	static final protected String GOTO = "Go";
	static final protected String CANCEL = "cancel";

	protected ReaccsMDLRXNReader reader;
	protected ImagePanel imagePanel;
	protected int currentRireg = 0;

	protected SwingWorker swingWorker = null;

	protected String readerFileName;

	protected MetaboliteHandler metaboliteHandler = new MetaboliteHandler();
	private JButton previousButton;
	public JButton nextButton;
	public JButton cancelButton;
	public JButton goButton;
	public JTextArea logTextArea;
	public JPanel topPanel;

	public MoleculeViewer(ReaccsMDLRXNReader reader, String fileName)
			throws Exception {
		super(new BorderLayout());
		this.reader = reader;
		this.readerFileName = fileName;
		// Create the toolbar.
		JToolBar toolBar = new JToolBar();
		addButtons(toolBar);

		initImagePanel();
		
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(toolBar,BorderLayout.NORTH);
		
		JToolBar optionsBar = new JToolBar();
		addOptions(optionsBar);
		topPanel.add(optionsBar,BorderLayout.SOUTH);
		
		add(topPanel, BorderLayout.NORTH);
		add(imagePanel, BorderLayout.CENTER);

		logTextArea = new JTextArea(5, 20);
		JScrollPane scrollPane = new JScrollPane(logTextArea);
		logTextArea.setEditable(false);
		add(scrollPane, BorderLayout.SOUTH);
	}

	protected void addOptions(JToolBar optionsBar) {
		// TODO Auto-generated method stub
		
	}

	public MoleculeViewer(String fileName) throws Exception {
		this(getReaccsReader(fileName), fileName);
		this.readerFileName = fileName;
	}

	protected void initImagePanel() throws CDKException {
		Image i1 = getImage(null, null, false, null, 3, false);
		Image i2 = getImage(null, null, false, null, 3, false);
		Image i3 = getImage(null, null, false, null, 3, false);
		imagePanel = new ImagePanel(i1, i2, i3);
	}

	protected void tryToReset() throws Exception, IOException {
		try {
			// System.out.println("Resetting");
			reader.reset();
		} catch (Exception e) {
			try {
				reader.close();
			} catch (Exception e1) {

			}
			// System.out.println("failed to reset: Try to create new file reader.");
			this.reader = getReaccsReader(this.readerFileName);
			// System.out.println("Created new file reader.");
		}
	}

	protected IReactionSet getNextReactionSetForRendering()
			throws ReaccsFileEndedException, CDKException {
		return (IReactionSet) reader.read(new NNReactionSet());
	}

	protected void generateImage() throws Exception {
		Image i1 = null;
		Image i2 = null;
		Image i3 = null;

		try {
			IReactionSet reactionSet = getNextReactionSetForRendering();
			Map returnList = metaboliteHandler
					.prepareForTransformation(reactionSet);
			IAtomContainer reactant = (IAtomContainer) returnList.get("reactant");
			IAtomContainer product = (IAtomContainer) returnList.get("product");
			IAtomContainer mcs = (IAtomContainer) returnList.get("mcss");

			i1 = getImage(reactant, mcs, true, product, 3, false);
			i2 = getImage(product, mcs, false, null, 3, false);
			i3 = getImage(mcs, mcs, false, null, 3, false);
		} catch (ReaccsFileEndedException e) {
			i1 = getImage(null, null, false, null, 3, false);
			i2 = getImage(null, null, false, null, 3, false);
			i3 = getImage(null, null, false, null, 3, false);
		}
		imagePanel.setImages(i1, i2, i3);
	}

	protected Image getImage(IAtomContainer atomContainer,
			IAtomContainer mcsContainer, boolean renderReactionCentre,
			IAtomContainer productContainer, int numberOfGraphs,
			boolean drawNumbers)
			throws CDKException {
		Dimension imagePanelDimension = null;
		double imagePanelWidth = 0.0;
		double imagePanelHeight = 0.0;
		int WIDTH = 400;
		int HEIGHT = 400;
		if(imagePanel != null){
			imagePanelDimension = imagePanel.getSize();
			
			imagePanelWidth = imagePanelDimension.getWidth();
			imagePanelHeight = imagePanelDimension.getHeight();
			
			WIDTH = (int)imagePanelWidth / numberOfGraphs;
			HEIGHT = (int) imagePanelHeight;
		}

		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
		Image image = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_INT_RGB);

		if (atomContainer == null) {
			return image;
		}
		
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		IMolecule molecule = atomContainer.getBuilder().newMolecule(
				atomContainer);
		sdg.setMolecule(molecule);
		try {
			sdg.generateCoordinates();
		} catch (Exception e) {
		}
		molecule = sdg.getMolecule();

		// generators make the image elements
		List<IGenerator> generators = new ArrayList<IGenerator>();

		// generators.add(new BasicAtomGenerator());
		// generators.add(new MCSOverlayAtomGenerator(mcsContainer));
		//generators.add(new AtomNumberGenerator());
		generators.add(new ExtendedAtomGenerator());
		generators.add(new RingGenerator());
		if (renderReactionCentre == true) {
			generators.add(new SmartHitsGenerator());
			generators.add(new ReactionCentreGenerator());
		}

		// the renderer needs to have a toolkit-specific font manager
		Renderer renderer = new Renderer(generators, new AWTFontManager());

		renderer.getRenderer2DModel().setDrawNumbers(drawNumbers);
		//renderer.getRenderer2DModel().set .setIsCompact(false);
		// the call to 'setup' only needs to be done on the first paint
		renderer.setup(molecule, drawArea);

		// paint the background
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);

		// the paint method also needs a toolkit-specific renderer
		renderer.paintMolecule(molecule, new AWTDrawVisitor(g2), new Rectangle(
				0, 0, WIDTH, HEIGHT), true);

		// SVGGenerator svgGenerator = new SVGGenerator();
		// renderer.paintMolecule(molecule, svgGenerator, new
		// Rectangle(0,0,WIDTH,HEIGHT),true);
		// logger.info(svgGenerator.getResult());

		return image;
	}

	protected void addButtons(JToolBar toolBar) {
		text = new JTextArea(1, 3);
		JLabel riregNoLabelLabel = new JLabel("current RIREG: ");
		// riregNoLabel = new JLabel("null");
		// first button
		previousButton = makeNavigationButton("Back", PREVIOUS,
				"Back to previous something-or-other", "Previous");
		toolBar.add(previousButton);

		toolBar.add(riregNoLabelLabel);
		toolBar.add(riregNoLabel);

		addTextfields(toolBar);

		// third button
		addGoButton(toolBar);

		cancelButton = makeNavigationButton("Cancel", CANCEL,
				"Cancel current operation", "Cancel");
		cancelButton.setEnabled(false);
		toolBar.add(cancelButton);

		nextButton = makeNavigationButton("Forward", NEXT,
				"Forward to something-or-other", "Next");
		toolBar.add(nextButton);
	}

	protected void addTextfields(JToolBar toolBar) {
		toolBar.add(text);
	}

	protected void addGoButton(JToolBar toolBar) {
		goButton = makeNavigationButton("GO", GOTO,
				"Forward to specific rireg", "Go");
		toolBar.add(goButton);
	}

	protected JButton makeNavigationButton(String imageName,
			String actionCommand, String toolTipText, String altText) {
		// Look for the image.
		String imgLocation = "images/" + imageName + ".gif";
		URL imageURL = MoleculeViewer.class.getResource(imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
		}

		return button;
	}

	public void actionPerformed(ActionEvent e) {
		// if(swingWorker == null){
		swingWorker = new MoleculeViewerWorker(this, e.getActionCommand());
		// }
		swingWorker.execute();
	}
	
	protected void displayResult(String actionDescription) {
		textArea.append(actionDescription + newline);
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	protected static ReaccsMDLRXNReader getReaccsReader(String fileName)
			throws URISyntaxException, IOException {
		InputStream ins = new FileInputStream(fileName);
		if (fileName.endsWith(".gz")) {
			ins = new GZIPInputStream(ins);
		}
		File file = new File(fileName);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		long fileLengthLong = file.length();
		reader.activateReset(1024);
		return reader;
	}

	public static void main(String[] args) throws Exception, IOException {
		String fileName = null;
		if (args == null || (args != null && args.length == 0)) {
			System.out
					.println("syntax: java [-Dcdk.debugging=true|false] -jar mp2Dbuilder-0.0.1-SNAPSHOT.jar <file path, e.g. /tmp/rdffile.rdf>");
			return;
		}
		fileName = args[0];
		logger.info("using file:" + fileName);
		MoleculeViewer gui = new MoleculeViewer(fileName);
		// gui.setRireg(1);
		showGUI(gui, true);
	}

	protected static void showGUI(final MoleculeViewer gui,
			boolean showFirstRireg) {
		try {

			JFrame frame = new JFrame("Reactant - Product - MCS");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(gui);
			frame.pack();
			frame.setVisible(true);
			if (showFirstRireg) {
				gui.nextButton.doClick();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
