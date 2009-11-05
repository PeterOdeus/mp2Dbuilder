/*     */ package metaprint2d.mol2;
/*     */ 
/*     */ import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import metaprint2d.Constants;
import metaprint2d.FPLevel;
import metaprint2d.Fingerprint;

import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
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
/*     */   public List<Fingerprint> fingerprint(IAtomContainer molecule, int radius)
/*     */   {
	InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org/openscience/cdk/dict/data/sybyl-atom-types.owl");
	AtomTypeFactory factory = AtomTypeFactory.getInstance(stream, "owl", molecule.getBuilder());
	IAtomType atomTypes [] = factory.getAllAtomTypes();
/*  53 */     int nEnvs = atomTypes.length;
/*     */ 
/*  55 */     List list = new ArrayList();
/*  56 */     Iterable<IAtom> atoms = molecule.atoms();
/*     */ 
/*  59 */     Set<IAtom> visitedAtoms = new HashSet<IAtom>();
/*     */ 
/*  62 */     List<IAtom> nextLevelAtoms = new ArrayList<IAtom>();
/*  63 */     List<IAtom> temp = new ArrayList<IAtom>();
/*     */ 		SortedMap<String,AtomicInteger> atomTypeMap;
/*  65 */     for (IAtom atom: atoms) {
				List<SortedMap<String,AtomicInteger>> atomEnv = 
					new ArrayList<SortedMap<String,AtomicInteger>>(radius + 1);
				for(int i = 0; i < (radius + 1); i++){
					atomTypeMap = new TreeMap<String,AtomicInteger>();
					for(int ii = 0; ii < atomTypes.length; ii++){
						atomTypeMap.put(atomTypes[ii].getAtomTypeName(), new AtomicInteger());
					}
					atomEnv.add(atomTypeMap);
				}
				//atomEnv
/*  66 */       //byte[][] atomEnv = new byte[radius + 1][nEnvs];
/*     */ 
///*  68 */       Atom atom = (Atom)atoms.get(i);
/*  69 */       String atype = ((IAtomType)atom).getAtomTypeName();
				AtomicInteger value = atomEnv.get(0).get(atype);
				if(value != null){
					value.addAndGet(1);
				}
///*  70 */       if (atype != -1) {
///*  71 */         atomEnv[0][atype] = 1;
///*     */       }
/*     */ 
/*  75 */       visitedAtoms.clear();
/*  76 */       nextLevelAtoms.clear();
/*  77 */       temp.clear();
/*     */ 
/*  79 */       visitedAtoms.add(atom);
/*  80 */       nextLevelAtoms.addAll(molecule.getConnectedAtomsList(atom));
/*  81 */       visitedAtoms.addAll(nextLevelAtoms);
/*     */ 
/*  83 */       for (int level = 1; level <= radius; ++level)
/*     */       {
/*  TODO added generics */List<IAtom> currentLevelAtoms = nextLevelAtoms;
/*     */ 
/*  88 */         nextLevelAtoms = temp;
/*  89 */         nextLevelAtoms.clear();
/*  90 */         temp = currentLevelAtoms;
/*     */ 
/*  93 */         for (IAtom currentAtom : currentLevelAtoms)
/*     */         {
					String catype = ((IAtomType)currentAtom).getAtomTypeName();
/*  97 */           if ((catype != null) && (this.ignoreHydrogen == false || catype.equals("H") == false))
/*     */           {
/*     */             //int tmp259_257 = catype;
/*     */             value = atomEnv.get(level).get(catype);
						if(value != null){
							value.addAndGet(1);
						}
						//tmp259_256[tmp259_257] = (byte)(tmp259_256[tmp259_257] + 1);
/*     */           }
/*     */ 
/* 102 */           for (IAtom a : molecule.getConnectedAtomsList(currentAtom)) {
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
/* 114 */         FPLevel fpl = new FPLevel(atomEnv.get(j));
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
