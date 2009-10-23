/*    */ package metaprint2d.mol2;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class Molecule
/*    */ {
/*    */   private String title;
/*    */   private List<Atom> atoms;
/*    */ 
/*    */   public Molecule()
/*    */   {
/* 30 */     this.atoms = new ArrayList(); }
/*    */ 
/*    */   public void setTitle(String title) {
/* 33 */     this.title = title;
/*    */   }
/*    */ 
/*    */   public void addAtom(Atom atom) {
/* 37 */     this.atoms.add(atom);
/*    */   }
/*    */ 
/*    */   public Atom getAtom(int i) {
/* 41 */     return ((Atom)this.atoms.get(i));
/*    */   }
/*    */ 
/*    */   public List<Atom> getAtoms() {
/* 45 */     return new ArrayList(this.atoms);
/*    */   }
/*    */ 
/*    */   public String getTitle() {
/* 49 */     return this.title;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.mol2.Molecule
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
