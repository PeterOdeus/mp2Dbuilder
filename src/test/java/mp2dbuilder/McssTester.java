package mp2dbuilder;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class McssTester {

	private static ILoggingTool logger = null;

	@BeforeClass
	public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(McssTester.class);
	}
	
	@Test
	public void testMCSSingle() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet) reader
				.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertEquals(1, mcsList.size());
		Assert.assertEquals(9, mcsList.get(0).getAtomCount());
	}
	
	@Test
	public void testMultipleMCS() throws Exception {
		String filename = "data/mdl/24thRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}
	
	@Test
	public void testExtractSingleMCS() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(26);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		IAtomContainer chosenAtomContainer = null;
		int maxCount = 0;
		for (IAtomContainer atoms : mcsList) {
			System.out.println(atoms.getAtomCount());
			if (atoms.getAtomCount() > maxCount) {
				maxCount = atoms.getAtomCount();
				chosenAtomContainer = atoms;
			}
		}
		Assert.assertEquals(17, chosenAtomContainer.getAtomCount());
	}

	@Test
	public void testGetMultipleMCSMap() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		StringBuffer buf = new StringBuffer();
		IMolecule product = null;
		IMolecule reactant = null;
		List<IAtomContainer> mcsList = null;
		for (int i = 0; i <= 30; i++) {
			reactionSet = (IReactionSet) reader.read(new NNReactionSet());
			reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
			buf.setLength(0);
			buf.append("Rireg #" + (i + 1) + "\t");
			for (IAtomContainer atoms : mcsList) {
				buf.append(" :" + atoms.getAtomCount());
			}
			System.out.println(buf.toString());
		}	
	}
	
	@Test
	public void testSpecificRiReg() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		int fileLengthInt = (int) fileLengthLong;
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}

	@Test
	public void testSpecificRiRegAndPrevious() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(fileLengthLong);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertEquals(16, mcsList.get(0).getAtomCount());
		reader.reset();
		reader.setInitialRiregNo(23);
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(14, mcsList.get(0).getAtomCount());
	}

	@Test
	public void testSpecificRiRegAndNext() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertEquals(2, mcsList.size());
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(1, mcsList.size());
	}
	
	@Test
	public void testAbsentAtomId() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(2);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(
				reactant, product);
		Assert.assertNull(mcsList.get(0).getAtom(10).getID());
	}
}
