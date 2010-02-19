package org.mp2dbuilder.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;
import metaprint2d.analyzer.data.AtomData;

import org.mp2dbuilder.smiles.smarts.ReactionSmartsQueryTool;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;

public class ReactionSmartsHandler extends MetaboliteHandler {

	private Map<String, String> _reactionSmarts = null;

	public ReactionSmartsHandler(Map<String, String> reactionSmarts) {
		this._reactionSmarts = reactionSmarts;
	}

	@Override
	public Map<String,? extends Object> prepareForTransformation(IReactionSet reactionSet)
			throws Exception {
		Map returnMap = new HashMap();

		IReaction theReaction = reactionSet.getReaction(0);

		// Get the reaction and its Sybyl types
		IAtomContainer reactant = (IAtomContainer) theReaction.getReactants()
				.getMolecule(0);
		returnMap.put("reactant",reactant);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher
				.getInstance(reactant.getBuilder());
		IAtomType[] reactantTypes = reactantMatcher
				.findMatchingAtomType(reactant);

		// and generate its fingerprints.
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
		List<Fingerprint> fpList = fpGenerator.generateFingerprints(reactant,
				reactantTypes);

		ReactionSmartsQueryTool sqt = null;
		List<AtomData> atomDataList = null;
		for (Map.Entry<String, String> mapEntry : _reactionSmarts.entrySet()) {
			String reactionName = mapEntry.getKey();
			StringTokenizer tokenizer = new StringTokenizer(mapEntry.getValue());
			String reactantString = tokenizer.nextToken(">>");
			String productString = tokenizer.nextToken(">>");
			sqt = new ReactionSmartsQueryTool(reactantString, productString);
			if (sqt.matches(theReaction)) {
				List<List<Integer>> matchingReactantAtomsList = sqt
						.getUniqueReactantMatchingAtoms();
				IAtom targetAtom = null;
				for (List<Integer> list : matchingReactantAtomsList) {
					for (Integer i : list) {
						targetAtom = reactant.getAtom(i);
						targetAtom.setProperty(reactionName, Boolean.TRUE);
					}
				}
			}
		}

		// and generate a list of atoms to be returned
		atomDataList = getProcessedAtoms(fpList, reactant, _reactionSmarts
				.keySet());

		returnMap.put("atomDataList",atomDataList);

		return returnMap;
	}

}
