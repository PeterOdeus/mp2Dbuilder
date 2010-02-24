package prototyping;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.SMSD;
import org.openscience.cdk.smsd.interfaces.IMCS.Algorithm;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class TestSMSD {

	
	public static void main(String[] args) throws Exception {
		
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
//		IMolecule A1 = sp.parseSmiles("CNC(CC=OC)CCCCN(C)C");
//		IMolecule A2 = sp.parseSmiles("CNC(CC=OC)CCCCN(C)C");
		IMolecule A1 = sp.parseSmiles("CN(=C)O");
		IMolecule A2 = sp.parseSmiles("CN(=C)O");
		System.out.println("mol1: " + A1);
		System.out.println("mol2: " + A1);
		
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A1);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A2);

        A1 = (IMolecule) AtomContainerManipulator.removeHydrogens(A1);
        A2 = (IMolecule) AtomContainerManipulator.removeHydrogens(A2);

        CDKHueckelAromaticityDetector.detectAromaticity(A1);
        CDKHueckelAromaticityDetector.detectAromaticity(A2);


        boolean bondSensitive = true;
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
            System.out.println(queryMappingNumber + " " +
                    (targetMappingNumber));
            //Print mapped atoms
            System.out.println(queryAtom.getSymbol() + " " +
                    targetAtom.getSymbol());
        }
        System.out.println("");

        System.out.println("");

        System.out.println("Stereo Match: " + comparison.getStereoScore(0));
        System.out.println("Stereo different: " + comparison.isStereoMisMatch());
        System.out.println("Fragment Size: " + comparison.getFragmentSize(0));
        System.out.println("Tanimoto Similarity Score: " + comparison.getTanimotoSimilarity());
        System.out.println("Tanimoto Euclidean Distance: " + comparison.getEuclideanDistance());
        System.out.println("");

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
	            System.out.println(queryMappingNumber + " " +
	                    (targetMappingNumber));
	            //Print mapped atoms
	            System.out.println(queryAtom.getSymbol() + " " +
	                    targetAtom.getSymbol());
				
			}
			System.out.println("------- Nr of MCS atoms: " + nrMCSAtoms);
		}
		System.out.println("+++++++++ Nr of MCS: " + nrMCSs);
	}
}
