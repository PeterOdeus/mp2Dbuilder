package metaprint2d.analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import metaprint2d.Fingerprint;
import metaprint2d.mol2.Atom;
import metaprint2d.mol2.Fingerprinter;
import metaprint2d.mol2.Molecule;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;

public class FingerprintGenerator {
	private Fingerprinter fingerprinter = new Fingerprinter();

	public List<Fingerprint> generateFingerprints(IAtomContainer mol,
			IAtomType[] types) {
		Molecule cmol;
		try {
			cmol = getAtomTypedMolecule(mol, types);
		} catch (CDKException e) {
			throw new RuntimeException(e);
		}

		return this.fingerprinter.fingerprint(cmol, 5);
	}

	public static Molecule getAtomTypedMolecule(IAtomContainer m,
			IAtomType[] types) throws CDKException {
		Atom atom;
		// IMolecule ac = CDKAdaptor.getDefaultInstance().getCDKMolecule(m);

		Molecule mol = new Molecule();
		Map map = new HashMap();
		for (int i = 0; i < types.length; ++i) {
			IAtom cdkAtom = m.getAtom(i);

			if (types[i] == null)
				atom = new Atom("Du");
			else {
				atom = new Atom(types[i].getAtomTypeName());
			}
			mol.addAtom(atom);
			map.put(cdkAtom, atom);
		}
		for (Iterator it = m.atoms().iterator(); it.hasNext();) {
			IAtom cdkAt = (IAtom) it.next();
			atom = (Atom) map.get(cdkAt);
			for (IAtom an : m.getConnectedAtomsList(cdkAt)) {
				atom.addNeighbour((Atom) map.get(an));
			}
		}

		return mol;
	}
}
