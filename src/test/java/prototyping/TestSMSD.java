package prototyping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.nonotify.NNChemObject;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.SMSD;
import org.openscience.cdk.smsd.interfaces.IMCS.Algorithm;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class TestSMSD {

	
	public static void main(String[] args) throws Exception {

		String filename = "metaprint2d/data/First500DB2005AllFields.rdf";
		
		//Should work too, just rename
		String filename2 = "metaprint2d/data/First500DB2005AllFields.rdf.gz";

		System.out.println("Testing: " + filename);
		InputStream ins = TestSMSD.class.getClassLoader().getResourceAsStream(filename);
    	if(filename.endsWith(".gz")){
    		ins = new GZIPInputStream(ins);
    	}
		URL url = TestSMSD.class.getClassLoader().getResource(filename);
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		File file = new File(url.toURI());
		long fileLengthLong = file.length();

		reader.activateReset(fileLengthLong);

		//Read reaction
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IReaction reaction = reactionSet.getReaction(0);

		System.out.println("Noreacs: " + reactionSet.getReactionCount());

		int cnt=1;
		System.out.print("Running SMSD on reaction: " + cnt);
		while(reaction!=null){
			runSMSD(reaction);
			reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			reaction = reactionSet.getReaction(0);
			cnt++;
			System.out.print("," + cnt);
		}
		

	}
	
	public static void runSMSD(IReaction reaction) throws Exception{
		
		IAtomContainer A1=reaction.getReactants().getAtomContainer(0);
		IAtomContainer A2=reaction.getProducts().getAtomContainer(0);
		
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A1);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A2);

        A1 = (IMolecule) AtomContainerManipulator.removeHydrogens(A1);
        A2 = (IMolecule) AtomContainerManipulator.removeHydrogens(A2);

        CDKHueckelAromaticityDetector.detectAromaticity(A1);
        CDKHueckelAromaticityDetector.detectAromaticity(A2);


        boolean bondSensitive = false;
        boolean removeHydrogen = true;
        boolean stereoMatch = false;
        boolean fragmentMinimization = true;
        boolean energyMinimization = true;

        SMSD comparison = new SMSD(Algorithm.DEFAULT, bondSensitive);
        comparison.init(A1, A2, removeHydrogen);
        comparison.setChemFilters(stereoMatch, fragmentMinimization, energyMinimization);


//        Get modified Query and Target Molecules as Mappings will correspond to these molecules
        IAtomContainer Query = comparison.getReactantMolecule();
        IAtomContainer Target = comparison.getProductMolecule();

        
        for (Map.Entry<Integer, Integer> mappings : comparison.getFirstMapping().entrySet()) {
            //Get the mapped atom number in Query Molecule
            int queryMappingNumber = mappings.getKey();
            //Get the mapped atom number in Target Molecule
            int targetMappingNumber = mappings.getValue();

            //Get the mapped atom in Query Molecule
            IAtom queryAtom = Query.getAtom(queryMappingNumber);
            //Get the mapped atom in Target Molecule
            IAtom targetAtom = Target.getAtom(targetMappingNumber);
            //Print mapped atom numbers
//            System.out.println(queryMappingNumber + " " +
//                    (targetMappingNumber));
//            //Print mapped atoms
//            System.out.println(queryAtom.getSymbol() + " " +
//                    targetAtom.getSymbol());
        }
//        System.out.println("");
//
//        System.out.println("");

//        System.out.println("Stereo Match: " + comparison.getStereoScore(0));
//        System.out.println("Stereo different: " + comparison.isStereoMisMatch());
//        System.out.println("Fragment Size: " + comparison.getFragmentSize(0));
//        System.out.println("Tanimoto Similarity Score: " + comparison.getTanimotoSimilarity());
//        System.out.println("Tanimoto Euclidean Distance: " + comparison.getEuclideanDistance());
//        System.out.println("");

        int nrMCSs = 0;
		for (TreeMap<Integer, Integer> listOfMappings : comparison.getAllMapping()){
			nrMCSs++;
			int nrMCSAtoms = 0;
			for (Map.Entry<Integer, Integer> mappings : listOfMappings.entrySet()){
				nrMCSAtoms++;
	            int queryMappingNumber = mappings.getKey();
	            //Get the mapped atom number in Target Molecule
	            int targetMappingNumber = mappings.getValue();

	            //Get the mapped atom in Query Molecule
	            IAtom queryAtom = Query.getAtom(queryMappingNumber);
	            //Get the mapped atom in Target Molecule
	            IAtom targetAtom = Target.getAtom(targetMappingNumber);
	            //Print mapped atom numbers
//	            System.out.println(queryMappingNumber + " " +
//	                    (targetMappingNumber));
//	            //Print mapped atoms
//	            System.out.println(queryAtom.getSymbol() + " " +
//	                    targetAtom.getSymbol());
				
			}
//			System.out.println("------- Nr of MCS atoms: " + nrMCSAtoms);
		}
//		System.out.println("+++++++++ Nr of MCS: " + nrMCSs);
		
	}
	
	
}
