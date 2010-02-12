 package metaprint2d.mol2;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import metaprint2d.Constants;
 
 public class Atom
 {
   private String name;
   private int atomType = -1;
   private List<Atom> neighbours = new ArrayList(4);
   private String atomTypeString;
 
   public Atom(String atomTypeString)
   {
     Integer atype = (Integer)Constants.ATOM_TYPE_INDEX.get(atomTypeString.toUpperCase());
     if (atype != null) {
       this.atomType = atype.intValue();
       this.atomTypeString = ((String)Constants.ATOM_TYPE_LIST.get(atype.intValue()));
     } else {
       this.atomTypeString = atomTypeString;
     }
   }
 
   public Atom(int atomType) {
     this.atomType = atomType;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public void addNeighbour(Atom atom) {
     this.neighbours.add(atom);
   }
 
   public String getName() {
     return this.name;
   }
 
   public List<Atom> getNeighbours() {
     return new ArrayList(this.neighbours);
   }
 
   public int getAtomType() {
     return this.atomType;
   }
 
   public String getAtomTypeString() {
     return this.atomTypeString;
   }
 }
