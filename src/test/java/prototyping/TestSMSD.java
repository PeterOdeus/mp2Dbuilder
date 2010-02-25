package prototyping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.io.ReadingReaccsFileCancelledException;
import org.openscience.cdk.nonotify.NNChemObject;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.SMSD;
import org.openscience.cdk.smsd.interfaces.IMCS.Algorithm;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class TestSMSD {

	
	public static void main(String[] args) throws IOException, URISyntaxException, ReaccsFileEndedException, ReadingReaccsFileCancelledException, CDKException {

//		String filename2 = "metaprint2d/data/First500DB2005AllFields.rdf";
		
		//Should work too, just rename
		String filename2 = "metaprint2d/data/Metab_exp_2009-08-06_All.rdf.gz";

		System.out.println("Testing: " + filename2);
		InputStream ins = TestSMSD.class.getClassLoader().getResourceAsStream(filename2);
    	if(filename2.endsWith(".gz")){
    		ins = new GZIPInputStream(ins);
    	}
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
//		reader.activateReset(10024);

		//Read reaction
		IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
		IReaction reaction = reactionSet.getReaction(0);

		System.out.println("Noreacs: " + reactionSet.getReactionCount());
		
        long start = System.currentTimeMillis(); // start timing

		int cnt=1;
		System.out.print("Running SMSD on reaction: " + cnt);
		while(reaction!=null){
			try{
			runSMSD(reaction);
			}catch(Exception e){
				System.out.print("\nProblem with entry: " + cnt + "\n");
			}
			reactionSet = (IReactionSet)reader.read(new NNReactionSet());
			reaction = reactionSet.getReaction(0);
			cnt++;
			if (cnt%30==0){
	            long now = System.currentTimeMillis(); // stop timing
				System.out.print("\nTime elapsed: " + millisecsToString(now-start) +"\nContinuing: " + cnt);
			}else{
				System.out.print("," + cnt);
			}
		}
		

	}
	
	public static void runSMSD(IReaction reaction) throws CDKException{
		
		IAtomContainer A1=reaction.getReactants().getAtomContainer(0);
		IAtomContainer A2=reaction.getProducts().getAtomContainer(0);
		
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A1);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A2);

        A1 = (IMolecule) AtomContainerManipulator.removeHydrogens(A1);
        A2 = (IMolecule) AtomContainerManipulator.removeHydrogens(A2);

        CDKHueckelAromaticityDetector.detectAromaticity(A1);
        CDKHueckelAromaticityDetector.detectAromaticity(A2);


        boolean bondSensitive = true;
        boolean removeHydrogen = true;
        boolean stereoMatch = false;
        boolean fragmentMinimization = false;
        boolean energyMinimization = false;

//        SMSD comparison = new SMSD(Algorithm.DEFAULT, bondSensitive);
        SMSD comparison = new SMSD(Algorithm.CDKMCS, bondSensitive);
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

        int nrMCSs = 0; int maxAtoms = 0; int minAtoms = 1000000; 
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
			if (nrMCSAtoms < minAtoms) {
				minAtoms = nrMCSAtoms;
			}
			if (nrMCSAtoms > maxAtoms) {
				maxAtoms = nrMCSAtoms;
			}
//			System.out.println("------- Nr of MCS atoms: " + nrMCSAtoms);
		}
		if (minAtoms != maxAtoms){
			System.out.println("+++++++++ Diff min max atoms: " + minAtoms + ":" + maxAtoms);
		}
	}

    public final static long SECOND = 1000;
    public final static long MINUTE = SECOND * 60;
    public final static long HOUR = MINUTE * 60;
    public final static long DAY = HOUR * 24;

    
    public static String millisecsToString(long time) {
        StringBuilder result = new StringBuilder();
        long timeleft = time;
        if ( timeleft < SECOND ) {
            return "less than a second";
        }
        if ( timeleft > DAY ) {
            long days = (time / DAY);
            result.append( days );
            result.append( "d " );
            timeleft = timeleft - (days * DAY);
        }
        if ( timeleft > HOUR ) {
            long hours = (timeleft / HOUR);
            result.append( hours );
            result.append( "h " );
            timeleft = timeleft - (hours * HOUR); 
        }
        if ( timeleft > MINUTE ) {
            long minutes = (timeleft / MINUTE);
            result.append( minutes );
            result.append( "min " );
            timeleft = timeleft - (minutes * MINUTE);
        }
        if ( timeleft > SECOND ) {
            long seconds = (timeleft / SECOND);
            result.append( seconds );
            result.append( "s" );
        }

        return result.toString();
    }


	
}
