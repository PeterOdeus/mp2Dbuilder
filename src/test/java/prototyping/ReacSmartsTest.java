package prototyping;

import static org.junit.Assert.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.JFrame;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.viewer.FilteringMoleculeViewer;
import org.mp2dbuilder.viewer.MoleculeViewer;
import org.mp2dbuilder.viewer.ReactSmartsMoleculeViewer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.ReactionSmartsQueryTool;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class ReacSmartsTest {

	private static ILoggingTool logger =  null;//LoggingToolFactory.createLoggingTool(InitialTest.class); // new LoggingTool();

	

	//Our hydroxylation smarts definition
	private static String HYDROXYLATION_REACTANT_SMARTS="[$([*:1])]";
	private static String HYDROXYLATION_PRODUCT_SMARTS="[*:1][OH]";
	
	@BeforeClass public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(InitialTest.class);
		//setSimpleChemObjectReader(new MDLRXNReader(), "data/mdl/reaction-1.rxn");
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
		rsmiles="CCCN(C)C>>CCN(C)C";
		assertFalse(isDoubleMatch(rsmiles, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_PRODUCT_SMARTS));
//
//		//This is a dealkylation without any difficulties.
//		//Daylight depict results in hit in subs and product
//		//This should return true, classes do not make difference in this case
		rsmiles="CCCCCCCN(C)C>>CCCCCCCNC";
		assertTrue(isDoubleMatch(rsmiles, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_PRODUCT_SMARTS));

		
//		This test has match in substrate and product but not on a conserved N.
//		Daylight depict returns true for [$([CH3][NH0;X3]([CH3])[*])]>>[CH3][NH][*] (No classes)
//		but we should return no matches due to non-conservation.
		rsmiles="CNC(CC=OC)CCCCN(C)C>>CNC(CC=OC)CCCCN(C)C";
		assertFalse(isDoubleMatch(rsmiles, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_PRODUCT_SMARTS));

////		This test has match in substrate and 2 matches in product, but only one is conserved.
////		Daylight depict returns true for [$([CH3][NH0;X3]([CH3])[*])]>>[CH3][NH][*] (No classes)
////		but we should return no matches due to non-conservation.
		rsmiles="CNCC(O)CCCN(C)C>>CNCC(O)CCCNC";
		assertTrue(isDoubleMatch(rsmiles, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_REACTANT_SMARTS, 
				ReactSmartsMoleculeViewer.N_DEALKYLATION_PRODUCT_SMARTS));
		
		
	}
	
//	@Test 
	public void testHydroxylation() throws Exception {

		//The simplest form of hydroxylation.
		//Daylight depict and we return true
		String rsmiles="CCCCC>>CCCCCO";
		assertTrue(isDoubleMatch(rsmiles, HYDROXYLATION_REACTANT_SMARTS, HYDROXYLATION_PRODUCT_SMARTS));
		
		//TODO: add example to test conservation
	}
	

	/**
	 * Parse a reaction smiles and test a ReactionSmarts query for matches in both substrate and product
	 * @param rsmiles
	 * @param reactionQuery
	 * @param productQuery
	 * @return
	 * @throws CDKException 
	 */
	private boolean isDoubleMatch(String rsmiles, String reactionQuery,
			String productQuery) throws CDKException {

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
		String f = "data/mdl/First500DB2005AllFields.rdf";
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
