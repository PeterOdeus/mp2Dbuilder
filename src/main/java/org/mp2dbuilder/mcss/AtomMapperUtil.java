package org.mp2dbuilder.mcss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;

public class AtomMapperUtil {

	public void setCommonIds(String identifierName, IAtomContainer mcs,
			IAtomContainer reactant, IAtomContainer product) throws Exception {
		Map<Integer, Integer> mappedReactantAtoms = getAtomMappings(reactant,
				mcs);
		Map<Integer, Integer> mappedProductAtoms = getAtomMappings(product, mcs);
		Collection<Integer> reactantIds = mappedReactantAtoms.values();
		Collection<Integer> productIds = mappedProductAtoms.values();
		Collection<Integer> mcsIds = new HashSet<Integer>();
		mcsIds.addAll(reactantIds);
		mcsIds.addAll(productIds);
		for (int id : mcsIds) {
			mcs.getAtom(id).setProperty(identifierName, Integer.toString(id));
		}
		setIds(identifierName, reactant, mappedReactantAtoms);
		setIds(identifierName, product, mappedProductAtoms);
	}

	public void setIds(String identifierName,
			IAtomContainer targetAtomContainer, Map<Integer, Integer> mapper) {
		for (int targetIndex : mapper.keySet()) {
			targetAtomContainer.getAtom(targetIndex).setProperty(
					identifierName, Integer.toString(mapper.get(targetIndex)));
		}
	}

	public Map<Integer, Integer> getAtomMappings(
			IAtomContainer firstAtomContainer,
			IAtomContainer secondAtomContainer) throws CDKException {
		List<RMap> rMapList = UniversalIsomorphismTester.getSubgraphAtomsMap(
				firstAtomContainer, secondAtomContainer);
		if (rMapList == null) {
			rMapList = new ArrayList<RMap>();
		}
		Map<Integer, Integer> atomMappings = new HashMap<Integer, Integer>();
		for (RMap rMap : rMapList) {
			atomMappings.put(rMap.getId1(), rMap.getId2());
		}
		return atomMappings;
	}
}
