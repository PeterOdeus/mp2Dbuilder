package prototyping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;


import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.io.ReadingReaccsFileCancelledException;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.nonotify.NNMoleculeSet;
import org.openscience.cdk.nonotify.NNReactionSet;

/**
 * 
 * @author ola
 *
 */
public class ExtractDifferingReactants {

	private static final Object FP_KEY = "FP-m2d";

	public static void main(String[] args) throws Exception {

		
		/* ************************************
		 These paths are hardcoded in this impl
		***************************************/
		//The smaller subset of molecules
		String filename1="/Users/ola/repos2/mp2Dbuilder/src/test/resources/data/mdl/First50DB2005withChanges.rdf";
		
		//The larger subset of molecules
		String filename2="/Users/ola/repos2/mp2Dbuilder/src/test/resources/data/mdl/First50DB2005AllFields.rdf";
		
		//SDF to write
		String outputFilename="/tmp/wee.sdf";
		/* *****************/

		
    	System.out.println("START");

		File file1=new File(filename1);
		if (!file1.canRead()){
			System.out.println("File: " + filename1 + " could not be read");
			System.exit(1);
		}
		
		File file2=new File(filename2);
		if (!file2.canRead()){
			System.out.println("File: " + filename2 + " could not be read");
			System.exit(1);
		}

		File outputFile=new File(outputFilename);
		if (!outputFile.canWrite()){
			System.out.println("File: " + outputFilename + " could not be written");
			System.exit(1);
		}

		//Read files and produce unique Lists with FP property
    	List<IAtomContainer> ac1 = readReactions(file1);
    	List<IAtomContainer> ac2 = readReactions(file2);

    	System.out.println("Extracting non-matches by FP comparison...");
    	IMoleculeSet newMols= new NNMoleculeSet();		//Store new mols here
    	for (IAtomContainer largeAC : ac2){	//Loop over large colelction
        	boolean match=false;
        	for (IAtomContainer smallAC : ac1){ //Check presence in small collection
        		BitSet outerFP=(BitSet)largeAC.getProperty(FP_KEY);
        		BitSet smallFP=(BitSet)smallAC.getProperty(FP_KEY);
        		if (outerFP.equals(smallFP)){
        			match=true;
        		}
        	}
        	//If no match, this ac from large was not present in small
        	if (!match){
        		largeAC.getProperties().clear();	//Do not store FP
        		newMols.addAtomContainer(largeAC);
        	}
    	}

    	System.out.println("    Identified " + newMols.getAtomContainerCount() + " not in original file.");
    	
    	FileOutputStream fo=new FileOutputStream(outputFile);
    	SDFWriter writer=new SDFWriter(fo);
    	writer.write(newMols);
    	writer.close();
    	
    	System.out.println("    Wrote file: " + outputFilename);
    	System.out.println("END");
    	
	}

/*
 *  We do not use INCHI since no JNI implementation for Mac OS X systems currently :(
	private static String generateInchi(IAtomContainer ac) throws CDKException {

		// Get InChIGenerator
   	 InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(ac);
   	 
   	 INCHI_RET ret = gen.getReturnStatus();
   	 if (ret == INCHI_RET.WARNING) {
   	   // InChI generated, but with warning message
   	   System.out.println("InChI warning: " + gen.getMessage());
   	 } else if (ret != INCHI_RET.OKAY) {
   	   // InChI generation failed
   	   throw new CDKException("InChI failed: " + ret.toString()
   	     + " [" + gen.getMessage() + "]");
   	 }
   	 
   	 String inchi = gen.getInchi();
   	 String auxinfo = gen.getAuxInfo();

   	 return inchi;
	}
	*/

	/**
	 * Read reactions, extract reactant, store FP as property, and return list of unique
	 * IAtomContainers.
	 */
	private static List<IAtomContainer> readReactions(File file) throws FileNotFoundException,
	IOException,
	ReadingReaccsFileCancelledException{
		
    	Fingerprinter fprinter=new Fingerprinter();
		System.out.println("Attempting to read rxn file: " + file.getAbsolutePath());
		InputStream ins = new FileInputStream(file);
    	if(file.getName().endsWith(".gz")){
    		ins = new GZIPInputStream(ins);
    	}
		ReaccsMDLRXNReader reader = new ReaccsMDLRXNReader(ins);
		reader.activateReset(1024);
		IReactionSet rs=null;
		try {
			rs = (IReactionSet)reader.read(new NNReactionSet());
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		List<IAtomContainer> mols=new ArrayList<IAtomContainer>();
		Set<BitSet> uniqueFPs=new HashSet<BitSet>();
		int nonUnique=0;
		int unique=0;
		int total=0;
		while (rs!=null){
			total++;
			try {
				IAtomContainer ac = rs.getReaction(0).getReactants().getAtomContainer(0);
	   		 	BitSet fp = fprinter.getFingerprint(ac);
	   		 	if (uniqueFPs.contains(fp)){
	   		 		nonUnique++;
	   		 	}else{
	   		 		unique++;
		   		 	ac.setProperty(FP_KEY, fp);
					mols.add(ac);
					uniqueFPs.add(fp);
	   		 	}
				rs = (IReactionSet)reader.read(new NNReactionSet());
			} catch (ReaccsFileEndedException e) {
				rs=null;
			} catch (CDKException e) {
				e.printStackTrace();
			}
		}
		System.out.println("    Succesfully read " + total + " reactions");
		System.out.println("    Unique reactants: " + unique);
		System.out.println("    Non-Unique reactants: " + nonUnique);
		return mols;
	}
}
