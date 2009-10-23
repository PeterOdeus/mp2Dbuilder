/*     */ package metaprint2d.mol2;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;

import org.openscience.cdk.interfaces.IMolecule;

/*     */ import metaprint2d.Constants;
/*     */ import metaprint2d.Fingerprint;
import metaprint2d.FPLevel;
/*     */ 
/*     */ public class Fingerprinter
/*     */ {
/*  38 */   private static final int ATOM_H = ((Integer)Constants.ATOM_TYPE_INDEX.get("H")).intValue();
/*     */   private Map<FPLevel, FPLevel> fplcache;
/*     */   private boolean ignoreHydrogen;
/*     */ 
/*     */   public Fingerprinter()
/*     */   {
/*  40 */     this.fplcache = new HashMap<FPLevel, FPLevel>();
/*     */ 
/*  42 */     this.ignoreHydrogen = true;
/*     */   }
/*     */ 
/*     */   public List<Fingerprint> fingerprint(Molecule molecule, int radius)
/*     */   {
/*  53 */     int nEnvs = Constants.ATOM_TYPE_LIST.size();
/*     */ 
/*  55 */     List list = new ArrayList();
/*  56 */     List atoms = molecule.getAtoms();
/*     */ 
/*  59 */     Set visitedAtoms = new HashSet();
/*     */ 
/*  62 */     List nextLevelAtoms = new ArrayList();
/*  63 */     List temp = new ArrayList();
/*     */ 
/*  65 */     for (int i = 0; i < atoms.size(); ++i) {
/*  66 */       byte[][] atomEnv = new byte[radius + 1][nEnvs];
/*     */ 
/*  68 */       Atom atom = (Atom)atoms.get(i);
/*  69 */       int atype = atom.getAtomType();
/*  70 */       if (atype != -1) {
/*  71 */         atomEnv[0][atype] = 1;
/*     */       }
/*     */ 
/*  75 */       visitedAtoms.clear();
/*  76 */       nextLevelAtoms.clear();
/*  77 */       temp.clear();
/*     */ 
/*  79 */       visitedAtoms.add(atom);
/*  80 */       nextLevelAtoms.addAll(atom.getNeighbours());
/*  81 */       visitedAtoms.addAll(nextLevelAtoms);
/*     */ 
/*  83 */       for (int level = 1; level <= radius; ++level)
/*     */       {
/*  TODO added generics */         List<Atom> currentLevelAtoms = nextLevelAtoms;
/*     */ 
/*  88 */         nextLevelAtoms = temp;
/*  89 */         nextLevelAtoms.clear();
/*  90 */         temp = currentLevelAtoms;
/*     */ 
/*  93 */         for (Atom currentAtom : currentLevelAtoms)
/*     */         {
/*  96 */           int catype = currentAtom.getAtomType();
/*  97 */           if ((catype != -1) && (((!(this.ignoreHydrogen)) || (catype != ATOM_H))))
/*     */           {
/*     */             int tmp259_257 = catype;
/*     */             byte[] tmp259_256 = atomEnv[level]; tmp259_256[tmp259_257] = (byte)(tmp259_256[tmp259_257] + 1);
/*     */           }
/*     */ 
/* 102 */           for (Atom a : currentAtom.getNeighbours()) {
/* 103 */             if (visitedAtoms.add(a)) {
/* 104 */               nextLevelAtoms.add(a);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 112 */       FPLevel[] fpls = new FPLevel[radius + 1];
/* 113 */       for (int j = 0; j <= radius; ++j) {
/* 114 */         FPLevel fpl = new FPLevel(atomEnv[j]);
/* 115 */         FPLevel cfpl = (FPLevel)this.fplcache.get(fpl);
/* 116 */         if (cfpl == null)
/* 117 */           this.fplcache.put(fpl, fpl);
/*     */         else {
/* 119 */           fpl = cfpl;
/*     */         }
/* 121 */         fpls[j] = fpl;
/*     */       }
/* 123 */       Fingerprint fp = new Fingerprint(fpls);
/* 124 */       list.add(fp);
/*     */     }
/*     */ 
/* 127 */     return list;
/*     */   }
/*     */ 
/*     */   public void setIgnoreHydrogen(boolean ignoreHydrogen) {
/* 131 */     this.ignoreHydrogen = ignoreHydrogen;
/*     */   }
/*     */ 
/*     */   public boolean isIgnoreHydrogen() {
/* 135 */     return this.ignoreHydrogen;
/*     */   }
/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.mol2.Fingerprinter
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
