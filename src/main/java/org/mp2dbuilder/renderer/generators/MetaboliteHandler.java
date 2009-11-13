package org.mp2dbuilder.renderer.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;
import metaprint2d.analyzer.data.AtomData;
import metaprint2d.analyzer.data.Transformation;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.AtomMappingTools;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
@SuppressWarnings("unused")
public class MetaboliteHandler {

	public static final String COMMON_ID_FIELD_NAME = "mcsCommonId";
	public static final String REACTION_CENTRE_FIELD_NAME = "reactionCentre";
	
	public Transformation getTransformation(IReactionSet reactionSet) throws Exception{
		Transformation t = null;
		
		//Get the reaction
		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
		
		//and generate its fingerprints.
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
	    List<Fingerprint> fpList = fpGenerator.generateFingerprints(reactant);
		
	    //Then get the product
		IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
		
		//and generate the Maximum Common SubStructure
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		IAtomContainer mcs = getFirstMCSHavingMostAtoms(mcsList);
		
		//Mark the reactant atoms that are reaction centres
		setReactionCentres(reactant, product, mcs);
		
		//and generate a list of atoms to be returned
		List<AtomData> atomDataList = getProcessedAtoms(fpList, reactant);
		
		t = new Transformation();
		t.setAtomData(atomDataList);
		
//		List atomData = analysis.getAtomData();
//		t.setAtomData(atomData);
//		t.setMappings(analysis.getMappings());

		return t;
	}
	
	private List<AtomData> getProcessedAtoms(List<Fingerprint> fpList,
			IAtomContainer reactant) {
		List<AtomData> atomDataList = new ArrayList<AtomData>();
		int i = 0;
		Boolean isReactionCentre;
		AtomData atomData = null;
		for(Fingerprint fp: fpList){
			IAtom atom = reactant.getAtom(i);
			isReactionCentre = (Boolean) atom.getProperty(REACTION_CENTRE_FIELD_NAME);
			if(isReactionCentre == null){
				isReactionCentre = Boolean.FALSE;
			}
			atomData = new AtomData(fp, isReactionCentre.booleanValue());
			atomDataList.add(atomData);
			i++;
		}
		return atomDataList;
	}

	public void setReactionCentres(
			IAtomContainer reactant,
			IAtomContainer product,
			IAtomContainer mcs) throws Exception {
		//Make sure that all structures have there atom IDs in common
		setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);
		
		setRemovedAtomsReactionCentres(reactant);		
		
		setAddedAtomsReactionCentres(reactant, product);
	}

	private void setAddedAtomsReactionCentres(
			IAtomContainer reactant,
			IAtomContainer product) {
		//get all product atoms that have null ids
		List<IAtom> nullIdAtoms = getNullIdAtoms(product);
		//for each product null atom X: if X is connected to "id atoms" (Plural), 
		//then the reactant atoms having those ids are reaction centres.
		List<IAtom> connectedAtoms = null;
		String currentId = null;
		Set<String> idSet = new HashSet<String>();
		for(IAtom atom: nullIdAtoms){
			connectedAtoms = product.getConnectedAtomsList(atom);
			for(IAtom connectedAtom: connectedAtoms){
				currentId = (String) connectedAtom.getProperty(COMMON_ID_FIELD_NAME);
				if(currentId != null){
					idSet.add(currentId);
				}
			}
		}
		for(String id: idSet){
			for(IAtom atom: reactant.atoms()){
				currentId = (String) atom.getProperty(COMMON_ID_FIELD_NAME);
				if(id.equals(currentId)){
					atom.setProperty(REACTION_CENTRE_FIELD_NAME, new Boolean(true));
				}
			}
		}
	}

	private void setRemovedAtomsReactionCentres(IAtomContainer reactant) {
		
		//get all reactant atoms that have null ids
		List<IAtom> nullIdAtoms = getNullIdAtoms(reactant);
		
		//for each reactant null atom X: if X is connected to an "id atom", then X is a reaction centre.
		List<IAtom> connectedAtoms = null;
		for(IAtom atom: nullIdAtoms){
			connectedAtoms = reactant.getConnectedAtomsList(atom);
			for(IAtom connectedAtom: connectedAtoms){
				if(connectedAtom.getProperty(COMMON_ID_FIELD_NAME) != null){
					atom.setProperty(REACTION_CENTRE_FIELD_NAME, new Boolean(true));
				}
			}
		}
	}

	private List<IAtom> getNullIdAtoms(IAtomContainer atomContainer) {
		List<IAtom> nullIdAtoms = new ArrayList<IAtom>();
		String currentId = null;
		for(IAtom atom: atomContainer.atoms()){
			currentId = (String) atom.getProperty(COMMON_ID_FIELD_NAME);
			if(currentId == null){
				nullIdAtoms.add(atom);
			}
		}
		return nullIdAtoms;
	}
	
	public IAtomContainer getFirstMCSHavingMostAtoms(List<IAtomContainer> mcsList){
    	IAtomContainer chosenAtomContainer = null;
		int maxCount = 0;
		for(IAtomContainer atoms: mcsList){
			System.out.println(atoms.getAtomCount());
			if(atoms.getAtomCount() > maxCount){
				maxCount = atoms.getAtomCount();
				chosenAtomContainer = atoms;
			}
		}
		return chosenAtomContainer;
    }
	
	public void setCommonIds(String identifierName, IAtomContainer mcs, IAtomContainer reactant,IAtomContainer product) throws Exception {
		Map<Integer,Integer> mappedReactantAtoms = new HashMap<Integer,Integer>();
		AtomMappingTools.mapAtomsOfAlignedStructures(reactant, mcs, mappedReactantAtoms);
		Map<Integer,Integer> mappedProductAtoms = new HashMap<Integer,Integer>();
		AtomMappingTools.mapAtomsOfAlignedStructures(product, mcs, mappedProductAtoms);
		Collection<Integer> reactantIds = mappedReactantAtoms.values();
		Collection<Integer> productIds = mappedProductAtoms.values();
		Collection<Integer> mcsIds = new HashSet();
		mcsIds.addAll(reactantIds);
		mcsIds.addAll(productIds);
		for(int id: mcsIds){
			mcs.getAtom(id).setProperty(identifierName, Integer.toString(id));
		}
		setIds(identifierName, reactant, mappedReactantAtoms);
		setIds(identifierName, product, mappedProductAtoms);
	}
	
	public void setIds(String identifierName, IAtomContainer targetAtomContainer, Map<Integer,Integer> mapper){
		for(int targetIndex: mapper.keySet()){
			targetAtomContainer.getAtom(targetIndex).setProperty(identifierName, Integer.toString(mapper.get(targetIndex)));
		}
	}

}
