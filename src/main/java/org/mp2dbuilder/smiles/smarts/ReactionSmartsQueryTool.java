package org.mp2dbuilder.smiles.smarts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mp2dbuilder.mcss.AtomMapperUtil;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.renderer.generators.AtomMassGenerator;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 * ReactionSmartsQueryTool can be used to query a reaction for conserved matches in reactant and product.
 * 
 * @author ola
 *
 */
public class ReactionSmartsQueryTool {


	public static final String COMMON_ID_FIELD_NAME = "ReactionSmartsCommonId";
	private List<List<Integer>> reactantAtomNumbers;
	private List<List<Integer>> productAtomNumbers;
	private String reactantQuery;
	private String productQuery;
	private String reactantQueryNoClasses;
	private String productQueryNoClasses;

	private String reactantQueryNoDollar;
	private String productQueryNoDollar;

	private Map<Integer, String> reactClasses;
	private Map<Integer, String> prodClasses;

	/**
	 * Constructor.
	 * @param reactantQuery the SMARTS string for the reactant part of the reaction
	 * @param productQuery the SMARTS string for the product part of the reaction
	 */
	public ReactionSmartsQueryTool(String reactantQuery, String productQuery) {
		this.reactantQuery=reactantQuery;
		this.productQuery=productQuery;


		//remove the [$( partof reactant
		reactantQueryNoDollar=removeDollarPart(reactantQuery);

		//product query must not have [$(
		productQueryNoDollar=productQuery;

		//Extract classes to map INT > String
		reactClasses = getClasses(reactantQueryNoDollar);
		prodClasses = getClasses(productQueryNoDollar);

		//Extract full query without dollar and without classes
		reactantQueryNoClasses=removeAllClasses(reactantQuery);
		productQueryNoClasses=removeAllClasses(productQuery);

		//remove the [$( partof reactant. Assign it again to get it without classes.
		reactantQueryNoDollar=removeDollarPart(reactantQueryNoClasses);
	}

	/**
	 * Use a regexp to remove all :X (class information) from a SMARTS string
	 * @param q
	 * @return
	 */
	private String removeAllClasses(String q) {
		return q.replaceAll(":\\d", "");
	}

	/**
	 * Get the atoms in the reactant molecule that match the query pattern. <p/>
	 * Since there may be multiple matches, the return value is a List of List
	 * objects. Each List object contains the unique set of indices of the atoms in the reactant
	 * molecule, that match the query pattern
	 *
	 * @return A List of List of atom indices in the reactant molecule
	 */
	public List<List<Integer>> getUniqueReactantMatchingAtoms() {
		//TODO: Ensure unique
		return reactantAtomNumbers;
	}

	/**
	 * Get the atoms in the product molecule that match the query pattern. <p/>
	 * Since there may be multiple matches, the return value is a List of List
	 * objects. Each List object contains the unique set of indices of the atoms in the product
	 * molecule, that match the query pattern
	 *
	 * @return A List of List of atom indices in the product molecule
	 */
	public List<List<Integer>> getUniqueProductMatchingAtoms() {
		//TODO: Ensure unique
		return productAtomNumbers;
	}

	/**
	 * Do matching in reaction and product.
	 * @param reaction The reaction to be queried
	 * @return true if there is a match in reactant and product molecules, 
	 * and all atoms marked with class are conserved in reactant and product.
	 * @throws Exception 
	 */
	public boolean matches(IReaction reaction) throws Exception {
		//TODO: Change some of the List<List<Integer>> to List<Integer>, we only use them as lists and not lists of lists.
		
		System.out.println("Computing matches. ============================================");
		
		//Assert only one reactant and one product, this is all we can handle for now
		//TODO: Extend this to be more generic
		assert(reaction.getReactantCount()==1);
		assert(reaction.getProductCount()==1);

		// Since we only have one each we pull out the first ones in the lists.
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		assert(reactant!=null);
		assert(product!=null);

		reactantAtomNumbers=new ArrayList<List<Integer>>();
		productAtomNumbers=new ArrayList<List<Integer>>();

		//Count number of classes in reactant and product and require identical
		assertEqualClasses(reactClasses, prodClasses);

		// Compute queries for the various SMARTS defining the rc atom, the complete match in the reactant and the corresponding match in the product.
		// If no SMARTS matches in reactant or product, fail early
		SMARTSQueryTool rcQueryTool = new SMARTSQueryTool(reactantQueryNoClasses);
		if (!rcQueryTool.matches(reactant)){
			System.out.println("== No match in reactant query: " + reactantQueryNoClasses + "\nExiting.");
			return false;
		}
		List<List<Integer>> putativeRC_Atomlist = rcQueryTool.getMatchingAtoms(); //getUniqueMatchingAtoms();
		SMARTSQueryTool reactantQueryTool = new SMARTSQueryTool(reactantQueryNoDollar);
		reactantQueryTool.matches(reactant); // This 
		SMARTSQueryTool productQueryTool = new SMARTSQueryTool(productQueryNoClasses);
		if (!productQueryTool.matches(product)){
			System.out.println("== No match in product query: " + productQueryNoClasses + "\nExiting.");
			return false;
		}

		//We have at least one match in reactant and one in product.
		//Save these indices in list
		System.out.println(":"+reactantQueryNoDollar+":");
		List<List<Integer>> fullReactantHit_AtomList = reactantQueryTool.getMatchingAtoms();// getUniqueMatchingAtoms();//It might be needed to associate the reactant hits with the rc.
		List<List<Integer>> fullProductHit_AtomList = productQueryTool.getMatchingAtoms();//getUniqueMatchingAtoms();

		// Generate and pick largest MCS. Since there are only one structure as a reactant and one structure as a product the largest MCS should be ok.
		// Any mapping between reactant and the product should be ok as well.
		IAtomContainer mcs = getMCS(reactant, product);
		if (mcs==null || mcs != null && mcs.getAtomCount()<=0){
			System.out.println("No overlaps in MCSS. Exiting.");
			return false;
		}

		//Old CDK code, bug filed w/ junit test.
		//TODO: use this later maybe.
		//List<RMap> mcss = UniversalIsomorphismTester.getSubgraphAtomsMap(reactant, product);		

		//Workaround for CDK bug. Makes use of property to link atoms.
		//Add identifier fields used to map atoms from reactant to product.
		AtomMapperUtil mapperUtil = new AtomMapperUtil();
		mapperUtil.setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);
		// Determine the mapping of the mcs atoms.
		String mcsstr="";
		Map<Integer,Integer> reactantAtomFromMCSAtom = new HashMap<Integer, Integer>();
		Map<Integer,Integer> productAtomFromMCSAtom = new HashMap<Integer, Integer>();
		for (IAtom atom : mcs.atoms()){
			mcsstr=mcsstr + mcs.getAtomNumber(atom) + ",";
			String curMCSAtom = (String) atom.getProperty(COMMON_ID_FIELD_NAME);
			for (IAtom reactantAtom : reactant.atoms()){
				if (reactantAtom.getProperty(COMMON_ID_FIELD_NAME)!=null){
					String curReactantAtom = (String) reactantAtom.getProperty(COMMON_ID_FIELD_NAME);	
					if (curReactantAtom.equals(curMCSAtom)){
						reactantAtomFromMCSAtom.put(reactant.getAtomNumber(reactantAtom), mcs.getAtomNumber(atom));
					}
				}
			}
			for (IAtom productAtom : product.atoms()){
				if (productAtom.getProperty(COMMON_ID_FIELD_NAME)!=null){
					String curProductAtom = (String) productAtom.getProperty(COMMON_ID_FIELD_NAME);	
					if (curProductAtom.equals(curMCSAtom)){
						productAtomFromMCSAtom.put(product.getAtomNumber(productAtom), mcs.getAtomNumber(atom));
					}
				}
			}
		}
		
		// Print the mapping
		System.out.println("MCS atom nr:Reactant atom nr:ProductAtomnr - MCS atom type");
		for (IAtom atom : mcs.atoms()){
			Integer mcsAtomNr = mcs.getAtomNumber(atom);
			System.out.println( mcsAtomNr + ":" + reactantAtomFromMCSAtom.get(mcsAtomNr) + ":" + productAtomFromMCSAtom.get(mcsAtomNr) + 
					" - " + atom.getAtomTypeName());
		}
		System.out.println("MCS contains: " + mcsstr);
		System.out.println("Reactant non-dollar SMARTS hits:\n" + debugHits(fullReactantHit_AtomList));
		System.out.println("Product SMARTS hits:\n" + debugHits(fullProductHit_AtomList));

		//Remove all indices which are not available in MCS. If this makes some hits
		//or RC empty, remove the empty lists.
		//fullReactantHit_AtomList=removeIndicesWithoutCommonId(fullReactantHit_AtomList, reactant);//Don't do it for reactant atoms it might remove the rc.
		fullProductHit_AtomList=removeIndicesWithoutCommonId(fullProductHit_AtomList, product);
		//System.out.println("Reactant hits pruned by MCS:\n" + debugHits(fullReactantHit_AtomList));
		System.out.println("Product SMARTS hits pruned by MCS:\n" + debugHits(fullProductHit_AtomList));		
		if (fullProductHit_AtomList.size() == 0){
			System.out.println("No SMARTS hits in product. Exiting.");
			return false;
		}


		// Go through the possible reaction centers and see if any of them fulfill the requirements.
		System.out.println("** Starting to check putative reaction centers. **");
		boolean addedToMCSClasses = false; //If no class gets added to mcsClasses we can break early and return false. Behovs inte om saker lyckas sa breakar vi innan rc loopen ar slut.
		int rcno=0; //0-based index is easiest
		for (List<Integer> putativeRC : putativeRC_Atomlist){
			System.out.println(" %% Current putative RC: " + rcno + " with hits: " + putativeRC.get(0));
			// The possible rc must be part of at least one of the reactant sets. Pick the first one that contains the prc.
			// fullReactantHit_AtomList has to have the same length as putativeRC_Atomlist.
			List<Integer> curReactantSet = new ArrayList<Integer>();
			curReactantSet = extractCurrentReactantSet(reactant,
					fullReactantHit_AtomList, putativeRC, curReactantSet);

			// Setup the structure that holds the reactant class labels. Do this here so that it is cleared for each new putative RC.
			List<List<Integer>> reactantClasses = new ArrayList<List<Integer>>();
			for (IAtom atom : reactant.atoms()){
				if (atom.getProperty(COMMON_ID_FIELD_NAME)!=null){
					reactantClasses.add(new ArrayList<Integer>());
				}
			}

			// Pull out each list of the product hits and submit that as a list of list below. This is a bad solution but the code below was set up to deal with List<List<Integer>>.
			List<List<Integer>> currentProductHit_AtomList = new ArrayList<List<Integer>>();
			for (List<Integer> currentProductHits : fullProductHit_AtomList){
				currentProductHit_AtomList.clear();
				currentProductHit_AtomList.add(currentProductHits);


				// Setup the structure that holds the overlapping class labels. Do this here so that it is cleared for each new product SMARTS hits.
				List<List<Integer>> mcsClasses = new ArrayList<List<Integer>>();
				for (IAtom atom : reactant.atoms()){
					if (atom.getProperty(COMMON_ID_FIELD_NAME)!=null){
						mcsClasses.add(new ArrayList<Integer>());
					}
				}

				int[] mcsSize = new int[mcsClasses.size()];
				// Loop through the different classes. And check if they can be assigned to any MCS atoms.
				for (int i : reactClasses.keySet()){
					//REACTANT PART - We are looking at the hits of the SMARTS which overlap the current reaction center. If any of these belong to the mcs we store that info in reactantClasses.
					String reactantClassSMARTS = reactClasses.get(i);
					String reactantRemovedClassSMARTS=removeAllClasses(reactantClassSMARTS);
					System.out.println("\n## Reaction class: " + i + "=" + reactantClassSMARTS + "=" + reactantRemovedClassSMARTS);
					reactantQueryTool.setSmarts(reactantRemovedClassSMARTS);
					if (!reactantQueryTool.matches(reactant)){
						System.out.println("   Produced no hits.");
					}else{
						Set<Integer> reactHitsconcat=null;
						Set<Integer> reactHitsconcat_pruned=null;
						List<List<Integer>> reactHits = reactantQueryTool.getUniqueMatchingAtoms();
						reactHitsconcat=concatIndices(reactHits);
						// Remove the ones not belonging to the MCS.
						List<List<Integer>> reactHits_pruned = removeIndicesWithoutCommonId(reactHits, reactant);
						reactHitsconcat_pruned = concatIndices(reactHits_pruned);
						// Add reactant classes to the atoms that were originally hit by the smarts and part of the MCS. 
						for (int j : reactHitsconcat_pruned){
							if (curReactantSet.contains(j)){
								System.out.println("Adding class: "+ i + ", to reactant atom: " + j);
								reactantClasses.get(j).add(i);
							}
						}
					}

					//PRODUCT PART - This is different compared to the reactant.
					String productClassSMARTS = prodClasses.get(i);
					String productNoClassSMARTS=removeAllClasses(productClassSMARTS);
					System.out.println("## Product class: " + i + "=" + productClassSMARTS + "=" + productNoClassSMARTS);
					productQueryTool.setSmarts(productNoClassSMARTS);
					if (!productQueryTool.matches(product)){
						System.out.println("   Produced no hits.");
					}else{
						List<List<Integer>> prodHits = productQueryTool.getMatchingAtoms(); //getUniqueMatchingAtoms();
						addedToMCSClasses = checkProductSMARTSHit(reactant,
								product, productQueryTool, currentProductHit_AtomList,
								addedToMCSClasses, mcsClasses, reactantClasses,
								mcsSize, i, prodHits);

					}
				}
				if (addedToMCSClasses){
					addedToMCSClasses = false;
					if (checkClassCoverage(mcsClasses, mcsSize)){
						// Add the putative reaction center as a reaction center.
						reactantAtomNumbers.add(new ArrayList<Integer>());
						reactantAtomNumbers.get(reactantAtomNumbers.size()-1).add(putativeRC.get(0));

					}
				}
			}
			rcno++;
		}

		if (reactantAtomNumbers.size() > 0){
			System.out.println("These are the RC:s: " + reactantAtomNumbers.toString());
			return true;
		}
		else{
			return false;
		}
	}

	private List<Integer> extractCurrentReactantSet(IAtomContainer reactant,
			List<List<Integer>> fullReactantHit_AtomList,
			List<Integer> putativeRC, List<Integer> curReactantSet) {
		List<Integer> reactToRemove = new ArrayList<Integer>();
		boolean breaking = false;
		for (List<Integer> list : fullReactantHit_AtomList){
			for (int p : putativeRC){
				if (list.contains(p)) {
					curReactantSet.addAll(list);
					reactToRemove.addAll(list);
					breaking = true;
					break;
				}
			}
			if (breaking){
				break;
			}
		}
		// Remove the set from the list of sets.
		fullReactantHit_AtomList.removeAll(reactToRemove);
		//Remove the indices not in the MCS.
		curReactantSet = removeIndicesSimpleListWithoutCommonId(curReactantSet, reactant);

		return curReactantSet;
	}

	private boolean checkProductSMARTSHit(IAtomContainer reactant,
			IAtomContainer product, SMARTSQueryTool prodQueryTool,
			List<List<Integer>> fullProductHit_AtomList,
			boolean addedToMCSClasses, List<List<Integer>> mcsClasses,
			List<List<Integer>> reactantClasses, int[] mcsSize, int i,
			List<List<Integer>> prodHits) throws CDKException {
		Set<Integer> prodHitsconcat;
		Set<Integer> prodHitsconcat_pruned;
		prodHitsconcat=concatIndices(prodHits);
		System.out.println("   Produced hits: " + debugHits(prodHitsconcat));

		List<List<Integer>> prodHits_pruned = removeIndicesWithoutCommonId(prodHits, product);
		prodHitsconcat_pruned = concatIndices(prodHits_pruned);
		System.out.println("   Product hits pruned by MCS: " + debugHits(prodHitsconcat_pruned));
		// Determine if there are any non-class product hits and remove them.
		identifyNonClassAtoms(prodHitsconcat_pruned, product, prodQueryTool);
		System.out.println("   Product hits pruned by non-class SMARTS: " + debugHits(prodHitsconcat_pruned));

		// Keep hits hit by the class parts of the original smarts.
		Set<Integer> prodToKeep = new HashSet<Integer>();
		for (int prodHit : prodHitsconcat_pruned){
			for (List<Integer> prodList : fullProductHit_AtomList){
				if (prodList.contains(prodHit)){
					System.out.println("Prod atom to keep: " + prodHit);
					prodToKeep.add(prodHit);
				}	
			}
		}
		prodHitsconcat_pruned.clear();
		prodHitsconcat_pruned.addAll(prodToKeep);
		System.out.println("   Product hits produced by original SMARTS: " + debugHits(fullProductHit_AtomList));
		System.out.println("   Product hits pruned by original SMARTS: " + debugHits(prodHitsconcat_pruned));

		for (int j : prodHitsconcat_pruned){
			String commonId = (String) product.getAtom(j).getProperty(COMMON_ID_FIELD_NAME);
			// Pull out the corresponding atom from the reactant. 
			// If the same class exists in the reactant and the product add it to the mcsClasses for that particular mcs (product) atom.
			System.out.println("Checking pruned product hit: " + j + ", with mapping: " + commonId);
			for (IAtom atom : reactant.atoms()){
//							System.out.println("Common ID: " + atom.getProperty(COMMON_ID_FIELD_NAME));							
				if (commonId.equals((String)atom.getProperty(COMMON_ID_FIELD_NAME))){
					System.out.println("Pruned product hit" + j + ":" + atom.getProperty(COMMON_ID_FIELD_NAME) + ":" + commonId);
					if (reactantClasses.get(reactant.getAtomNumber(atom)).contains(i)){
						System.out.println("Adding class: "+ i + ", to mcs atom: " + j);
						if (!mcsClasses.get(reactant.getAtomNumber(atom)).contains(i)) {
							mcsClasses.get(reactant.getAtomNumber(atom)).add(i);
							mcsSize[reactant.getAtomNumber(atom)] = mcsClasses.get(reactant.getAtomNumber(atom)).size();
						}
						addedToMCSClasses = true;
					}
				}
			}
		}
		return addedToMCSClasses;
	}

	private void identifyNonClassAtoms(Set<Integer> prodHits, IAtomContainer product,
			SMARTSQueryTool prodQueryTool) throws CDKException {
		List<String>  prodNonClasses = getNonClasses(productQueryNoDollar);
		boolean removeAllProductHits = false;
		for (String pclass : prodNonClasses){
			String pclass_noclass=removeAllClasses(pclass);
			System.out.println("\n## Product non-class: " + pclass + "=" + pclass_noclass);
			Set<Integer> prodHitsconcat=null;
			prodQueryTool.setSmarts(pclass_noclass);
			if (!prodQueryTool.matches(product)){
				System.out.println("   Produced no hits.");
			}else{
				List<List<Integer>> hits = prodQueryTool.getUniqueMatchingAtoms();
				prodHitsconcat=concatIndices(hits);
				System.out.println("   Product hits: " + debugHits(prodHitsconcat));
				int lenProdHitsBefore = prodHits.size();
				prodHits.removeAll(prodHitsconcat);
				int lenProdHitsAfter = prodHits.size();
				System.out.println("   identifyNonClassAtoms::Produced hits pruned by non-class SMARTS: " + debugHits(prodHits));
				if (lenProdHitsBefore > lenProdHitsAfter){
					removeAllProductHits = true;
				}
				
			}
		}
		if (removeAllProductHits) {
			System.out.println("Non-class MCS atoms. This SMARTS can't be used.");
			prodHits.clear();
		}
	}

	private boolean checkClassCoverage(List<List<Integer>> mcsClasses, int[] mcsSize) {
		int[] curIndex = new int[mcsClasses.size()];
		int maxNrPermutations = 1;
		for (int mcsSizeEl : mcsSize){
			if (mcsSizeEl > 0){
				maxNrPermutations = maxNrPermutations * mcsSizeEl;
			}
		}
		// Loop through mcsClasses and see if all classes are mapped on different atoms.
		//boolean finishedCheckingClasses = false;
		int indexToIncrease = 0;
		for (int permutation = 0; permutation < maxNrPermutations; permutation++){
			// Go through the next permutation.
			List<Integer> classList = new ArrayList<Integer>();
			int curNr = 0;
			// Go through each atom number and add classes.
			while (curNr < mcsClasses.size()){
				if ( (mcsClasses.get(curNr).size() > 0) && (mcsClasses.get(curNr).size() > curIndex[curNr])){
					int classVal = mcsClasses.get(curNr).get(curIndex[curNr]);
					if (!classList.contains(classVal)){
						classList.add(classVal);
					}
				}
				curNr++;
			}
			//System.out.println("Len class list: " + classList.size());
			//System.out.println("Len reactantClasses list: " + reactClasses.size());
			if (classList.size() == reactClasses.size() ){
				System.out.println("-----------------------------The current putative RC is a reaction center.");
				return true;
			}
			// Update the indices defined by curIndex.
			updateIndices(curIndex, mcsClasses, indexToIncrease);
		}		
		return false;
	}


	private void updateIndices(int[] curIndex, List<List<Integer>> mcsClasses, int indexToIncrease) {
		// This function updates the indices for the mcs atoms. It goes through all permutations that the class mappings on atoms can be described by.
		// Check if it will be too large. 
		if (curIndex[indexToIncrease]+2 >= mcsClasses.get(indexToIncrease).size()){
			// Increase the next more significant index.
			//boolean foundNextIndex = false;
			while (true){
				curIndex[indexToIncrease] = 0; //Reset
				indexToIncrease++;
				if ( (curIndex[indexToIncrease] < mcsClasses.get(indexToIncrease).size()) && (mcsClasses.get(indexToIncrease).size() > 1) ){
					curIndex[indexToIncrease]++;
					break;
				}
			}
		}
		else{
			curIndex[indexToIncrease]++;
		}
		indexToIncrease = 0;
	}


	private IAtomContainer getMCS(IAtomContainer reactant,
			IAtomContainer product) throws CDKException {

		//Preprocess AC for MCSS (TODO: Verify if this is needed)
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(reactant);
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(product);
		CDKHueckelAromaticityDetector.detectAromaticity(reactant);
		CDKHueckelAromaticityDetector.detectAromaticity(product);

		//We now need an MCSS to link atoms

		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);

		//How many overlaps can we get? Anyway, pick largest for now TODO: Verify this
		return getFirstMCSHavingMostAtoms(mcsList);

	}

	private List<List<Integer>> removeIndicesWithoutCommonId(
			List<List<Integer>> rclist,
			IAtomContainer mol) {

		for (IAtom atom : mol.atoms()){
			for (List<Integer> rc : rclist){
				if (atom.getProperty(COMMON_ID_FIELD_NAME)==null){
					//REMOVE
					Integer i = mol.getAtomNumber(atom);
					rc.remove(i);
				}
			}
		}

		//remove empty lists
		Set<List<Integer>> toRemove=new HashSet<List<Integer>>();
		for (List<Integer> rc : rclist){
			if (rc.size()==0)
				toRemove.add(rc);
		}
		rclist.removeAll(toRemove);

		return rclist;
	}

	private List<Integer> removeIndicesSimpleListWithoutCommonId(
			List<Integer> rclist,
			IAtomContainer mol) {

		List<Integer> toRemove = new ArrayList<Integer>();
		for (IAtom atom : mol.atoms()){
			if (atom.getProperty(COMMON_ID_FIELD_NAME)==null){
				toRemove.add(mol.getAtomNumber(atom));  
			}
		}
		rclist.removeAll(toRemove);

		return rclist;
	}

	private boolean isConserved(IAtomContainer reactant, Set<Integer> reactionIndices, IAtomContainer product, Set<Integer> productIndices) {
		List<Integer> consReactIndices=new ArrayList<Integer>();

		//Check conservation for each reaction index

		String reactantCommonId = null;
		String productCommonId = null;
		boolean tempMatch = false;

		if (reactionIndices== null || reactionIndices.size()<=0){
			System.out.println("  No indices to test conservation for. Ending TRUE.");
			return true;
		}

		//Confirm at least one reactant atom conserved
		//=========
		for (Integer ratom : reactionIndices){
			//			System.out.println("+ checking atom index=" + ratom);

			//get the common id from the reactant atom having index value of ratom
			reactantCommonId = (String) reactant.getAtom(ratom).getProperty(COMMON_ID_FIELD_NAME);
			if(reactantCommonId == null){
				System.out.println("Skipping index " + ratom + " because it lacks a common id field.");
				continue;
			}

			for(IAtom productAtom : product.atoms()){
				productCommonId = (String) productAtom.getProperty(COMMON_ID_FIELD_NAME); 
				if(	productCommonId != null
						&&
						reactantCommonId.equals(productCommonId)
						&&
						productIndices.contains(product.getAtomNumber(productAtom))
				){
					tempMatch = true;
					break;
				}
			}

			if(tempMatch == false){
				System.out.println("  --- atom index=" + ratom + " is NOT-CONSERVED");
			}else{
				System.out.println("  --- atom index=" + ratom + " is CONSERVED");
				consReactIndices.add(ratom);
			}

		}

		return tempMatch;
	}

	private Set<Integer> concatIndices(
			List<List<Integer>> hits) {

		LinkedHashSet<Integer> lp=new LinkedHashSet<Integer>();

		for (List<Integer> in : hits){
			for (int i : in){
				lp.add(i);
			}
		}
		//Remove duplicates
		return lp;
	}

	/**
	 * 
	 * @param mcss
	 * @param reactionIndices
	 * @param productIndices
	 * @return true if all atoms in reactionIndices are present in productIndices linked via 
	 * COMMON_ID_FIELD_NAME constant
	 */
	private boolean areAnyAtomConserved(
			IAtomContainer reactant, List<List<Integer>> reactionIndices,
			IAtomContainer product, List<List<Integer>> productIndices) {

		//We do not care about individual matches so merge all in atom index lists
		Set<Integer> rlist=new HashSet<Integer>();
		for (List<Integer> l : reactionIndices){
			rlist.addAll(l);
		}
		Set<Integer> plist=new HashSet<Integer>();
		for (List<Integer> l : productIndices){
			plist.addAll(l);
		}

		//Check conservation for each reaction index

		String reactantCommonId = null;
		String productCommonId = null;
		boolean tempMatch = false;

		for (Integer ratom : rlist){
			System.out.println("+ checking reactant atom index=" + ratom);

			//get the common id from the reactant atom having index value of ratom
			reactantCommonId = (String) reactant.getAtom(ratom).getProperty(COMMON_ID_FIELD_NAME);
			if(reactantCommonId == null){
				System.out.println("Skipping reactant having index " + ratom + " because it lacks a common id field.");
				continue;
			}

			for(IAtom productAtom : product.atoms()){
				productCommonId = (String) productAtom.getProperty(COMMON_ID_FIELD_NAME); 
				if(	productCommonId != null
						&&
						reactantCommonId.equals(productCommonId)
						&&
						plist.contains(product.getAtomNumber(productAtom))
				){
					tempMatch = true;
					break;
				}
			}

			if(tempMatch == false){
				System.out.println("+++ NOT-CONSERVED, since reactant index" + ratom + " is NOT present in productlist");
				//				return false; //Found a non-conserved atom
			}else{
				System.out.println("+++ CONSERVED, since reactant index " + ratom + " is present in productlist");
			}

			//			for (RMap rmap : mcss){
			////				System.out.println("++ rmap.getId1()=" + rmap.getId1());
			//				if (ratom==rmap.getId1()){
			//					System.out.println("+++ Found in mcs.getID1");
			//					//verify that rmap.getId2() is present in plist
			//					if (!(plist.contains(rmap.getId2()))){
			//						System.out.println("+++ NOT-CONSERVED, since rmap.getId2()=" + rmap.getId2() + " NOT present in productlist");
			//						return false; //Found a non-conserved atom
			//					}else{
			//						System.out.println("+++ CONSERVED, since rmap.getId2()=" + rmap.getId2() + " present in productlist");
			//					}
			//				}
			//			}
		}

		System.out.println("We found at least one conserved atom.");

		return tempMatch;
	}


	private List<Integer> getConservedProductIndices(
			IAtomContainer reactant, List<List<Integer>> reactionIndices,
			IAtomContainer product, List<List<Integer>> productIndices) {

		//Just swap the inputs I guess...
		return getConservedReactantIndices(product, productIndices, reactant, reactionIndices);
	}

	private List<Integer> getConservedReactantIndices(
			IAtomContainer reactant, List<List<Integer>> reactionIndices,
			IAtomContainer product, List<List<Integer>> productIndices) {

		List<Integer> consReactIndices=new ArrayList<Integer>();

		//We do not care about individual matches so merge all in atom index lists
		Set<Integer> rlist=new HashSet<Integer>();
		for (List<Integer> l : reactionIndices){
			rlist.addAll(l);
		}
		Set<Integer> plist=new HashSet<Integer>();
		for (List<Integer> l : productIndices){
			plist.addAll(l);
		}

		//Check conservation for each reaction index

		String reactantCommonId = null;
		String productCommonId = null;
		boolean tempMatch = false;

		for (Integer ratom : rlist){
			System.out.println("+ checking reactant atom index=" + ratom);

			//get the common id from the reactant atom having index value of ratom
			reactantCommonId = (String) reactant.getAtom(ratom).getProperty(COMMON_ID_FIELD_NAME);
			if(reactantCommonId == null){
				System.out.println("Skipping reactant having index " + ratom + " because it lacks a common id field.");
				continue;
			}

			for(IAtom productAtom : product.atoms()){
				productCommonId = (String) productAtom.getProperty(COMMON_ID_FIELD_NAME); 
				if(	productCommonId != null
						&&
						reactantCommonId.equals(productCommonId)
						&&
						plist.contains(product.getAtomNumber(productAtom))
				){
					tempMatch = true;
					break;
				}
			}

			if(tempMatch == false){
				System.out.println("+++ NOT-CONSERVED, since reactant index" + ratom + " is NOT present in productlist");
				//				return false; //Found a non-conserved atom
			}else{
				System.out.println("+++ CONSERVED, since reactant index " + ratom + " is present in productlist");
				consReactIndices.add(ratom);
			}

			//			for (RMap rmap : mcss){
			////				System.out.println("++ rmap.getId1()=" + rmap.getId1());
			//				if (ratom==rmap.getId1()){
			//					System.out.println("+++ Found in mcs.getID1");
			//					//verify that rmap.getId2() is present in plist
			//					if (!(plist.contains(rmap.getId2()))){
			//						System.out.println("+++ NOT-CONSERVED, since rmap.getId2()=" + rmap.getId2() + " NOT present in productlist");
			//						return false; //Found a non-conserved atom
			//					}else{
			//						System.out.println("+++ CONSERVED, since rmap.getId2()=" + rmap.getId2() + " present in productlist");
			//					}
			//				}
			//			}
		}

		System.out.println("We found at least one conserved atom.");

		return consReactIndices;
	}

	private String escapeBrackets(String reactGroup) {
		String s = reactGroup.replaceAll("\\[", "\\\\[");
		s = s.replaceAll("\\*", "\\\\*");
		s=s.replaceAll("\\]", "\\\\]");
		return s;
	}

	private String debugHits(Set<Integer> hits) {
		String s="";
		for (Integer hit : hits){
			s=s+hit+",";
		}
		return s;
	}
	private String debugHits(List<List<Integer>> hits) {

		int c=0;
		StringBuffer buf=new StringBuffer();
		for (List<Integer> in : hits){
			buf.append("   Hit: " + c + ": atoms=");
			if (in!=null){
				for (int i : in){
					buf.append(i+",");
				}
				buf.append("\n");
				c++;
			}
		}
		return buf.toString();
	}

	/**
	 * Assert equal number of classes and same ID of classes in two smart queries with colon notation
	 * @param reactClasses2
	 * @param prodClasses2
	 */
	private void assertEqualClasses(Map<Integer, String> reactClasses,
			Map<Integer, String> prodClasses) {

		//Assert equal number of classes
		if (reactClasses.size()!=prodClasses.size())
			throw new IllegalArgumentException("Number of classes not equal. " +
					"Reaction query has " + reactClasses.size() + 
					" but product query has " + prodClasses.size());

		//Assert classes exist in both lists
		for (int c : reactClasses.keySet()){
			if (!prodClasses.containsKey(c)) throw new IllegalArgumentException(
					"Class " + c + " exists in reactant querybut not in product.");
		}
		String classes="";
		for (int c : prodClasses.keySet()){
			if (!reactClasses.containsKey(c)) throw new IllegalArgumentException(
					"Class " + c + " exists in product query but not in reactant.");
			classes=classes + c + ",";
		}

		System.out.println("Product and Reactant have the following classes: " + classes);

	}

	/**
	 * Get classes for a smarts query with colon notaion
	 * @param smarts
	 * @return Map<Integer, String> linking a class ID to a group [xxx:ID] with brackets and ID within
	 */
	private Map<Integer, String> getClasses(String smarts) {

		//Create a descending sorted map to get classes in descending order
		Map<Integer, String> classes= new TreeMap<Integer, String>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		});

		//RegExp to find all groups (surrounded by [ and ])
		String regx="\\[([^\\]]*)\\]";
		Pattern groupPattern = Pattern.compile(regx);

		String nregx=":(\\d)";
		Pattern classPattern = Pattern.compile(nregx);

		//Extract all groups from input
		Matcher matcher = groupPattern.matcher(smarts);

		System.out.println("Extracting classes from query: " + smarts);

		while (matcher.find()) {
			String group=matcher.group();

			//Extract the number from the group
			Matcher classMatcher = classPattern.matcher(group);
			if (classMatcher.find()){
				String classString=classMatcher.group();

				//Remove first colon
				String classnumber=classString.substring(1);
				Integer clazz=Integer.parseInt(classnumber);

				System.out.println("     Found class=" + clazz + ", group=" + group);

				//add to list
				classes.put(clazz, group);
			}

		}		

		return classes;
	}

	/**
	 * Get classes for a smarts query with colon notaion
	 * @param smarts
	 * @return Map<Integer, String> linking a class ID to a group [xxx:ID] with brackets and ID within
	 */
	private List<String> getNonClasses(String smarts) {
		List<String> nonClasses= new ArrayList<String>();

		//RegExp to find all groups (surrounded by [ and ])
		String regx="\\[([^\\]]*)\\]";
		Pattern groupPattern = Pattern.compile(regx);

		String nregx=":(\\d)";
		Pattern classPattern = Pattern.compile(nregx);

		//Extract all groups from input
		Matcher matcher = groupPattern.matcher(smarts);

		System.out.println("Extracting non-classes from query: " + smarts);

		while (matcher.find()) {
			String group=matcher.group();

			//Extract the number from the group
			Matcher classMatcher = classPattern.matcher(group);
			if (!classMatcher.find()){
				System.out.println("     Found non class=" +  group);

				//add to list
				nonClasses.add(group);

			}
			
		}		

		return nonClasses;
	}
	/**
	 * Remove the starting [$( and trailing )] from a string
	 * @param q
	 * @return new string without the [$( and )] parts.
	 */
	private String removeDollarPart(String q) {

		q=q.trim();

		if (!q.startsWith("[$(")) 
			throw new IllegalArgumentException("The SMARTS query '"+ q + "' does not start with [$( ");
		if (!q.endsWith(")]")) 
			throw new IllegalArgumentException("The SMARTS query '"+ q + "' does not end with )] ");

		return q.substring(3,q.length()-2);

	}

	/**
	 * Sort out the AC having most atoms from a list of ACs and return it.
	 * @param acList
	 * @return
	 */
	public IAtomContainer getFirstMCSHavingMostAtoms(List<IAtomContainer> acList){
		IAtomContainer chosenAtomContainer = null;
		int maxCount = -1;
		for(IAtomContainer atoms: acList){
			if(atoms.getAtomCount() > maxCount){
				maxCount = atoms.getAtomCount();
				if(chosenAtomContainer == null){
					System.out.println("Choosing AC from MCSS having " + atoms.getAtomCount() + ".");
				}else{
					System.out.println("No, wait... Choosing AC from MCSS having " + atoms.getAtomCount() + " instead.");
				}
				chosenAtomContainer = atoms;
			}
		}
		return chosenAtomContainer;
	}

	/*
    private Map<Integer, Integer> extractClasses(String smarts, IAtomContainer ac) throws CDKException {

    	Map<Integer, Integer> retmap = new HashMap<Integer, Integer>();

//    	String s="[CH3:1]=[N:2]";

    	//RegExp to find all groups (surrounded by [ and ])
    	String regx="\\[([^\\]]*)\\]";
    	Pattern groupPattern = Pattern.compile(regx);

    	String nregx=":(\\d)";
    	Pattern classPattern = Pattern.compile(nregx);

    	//Extract all groups from input
    	Matcher matcher = groupPattern.matcher(smarts);

        while (matcher.find()) {
        	String group=matcher.group();

        	//Do SMARTS matching to get the atom number of this group
        	SMARTSQueryTool sm = new SMARTSQueryTool(group);
        	if (sm.matches(ac)){
        		//Assert only one atom matched
        		assert(sm.getMatchingAtoms().size()==1);

        		//Assign the atom to the correct class
//        		int atomno=sm.getMatchingAtoms().get(0);
        		//TODO

        	}

        	//Extract the number from the group
            Matcher classMatcher = classPattern.matcher(group);
            if (classMatcher.find()){
            	String classString=classMatcher.group();

            	//Remove first colon
            	String classnumber=classString.substring(1);
            	Integer clazz=Integer.parseInt(classnumber);

            	group=removeAllClasses(group);

            	int groupint=0;//BOGUS

            	//DO smarts matching here?

            	System.out.println("class="+classnumber + ", group=" + group);

            	//Add to map
            	retmap.put(clazz,groupint);
            }

        }		

		return retmap;
	}
	 */


}
