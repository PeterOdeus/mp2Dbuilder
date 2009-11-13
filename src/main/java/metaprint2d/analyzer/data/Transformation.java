/*     */ package metaprint2d.analyzer.data;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import metaprint2d.Fingerprint;
/*     */ 
/*     */ public class Transformation
/*     */   implements Cloneable
/*     */ {
/*     */   private int rxnNumber;
/*     */   private String reactionId;
/*     */   private String reactantId;
/*     */   private String productId;
/*     */   private String step;
/*     */   private String path;
/*     */   private List<String> rxnClasses;
/*     */   private Set<String> species;
/*     */   private int[][] mappings;
/*     */   private List<AtomData> atomData;
/*     */ 
/*     */   public int getRxnNumber()
/*     */   {
/*  27 */     return this.rxnNumber;
/*     */   }
/*     */ 
/*     */   public void setRxnNumber(int rxnNumber) {
/*  31 */     this.rxnNumber = rxnNumber;
/*     */   }
/*     */ 
/*     */   public String getReactionId() {
/*  35 */     return this.reactionId;
/*     */   }
/*     */ 
/*     */   public void setReactionId(String rxnId) {
/*  39 */     this.reactionId = rxnId;
/*     */   }
/*     */ 
/*     */   public String getReactantId() {
/*  43 */     return this.reactantId;
/*     */   }
/*     */ 
/*     */   public void setReactantId(String reactantId) {
/*  47 */     this.reactantId = reactantId;
/*     */   }
/*     */ 
/*     */   public String getProductId() {
/*  51 */     return this.productId;
/*     */   }
/*     */ 
/*     */   public void setProductId(String productId) {
/*  55 */     this.productId = productId;
/*     */   }
/*     */ 
/*     */   public String getStep() {
/*  59 */     return this.step;
/*     */   }
/*     */ 
/*     */   public void setStep(String step) {
/*  63 */     this.step = step;
/*     */   }
/*     */ 
/*     */   public String getPath() {
/*  67 */     return this.path;
/*     */   }
/*     */ 
/*     */   public void setPath(String path) {
/*  71 */     this.path = path;
/*     */   }
/*     */ 
/*     */   public List<String> getReactionClasses() {
/*  75 */     return this.rxnClasses;
/*     */   }
/*     */ 
/*     */   public void setReactionClasses(List<String> rxnClasses) {
/*  79 */     this.rxnClasses = rxnClasses;
/*     */   }
/*     */ 
/*     */   public Set<String> getSpecies() {
/*  83 */     return this.species;
/*     */   }
/*     */ 
/*     */   public void setSpecies(Set<String> species) {
/*  87 */     this.species = species;
/*     */   }
/*     */ 
/*     */   public List<AtomData> getAtomData() {
/*  91 */     return this.atomData;
/*     */   }
/*     */ 
/*     */   public void setAtomData(List<AtomData> atomData) {
/*  95 */     this.atomData = atomData;
/*     */   }
/*     */ 
/*     */   public boolean hasAtoms() {
/*  99 */     return (!(this.atomData.isEmpty()));
/*     */   }
/*     */ 
/*     */   public boolean isSingleStep() {
/* 103 */     if (this.step == null) {
/* 104 */       throw new IllegalStateException("Step not set");
/*     */     }
/*     */ 
/* 107 */     return ((!(this.step.equals("1 Step"))) && (!(this.step.contains(" of "))));
/*     */   }
/*     */ 
/*     */   public boolean isMultiStep() {
/* 111 */     return (!(isSingleStep()));
/*     */   }
/*     */ 
/*     */   public int[][] getMappings() {
/* 115 */     return this.mappings;
/*     */   }
/*     */ 
/*     */   public void setMappings(int[][] mappings) {
/* 119 */     this.mappings = mappings;
/*     */   }
/*     */ 
/*     */   public Transformation clone()
/*     */   {
/*     */     try {
/* 125 */       Transformation clone = (Transformation)super.clone();
/* 126 */       if (clone.rxnClasses != null) {
/* 127 */         clone.rxnClasses = new ArrayList(clone.rxnClasses);
/*     */       }
/* 129 */       if (clone.species != null) {
/* 130 */         clone.species = new HashSet(clone.species);
/*     */       }
/* 132 */       if (clone.atomData != null) {
/* 133 */         clone.atomData = new ArrayList(clone.atomData);
/* 134 */         for (int i = 0; i < clone.atomData.size(); ++i) {
/* 135 */           clone.atomData.set(i, ((AtomData)clone.atomData.get(i)).clone());
/*     */         }
/*     */       }
/* 138 */       return clone;
/*     */     } catch (CloneNotSupportedException e) {
/* 140 */       throw new RuntimeException(e);
/*     */     }
/*     */   }
/*     */ 

/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.Transformation
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
