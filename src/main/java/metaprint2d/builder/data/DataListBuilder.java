/*    */ package metaprint2d.builder.data;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.ArrayList;
/*    */ import java.util.HashMap;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import java.util.Map.Entry;
/*    */ import java.util.Set;
/*    */ import metaprint2d.Fingerprint;
/*    */ import metaprint2d.FingerprintData;
import metaprint2d.analyzer.data.AtomData;
/*    */ import metaprint2d.analyzer.data.Transformation;
/*     import metaprint2d.analyzer.data.Transformation.AtomData;*/
import metaprint2d.analyzer.data.processor.DataSink;
/*    */ 
/*    */ public abstract class DataListBuilder
/*    */   implements DataSink<Transformation>
/*    */ {
/* 19 */   private Map<Fingerprint, FPData> data = new HashMap();
/*    */ 
/*    */   public void put(Transformation t) throws IOException {
/* 22 */     if (this.data == null) {
/* 23 */       throw new IOException("File closed");
/*    */     }
/* 25 */     for (AtomData d : t.getAtomData()) {
/* 26 */       Fingerprint fp = d.getFingerprint();
/*    */ 
/* 28 */       FPData fpd = (FPData)this.data.get(fp);
/* 29 */       if (fpd == null) {
/* 30 */         fpd = new FPData();
/* 31 */         this.data.put(fp, fpd);
/*    */       }
/*    */ 
/* 35 */       fpd.sCount += 1;
/*    */ 		if(d.isReactionCentre()){
					fpd.rcCount += 1;
				}
///* 38 */       Set rts = d.getReactionTypes();
///* 39 */       if ((rts != null) && (!(rts.isEmpty())))
///* 40 */         fpd.rcCount += 1;
/*    */     }
/*    */   }
/*    */ 
/*    */   protected List<FingerprintData> getData() throws IOException
/*    */   {
/* 46 */     if (this.data == null) {
/* 47 */       throw new IOException("File closed");
/*    */     }
/* 49 */     List list = new ArrayList(this.data.size());
/* 50 */     for (Map.Entry e : this.data.entrySet()) {
/* 51 */       Fingerprint fp = (Fingerprint)e.getKey();
/* 52 */       FPData fpd = (FPData)e.getValue();
/* 53 */       list.add(new FingerprintData(fp, fpd.rcCount, fpd.sCount));
/*    */     }
/* 55 */     return list;
/*    */   }
/*    */ 
/*    */   public void close()
/*    */     throws IOException
/*    */   {
/* 62 */     if (this.data != null)
/* 63 */       this.data = null;
/*    */   }
/*    */ 
/*    */   public void flush()
/*    */   {
/*    */   }
/*    */ 
/*    */   private static class FPData
/*    */   {
/* 76 */     private int sCount = 0;
/*    */ 
/* 81 */     private int rcCount = 0;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.builder.data.DataListBuilder
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
