/*     */ package metaprint2d;
/*     */ 
/*     */ public class FingerprintData extends Fingerprint
/*     */ {
/*  28 */   public static boolean eq = true;
/*     */   private final int id;
/*     */   private final int rcCount;
/*     */   private final int substrateCount;
/*     */ 
/*     */   public FingerprintData(int id, byte[][] bytes, int rc, int sc)
/*     */   {
/*  46 */     super(bytes);
/*  47 */     this.id = id;
/*  48 */     this.rcCount = rc;
/*  49 */     this.substrateCount = sc;
/*     */   }
/*     */ 
/*     */   public FingerprintData(int id, FPLevel[] levels, int rc, int sc) {
/*  53 */     super(levels);
/*  54 */     this.id = id;
/*  55 */     this.rcCount = rc;
/*  56 */     this.substrateCount = sc;
/*     */   }
/*     */ 
/*     */   public FingerprintData(int id, Fingerprint fp, int rc, int sc) {
/*  60 */     super(fp);
/*  61 */     this.id = id;
/*  62 */     this.rcCount = rc;
/*  63 */     this.substrateCount = sc;
/*     */   }
/*     */ 
/*     */   public FingerprintData(Fingerprint fp, int rc, int sc) {
/*  67 */     super(fp);
/*  68 */     this.id = 0;
/*  69 */     this.rcCount = rc;
/*  70 */     this.substrateCount = sc;
/*     */   }
/*     */ 
/*     */   public int getId()
/*     */   {
/*  78 */     return this.id;
/*     */   }
/*     */ 
/*     */   public int getRcCount()
/*     */   {
/*  86 */     return this.rcCount;
/*     */   }
/*     */ 
/*     */   public int getSubstrateCount()
/*     */   {
/*  94 */     return this.substrateCount;
/*     */   }
/*     */ 
/*     */   public static FingerprintData parseString(String string, int rc, int sc)
/*     */   {
/*  99 */     byte[][] bytes = Fingerprint.getBytes(string);
/* 100 */     Fingerprint fp = new Fingerprint(bytes);
/* 101 */     return new FingerprintData(fp, rc, sc);
/*     */   }
/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.FingerprintData
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
