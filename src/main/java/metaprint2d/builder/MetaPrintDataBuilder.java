 package metaprint2d.builder;
 
 import metaprint2d.analyzer.data.Transformation;
import metaprint2d.analyzer.data.processor.DataProcessor;
import metaprint2d.analyzer.data.processor.DataSink;
import metaprint2d.analyzer.data.processor.DataSource;

import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
 
 public class MetaPrintDataBuilder extends DataProcessor<Transformation>
 {
   private static ILoggingTool LOG = LoggingToolFactory.createLoggingTool(MetaPrintDataBuilder.class);
 
   private boolean skipMultistep = true;
   private boolean skipNoRc = true;
//   private Set<ReactionTypes.ReactionType> reactionTypeFilter;
//   private Set<Species> speciesFilter;
 
   public MetaPrintDataBuilder(DataSource<Transformation> in, DataSink<Transformation> out, int nn)
   {
     super(in, out, nn);
     setLogger(LOG);
   }
 
   protected boolean acceptPreProcess(Transformation o)
   {
				return super.acceptPreProcess(o);
//     if ((this.skipMultistep) && (o.isMultiStep())) {
//       return false;
//     }
//				boolean hit = false;
//     if (this.speciesFilter == null){
//				assert "hej".equals("hejsan");
//				return (!(hit));
//	/*TODO return what?*///			break label123;
//				}
//     
//     if (o.getSpecies() != null) {
//       for (Species sf : this.speciesFilter) {
//         for (String sp : o.getSpecies()) {
//           if (sf.matches(sp)) {
//             hit = true;
//					  assert "hej".equals("hejsan");
//					  return (!(hit));
///*  TODO  what to do?*///             break label117:
//           }
//         }
//       }
//     }
// 
//     label117: label123: return (!(hit));
   }
 
   protected Transformation process(Transformation o)
   {
//     if (this.reactionTypeFilter != null) {
///*  TODO added generics */       List<AtomData> atomData = o.getAtomData();
//       if (atomData != null) {
//         for (AtomData atom : atomData) {
//           atom.getReactionTypes().retainAll(this.reactionTypeFilter);
//         }
//       }
//     }
     return ((Transformation)super.process(o));
   }
 
   protected boolean acceptPostProcess(Transformation o)
   {
//     if (this.skipNoRc)
//     {
//       Iterator<AtomData> localIterator=null;
//       List<AtomData> atomData = o.getAtomData();
//       if (atomData != null)
//         localIterator = atomData.iterator(); 
//				while (true) { 
///*TODO is non-null iterator possible?*/					assert localIterator != null;
//					AtomData atom = localIterator.next();
//         if (!(atom.getReactionTypes().isEmpty()))
//           return true;
//         if (!(localIterator.hasNext()))
//         {
//           return false; } }
//     }
     return true;
   }
 
   public boolean isSkipMultistep()
   {
     return this.skipMultistep;
   }
 
   public void setSkipMultistep(boolean skipMultistep) {
     checkState();
     this.skipMultistep = skipMultistep;
   }
 
//   public Set<ReactionTypes.ReactionType> getReactionTypeFilter() {
//     return new HashSet(this.reactionTypeFilter);
//   }
// 
//   public void setReactionTypeFilter(Collection<ReactionTypes.ReactionType> reactionTypeFilter) {
//     checkState();
//     this.reactionTypeFilter = new HashSet(reactionTypeFilter);
//   }
// 
//   public Set<Species> getSpeciesFilter() {
//     return this.speciesFilter;
//   }
// 
//   public void setSpeciesFilter(Collection<Species> speciesFilter) {
//     checkState();
//     this.speciesFilter = new HashSet(speciesFilter);
//   }
 
   public void setSkipNoRc(boolean skipNoRc) {
     this.skipNoRc = skipNoRc;
   }
 }
