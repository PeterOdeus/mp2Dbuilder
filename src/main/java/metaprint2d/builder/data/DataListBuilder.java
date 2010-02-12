 package metaprint2d.builder.data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import metaprint2d.Fingerprint;
 import metaprint2d.FingerprintData;
import metaprint2d.analyzer.data.AtomData;
 import metaprint2d.analyzer.data.Transformation;
/*     import metaprint2d.analyzer.data.Transformation.AtomData;*/
import metaprint2d.analyzer.data.processor.DataSink;
 
 public abstract class DataListBuilder
   implements DataSink<Transformation>
 {
   private Map<Fingerprint, FPData> data = new HashMap();
 
   public void put(Transformation t) throws IOException {
     if (this.data == null) {
       throw new IOException("File closed");
     }
     for (AtomData d : t.getAtomData()) {
       Fingerprint fp = d.getFingerprint();
 
       FPData fpd = (FPData)this.data.get(fp);
       if (fpd == null) {
         fpd = new FPData();
         this.data.put(fp, fpd);
       }
 
       fpd.sCount += 1;
 		if(d.isReactionCentre()){
					fpd.rcCount += 1;
				}
//       Set rts = d.getReactionTypes();
//       if ((rts != null) && (!(rts.isEmpty())))
//         fpd.rcCount += 1;
     }
   }
 
   protected List<FingerprintData> getData() throws IOException
   {
     if (this.data == null) {
       throw new IOException("File closed");
     }
     List list = new ArrayList(this.data.size());
     for (Map.Entry e : this.data.entrySet()) {
       Fingerprint fp = (Fingerprint)e.getKey();
       FPData fpd = (FPData)e.getValue();
       list.add(new FingerprintData(fp, fpd.rcCount, fpd.sCount));
     }
     return list;
   }
 
   public void close()
     throws IOException
   {
     if (this.data != null)
       this.data = null;
   }
 
   public void flush()
   {
   }
 
   private static class FPData
   {
     private int sCount = 0;
 
     private int rcCount = 0;
   }
 }
