/*     */ package metaprint2d.builder;
/*     */ 
/*     */ import metaprint2d.analyzer.data.Transformation;
import metaprint2d.analyzer.data.processor.DataProcessor;
import metaprint2d.analyzer.data.processor.DataSink;
import metaprint2d.analyzer.data.processor.DataSource;

import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
//import sea36.util.LogTool;
/*     */ 
/*     */ public class MetaPrintDataBuilder extends DataProcessor<Transformation>
/*     */ {
/*  19 */   private static ILoggingTool LOG = LoggingToolFactory.createLoggingTool(MetaPrintDataBuilder.class);
/*     */ 
/*  21 */   private boolean skipMultistep = true;
/*  22 */   private boolean skipNoRc = true;
///*     */   private Set<ReactionTypes.ReactionType> reactionTypeFilter;
///*     */   private Set<Species> speciesFilter;
/*     */ 
/*     */   public MetaPrintDataBuilder(DataSource<Transformation> in, DataSink<Transformation> out, int nn)
/*     */   {
/*  27 */     super(in, out, nn);
/*  28 */     setLogger(LOG);
/*     */   }
/*     */ 
/*     */   protected boolean acceptPreProcess(Transformation o)
/*     */   {
				return super.acceptPreProcess(o);
///*  33 */     if ((this.skipMultistep) && (o.isMultiStep())) {
///*  34 */       return false;
///*     */     }
//				boolean hit = false;
///*  36 */     if (this.speciesFilter == null){
//				assert "hej".equals("hejsan");
//				return (!(hit));
//	/*TODO return what?*///			break label123;
//				}
///*  37 */     
///*  38 */     if (o.getSpecies() != null) {
///*  39 */       for (Species sf : this.speciesFilter) {
///*  40 */         for (String sp : o.getSpecies()) {
///*  41 */           if (sf.matches(sp)) {
///*  42 */             hit = true;
//					  assert "hej".equals("hejsan");
//					  return (!(hit));
///*  TODO  what to do?*///             break label117:
///*     */           }
///*     */         }
///*     */       }
///*     */     }
///*     */ 
///*  49 */     label117: label123: return (!(hit));
/*     */   }
/*     */ 
/*     */   protected Transformation process(Transformation o)
/*     */   {
///*  57 */     if (this.reactionTypeFilter != null) {
///*  TODO added generics */       List<AtomData> atomData = o.getAtomData();
///*  59 */       if (atomData != null) {
///*  60 */         for (AtomData atom : atomData) {
///*  61 */           atom.getReactionTypes().retainAll(this.reactionTypeFilter);
///*     */         }
///*     */       }
///*     */     }
/*  65 */     return ((Transformation)super.process(o));
/*     */   }
/*     */ 
/*     */   protected boolean acceptPostProcess(Transformation o)
/*     */   {
///*  70 */     if (this.skipNoRc)
///*     */     {
///*     */       Iterator<AtomData> localIterator=null;
///*  71 */       List<AtomData> atomData = o.getAtomData();
///*  72 */       if (atomData != null)
///*  73 */         localIterator = atomData.iterator(); 
//				while (true) { 
///*TODO is non-null iterator possible?*/					assert localIterator != null;
//					AtomData atom = localIterator.next();
///*  74 */         if (!(atom.getReactionTypes().isEmpty()))
///*  75 */           return true;
///*  73 */         if (!(localIterator.hasNext()))
///*     */         {
///*  79 */           return false; } }
///*     */     }
/*  81 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isSkipMultistep()
/*     */   {
/*  86 */     return this.skipMultistep;
/*     */   }
/*     */ 
/*     */   public void setSkipMultistep(boolean skipMultistep) {
/*  90 */     checkState();
/*  91 */     this.skipMultistep = skipMultistep;
/*     */   }
/*     */ 
///*     */   public Set<ReactionTypes.ReactionType> getReactionTypeFilter() {
///*  95 */     return new HashSet(this.reactionTypeFilter);
///*     */   }
///*     */ 
///*     */   public void setReactionTypeFilter(Collection<ReactionTypes.ReactionType> reactionTypeFilter) {
///*  99 */     checkState();
///* 100 */     this.reactionTypeFilter = new HashSet(reactionTypeFilter);
///*     */   }
///*     */ 
///*     */   public Set<Species> getSpeciesFilter() {
///* 104 */     return this.speciesFilter;
///*     */   }
///*     */ 
///*     */   public void setSpeciesFilter(Collection<Species> speciesFilter) {
///* 108 */     checkState();
///* 109 */     this.speciesFilter = new HashSet(speciesFilter);
///*     */   }
/*     */ 
/*     */   public void setSkipNoRc(boolean skipNoRc) {
/* 113 */     this.skipNoRc = skipNoRc;
/*     */   }
/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.builder.MetaPrintDataBuilder
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
