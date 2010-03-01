package mp2dbuilder;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class SmartsTest {
	
	private static ILoggingTool logger = null;

	@BeforeClass
	public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(SmartsTest.class);
	}
	
	@Test
	public void testSMARTS() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());
		IAtomContainer reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		IAtomContainer product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);

		QueryAtomContainer query = SMARTSParser.parse("CBr");
		boolean reactantQueryMatch = UniversalIsomorphismTester.isSubgraph(
				reactant, query);
		Assert.assertEquals(true, reactantQueryMatch);
		boolean productQueryMatch = UniversalIsomorphismTester.isSubgraph(
				product, query);
		Assert.assertEquals(false, productQueryMatch);
	}

	@Test
	public void testCountMatchingSMARTS() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(
				filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(1);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet) reader.read(new NNReactionSet());

		IAtomContainer reactant = reactionSet.getReaction(0).getReactants()
				.getMolecule(0);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher
				.getInstance(reactant.getBuilder());
		reactantMatcher.findMatchingAtomType(reactant);

		IAtomContainer product = reactionSet.getReaction(0).getProducts()
				.getMolecule(0);
		SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher
				.getInstance(product.getBuilder());
		// we don't care about the types result,just the transformation the
		// product goes through.
		reactantMatcher.findMatchingAtomType(product);

		QueryAtomContainer query = SMARTSParser.parse("[#6][#7]");
		List<List<RMap>> res = UniversalIsomorphismTester.getSubgraphMaps(
				reactant, query);
		Assert.assertEquals(4, res.size());
	}

}
