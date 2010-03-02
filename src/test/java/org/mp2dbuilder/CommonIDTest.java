package org.mp2dbuilder;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.mp2dbuilder.mcss.AtomMapperUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import prototyping.InitialTest;

public class CommonIDTest {

	private static ILoggingTool logger = null;

	@BeforeClass
	public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(CommonIDTest.class);
	}
	
	@Test
	public void testCommonIdEquality() throws Exception {
		for (int i = 1; i < 2; i++) {
			doTestCommonIdEquality(i);
		}
	}
	
	@Test
	public void testReactantWithoutProduct() throws Exception {
		doTestCommonIdEquality(62);
	}

	@Test
	public void testReactantAndProductWithoutMCS() throws Exception {
		doTestCommonIdEquality(311);
	}
	
	@SuppressWarnings("unchecked")
	private void doTestCommonIdEquality(int reactionIndex) throws Exception {
		Map returnMap = getCommonIdAtomContainersForReaction(reactionIndex);
		IAtomContainer reactant = (IAtomContainer) returnMap.get("reactant");
		IAtomContainer product = (IAtomContainer) returnMap.get("product");
		IAtomContainer mcs = (IAtomContainer) returnMap.get("mcss");
		Map<Integer, Integer> mappedReactantAtoms = (Map<Integer, Integer>) returnMap
				.get("mappedReactantAtoms");
		Map<Integer, Integer> mappedProductAtoms = (Map<Integer, Integer>) returnMap
				.get("mappedProductAtoms");
		StringBuffer sb = new StringBuffer();

		int maxAtomCount = Math.max(reactant.getAtomCount(), product
				.getAtomCount());
		int i = 0;
		sb.append("\nIndex");
		while (i < maxAtomCount) {
			sb.append("\t" + i++);
		}
		sb.append("\nMCS");
		for (IAtom mcsAtom : mcs.atoms()) {
			sb.append("\t" + mcsAtom.getSymbol());
		}
		sb.append("\nReac");
		for (IAtom reactantAtom : reactant.atoms()) {
			sb.append("\t" + reactantAtom.getSymbol());
		}
		sb.append("\nProd");
		for (IAtom productAtom : product.atoms()) {
			sb.append("\t" + productAtom.getSymbol());
		}

		logger.debug(sb.toString());
		logger.debug("mappedReactantAtoms\n" + mappedReactantAtoms);
		logger.debug("mappedProductAtoms\n" + mappedProductAtoms);
		IAtom atom1;
		IAtom atom2;
		for (int index : mappedReactantAtoms.keySet()) {
			atom1 = reactant.getAtom(index);
			atom2 = mcs.getAtom(mappedReactantAtoms.get(index));
			try {
				Assert
						.assertTrue(atom1
								.getProperty(
										MetaboliteHandler.COMMON_ID_FIELD_NAME)
								.equals(
										atom2
												.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)));
				Assert.assertTrue(atom1.getSymbol().equals(atom2.getSymbol()));
			} catch (AssertionError err) {
				String msg = "failed to match id for reactant.getAtom(" + index
						+ ")";
				logger.debug(msg);
				throw new AssertionError(err.toString() + ". " + msg);
			}
		}
		for (int index : mappedProductAtoms.keySet()) {
			atom1 = product.getAtom(index);
			atom2 = mcs.getAtom(mappedProductAtoms.get(index));
			try {
				Assert
						.assertTrue(atom1
								.getProperty(
										MetaboliteHandler.COMMON_ID_FIELD_NAME)
								.equals(
										atom2
												.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)));
				Assert.assertTrue(atom1.getSymbol().equals(atom2.getSymbol()));
			} catch (AssertionError err) {
				String msg = "failed to match id for product.getAtom(" + index
						+ ")";
				logger.debug(msg);
				throw new AssertionError(err.toString() + ". " + msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,? extends Object> getCommonIdAtomContainersForReaction(
			int reactionId) throws Exception {
		Map returnMap = getAtomContainersForReaction(reactionId);
		IAtomContainer reactant = (IAtomContainer) returnMap.get("reactant");
		IAtomContainer product = (IAtomContainer) returnMap.get("product");
		IAtomContainer mcs = (IAtomContainer) returnMap.get("mcss");
		AtomMapperUtil atomMapperUtil = new AtomMapperUtil();
		Map<Integer, Integer> mappedReactantAtoms = atomMapperUtil
				.getAtomMappings(reactant, mcs);
		Map<Integer, Integer> mappedProductAtoms = atomMapperUtil
				.getAtomMappings(product, mcs);

		returnMap.put("mappedReactantAtoms",mappedReactantAtoms);
		returnMap.put("mappedProductAtoms",mappedProductAtoms);

		Collection<Integer> reactantIds = mappedReactantAtoms.values();
		Collection<Integer> productIds = mappedProductAtoms.values();
		Collection<Integer> mcsIds = new HashSet();
		mcsIds.addAll(reactantIds);
		mcsIds.addAll(productIds);
		for (int id : mcsIds) {
			mcs.getAtom(id).setProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME,
					Integer.toString(id));
		}
		atomMapperUtil.setIds(MetaboliteHandler.COMMON_ID_FIELD_NAME, reactant,
				mappedReactantAtoms);
		atomMapperUtil.setIds(MetaboliteHandler.COMMON_ID_FIELD_NAME, product,
				mappedProductAtoms);
		return returnMap;
	}
	
	private Map<String,? extends Object> getAtomContainersForReaction(int reactionId) throws Exception {
		IAtomContainer reactant = null;
		IAtomContainer product = null;
		IAtomContainer mcs = null;
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = null;
		Map<String,? extends Object> returnMap = null;
		try {
			ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);
			ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
			reader.setInitialRiregNo(reactionId);
			IReactionSet reactionSet = (IReactionSet) reader
					.read(new NNReactionSet());
			MetaboliteHandler metaboliteHandler = new MetaboliteHandler();
			returnMap = metaboliteHandler
					.prepareForTransformation(reactionSet);
		} finally {
			if (ins != null) {
				ins.close();
			}
		}
		return returnMap;
	}
}
