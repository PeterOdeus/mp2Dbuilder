 package metaprint2d.builder.data;
 
 import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import metaprint2d.Fingerprint;
import metaprint2d.FingerprintData;
import metaprint2d.analyzer.data.AtomData;
import metaprint2d.analyzer.data.Transformation;
import metaprint2d.analyzer.data.processor.DataSink;
 
 public abstract class DataListBuilder
   implements DataSink<Transformation>
 {
   private Map<Fingerprint, FPData> data = new HashMap();
 
   public synchronized void put(Transformation t) throws IOException {
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
   
   public void put(List<FingerprintData> fingerprintDataList) throws Exception{
	   FPData fpd = null;
	   for(FingerprintData fpData: fingerprintDataList){
		   fpd = (FPData)this.data.get(fpData);
		   if (fpd == null) {
	         fpd = new FPData();
	         this.data.put(fpData, fpd);
	       }
		   fpd.sCount += fpData.getSubstrateCount();
		   fpd.rcCount += fpData.getRcCount();
	   }
   }
   
   public int diff(FingerprintData fpData){
	   FPData fpd = null;
	   fpd = (FPData)this.data.get(fpData);
	   if (fpd == null) {
         return -1;
       }
	   if(fpd.sCount != fpData.getSubstrateCount()){
		   return -1;
	   }
	   if(fpd.rcCount != fpData.getRcCount()){
		   return -1;
	   }
	   return 0;
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
