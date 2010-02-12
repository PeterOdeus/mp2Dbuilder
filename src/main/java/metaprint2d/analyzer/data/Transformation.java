 package metaprint2d.analyzer.data;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import metaprint2d.Fingerprint;
 
 public class Transformation
   implements Cloneable
 {
   private int rxnNumber;
   private String reactionId;
   private String reactantId;
   private String productId;
   private String step;
   private String path;
   private List<String> rxnClasses;
   private Set<String> species;
   private int[][] mappings;
   private List<AtomData> atomData;
 
   public int getRxnNumber()
   {
     return this.rxnNumber;
   }
 
   public void setRxnNumber(int rxnNumber) {
     this.rxnNumber = rxnNumber;
   }
 
   public String getReactionId() {
     return this.reactionId;
   }
 
   public void setReactionId(String rxnId) {
     this.reactionId = rxnId;
   }
 
   public String getReactantId() {
     return this.reactantId;
   }
 
   public void setReactantId(String reactantId) {
     this.reactantId = reactantId;
   }
 
   public String getProductId() {
     return this.productId;
   }
 
   public void setProductId(String productId) {
     this.productId = productId;
   }
 
   public String getStep() {
     return this.step;
   }
 
   public void setStep(String step) {
     this.step = step;
   }
 
   public String getPath() {
     return this.path;
   }
 
   public void setPath(String path) {
     this.path = path;
   }
 
   public List<String> getReactionClasses() {
     return this.rxnClasses;
   }
 
   public void setReactionClasses(List<String> rxnClasses) {
     this.rxnClasses = rxnClasses;
   }
 
   public Set<String> getSpecies() {
     return this.species;
   }
 
   public void setSpecies(Set<String> species) {
     this.species = species;
   }
 
   public List<AtomData> getAtomData() {
     return this.atomData;
   }
 
   public void setAtomData(List<AtomData> atomData) {
     this.atomData = atomData;
   }
 
   public boolean hasAtoms() {
     return (!(this.atomData.isEmpty()));
   }
 
   public boolean isSingleStep() {
     if (this.step == null) {
       throw new IllegalStateException("Step not set");
     }
 
     return ((!(this.step.equals("1 Step"))) && (!(this.step.contains(" of "))));
   }
 
   public boolean isMultiStep() {
     return (!(isSingleStep()));
   }
 
   public int[][] getMappings() {
     return this.mappings;
   }
 
   public void setMappings(int[][] mappings) {
     this.mappings = mappings;
   }
 
   public Transformation clone()
   {
     try {
       Transformation clone = (Transformation)super.clone();
       if (clone.rxnClasses != null) {
         clone.rxnClasses = new ArrayList(clone.rxnClasses);
       }
       if (clone.species != null) {
         clone.species = new HashSet(clone.species);
       }
       if (clone.atomData != null) {
         clone.atomData = new ArrayList(clone.atomData);
         for (int i = 0; i < clone.atomData.size(); ++i) {
           clone.atomData.set(i, ((AtomData)clone.atomData.get(i)).clone());
         }
       }
       return clone;
     } catch (CloneNotSupportedException e) {
       throw new RuntimeException(e);
     }
   }
 

 }
