 package metaprint2d.mol2;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import metaprint2d.Constants;
 import metaprint2d.Fingerprint;
 import metaprint2d.FPLevel;
 
 public class Fingerprinter
 {
   private static final int ATOM_H = ((Integer)Constants.ATOM_TYPE_INDEX.get("H")).intValue();
   private Map<FPLevel, FPLevel> fplcache;
   private boolean ignoreHydrogen;
 
   public Fingerprinter()
   {
     this.fplcache = new HashMap<FPLevel, FPLevel>();
 
     this.ignoreHydrogen = true;
   }
 
   public List<Fingerprint> fingerprint(Molecule molecule, int radius)
   {
     int nEnvs = Constants.ATOM_TYPE_LIST.size();
 
     List list = new ArrayList();
     List atoms = molecule.getAtoms();
 
     Set visitedAtoms = new HashSet();
 
     List nextLevelAtoms = new ArrayList();
     List temp = new ArrayList();
 
     for (int i = 0; i < atoms.size(); ++i) {
       byte[][] atomEnv = new byte[radius + 1][nEnvs];
 
       Atom atom = (Atom)atoms.get(i);
       int atype = atom.getAtomType();
       if (atype != -1) {
         atomEnv[0][atype] = 1;
       }
 
       visitedAtoms.clear();
       nextLevelAtoms.clear();
       temp.clear();
 
       visitedAtoms.add(atom);
       nextLevelAtoms.addAll(atom.getNeighbours());
       visitedAtoms.addAll(nextLevelAtoms);
 
       for (int level = 1; level <= radius; ++level)
       {
/*  TODO added generics */         List<Atom> currentLevelAtoms = nextLevelAtoms;
 
         nextLevelAtoms = temp;
         nextLevelAtoms.clear();
         temp = currentLevelAtoms;
 
         for (Atom currentAtom : currentLevelAtoms)
         {
           int catype = currentAtom.getAtomType();
           if ((catype != -1) && (((!(this.ignoreHydrogen)) || (catype != ATOM_H))))
           {
             int tmp259_257 = catype;
             byte[] tmp259_256 = atomEnv[level]; tmp259_256[tmp259_257] = (byte)(tmp259_256[tmp259_257] + 1);
           }
 
           for (Atom a : currentAtom.getNeighbours()) {
             if (visitedAtoms.add(a)) {
               nextLevelAtoms.add(a);
             }
           }
 
         }
 
       }
 
       FPLevel[] fpls = new FPLevel[radius + 1];
       for (int j = 0; j <= radius; ++j) {
         FPLevel fpl = new FPLevel(atomEnv[j]);
         FPLevel cfpl = (FPLevel)this.fplcache.get(fpl);
         if (cfpl == null)
           this.fplcache.put(fpl, fpl);
         else {
           fpl = cfpl;
         }
         fpls[j] = fpl;
       }
       Fingerprint fp = new Fingerprint(fpls);
       list.add(fp);
     }
 
     return list;
   }
 
   public void setIgnoreHydrogen(boolean ignoreHydrogen) {
     this.ignoreHydrogen = ignoreHydrogen;
   }
 
   public boolean isIgnoreHydrogen() {
     return this.ignoreHydrogen;
   }
 }
