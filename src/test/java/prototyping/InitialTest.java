package prototyping;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import metaprint2d.Constants;
import metaprint2d.Fingerprint;
import metaprint2d.analyzer.FingerprintGenerator;
import metaprint2d.analyzer.data.AtomData;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.AtomMappingTools;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

@SuppressWarnings("unused")
public class InitialTest {

	private static LoggingTool logger = new LoggingTool(InitialTest.class);

	@BeforeClass public static void setup() {
		logger = new LoggingTool(InitialTest.class);
		//setSimpleChemObjectReader(new MDLRXNReader(), "data/mdl/reaction-1.rxn");
	}

	@Test public void testRDFReactioniSet() throws Exception {
		//String filename = "data/mdl/qsar-reaction-test.rdf";
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		Assert.assertNotNull(reactionSet);


		Assert.assertEquals(1, reactionSet.getReactionCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactantCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getReactants().getMoleculeCount());
		IMolecule molecule = reactionSet.getReaction(0).getReactants().getMolecule(0);
		Assert.assertEquals(15, molecule.getAtomCount());
		IAtom atom = molecule.getAtom(0);
		//Assert.assertEquals(2, reactionSet.getReaction(0).getReactants().getMolecule(1).getAtomCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getProductCount());
		Assert.assertEquals(1, reactionSet.getReaction(0).getProducts().getMoleculeCount());
		Assert.assertEquals(11, reactionSet.getReaction(0).getProducts().getMolecule(0).getAtomCount());
		/*Assert.assertEquals(2, reactionSet.getReaction(0).getProducts().getMolecule(1).getAtomCount());


        Assert.assertEquals(1, reactionSet.getReaction(1).getReactantCount());
        Assert.assertEquals(3, reactionSet.getReaction(1).getReactants().getMolecule(0).getAtomCount());
        Assert.assertEquals(1, reactionSet.getReaction(1).getProductCount());
        Assert.assertEquals(2, reactionSet.getReaction(1).getProducts().getMolecule(0).getAtomCount());*/

	}

	@Test public void testMCSSingle() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(1, mcsList.size());
	}
	
	private List<IAtomContainer> getAtomContainersForReaction(int reactionId) throws Exception{
		IAtomContainer reactant = null;
		IAtomContainer product = null; 
		IAtomContainer mcs = null;
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = null;
		List<IAtomContainer> returnList = null;
		try{
			ins = this.getClass().getClassLoader().getResourceAsStream(filename);
			ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
			reader.setInitialRiregNo(reactionId);
			IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			//appendCommonIds(reactant, product);
			List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
			mcs = mcsList.get(0);
			returnList = new ArrayList<IAtomContainer>();
			returnList.add(reactant);
			returnList.add(product);
			returnList.add(mcs);
		}finally{
			if(ins != null){
				ins.close();
			}
		}
		return returnList;		
	}
	
	@SuppressWarnings("unchecked")
	private List<? extends Object> getCommonIdAtomContainersForReaction(int reactionId) throws Exception{
		List returnList = getAtomContainersForReaction(reactionId);
		IAtomContainer reactant = (IAtomContainer)returnList.get(0);
		IAtomContainer product = (IAtomContainer)returnList.get(1);
		IAtomContainer mcs = (IAtomContainer)returnList.get(2);
		MetaboliteHandler metaboliteHandler = new MetaboliteHandler();
		Map<Integer,Integer> mappedReactantAtoms = metaboliteHandler.getAtomMappings(reactant, mcs);
		//AtomMappingTools.mapAtomsOfAlignedStructures(reactant, mcs, mappedReactantAtoms);
		Map<Integer,Integer> mappedProductAtoms = metaboliteHandler.getAtomMappings(product, mcs);
		//AtomMappingTools.mapAtomsOfAlignedStructures(product, mcs, mappedProductAtoms);
		
		returnList.add(mappedReactantAtoms);
		returnList.add(mappedProductAtoms);
		
		Collection<Integer> reactantIds = mappedReactantAtoms.values();
		Collection<Integer> productIds = mappedProductAtoms.values();
		Collection<Integer> mcsIds = new HashSet();
		mcsIds.addAll(reactantIds);
		mcsIds.addAll(productIds);
		for(int id: mcsIds){
			mcs.getAtom(id).setProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME, Integer.toString(id));
		}
		metaboliteHandler.setIds(MetaboliteHandler.COMMON_ID_FIELD_NAME, reactant, mappedReactantAtoms);
		metaboliteHandler.setIds(MetaboliteHandler.COMMON_ID_FIELD_NAME, product, mappedProductAtoms);
		return returnList;
	}
	
	@Test public void testCommonIdEquality() throws Exception {
		for(int i = 1; i < 2; i++){
			doTestCommonIdEquality(i);
		}
	}
	
	private void doTestCommonIdEquality(int reactionIndex) throws Exception {
		List<? extends Object> returnList = getCommonIdAtomContainersForReaction(reactionIndex);
		IAtomContainer reactant = (IAtomContainer)returnList.get(0);
		IAtomContainer product = (IAtomContainer)returnList.get(1);
		IAtomContainer mcs = (IAtomContainer)returnList.get(2);
		Map<Integer,Integer> mappedReactantAtoms = (Map<Integer,Integer>)returnList.get(3);
		Map<Integer,Integer> mappedProductAtoms = (Map<Integer,Integer>)returnList.get(4);
		StringBuffer sb = new StringBuffer();
		
		int maxAtomCount = Math.max(reactant.getAtomCount(), product.getAtomCount());
		int i = 0;
		sb.append("\nIndex");
		while(i < maxAtomCount){
			sb.append("\t" + i++);
		}
		sb.append("\nMCS");
		for(IAtom mcsAtom: mcs.atoms()){
			sb.append("\t" + mcsAtom.getSymbol());
		}
		sb.append("\nReac");
		for(IAtom reactantAtom: reactant.atoms()){
			sb.append("\t" + reactantAtom.getSymbol());
		}
		sb.append("\nProd");
		for(IAtom productAtom: product.atoms()){
			sb.append("\t" + productAtom.getSymbol());
		}
			
		logger.debug(sb.toString());
		logger.debug("mappedReactantAtoms\n" + mappedReactantAtoms);
		logger.debug("mappedProductAtoms\n" + mappedProductAtoms);
		IAtom atom1; IAtom atom2;
		for(int index: mappedReactantAtoms.keySet()){
				atom1 = reactant.getAtom(index);
				atom2 = mcs.getAtom(mappedReactantAtoms.get(index));
			try{
				Assert.assertTrue(
						atom1.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)
						.equals(atom2.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)));
				Assert.assertTrue(atom1.getSymbol().equals(atom2.getSymbol()));
			}catch(AssertionError err){
				String msg = "failed to match id for reactant.getAtom(" + index + ")";
				logger.debug(msg);
				throw new AssertionError(err.toString() + ". " + msg);
			}
		}
		for(int index: mappedProductAtoms.keySet()){
			atom1 = product.getAtom(index);
			atom2 = mcs.getAtom(mappedProductAtoms.get(index));
			try{
				Assert.assertTrue(
						atom1.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)
						.equals(atom2.getProperty(MetaboliteHandler.COMMON_ID_FIELD_NAME)));
				Assert.assertTrue(atom1.getSymbol().equals(atom2.getSymbol()));
			}catch(AssertionError err){
				String msg = "failed to match id for product.getAtom(" + index + ")";
				logger.debug(msg);
				throw new AssertionError(err.toString() + ". " + msg);
			}
		}
	}
	
	
	@Test public void testMultipleMCS() throws Exception {
		String filename = "data/mdl/24thRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}
	
	@Test public void testMultipleRiRegs() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
		for(int i=0;i<=49;i++){
			try{
				reactionSet = (IReactionSet)reader.read(new NNReactionSet());
				IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
				IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			}catch(NullPointerException npe){
				System.out.println(i);
			}catch(java.lang.AssertionError err){
				System.out.println(i);
				throw err;
			}
		}
	}
	
	@Test public void testAbsentAtomId() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(2);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertNull(mcsList.get(0).getAtom(10).getID());
	}
	
	@Test public void testExtractSingleMCS() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(26);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		IAtomContainer chosenAtomContainer = null;
		int maxCount = 0;
		for(IAtomContainer atoms: mcsList){
			System.out.println(atoms.getAtomCount());
			if(atoms.getAtomCount() > maxCount){
				maxCount = atoms.getAtomCount();
				chosenAtomContainer = atoms;
			}
		}
		Assert.assertEquals(17, chosenAtomContainer.getAtomCount());
	}
	@Test public void testGetMultipleMCSMap() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = null;
//		Map<Integer, List<Integer>> mcsMap = new HashMap<Integer,List<Integer>>();
//		int maxCount = 0;
		StringBuffer buf = new StringBuffer();
		IMolecule product = null;
		IMolecule reactant = null;
		List<IAtomContainer> mcsList = null;
		for(int i = 0; i <= 30; i++){
			reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
//			if(mcsList.size() > 1){
				buf.setLength(0);
				buf.append("Rireg #" + (i + 1) + "\t");
				for(IAtomContainer atoms: mcsList){
					buf.append(" :" + atoms.getAtomCount());
				}
				System.out.println(buf.toString());
//			}			
		}
//		Iterator<Entry<Integer,List<Integer>>> iter = mcsMap.entrySet().iterator(); 
//		;
//		while(iter.hasNext()){
//			
//			Entry<Integer,List<Integer>> entry = iter.next();
//			
//			for(Integer i: entry.getValue()){
//				buf.append(":" + i);
//			}
//			
//		}
	}
	
	@Test public void testFileLength() throws Exception{
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		boolean isLessThanInt = fileLengthLong < Integer.MAX_VALUE;
		Assert.assertEquals(true, isLessThanInt);
	}
	
	@Test public void testSpecificRiReg() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		int fileLengthInt = (int)fileLengthLong;
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
	}
	
	@Test public void testSpecificRiRegAndPrevious() throws Exception {
		String filename = "data/mdl/First500DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(fileLengthLong);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(16, mcsList.get(0).getAtomCount());
		reader.reset();
		reader.setInitialRiregNo(23);
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(14, mcsList.get(0).getAtomCount());
	}
	
	
	@Test public void testSpecificRiRegAndNext() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.setInitialRiregNo(24);
		IReactionSet reactionSet = null;
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		IMolecule product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(2, mcsList.size());
		reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		Assert.assertEquals(1, mcsList.size());
	}
	/**
	 * tests whether the old (metaprint2d) sybyl atom types are all part of current 
	 * CDK version of sybyl atom types
	 * */
	@Test public void testVerifyCDKSybylAtomTypesContainsAllOldSybylAtomTypes() throws Exception{
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IMolecule reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		
		//Get current sybyl atom types
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org/openscience/cdk/dict/data/sybyl-atom-types.owl");
		AtomTypeFactory factory = AtomTypeFactory.getInstance(stream, "owl", reactant.getBuilder());
		IAtomType atomTypes [] = factory.getAllAtomTypes();
		List <String> newAtomTypeNames = new ArrayList<String>();
		for(IAtomType atomType: atomTypes){
			newAtomTypeNames.add(atomType.getAtomTypeName().toUpperCase());
		}
		//Get old sybyl atom types
		List <String> oldSybylAtomTypes = Constants.ATOM_TYPE_LIST;
		
		for(String oldAtomTypeName: oldSybylAtomTypes){
			try{
				Assert.assertEquals(true, newAtomTypeNames.contains(oldAtomTypeName.toUpperCase()));
			}catch(java.lang.AssertionError err){
				throw new java.lang.AssertionError(err.toString() + ". " + 
						oldAtomTypeName + " is not in new list of sybyl atom types:\n" +
						newAtomTypeNames.toString());
			}
		}
		
	}

	static boolean shouldExit = false;

	@Test public void testFingerprint() throws Exception {
		String filename = "data/mdl/firstRiReg.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
		FingerprintGenerator fpGenerator = new FingerprintGenerator();
	    List<Fingerprint> fpList = fpGenerator.generateFingerprints(reactant);
	}
	
	private void percieveAtomTypesAndConfigureAtoms(IAtomContainer container) throws Exception {
    	SybylAtomTypeMatcher matcher = SybylAtomTypeMatcher.getInstance(container.getBuilder());
        Iterator<IAtom> atoms = container.atoms().iterator();
        while (atoms.hasNext()) {
        	IAtom atom = atoms.next();
        	atom.setAtomTypeName(null);
        	IAtomType matched = matcher.findMatchingAtomType(container, atom);
        	if (matched != null) AtomTypeManipulator.configure(atom, matched);
        }
	}
	
	@Test public void testCloneAtomData() throws Exception {
		byte [][] byteMatrix = new byte[1][1];
		byteMatrix[0][0] = 1;
		Fingerprint fp = new Fingerprint(byteMatrix);
		AtomData atomData = new AtomData(fp, true);
		AtomData clone = atomData.clone();
		Assert.assertArrayEquals(byteMatrix, clone.getFingerprint().getBytes());
		Assert.assertEquals(true, clone.getIsReactionCentre());
	}
	
	@Test public void testGUI() throws Exception {
		String filename = "data/mdl/First50DB2005AllFields.rdf";
		logger.info("Testing: " + filename);
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
		URL url = this.getClass().getClassLoader().getResource(filename);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(fileLengthLong);
//		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
//		IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
//		IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
//		List<IAtomContainer> mcsList = UniversalIsomorphismTester.getOverlaps(reactant, product);
		render(reader);
		
	}
	
	public void render(ReaccsMDLRXNReader reader){
		try {
//			ImagePanel panel = new ImagePanel(
//					getImage(reactant, mcs, true), 
//					getImage(product, mcs, false), 
//					getImage(mcs, mcs, false));

			JFrame frame = new JFrame("Reactant - Product - MCS");
			frame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent paramWindowEvent)
				{
					shouldExit = true;
				}
			});
			
			frame.getContentPane().add(new ToolBarDemo(reader));
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0;
		while(shouldExit == false){
			try{
				Thread.currentThread().sleep(1000);
			}catch(Exception e){
				i++;
			}
		}
	}

	
}


