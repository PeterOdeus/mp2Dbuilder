import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.MDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.templates.MoleculeFactory;
import org.openscience.cdk.tools.LoggingTool;


public class InitialTest {

	private static LoggingTool logger;

	@BeforeClass public static void setup() {
		logger = new LoggingTool(InitialTest.class);
		//setSimpleChemObjectReader(new MDLRXNReader(), "data/mdl/reaction-1.rxn");
	}

	@Test public void testRDFReactioniSet() throws Exception {
		//String filename = "data/mdl/qsar-reaction-test.rdf";
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		MDLRXNReader reader = new MDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		Assert.assertNotNull(reactionSet);


		Assert.assertEquals(1, reactionSet.getReactionCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactantCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactants().getMoleculeCount());
		IMolecule molecule = reactionSet.getReaction(0).getReactants().getMolecule(0);
		Assert.assertEquals(15, molecule.getAtomCount());
		IAtom atom = molecule.getAtom(0);
		//Assert.assertEquals(2, reactionSet.getReaction(0).getReactants().getMolecule(1).getAtomCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getProductCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getProducts().getMoleculeCount());
		Assert.assertEquals(11, reactionSet.getReaction(0).getProducts().getMolecule(0).getAtomCount());
		/*Assert.assertEquals(2, reactionSet.getReaction(0).getProducts().getMolecule(1).getAtomCount());


        Assert.assertEquals(1, reactionSet.getReaction(1).getReactantCount());
        Assert.assertEquals(3, reactionSet.getReaction(1).getReactants().getMolecule(0).getAtomCount());
        Assert.assertEquals(1, reactionSet.getReaction(1).getProductCount());
        Assert.assertEquals(2, reactionSet.getReaction(1).getProducts().getMolecule(0).getAtomCount());*/

	}

	@Test public void testMCS() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		MDLRXNReader reader = new MDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		int i = 0;
	}

	static boolean shouldExit = false;

	@Test public void testFingerprint() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		MDLRXNReader reader = new MDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		FingerprintGenerator generator = new FingerprintGenerator();
		List<Fingerprint> fingerprintList = generator.generateFingerprints(reactant);

		render();
		
	}
	
	public void render(){
		try {
			ImagePanel panel = new ImagePanel(getImage(), getImage());

			JFrame frame = new JFrame("Reactant - Product");
			frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent paramWindowEvent)
				{
					shouldExit = true;
				}
			});
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0;
		while(shouldExit == false){
			try{
				Thread.currentThread().sleep(1000);
			}catch(Exception e){
				i++;
			}
		}
	}

	private Image getImage() throws Exception {
		int WIDTH = 500;
		int HEIGHT = 500;

		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
		Image image = new BufferedImage(
				WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		// any molecule will do
		IMolecule triazole = MoleculeFactory.make123Triazole();
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(triazole);
		try {
			sdg.generateCoordinates();
		} catch (Exception e) { }
		triazole = sdg.getMolecule();


		// generators make the image elements
		List<IGenerator> generators = new ArrayList<IGenerator>();
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());

		// the renderer needs to have a toolkit-specific font manager 
		Renderer renderer = new Renderer(generators, new AWTFontManager());

		// the call to 'setup' only needs to be done on the first paint
		renderer.setup(triazole, drawArea);

		// paint the background
		Graphics2D g2 = (Graphics2D)image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);

		// the paint method also needs a toolkit-specific renderer
		renderer.paintMolecule(triazole, new AWTDrawVisitor(g2), new Rectangle(0,0,500,500),true);

		return image;
	}
}

class ImagePanel extends JPanel {

	private Image img;
	private Image img2;

	public ImagePanel(Image img, Image img2) {
		this.img = img;
		this.img2 = img2;
		Dimension size = new Dimension(img.getWidth(null)*2, img.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
		g.drawImage(img2, img.getWidth(null), 0, null);
	}

}
