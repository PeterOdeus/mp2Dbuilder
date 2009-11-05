package prototyping;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import metaprint2d.analyzer.FingerprintGenerator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;


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
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
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

	@Test public void testMCSSingle() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(1, mcsList.size());
	}
	
	@Test public void testMultipleMCS() throws Exception {
		String filename = "data/mdl/24thRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}
	
	@Test public void testMultipleRiRegs() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		for(int i=0;i<=49;i++){
			try{
				reactionSet = (IReactionSet)reader.read(new NNReactionSet());
				IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
				IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			}catch(NullPointerException npe){
				System.out.println(i);
			}catch(java.lang.AssertionError err){
				System.out.println(i);
				throw err;
			}
		}
	}
	
	@Test public void testExtractSingleMCS() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(26);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		IAtomContainer chosenAtomContainer = null;
		int maxCount = 0;
		for(IAtomContainer atoms: mcsList){
			System.out.println(atoms.getAtomCount());
			if(atoms.getAtomCount() > maxCount){
				maxCount = atoms.getAtomCount();
				chosenAtomContainer = atoms;
			}
		}
		Assert.assertEquals(17, chosenAtomContainer.getAtomCount());
	}
	@Test public void testGetMultipleMCSMap() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
//		Map<Integer, List<Integer>> mcsMap = new HashMap<Integer,List<Integer>>();
//		int maxCount = 0;
		StringBuffer buf = new StringBuffer();
		IMolecule product = null;
		IMolecule reactant = null;
		List<IAtomContainer> mcsList = null;
		for(int i = 0; i <= 30; i++){
			reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
//			if(mcsList.size() > 1){
				buf.setLength(0);
				buf.append("Rireg #" + (i + 1) + "\t");
				for(IAtomContainer atoms: mcsList){
					buf.append(" :" + atoms.getAtomCount());
				}
				System.out.println(buf.toString());
//			}			
		}
//		Iterator<Entry<Integer,List<Integer>>> iter = mcsMap.entrySet().iterator(); 
//		;
//		while(iter.hasNext()){
//			
//			Entry<Integer,List<Integer>> entry = iter.next();
//			
//			for(Integer i: entry.getValue()){
//				buf.append(":" + i);
//			}
//			
//		}
	}
	
	@Test public void testFileLength() throws Exception{
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		boolean isLessThanInt = fileLengthLong < Integer.MAX_VALUE;
		Assert.assertEquals(true, isLessThanInt);
	}
	
	@Test public void testSpecificRiReg() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		int fileLengthInt = (int)fileLengthLong;
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}
	
	@Test public void testSpecificRiRegAndPrevious() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(fileLengthLong);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(16, mcsList.get(0).getAtomCount());
		reader.reset();
		reader.setInitialRiregNo(23);
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(14, mcsList.get(0).getAtomCount());
	}
	
	
	@Test public void testSpecificRiRegAndNext() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(1, mcsList.size());
	}

	static boolean shouldExit = false;

	@Test public void testSybylAtomType() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
	    fpGenerator.generateFingerprints(reactant);
	    for(IAtom atom: reactant.atoms()){
	    	logger.debug(atom.getID() + ":" + ((IAtomType)atom).getAtomTypeName());
	    	//Map<String Atomtype("C.sp3","Br"), Integer >
	    }
	    
	    
	}
	
	private void percieveAtomTypesAndConfigureAtoms(IAtomContainer container) throws Exception {
    	SybylAtomTypeMatcher matcher = SybylAtomTypeMatcher.getInstance(container.getBuilder());
        Iterator<IAtom> atoms = container.atoms().iterator();
        while (atoms.hasNext()) {
        	IAtom atom = atoms.next();
        	atom.setAtomTypeName(null);
        	IAtomType matched = matcher.findMatchingAtomType(container, atom);
        	if (matched != null) AtomTypeManipulator.configure(atom, matched);
        }
	}
	
	@Test public void testGUI() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(fileLengthLong);
//		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
//		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
//		IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
//		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		render(reader);
		
	}
	
	public void render(ReaccsMDLRXNReader reader){
		try {
//			ImagePanel panel = new ImagePanel(
//					getImage(reactant, mcs, true), 
//					getImage(product, mcs, false), 
//					getImage(mcs, mcs, false));

			JFrame frame = new JFrame("Reactant - Product - MCS");
			frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent paramWindowEvent)
				{
					shouldExit = true;
				}
			});
			
			frame.getContentPane().add(new ToolBarDemo(reader));
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

	
}


