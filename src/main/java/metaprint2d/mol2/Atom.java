/*    */ package metaprint2d.mol2;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import metaprint2d.Constants;
/*    */ 
/*    */ public class Atom
/*    */ {
/*    */   private String name;
/* 32 */   private int atomType = -1;
/* 33 */   private List<Atom> neighbours = new ArrayList(4);
/*    */   private String atomTypeString;
/*    */ 
/*    */   public Atom(String atomTypeString)
/*    */   {
/* 38 */     Integer atype = (Integer)Constants.ATOM_TYPE_INDEX.get(atomTypeString.toUpperCase());
/* 39 */     if (atype != null) {
/* 40 */       this.atomType = atype.intValue();
/* 41 */       this.atomTypeString = ((String)Constants.ATOM_TYPE_LIST.get(atype.intValue()));
/*    */     } else {
/* 43 */       this.atomTypeString = atomTypeString;
/*    */     }
/*    */   }
/*    */ 
/*    */   public Atom(int atomType) {
/* 48 */     this.atomType = atomType;
/*    */   }
/*    */ 
/*    */   public void setName(String name) {
/* 52 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public void addNeighbour(Atom atom) {
/* 56 */     this.neighbours.add(atom);
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 60 */     return this.name;
/*    */   }
/*    */ 
/*    */   public List<Atom> getNeighbours() {
/* 64 */     return new ArrayList(this.neighbours);
/*    */   }
/*    */ 
/*    */   public int getAtomType() {
/* 68 */     return this.atomType;
/*    */   }
/*    */ 
/*    */   public String getAtomTypeString() {
/* 72 */     return this.atomTypeString;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.mol2.Atom
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
