 package metaprint2d.mol2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Molecule
 {
   private String title;
   private List<Atom> atoms;
 
   public Molecule()
   {
     this.atoms = new ArrayList(); }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public void addAtom(Atom atom) {
     this.atoms.add(atom);
   }
 
   public Atom getAtom(int i) {
     return ((Atom)this.atoms.get(i));
   }
 
   public List<Atom> getAtoms() {
     return new ArrayList(this.atoms);
   }
 
   public String getTitle() {
     return this.title;
   }
 }

