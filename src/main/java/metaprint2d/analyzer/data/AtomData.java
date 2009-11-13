package metaprint2d.analyzer.data;
import java.util.HashSet;
import java.util.Set;

import metaprint2d.Fingerprint;

/*     */   public class AtomData
/*     */     implements Cloneable
/*     */   {
/*     */     private Fingerprint fingerprint;
/*     */     private boolean isReactionCentre;
/*     */ 
/*     */     public AtomData(Fingerprint fingerprint, boolean isReactionCentre)
/*     */     {
/* 154 */       if (fingerprint == null) {
/* 155 */         throw new NullPointerException();
/*     */       }
/* 160 */       this.fingerprint = fingerprint;
/* 161 */       this.isReactionCentre = isReactionCentre;
/*     */     }
/*     */ 
/*     */     public Fingerprint getFingerprint() {
/* 165 */       return this.fingerprint;
/*     */     }
/*     */ 
/*     */     public boolean getIsReactionCentre() {
/* 169 */       return this.isReactionCentre;
/*     */     }
/*     */ 
/*     */     public AtomData clone() {
/*     */       try {
/* 174 */         AtomData clone = (AtomData)super.clone();
/* 176 */         return clone;
/*     */       } catch (CloneNotSupportedException e) {
/* 178 */         throw new RuntimeException(e);
/*     */       }
/*     */     }
/*     */ 
/*     */     public boolean isReactionCentre() {
/* 183 */       return this.isReactionCentre;
/*     */     }
/*     */   }