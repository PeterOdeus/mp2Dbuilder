package prototyping;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import metaprint2d.Constants;
import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;
import metaprint2d.analyzer.data.AtomData;
import metaprint2d.builder.DataBuilderApp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.mp2dbuilder.mcss.AtomMapperUtil;
import org.mp2dbuilder.viewer.MoleculeViewer;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

@SuppressWarnings("unused")
public class InitialTest {

	private static ILoggingTool logger = null;

	@BeforeClass
	public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(InitialTest.class);
	}

	@Test
	public void testRDFReactionSet() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet) reader
				.read(new NNReactionSet());
		Assert.assertNotNull(reactionSet);

		Assert.assertEquals(1, reactionSet.getReactionCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactantCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactants()
				.getMoleculeCount());
		IMolecule molecule = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		Assert.assertEquals(15, molecule.getAtomCount());
		IAtom atom = molecule.getAtom(0);
		Assert.assertEquals(1, reactionSet.getReaction(0).getProductCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getProducts()
				.getMoleculeCount());
		Assert.assertEquals(11, reactionSet.getReaction(0).getProducts()
				.getMolecule(0).getAtomCount());
	}

	

	@Test
	public void testMultipleRiRegs() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		for (int i = 0; i <= 49; i++) {
			try {
				reactionSet = (IReactionSet) reader.read(new NNReactionSet());
				IMolecule reactant = reactionSet.getReaction(0).getReactants()
						.getMolecule(0);
				IMolecule product = reactionSet.getReaction(0).getProducts()
						.getMolecule(0);
			} catch (NullPointerException npe) {
				System.out.println(i);
				throw npe;
			} catch (java.lang.AssertionError err) {
				System.out.println(i);
				throw err;
			}
		}
	}

	@Test
	public void testFileLength() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		boolean isLessThanInt = fileLengthLong < Integer.MAX_VALUE;
		Assert.assertEquals(true, isLessThanInt);
	}

	/**
	 * tests whether the old (metaprint2d) sybyl atom types are all part of
	 * current CDK version of sybyl atom types
	 * */
	@Test
	public void testVerifyCDKSybylAtomTypesContainsAllOldSybylAtomTypes()
			throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet) reader
				.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);

		// Get current sybyl atom types
		InputStream stream = this.getClass().getClassLoader()
				.getResourceAsStream(
						"org/openscience/cdk/dict/data/sybyl-atom-types.owl");
		AtomTypeFactory factory = AtomTypeFactory.getInstance(stream, "owl",
				reactant.getBuilder());
		IAtomType atomTypes[] = factory.getAllAtomTypes();
		List<String> newAtomTypeNames = new ArrayList<String>();
		for (IAtomType atomType : atomTypes) {
			newAtomTypeNames.add(atomType.getAtomTypeName().toUpperCase());
		}
		// Get old sybyl atom types
		List<String> oldSybylAtomTypes = Constants.ATOM_TYPE_LIST;

		for (String oldAtomTypeName : oldSybylAtomTypes) {
			try {
				Assert.assertEquals(true, newAtomTypeNames
						.contains(oldAtomTypeName.toUpperCase()));
			} catch (java.lang.AssertionError err) {
				throw new java.lang.AssertionError(err.toString() + ". "
						+ oldAtomTypeName
						+ " is not in new list of sybyl atom types:\n"
						+ newAtomTypeNames.toString());
			}
		}

	}

	@Test
	public void testFingerprint() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet) reader
				.read(new NNReactionSet());
		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0)
				.getReactants().getMolecule(0);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher
				.getInstance(reactant.getBuilder());
		IAtomType[] reactantTypes = reactantMatcher
				.findMatchingAtomType(reactant);
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
		List<Fingerprint> fpList = fpGenerator.generateFingerprints(reactant,
				reactantTypes);
	}

}
