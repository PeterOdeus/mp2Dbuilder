package prototyping;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;

public class BuildMols {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		IAtomContainer ac1 = new AtomContainer();
		IAtom atom1 = new Atom("C");
		IAtom atom2 = new Atom("C");
		IAtom atom3 = new Atom("O");
		IBond bond1 = new Bond(atom1,atom2);
		IBond bond2 = new Bond(atom2,atom3);
		ac1.addAtom(atom1);
		ac1.addAtom(atom2);
		ac1.addBond(bond1);
		ac1.addBond(bond2);
		
		IAtomContainer ac2 = new AtomContainer();
		IAtom atom11 = new Atom("C");
		IAtom atom21 = new Atom("C");
		IAtom atom31 = new Atom("N");
		IBond bond11 = new Bond(atom11,atom21);
		IBond bond21 = new Bond(atom21,atom31);
		ac2.addAtom(atom11);
		ac2.addAtom(atom21);
		ac2.addBond(bond11);
		ac2.addBond(bond21);
		
		List<IAtomContainer> rmaps = UniversalIsomorphismTester.getOverlaps(ac1,ac2);
		System.out.println("Overlap sizse: " + rmaps.get(0).getAtomCount());


		

	}

}
