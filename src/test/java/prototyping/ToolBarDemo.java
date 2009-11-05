package prototyping;



/*
 * ToolBarDemo.java requires the following addditional files:
 * images/Back24.gif
 * images/Forward24.gif
 * images/Up24.gif
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.mp2dbuilder.renderer.generators.MCSOverlayAtomGenerator;
import org.mp2dbuilder.renderer.generators.ReactionCentreGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

public class ToolBarDemo extends JPanel
                         implements ActionListener {
    protected JTextArea textArea;
    protected String newline = "\n";
    static final private String PREVIOUS = "previous";
    private JTextArea text;
    static final private String NEXT = "next";
    
    private ReaccsMDLRXNReader reader;
    private ImagePanel imagePanel;
    private int currentRireg = 1;

    public ToolBarDemo(ReaccsMDLRXNReader reader) throws CDKException {
        super(new BorderLayout());
        this.reader = reader;
        reader.setInitialRiregNo(currentRireg);
        //Create the toolbar.
        JToolBar toolBar = new JToolBar("Still draggable");
        addButtons(toolBar);
       
        generateImage();
        
        add(toolBar, BorderLayout.PAGE_START);
        add(imagePanel, BorderLayout.CENTER);
    }
    
    private void setRireg(int targetRireg) throws IOException, CDKException{
    	if(targetRireg <= currentRireg){
    		reader.reset();
    	}
    	currentRireg = targetRireg;
    	if(currentRireg < 1){
    		currentRireg = 1;
    	}
    	this.text.setText(this.currentRireg + "");
    	reader.setInitialRiregNo(currentRireg);
    	this.generateImage();
    	this.repaint();
    }
    
    private void generateImage() throws CDKException{
    	Image i1 = null;
    	Image i2 = null;
    	Image i3 = null;
    	
    	try{
	    	IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
			IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
			List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
			IAtomContainer mcs = getFirstMCSHavingMostAtoms(mcsList);
			i1 = getImage(reactant, mcs, true);
			i2 = getImage(product, mcs, false);
			i3 = getImage(mcs, mcs, false);
    	} catch(ReaccsFileEndedException e){
    		i1 = getImage(null,null,false);
    		i2 = getImage(null,null,false);
    		i3 = getImage(null,null,false);
    	}
		if(imagePanel == null){
			imagePanel = new ImagePanel(i1,i2,i3);
		}else{
			imagePanel.setImages(i1	,i2,i3);
					
		}
    }
    
    private IAtomContainer getFirstMCSHavingMostAtoms(List<IAtomContainer> mcsList){
    	IAtomContainer chosenAtomContainer = null;
		int maxCount = 0;
		for(IAtomContainer atoms: mcsList){
			System.out.println(atoms.getAtomCount());
			if(atoms.getAtomCount() > maxCount){
				maxCount = atoms.getAtomCount();
				chosenAtomContainer = atoms;
			}
		}
		return chosenAtomContainer;
    }
    
    private Image getImage(IAtomContainer atomContainer, 
			IAtomContainer mcsContainer,
			boolean renderReactionCentre) throws CDKException {
		int WIDTH = 400;
		int HEIGHT = 500;

		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
		Image image = new BufferedImage(
				WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		if(atomContainer == null){
			return image;
		}

		// any molecule will do
		//IMolecule theMolecule = MoleculeFactory.make123Triazole();
		
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		IMolecule molecule = atomContainer.getBuilder().newMolecule(atomContainer);
		sdg.setMolecule(molecule);
		try {
			sdg.generateCoordinates();
		} catch (Exception e) { }
		molecule = sdg.getMolecule();
		

		// generators make the image elements
		List<IGenerator> generators = new ArrayList<IGenerator>();
		
		//generators.add(new BasicAtomGenerator());
		generators.add(new MCSOverlayAtomGenerator(mcsContainer));
		generators.add(new RingGenerator());
		if(renderReactionCentre == true){
			generators.add(new ReactionCentreGenerator(mcsContainer));
		}
		
		//generators.add(new AtomNumberGenerator());

		// the renderer needs to have a toolkit-specific font manager 
		Renderer renderer = new Renderer(generators, new AWTFontManager());

		//renderer.getRenderer2DModel().setDrawNumbers(true);
		//renderer.getRenderer2DModel().setIsCompact(true);
		// the call to 'setup' only needs to be done on the first paint
		renderer.setup(molecule, drawArea);

		// paint the background
		Graphics2D g2 = (Graphics2D)image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);

		// the paint method also needs a toolkit-specific renderer
		renderer.paintMolecule(molecule, new AWTDrawVisitor(g2), new Rectangle(0,0,400,500),true);

		return image;
	}

    protected void addButtons(JToolBar toolBar) {
        JButton button = null;
        text = new JTextArea(1,3);

        //first button
        button = makeNavigationButton("Back24", PREVIOUS,
                                      "Back to previous something-or-other",
                                      "Previous");
        toolBar.add(button);

        
        toolBar.add(text);

        //third button
        button = makeNavigationButton("Forward24", NEXT,
                                      "Forward to something-or-other",
                                      "Next");
        toolBar.add(button);
    }

    protected JButton makeNavigationButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        //Look for the image.
        String imgLocation = "images/"
                             + imageName
                             + ".gif";
        URL imageURL = ToolBarDemo.class.getResource(imgLocation);
       
        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        if (imageURL != null) {                      //image found
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {                                     //no image found
            button.setText(altText);
            System.err.println("Resource not found: "
                               + imgLocation);
        }

        return button;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        String description = null;

        // Handle each button.
        try{
	        if (PREVIOUS.equals(cmd)) { //first button clicked
	            this.setRireg(this.currentRireg - 1);
	        } else if (NEXT.equals(cmd)) { // third button clicked
	        	this.setRireg(this.currentRireg + 1);
	        }
        }
        catch (Exception e1) {
			throw new RuntimeException(e1);
		}
    }

    protected void displayResult(String actionDescription) {
        textArea.append(actionDescription + newline);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

//    /**
//     * Create the GUI and show it.  For thread safety,
//     * this method should be invoked from the
//     * event dispatch thread.
//     */
//    private static void createAndShowGUI() {
//        //Create and set up the window.
//        JFrame frame = new JFrame("ToolBarDemo");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        //Add content to the window.
//        frame.add(new ToolBarDemo());
//
//        //Display the window.
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    public static void main(String[] args) {
//        //Schedule a job for the event dispatch thread:
//        //creating and showing this application's GUI.
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                //Turn off metal's use of bold fonts
//	        UIManager.put("swing.boldMetal", Boolean.FALSE);
//	        createAndShowGUI();
//            }
//        });
//    }
}

