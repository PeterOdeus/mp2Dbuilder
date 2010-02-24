package prototyping;

import static org.junit.Assert.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.smiles.smarts.ReactionSmartsQueryTool;
import org.mp2dbuilder.viewer.MoleculeViewer;
import org.mp2dbuilder.viewer.ReactSmartsMoleculeViewer;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.nonotify.NNChemObject;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class ReacSmartsTest {

	private static ILoggingTool logger =  null;//LoggingToolFactory.createLoggingTool(InitialTest.class); // new LoggingTool();

	@BeforeClass public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(InitialTest.class);
		//setSimpleChemObjectReader(new MDLRXNReader(), "data/mdl/reaction-1.rxn");
	}
	
	
	@Test 
	public void testQueryAtomMCS() throws Exception {
		
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
		IMolecule mol1 = sp.parseSmiles("CCCO");
		IMolecule mol2 = sp.parseSmiles("CC=O");
		System.out.println("mol1: " + mol1);
		System.out.println("mol2: " + mol2);
		QueryAtomContainer q2 = ReactionSmartsQueryTool.createSymbolAndAnyBondOrderQueryContainer(mol2);
		QueryAtomContainer q3 = ReactionSmartsQueryTool.createSymbolAndBondOrderQueryContainer(mol2);
		
		List<IAtomContainer> rmaps = UniversalIsomorphismTester.getOverlaps(mol1,q2);
		System.out.println("Any order bonds: " + rmaps.get(0).getAtomCount());
		assertEquals(3, rmaps.get(0).getAtomCount());

		rmaps = UniversalIsomorphismTester.getOverlaps(mol1,q3);
		System.out.println("Order bonds: " + rmaps.get(0).getAtomCount());
		assertEquals(2, rmaps.get(0).getAtomCount());

	}	

	@Test 
	public void testCompareSMILESandFILESmatching() throws Exception {
		
		//The simplest form of hydroxylation.
		//Daylight depict and we return true
		String rsmiles="CCCCC>>CCCCCO";
		assertTrue(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));

		//Write in to file
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
		IMolecule mol1 = sp.parseSmiles("CCCCC");
//		System.out.println("MOL1-smi: " + mol1);
		debugMol(mol1);
		File file=new File("/tmp/sm_in.mdl");
		FileOutputStream fo=new FileOutputStream(file);
		MDLWriter writer=new MDLWriter(fo);
		writer.write(mol1);
		writer.close();

		//Write out to file
		IMolecule mol2 = sp.parseSmiles("CCCCCO");
//		System.out.println("MOL2-smi: " + mol2);
		file=new File("/tmp/sm_out.mdl");
		fo=new FileOutputStream(file);
		writer=new MDLWriter(fo);
		writer.write(mol2);
		writer.close();

		//Read back in from file
		file=new File("/tmp/sm_in.mdl");
		FileInputStream fi=new FileInputStream(file);
		MDLV2000Reader r=new MDLV2000Reader(fi);
		IMolecule mol_in=(IMolecule) r.read(new Molecule());
//		System.out.println("MOL1-mdl: " + mol_in);
//		doAT(mol_in);
//		debugMol(mol_in);

		file=new File("/tmp/sm_out.mdl");
		fi=new FileInputStream(file);
		r=new MDLV2000Reader(fi);
		IMolecule mol_out=(IMolecule) r.read(new Molecule());
//		System.out.println("MOL2-mdl: " + mol_out);
//		doAT(mol_out);
		
		IReaction rr=new Reaction();
		rr.addReactant(mol_in);
		rr.addProduct(mol_out);

		ReactionSmartsQueryTool rq= new ReactionSmartsQueryTool(ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS);
		boolean ret= rq.matches(rr);
		assertTrue(ret);

	}
	



	private void debugMol(IMolecule mol){
		System.out.println("Mol.");
		for (IAtom atom : mol.atoms()){
			System.out.println("   Atom: " + atom.getSymbol() + mol.getAtomNumber(atom) + "-" + atom.getAtomTypeName());
		}
	}
		


	
//	@Test 
	public void testMCSSOverlaps() throws Exception {
		String f = "data/mdl/firstRiReg.rdf";
		ReaccsMDLRXNReader reader = getReaccsReader(f);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IReaction reaction = reactionSet.getReaction(0);
		String reactantQuery = ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS;
		String productQuery = "[*:1]";
		ReactionSmartsQueryTool sqt = new ReactionSmartsQueryTool(reactantQuery,productQuery);
		
		//We also know we only have one reactant and one product
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		Assert.assertEquals(15, reactant.getAtomCount());
		Assert.assertEquals(11, product.getAtomCount());
		assertTrue(sqt.matches(reaction));
	}
	
	@Test 
	public void testMCSSOverlapsForSecondRiReg() throws Exception {
		String f = "data/mdl/2ndRiReg.rdf";
		ReaccsMDLRXNReader reader = getReaccsReader(f);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IReaction reaction = reactionSet.getReaction(0);
		String reactantQuery = ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS;
		String productQuery = "[*:1]";
		ReactionSmartsQueryTool sqt = new ReactionSmartsQueryTool(reactantQuery,productQuery);
		
		//We also know we only have one reactant and one product
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		Assert.assertEquals(15, reactant.getAtomCount());
		Assert.assertEquals(12, product.getAtomCount());
		assertTrue(sqt.matches(reaction));
	}
	

	@Test public void testN_Dealkylation() throws Exception {
		
/* Reference:
http://www.daylight.com/daycgi_tutorials/depictmatch.cgi
SMILES:
CCCN(C)C>>CCN(C)C
CCCCCCCN(C)C>>CCCCCCCNC
CNC(CC=OC)CCCCN(C)C>>CNC(CC=OC)CCCCN(C)C
CNCC(O)CCCN(C)C>>CNCC(O)CCCNC

SMARTS:
[$([CH3][NH0;X3]([CH3])[*])]>>[CH3][NH][*]
*/

		String rsmiles;

//		//No dealkylation, daylight and we return false
		rsmiles="CCCN(C)C>>CCNC";
		assertTrue(isDoubleMatch(rsmiles, 
				ReactionSmartsDefinitions.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactionSmartsDefinitions.N_DEALKYLATION_PRODUCT_SMARTS));
//
//		//This is a dealkylation without any difficulties.
//		//Daylight depict results in hit in subs and product
//		//This should return true, classes do not make difference in this case
		rsmiles="CCN(C)C>>CCNC";
		assertTrue(isDoubleMatch(rsmiles, 
				ReactionSmartsDefinitions.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactionSmartsDefinitions.N_DEALKYLATION_PRODUCT_SMARTS));

		
//		This test has match in substrate and product but not on a conserved N.
//		Daylight depict returns true for [$([CH3][NH0;X3]([CH3])[*])]>>[CH3][NH][*] (No classes)
//		but we should return no matches due to non-conservation.
		rsmiles="CNC(CC=OC)CCCCN(C)C>>CNC(CC=OC)CCCCN(C)C";
		assertFalse(isDoubleMatch(rsmiles, 
				ReactionSmartsDefinitions.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactionSmartsDefinitions.N_DEALKYLATION_PRODUCT_SMARTS));

////		This test has match in substrate and 2 matches in product, but only one is conserved.
////		Daylight depict returns true for [$([CH3][NH0;X3]([CH3])[*])]>>[CH3][NH][*] (No classes)
////		but we should return no matches due to non-conservation.
		rsmiles="CNCC(O)CCCN(C)C>>CNCC(O)CCCNC";
		assertTrue(isDoubleMatch(rsmiles, 
				ReactionSmartsDefinitions.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactionSmartsDefinitions.N_DEALKYLATION_PRODUCT_SMARTS));
		
		
	}

	@Test 
	public void testERROR() throws Exception {
		String rsmiles;

//		//No dealkylation, daylight and we return false
//		rsmiles="CCCN(C)C>>CCNC";
//		assertTrue(isDoubleMatch(rsmiles, 
//				ReactionSmartsDefinitions.N_DEALKYLATION_REACTANT_SMARTS, 
//				ReactionSmartsDefinitions.N_DEALKYLATION_PRODUCT_SMARTS));
		
		rsmiles="CCCCC>>CCCCCO";
		assertTrue(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));

	}
	
	@Test 
	public void testHydroxylation() throws Exception {

		//The simplest form of hydroxylation.
		//Daylight depict and we return true
		String rsmiles="CCCCC>>CCCCCO";
		assertTrue(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));

		//OH added, but via a N hence not on a conserved atom. Should return false.
		rsmiles="CCCCC>>CCCCCNO";
		assertFalse(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));
		
		//Hydroxylation on conserved atom. Should return true.
		rsmiles="C1CCCCC1CCN(CC)CC>>C1CCCCC1CCN(CC)C(O)C";
		assertTrue(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));
		
		//Hydroxylation on both ends of a molecule. Should return true.
		rsmiles="CCCC>>OCCCCO";
		assertTrue(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));
		
		//Hydroxylation on both ends of a molecule but add N so no conservation. Should return false.
		rsmiles="CCCC>>ONCCNO";
		assertFalse(isDoubleMatch(rsmiles, ReactionSmartsDefinitions.HYDROXYLATION_REACTANT_SMARTS, ReactionSmartsDefinitions.HYDROXYLATION_PRODUCT_SMARTS));
		
	}
	

	/**
	 * Parse a reaction smiles and test a ReactionSmarts query for matches in both substrate and product
	 * @param rsmiles
	 * @param reactionQuery
	 * @param productQuery
	 * @return
	 * @throws Exception 
	 */
	private boolean isDoubleMatch(String rsmiles, String reactionQuery,
			String productQuery) throws Exception {

		System.out.println("**************");
		System.out.println("* Testing smiles: " + rsmiles);
		System.out.println("* Testing smarts: " + reactionQuery + ">>" + productQuery);
		System.out.println("**************");

		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
		IReaction reaction = sp.parseReactionSmiles(rsmiles);
		ReactionSmartsQueryTool rq= new ReactionSmartsQueryTool(reactionQuery, productQuery);

		boolean ret= rq.matches(reaction);
		System.out.println("**************");
		System.out.println("* Testing done. Match: " + ret);
		System.out.println("**************");
		return ret; 
	}

	
	
	


	@Test 
	public void testReactSmartsMoleculeViewer() throws Exception {
		String f = "data/mdl/First500DB2005AllFields.rdf"; //"data/mdl/73320thRiReg.rdf";
		ReaccsMDLRXNReader reader = getReaccsReader(f);
		//		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		//		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
		//		IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
		//		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		URL url = this.getClass().getClassLoader().getResource(f);
		File file = new File(url.toURI());
		ReactSmartsMoleculeViewer gui = new ReactSmartsMoleculeViewer(reader, file.getAbsolutePath());
		showGUI(gui);
	}
	
	public void showGUI(final MoleculeViewer gui){
		new Runnable() {
			boolean shouldExit = false;
			public void run() {
				
				try {

					JFrame frame = new JFrame("Reactant - Product - ReacSMARTS");
					frame.addWindowListener(new WindowAdapter()
					{
						public void windowClosing(WindowEvent paramWindowEvent)
						{
							shouldExit = true;
						}
					});
					
					frame.getContentPane().add(gui);
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int i = 0;
				while(shouldExit == false){
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						i++;
					}
				}
			}
		}.run();
		
	}
	
	private ReaccsMDLRXNReader getReaccsReader(String filename) throws URISyntaxException, IOException{
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		long fileLengthLong = file.length();
		reader.activateReset(fileLengthLong);
		return reader;
	}

}
