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
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
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
	private List<List<Integer>> reactionAtomNumbers;
	private List<List<Integer>> productAtomNumbers;
	private String reactionQuery;
	private String productQuery;
	private String reactionQueryNoClasses;
	private String productQueryNoClasses;

	private String reactionQueryNoDollar;
	private String productQueryNoDollar;

	private Map<Integer, String> reactClasses;
	private Map<Integer, String> prodClasses;

	/**
	 * Constructor.
	 * @param reactionQuery the SMARTS string for the reactant part of the reaction
	 * @param productQuery the SMARTS string for the product part of the reaction
	 */
	public ReactionSmartsQueryTool(String reactionQuery, String productQuery) {
		this.reactionQuery=reactionQuery;
		this.productQuery=productQuery;

		//remove the [$( partof reactant
		reactionQueryNoDollar=removeDollarPart(reactionQuery);
		
		//product query must not have [$(
		productQueryNoDollar=productQuery;

		//Extract classes to map INT > String
		reactClasses = getClasses(reactionQueryNoDollar);
		prodClasses = getClasses(productQueryNoDollar);

		//Extract full query without dollar and without classes
		reactionQueryNoClasses=removeAllClasses(reactionQuery);
		productQueryNoClasses=removeAllClasses(productQuery);

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
		return reactionAtomNumbers;
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
		
		reactionAtomNumbers=new ArrayList<List<Integer>>();
		productAtomNumbers=new ArrayList<List<Integer>>();
		
		//Count number of classes in reactant and product and require identical
		assertEqualClasses(reactClasses, prodClasses);

		// If no matches in reactant, fail early
		SMARTSQueryTool reactQueryTool = new SMARTSQueryTool(reactionQueryNoClasses);
		if (!reactQueryTool.matches(reactant)){
			System.out.println("== No match in first reactant query: " + reactionQueryNoClasses);
			return false;
		}

		// If no matches in product, fail early
		SMARTSQueryTool prodtQueryTool = new SMARTSQueryTool(productQueryNoClasses);
		if (!prodtQueryTool.matches(product)){
			System.out.println("== No match in first product query: " + productQueryNoClasses);
			return false;
		}

		//We have at least one match in reactant and one in product.
		//Save these indices in list
		List<List<Integer>> fullReactionQueryIndices = reactQueryTool.getUniqueMatchingAtoms();
		System.out.println("Reaction hits:\n" + debugHits(fullReactionQueryIndices));

		List<List<Integer>> fullProductQueryIndices = prodtQueryTool.getUniqueMatchingAtoms();
		System.out.println("Product hits:\n" + debugHits(fullProductQueryIndices));

		//Preprocess AC for MCSS (TODO: Verify if this is needed)
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(reactant);
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(product);
		CDKHueckelAromaticityDetector.detectAromaticity(reactant);
		CDKHueckelAromaticityDetector.detectAromaticity(product);

		//We now need an MCSS to link atoms
		
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		
		//How many overlaps can we get? Anyway, pick largest for now TODO: Verify this
		IAtomContainer mcs = getFirstMCSHavingMostAtoms(mcsList);
		
		//List<RMap> mcss = UniversalIsomorphismTester.getSubgraphAtomsMap(reactant, product);		
		if (mcs==null || mcs != null && mcs.getAtomCount()<=0){
			System.out.println("No overlaps in MCSS. Exiting.");
			return false;
		}
		
		System.out.println("MCSS has " + mcs.getAtomCount() + " atoms.");
		
		//Add identifier fields used to map atoms from reactant to product.
		AtomMapperUtil mapperUtil = new AtomMapperUtil();
		mapperUtil.setCommonIds(COMMON_ID_FIELD_NAME, mcs, reactant, product);
		
		//Verify conservation on this point or fail
//		if (!(areAnyAtomConserved(reactant,fullReactionQueryIndices, product, fullProductQueryIndices))){
//			System.out.println("== No reactant atoms are conserved on first conservation test. Exiting. ==");
//			return false;
//		}
		
		List<Integer> consReactIndices = getConservedReactantIndices(reactant, fullReactionQueryIndices, product, fullProductQueryIndices);
		List<Integer> consProductIndices = getConservedProductIndices(reactant, fullReactionQueryIndices, product, fullProductQueryIndices);
		if (consProductIndices.size()<=0 || consReactIndices.size()<=0){
			System.out.println("== No reactant atoms are conserved on first conservation test. Exiting. ==");
			return false;
		}

		

		//***************************
		//Ok, now extract one class at a time for reactant and product and verify class conservation.
		//Start with highest class (from the right of String) and stepwise cut away query parts.
		//***************************

		//The starting values, dollar parts removed from reactant part (product query has no such constraint)
		String workingReactQuery=reactionQueryNoDollar;
		String workingProductQuery=productQuery;
		
		System.out.println("** Extracting subclasses **");

		//Concat indices for hits in full SMARTS with no classes. This is start of algo.
		Set<Integer> previousRI=concatIndices(fullReactionQueryIndices);
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

				
				
//				consReactIndices = getConservedReactantIndices(reactant, fullReactionQueryIndices, product, fullProductQueryIndices);
//				consProductIndices = getConservedProductIndices(reactant, fullReactionQueryIndices, product, fullProductQueryIndices);
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
		reactionAtomNumbers.add(consReactIndices);
		
		//Product hits should be conserved hits. TODO: Implement this. For now, show all hits (including non-conserved)
		productAtomNumbers=fullProductQueryIndices;

		return true;
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
			System.out.println("+ checking atom index=" + ratom);
			
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
				System.out.println("+++ NOT-CONSERVED, since index" + ratom + " is NOT present in target list");
//				return false; //Found a non-conserved atom
			}else{
				System.out.println("+++ CONSERVED, since index " + ratom + " is present in target list");
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
			for (int i : in){
				buf.append(i+",");
			}
			buf.append("\n");
			c++;
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
