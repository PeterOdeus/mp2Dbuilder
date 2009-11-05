/*    */ package metaprint2d.analyzer;
/*    */ 
/*    */ import java.util.List;

import metaprint2d.Fingerprint;
import metaprint2d.mol2.Fingerprinter;
import metaprint2d.mol2.Molecule;

import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
/*    */ //import sea36.chem.core.CMLMolecule;
/*    */ //import sea36.chemkit.cdk.CDKAdaptor;
/*    */ 
/*    */ public class FingerprintGenerator
/*    */ {
/* 24 */   private Fingerprinter fingerprinter = new Fingerprinter();
/*    */ 
/*    */   //public List<Fingerprint> generateFingerprints(CMLMolecule mol)
			public List<Fingerprint> generateFingerprints(IAtomContainer mol)
/*    */   {
///*    */     Molecule cmol;
///*    */     try {
///* 30 */       cmol = getAtomTypedMolecule(mol);
///*    */     } catch (CDKException e) {
///* 32 */       throw new RuntimeException(e);
///*    */     }
				try {
					setAtomType(mol);
				} catch (CDKException e) {
					throw new RuntimeException(e);
				}
/*    */ 
/* 35 */     return this.fingerprinter.fingerprint(mol, 5);
/*    */   }
/*    */ 
/*    */   //public static Molecule getAtomTypedMolecule(CMLMolecule m)
			public static void setAtomType(IAtomContainer  m)
/*    */     throws CDKException
/*    */   {
/*    */     //Atom atom;
/* 41 */     //IMolecule ac = CDKAdaptor.getDefaultInstance().getCDKMolecule(m);
/*    */ 
/* 55 */     //SybylAtomTypeMatcher matcher = SybylAtomTypeMatcher.getInstance(ac.getBuilder());
/* 56 */     //IAtomType[] types = matcher.findMatchingAtomType(ac);
			SybylAtomTypeMatcher matcher = SybylAtomTypeMatcher.getInstance(m.getBuilder());
			IAtomType[] types = matcher.findMatchingAtomType(m);
/*    */ 
/* 59 */     //Molecule mol = new Molecule();
/* 60 */     //Map map = new HashMap();
/* 61 */     for (int i = 0; i < types.length; ++i) {
/* 62 */       IAtom cdkAtom = m.getAtom(i);
/*    */ 
/* 64 */       if (types[i] == null)
					throw new CDKException("TODO: handle null atom type");
/* 65 */         //atom = new Atom("Du");
/*    */       else {
					cdkAtom.setAtomTypeName(types[i].getAtomTypeName());
/* 67 */         //atom = new Atom(types[i].getAtomTypeName());
/*    */       }
/* 69 */       //mol.addAtom(atom);
/* 70 */       //map.put(cdkAtom, atom);
/*    */     }
///* 72 */     for (Iterator it = m.atoms().iterator(); it.hasNext(); ) {
///* 73 */       IAtom cdkAt = (IAtom)it.next();
///* 74 */       atom = (Atom)map.get(cdkAt);
///* 75 */       for (IAtom an : m.getConnectedAtomsList(cdkAt)) {
///* 76 */         atom.addNeighbour((Atom)map.get(an));
///*    */       }
///*    */     }
/*    */ 
///* 80 */     return mol;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.FingerprintGenerator
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
