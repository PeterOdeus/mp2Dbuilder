package org.mp2dbuilder.smiles.smarts;

import java.util.ArrayList;
import java.util.Comparator;
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
		
		//Assert only one reactant and one product, this is all we can handle for now
		//TODO: Extend this to be more generic
		
		assert(reaction.getReactantCount()==1);
		assert(reaction.getProductCount()==1);
		
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		
		assert(reactant!=null);
		assert(product!=null);
		
		reactantAtomNumbers=new ArrayList<List<Integer>>();
		productAtomNumbers=new ArrayList<List<Integer>>();
		
		//Count number of classes in reactant and product and require identical
		assertEqualClasses(reactClasses, prodClasses);

		// If no matches in reactant, fail early
		//We have cut away class part but kept the dollar part
		SMARTSQueryTool rcQueryTool = new SMARTSQueryTool(reactantQueryNoClasses);
		if (!rcQueryTool.matches(reactant)){
			System.out.println("== No match in first reactant query: " + reactantQueryNoClasses);
			return false;
		}

		//
		SMARTSQueryTool reactQueryTool = new SMARTSQueryTool(reactantQueryNoDollar);

		// If no matches in product, fail early
		SMARTSQueryTool prodQueryTool = new SMARTSQueryTool(productQueryNoClasses);
		if (!prodQueryTool.matches(product)){
			System.out.println("== No match in first product query: " + productQueryNoClasses);
			return false;
		}

		//We have at least one match in reactant and one in product.
		//Save these indices in list
		List<List<Integer>> putativeRC_Atomlist = rcQueryTool.getUniqueMatchingAtoms();
		for (List<Integer> list : putativeRC_Atomlist){
			System.out.println("Put rc list" + list.toString());
			for (Integer rc : list){
				System.out.println("rc: " + rc);
			}
		}
		System.out.println(":"+reactantQueryNoDollar+":");
		reactQueryTool.matches(reactant);
		List<List<Integer>> fullReactantHit_AtomList = reactQueryTool.getMatchingAtoms();// getUniqueMatchingAtoms();//It might be needed to associate the reactant hits with the rc.
		for (List<Integer> list : fullReactantHit_AtomList){
			System.out.println("Reactant list" + list.toString());
			for (Integer rc : list){
				System.out.println("rc: " + rc);
			}
		}

		
		
		
		List<List<Integer>> fullProductHit_AtomList = prodQueryTool.getUniqueMatchingAtoms();

		//Generate and pick largest MCS. Since there are only one structure as a reactant and one structure as a product the largest MCS should be ok.
		IAtomContainer mcs = getMCS(reactant, product);
		if (mcs==null || mcs != null && mcs.getAtomCount()<=0){
			System.out.println("No overlaps in MCSS. Exiting.");
			return false;
		}
		System.out.println("MCSS has " + mcs.getAtomCount() + " atoms.");

		//Old CDK code, bug filed w/ junit test.
		//TODO: use this later maybe.
		//List<RMap> mcss = UniversalIsomorphismTester.getSubgraphAtomsMap(reactant, product);		

		//Workaround for CDK bug. Makes use of property to link atoms.
		//Add identifier fields used to map atoms from reactant to product.
		AtomMapperUtil mapperUtil = new AtomMapperUtil();
		mapperUtil.setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);

		// Setup the structure that holds the class labels.
		List<List<Integer>> mcsClasses = new ArrayList<List<Integer>>();
		List<List<Integer>> reactantClasses = new ArrayList<List<Integer>>();
		for (IAtom atom : reactant.atoms()){
				if (atom.getProperty(COMMON_ID_FIELD_NAME)!=null){
					//System.out.println(atom.getProperty(COMMON_ID_FIELD_NAME));
					mcsClasses.add(new ArrayList<Integer>());
					reactantClasses.add(new ArrayList<Integer>());
			}
		}

		
		String mcsstr="";
		for (IAtom atom : mcs.atoms()){
			mcsstr=mcsstr + mcs.getAtomNumber(atom) + ",";
		}
		System.out.println("MCS contains: " + mcsstr);
		
		System.out.println("Reactant hits:\n" + debugHits(fullReactantHit_AtomList));
		System.out.println("Product hits:\n" + debugHits(fullProductHit_AtomList));
		
		//Remove all indices which are not available in MCS. If this makes some hits
		//or RC empty, remove the empty lists.
		//fullReactantHit_AtomList=removeIndicesWithoutCommonId(fullReactantHit_AtomList, reactant);//Don't do it for reactant atoms it might remove the rc.
		fullProductHit_AtomList=removeIndicesWithoutCommonId(fullProductHit_AtomList, product);
		//System.out.println("Reactant hits pruned by MCS:\n" + debugHits(fullReactantHit_AtomList));
		System.out.println("Product hits pruned by MCS:\n" + debugHits(fullProductHit_AtomList));
			
		//Verify conservation per RC and class
		//Start with reactant
		System.out.println("** Starting conservation checking **");
		boolean addedToMCSClasses = false; //If no class gets added to mcsClasses we can break early and return false. Behovs inte om saker lyckas sa breakar vi innan rc loopen ar slut.
		int rcno=0; //0-based index is easiest
		Set<Integer> rcToRemove=new HashSet<Integer>();  //Add non-conserved RCs here
		for (List<Integer> prc : putativeRC_Atomlist){
			System.out.println(" %% Current RC: " + rcno + " with hits: " + prc.get(0));
			
			// The possible rc must be part of at least one of the reactant sets. Pick the first one that contains the prc.
			// fullReactantHit_AtomList has to have the same length as putativeRC_Atomlist.
			List<Integer> curReactantSet = new ArrayList<Integer>();
			List<Integer> reactToRemove = new ArrayList<Integer>();
			boolean breaking = false;
			for (List<Integer> list : fullReactantHit_AtomList){
				for (int p : prc){
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
			fullReactantHit_AtomList.removeAll(reactToRemove);
			
			//Remove the indices not in the MCS.
			System.out.println("   Produced hits pruned by the current smarts: " + curReactantSet.toString());
			curReactantSet = removeIndicesSimpleListWithoutCommonId(curReactantSet, reactant);
			System.out.println("   Produced hits pruned by the current smarts: " + curReactantSet.toString());
						
			//Assign classes to the RC hit
			for (int i : reactClasses.keySet()){
				//REACTANT PART
				String rclass = reactClasses.get(i);
				String rclass_noclass=removeAllClasses(rclass);
				System.out.println("\n## Reaction class: " + i + "=" + rclass + "=" + rclass_noclass);
				Set<Integer> reactHitsconcat=null;
				Set<Integer> reactHitsconcat_pruned=null;
				reactQueryTool.setSmarts(rclass_noclass);
				if (!reactQueryTool.matches(reactant)){
					System.out.println("   Produced no hits.");
				}else{
					List<List<Integer>> reactHits = reactQueryTool.getUniqueMatchingAtoms();
					reactHitsconcat=concatIndices(reactHits);
					System.out.println("   Produced hits: " + debugHits(reactHitsconcat));
					List<List<Integer>> reactHits_pruned = removeIndicesWithoutCommonId(reactHits, reactant);
					reactHitsconcat_pruned = concatIndices(reactHits_pruned);
					System.out.println("   Produced hits pruned by MCS: " + debugHits(reactHitsconcat_pruned));
					// Add reactant classes to the atoms that were originally hit by the smarts. 
					for (int j : reactHitsconcat_pruned){
						if (curReactantSet.contains(j)){
							System.out.println("Adding class: "+ i + ", to reactant atom: " + j);
							reactantClasses.get(j).add(i);
						}
					}

				}
					
				//PRODUCT PART
				int[] mcsSize = new int[mcsClasses.size()];
				// Init all elements to one since we are forming a product for the number of permutations below.
				for (int mcsSizeEl : mcsSize){
					mcsSizeEl = 1;
				}
				String pclass = prodClasses.get(i);
				String pclass_noclass=removeAllClasses(pclass);
				System.out.println("Product class: " + i + "=" + pclass + "=" + pclass_noclass);
				Set<Integer> prodHitsconcat=null;
				Set<Integer> prodHitsconcat_pruned=null;
				prodQueryTool.setSmarts(pclass_noclass);
				if (!prodQueryTool.matches(product)){
					//no hit. How to deal with this?
					//TODO
					System.out.println("   Produced no hits.");
				}else{
					List<List<Integer>> prodHits = prodQueryTool.getUniqueMatchingAtoms();
					prodHitsconcat=concatIndices(prodHits);
					System.out.println("   Produced hits: " + debugHits(prodHitsconcat));

					List<List<Integer>> prodHits_pruned = removeIndicesWithoutCommonId(prodHits, product);
					prodHitsconcat_pruned = concatIndices(prodHits_pruned);
					System.out.println("   Produced hits pruned by MCS: " + debugHits(prodHitsconcat_pruned));
					// Remove hits not hit by the original smarts.
					Set<Integer> prodToRemove = new HashSet<Integer>();
					for (int prodHit : prodHitsconcat_pruned){
						for (List<Integer> prodList : fullProductHit_AtomList){
							if (!prodList.contains(prodHit)){
								System.out.println("Prod atom to remove: " + prodHit);
								prodToRemove.add(prodHit);
							}	
						}
					}
					prodHitsconcat_pruned.removeAll(prodToRemove);
					System.out.println("   Produced hits by original SMARTS: " + debugHits(fullProductHit_AtomList));
					System.out.println("   Produced hits pruned by original SMARTS: " + debugHits(prodHitsconcat_pruned));
					
					for (int j : prodHitsconcat_pruned){
						String commonId = (String) product.getAtom(j).getProperty(COMMON_ID_FIELD_NAME);
						// Pull out the corresponding atom from the reactant. 
						// If the same class exists in the reactant and the product add it to the mcsClasses for that particular mcs (product) atom.
						System.out.println("Checking pruned product hit: " + j + ", with mapping: " + commonId);
						for (IAtom atom : reactant.atoms()){
							System.out.println("Common ID: " + atom.getProperty(COMMON_ID_FIELD_NAME));							
							if (commonId.equals((String)atom.getProperty(COMMON_ID_FIELD_NAME))){
								System.out.println("Pruned product hit" + j + ":" + atom.getProperty(COMMON_ID_FIELD_NAME) + ":" + commonId);
								if (reactantClasses.get(reactant.getAtomNumber(atom)).contains(i)){
									System.out.println("Adding class: "+ i + ", to mcs atom: " + j);
									if (!mcsClasses.get(j).contains(i)) {
										mcsClasses.get(j).add(i);
										mcsSize[j] = mcsClasses.get(j).size();
									}
									addedToMCSClasses = true;
								}
							}
						}
					}

				}
			
				if (addedToMCSClasses){
					
					int[] curIndex = new int[mcsClasses.size()];
					for (int curIndexEl : curIndex){
						curIndexEl = 0;
					}					
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
						System.out.println("Len class list: " + classList.size());
						System.out.println("Len reactantClasses list: " + reactClasses.size());
						if (classList.size() == reactClasses.size() ){
							System.out.println("Returning TRUE!");
							return true;
						}
						// Update the indices defined by curIndex.
						updateIndices(curIndex, mcsClasses, indexToIncrease);
					}		
				}
			}
			rcno++;
		}
		
		return false;

	}
	
	
	private void updateIndices(int[] curIndex, List<List<Integer>> mcsClasses, int indexToIncrease) {
		// Check if it will be too large.
		if (curIndex[indexToIncrease]+1 >= mcsClasses.get(indexToIncrease).size()){
			curIndex[indexToIncrease] = 0; //Reset
			// Increase the next more significant index.
			//boolean foundNextIndex = false;
			while (true){
				indexToIncrease++;
				if (curIndex[indexToIncrease] < mcsClasses.get(indexToIncrease).size()){
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

	/**
	 * Do matching in reaction and product.
	 * @param reaction The reaction to be queried
	 * @return true if there is a match in reactant and product molecules, 
	 * and all atoms marked with class are conserved in reactant and product.
	 * @throws Exception 
	 */
	public boolean matchesOla(IReaction reaction) throws Exception {
		
		//Assert only one reactant and one product, this is all we can handle for now
		//TODO: Extend this to be more generic
		
		assert(reaction.getReactantCount()==1);
		assert(reaction.getProductCount()==1);
		
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		
		assert(reactant!=null);
		assert(product!=null);
		
		reactantAtomNumbers=new ArrayList<List<Integer>>();
		productAtomNumbers=new ArrayList<List<Integer>>();
		
		//Count number of classes in reactant and product and require identical
		assertEqualClasses(reactClasses, prodClasses);

		// If no matches in reactant, fail early
		//We have cut away class part but kept the dollar part
		SMARTSQueryTool reactQueryTool = new SMARTSQueryTool(reactantQueryNoClasses);
		if (!reactQueryTool.matches(reactant)){
			System.out.println("== No match in first reactant query: " + reactantQueryNoClasses);
			return false;
		}

		// If no matches in product, fail early
		SMARTSQueryTool prodQueryTool = new SMARTSQueryTool(productQueryNoClasses);
		if (!prodQueryTool.matches(product)){
			System.out.println("== No match in first product query: " + productQueryNoClasses);
			return false;
		}

		//We have at least one match in reactant and one in product.
		//Save these indices in list
		List<List<Integer>> putativeRC_Atomlist = reactQueryTool.getUniqueMatchingAtoms();
		List<List<Integer>> fullProductHit_AtomList = prodQueryTool.getUniqueMatchingAtoms();

		//Generate and pick largest MCS.
		IAtomContainer mcs = getMCS(reactant, product);
		if (mcs==null || mcs != null && mcs.getAtomCount()<=0){
			System.out.println("No overlaps in MCSS. Exiting.");
			return false;
		}
		System.out.println("MCSS has " + mcs.getAtomCount() + " atoms.");

		//Old CDK code, bug filed w/ junit test.
		//TODO: use this later maybe.
		//List<RMap> mcss = UniversalIsomorphismTester.getSubgraphAtomsMap(reactant, product);		

		//Workaround for CDK bug. Makes use of property to link atoms.
		//Add identifier fields used to map atoms from reactant to product.
		AtomMapperUtil mapperUtil = new AtomMapperUtil();
		mapperUtil.setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);

		
		
		//***************************
		// REWORKED conservation
		//***************************

//		allRC - Determine possible RC //putativeRCs
//		Determine atom mappings between reactant and product //mcs
//		reactantHits = get list of of lists of smarts hits for $-removed reaction smarts. One list for each possible rc.//fullreactantQueryIndices 
//		productHits = getList of product smarts hits //fullProductQueryIndices
//		remove atoms from the reactantHits and productHits that don't exist in the MCS.

		String mcsstr="";
		for (IAtom atom : mcs.atoms()){
			mcsstr=mcsstr + mcs.getAtomNumber(atom) + ",";
		}
		System.out.println("MCS contains: " + mcsstr);
		
		System.out.println("Reaction hits:\n" + debugHits(putativeRC_Atomlist));
		System.out.println("Product hits:\n" + debugHits(fullProductHit_AtomList));
		
		//Remove all indices which are not available in MCS. If this makes some hits
		//or RC empty, remove the empty lists.
		putativeRC_Atomlist=removeIndicesWithoutCommonId(putativeRC_Atomlist, reactant);
		fullProductHit_AtomList=removeIndicesWithoutCommonId(fullProductHit_AtomList, product);
		System.out.println("Reaction hits pruned by MCS:\n" + debugHits(putativeRC_Atomlist));
		System.out.println("Product hits pruned by MCS:\n" + debugHits(fullProductHit_AtomList));
			
		//Verify conservation per RC and class
		//Start with reactant
		System.out.println("** Starting conservation checking **");
		int rcno=0; //0-based index is easiest
		Set<Integer> rcToRemove=new HashSet<Integer>();  //Add non-conserved RCs here
		for (List<Integer> Ratoms : putativeRC_Atomlist){
			System.out.println(" %% Current RC: " + rcno + " with hits: " + Ratoms.get(0));
			
			//Assign classes to the RC hit
			for (int i : reactClasses.keySet()){
				
				//REACTION PART
				String rclass = reactClasses.get(i);
				String rclass_noclass=removeAllClasses(rclass);
				System.out.println("\n## Reaction class: " + i + "=" + rclass + "=" + rclass_noclass);
				Set<Integer> reactHitsconcat=null;
				Set<Integer> reactHitsconcat_pruned=null;
				reactQueryTool.setSmarts(rclass_noclass);
				if (!reactQueryTool.matches(reactant)){
					System.out.println("   Produced no hits.");
				}else{
					List<List<Integer>> reactHits = reactQueryTool.getUniqueMatchingAtoms();
					reactHitsconcat=concatIndices(reactHits);
					System.out.println("   Produced hits: " + debugHits(reactHitsconcat));
					List<List<Integer>> reactHits_pruned = removeIndicesWithoutCommonId(reactHits, reactant);
					reactHitsconcat_pruned = concatIndices(reactHits_pruned);
					System.out.println("   Produced hits pruned by MCS: " + debugHits(reactHitsconcat_pruned));

				}
					
				//PRODUCT PART
				String pclass = prodClasses.get(i);
				String pclass_noclass=removeAllClasses(pclass);
				System.out.println("Product class: " + i + "=" + pclass + "=" + pclass_noclass);
				Set<Integer> prodHitsconcat=null;
				Set<Integer> prodHitsconcat_pruned=null;
				prodQueryTool.setSmarts(pclass_noclass);
				if (!prodQueryTool.matches(product)){
					//no hit. How to deal with this?
					//TODO
					System.out.println("   Produced no hits.");
				}else{
					List<List<Integer>> prodHits = prodQueryTool.getUniqueMatchingAtoms();
					prodHitsconcat=concatIndices(prodHits);
					System.out.println("   Produced hits: " + debugHits(prodHitsconcat));

					List<List<Integer>> prodHits_pruned = removeIndicesWithoutCommonId(prodHits, product);
					prodHitsconcat_pruned = concatIndices(prodHits_pruned);
					System.out.println("   Produced hits pruned by MCS: " + debugHits(prodHitsconcat_pruned));

				}
				
				//Check conservation between result sets via MCS
				if (isConserved(reactant, reactHitsconcat_pruned, product, prodHitsconcat_pruned)){
					System.out.println(" ==> RC: " + rcno + " IS CONSERVED");
				}else{
					System.out.println(" ==> RC: " + rcno + " IS NOT CONSERVED");
					
					//Remove this RC
					rcToRemove.add(rcno);
				}

//				//FIXME
//				Set<Integer> inter=new HashSet<Integer>(reactHitsconcat);
//				inter.retainAll(prodHitsconcat);
				
			}
			
			rcno++;
		}
		
		//Remove all non-conserved RCs
		List<List<Integer>> istorem=new ArrayList<List<Integer>>();
		for (int i : rcToRemove){
			istorem.add(putativeRC_Atomlist.get(i));
		}
		
		for (List<Integer> l : istorem){
			putativeRC_Atomlist.remove(l);
		}
		

		//Return conserved RCs
		reactantAtomNumbers=putativeRC_Atomlist;
		
		System.out.println("Resulting RC (conserved):\n" + debugHits(reactantAtomNumbers));

		//For product, return all matches for now (
		productAtomNumbers=fullProductHit_AtomList;
		System.out.println("Resulting Product hits (not conserved (yet)):\n" + debugHits(productAtomNumbers));
		
		if (reactantAtomNumbers.size()<=0){
			System.out.println("No RC found. Return false.");
			return false;
		}
		
//
//		for rc in allRC
//		    pick the list of smart hits (reactant hits) corresponding to the rc
//		    assign classes to the reactantHits and productHits using individual atom properties
//		    if we have a simulataneous mapping of all classes in the hits shared between the reactant and the product
//		        add rc to list of established rc:s
		
		return true;
/*		
		//TODO: remove
		//***************************
		//Ok, now extract one class at a time for reactant and product and verify class conservation.
		//Start with highest class (from the right of String) and stepwise cut away query parts.
		//***************************

		//The starting values, dollar parts removed from reactant part (product query has no such constraint)
		String workingReactQuery=reactantQueryNoDollar;
		String workingProductQuery=productQuery;
		
		System.out.println("** Extracting subclasses **");

		//Concat indices for hits in full SMARTS with no classes. This is start of algo.
		Set<Integer> previousRI=concatIndices(fullreactantQueryIndices);
		Set<Integer> previousPI=concatIndices(fullProductQueryIndices);
		System.out.println("  Reactant full indices: " + debugHits(previousRI));
		System.out.println("  Product full indices: " + debugHits(previousPI));

		//Loop over all available classes in descending order
		for (int classid : reactClasses.keySet()){
			
			System.out.println("Processing REAC=" + workingReactQuery + " and PROD=" + workingProductQuery);
			
			String reactGroup=reactClasses.get(classid);
			String prodGroup=prodClasses.get(classid);
			System.out.println("   Removing last class: " + classid + 
					" which is R=" + reactGroup + " and P=" + prodGroup);

			//We require classes in incrementing order in query String, hence just cut away from last class part's index in string
			workingReactQuery=workingReactQuery.substring(0,workingReactQuery.indexOf(reactGroup));
			workingProductQuery=workingProductQuery.substring(0,workingProductQuery.indexOf(prodGroup));	

			//Verify that we have more classes to process
			if (!(workingProductQuery==null || workingReactQuery==null || workingProductQuery.equals("") || workingReactQuery.equals(""))){

				System.out.println("   Class extraction result: REACT=" + workingReactQuery + " and PROD=" + workingProductQuery);

				//Look up smarts matches for these subqueries with class=classid removed
				
				//REACTION QUERY
			    //==============
				reactQueryTool.setSmarts(removeAllClasses(workingReactQuery));
				if (!reactQueryTool.matches(reactant)) 
					throw new IllegalArgumentException("No hits for reactant query " + workingReactQuery);
				List<List<Integer>> workingReactionIndices = reactQueryTool.getUniqueMatchingAtoms();

				//Concatenate and find differences from previous round
				Set<Integer> concatRI=concatIndices(workingReactionIndices);
				System.out.println("  Reaction hits: " + debugHits(concatRI));
				
			    //Get the difference, i.e. what we have cut away by removing this class
			    Set<Integer> Rdifference = new HashSet<Integer>(previousRI);
			    Rdifference.removeAll(concatRI);
				System.out.println("  Reaction difference: " + debugHits(Rdifference));

			    //Extract and store intersection for next round.
			    previousRI.retainAll(concatRI);
				System.out.println("  Product intersection: " + debugHits(previousRI));

			    
				//PRODUCT QUERY
			    //==============
				prodtQueryTool.setSmarts(removeAllClasses(workingProductQuery));
				if (!prodtQueryTool.matches(product)) 
					throw new IllegalArgumentException("No hits for reactant query " + workingReactQuery);
				List<List<Integer>> workingProductIndices = prodtQueryTool.getUniqueMatchingAtoms();
				
				//Concatenate and find differences from previous round
				Set<Integer> concatPI=concatIndices(workingProductIndices);
				System.out.println("  Product hits: " + debugHits(concatPI));
				
			    Set<Integer> Pdifference = new HashSet<Integer>(previousPI);
			    Pdifference.removeAll(concatPI);
				System.out.println("  Product difference: " + debugHits(Pdifference));

			    //Extract and store intersection for next round.
			    previousPI.retainAll(concatPI);
				System.out.println("  Product intersection: " + debugHits(previousRI));


			    //Confirm reactant and product differences are conserved for this class
			    //==================
			    System.out.println("  == Testing reactant conservation");
			    if (!isConserved(reactant,Rdifference,product,Pdifference)){
					System.out.println("  == Failed conservation test. This ends matching. ==");
					return false;
			    }

			    System.out.println("  == Testing product conservation");
			    if (!isConserved(product,Pdifference,reactant,Rdifference)){
					System.out.println("  == Failed conservation test. This ends matching. ==");
					return false;
			    }
	
			    System.out.println("  Conservation test OK. Continue to next class.");
			    //TODO: continue here. We have not stored the conserved part yet.

				
				
//				consReactIndices = getConservedReactantIndices(reactant, fullreactantQueryIndices, product, fullProductQueryIndices);
//				consProductIndices = getConservedProductIndices(reactant, fullreactantQueryIndices, product, fullProductQueryIndices);
//				if (consProductIndices.size()<=0 || consReactIndices.size()<=0){
//					System.out.println("== No reactant atoms are conserved on first conservation test. Exiting. ==");
//					return false;
//				}


				System.out.println("  END OF loop for CLASS: " + classid);
			}else{
				System.out.println("No more classes available.");
			}

		}

		System.out.println("## DONE ##");

		//So, the reaction hits should be the first stored indices with full query, including dollar
		reactantAtomNumbers.add(consReactIndices);
		
		//Product hits should be conserved hits. TODO: Implement this. For now, show all hits (including non-conserved)
		productAtomNumbers=fullProductQueryIndices;

		return true;
		*/
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
