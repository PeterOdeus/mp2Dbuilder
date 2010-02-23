package prototyping;

import java.util.List;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

public class BuilderMols {

	/**
	 * @param args
	 * @throws CDKException 
	 */
	public static void main(String[] args) throws CDKException {

		IChemObjectBuilder builder = NoNotificationChemObjectBuilder.getInstance();
		
		IAtomContainer ac1 = builder.newAtomContainer();
		IAtom atom1 = builder.newAtom("C");
		IAtom atom2 = builder.newAtom("C");
		IAtom atom3 = builder.newAtom("O");
		IBond bond1 = builder.newBond(atom1,atom2);
		IBond bond2 = builder.newBond(atom2,atom3);
		ac1.addAtom(atom1);
		ac1.addAtom(atom2);
		ac1.addBond(bond1);
		ac1.addBond(bond2);
		
		IAtomContainer ac2 = builder.newAtomContainer();
		IAtom atom11 = builder.newAtom("C");
		IAtom atom21 = builder.newAtom("C");
		IAtom atom31 = builder.newAtom("N");
		IBond bond11 = builder.newBond(atom11,atom21);
		IBond bond21 = builder.newBond(atom21,atom31);
		ac2.addAtom(atom11);
		ac2.addAtom(atom21);
		ac2.addBond(bond11);
		ac2.addBond(bond21);

		List<IAtomContainer> rmaps = UniversalIsomorphismTester.getOverlaps(ac1,ac2);
		System.out.println("Overlap sizse: " + rmaps.get(0).getAtomCount());


	}

}
