/*     */ package metaprint2d;
/*     */ 
/*     */ import java.lang.ref.WeakReference;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.WeakHashMap;
/*     */ 
/*     */ public class Fingerprint
/*     */   implements Comparable<Fingerprint>
/*     */ {
/*     */   private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
/*  37 */   private static Map<Fingerprint, WeakReference<Fingerprint>> internMap = new WeakHashMap();
/*     */   private final FPLevel[] levels;
/*     */   private final String rootAtomType;
/*     */   private int hashCode;
/*     */ 
/*     */   public Fingerprint(byte[][] fp)
/*     */   {
/*  45 */     this.levels = new FPLevel[fp.length];
/*  46 */     for (int i = 0; i < fp.length; ++i) {
/*  47 */       this.levels[i] = new FPLevel(fp[i]);
/*     */     }
/*  49 */     this.rootAtomType = detectRootAtomType();
/*     */   }
/*     */ 
/*     */   public Fingerprint(FPLevel[] levels) {
/*  53 */     this.levels = new FPLevel[levels.length];
/*  54 */     System.arraycopy(levels, 0, this.levels, 0, levels.length);
/*  55 */     this.rootAtomType = detectRootAtomType();
/*     */   }
/*     */ 
/*     */   public Fingerprint(Fingerprint fp)
/*     */   {
/*  61 */     this.levels = fp.levels;
/*  62 */     this.rootAtomType = fp.rootAtomType;
/*     */   }
/*     */ 
/*     */   private String detectRootAtomType() {
				byte [] bytes = this.levels[0].getBytes();
/*  66 */     for (int i = 0; i < bytes.length; ++i) {
/*  67 */       if (bytes[i] != 0) {
/*  68 */         return ((String)Constants.ATOM_TYPE_LIST.get(i));
/*     */       }
/*     */     }
/*  71 */     return null;
/*     */   }
/*     */ 
/*     */   public Fingerprint intern()
/*     */   {
/*  89 */     WeakReference<Fingerprint> ref = (WeakReference)internMap.get(this);
/*     */ 	  Fingerprint ifp;
/*  91 */     if (ref != null) {
/*  92 */       ifp = (Fingerprint)ref.get();
/*  93 */       if (ifp != null) {
/*  94 */         return ifp;
/*     */       }
/*     */     }
/*  97 */     ifp = new Fingerprint(this);
/*     */ 
/*  99 */     for (int i = 0; i < this.levels.length; ++i) {
/* 100 */       this.levels[i] = this.levels[i].intern();
/*     */     }
/* 102 */     internMap.put(this, new WeakReference<Fingerprint>(this));
/* 103 */     return this;
/*     */   }
/*     */ 
/*     */   public byte[][] getBytes()
/*     */   {
/* 112 */     int nlevel = this.levels.length;
/* 113 */     byte[][] bytes = new byte[nlevel][];
/* 114 */     for (int i = 0; i < nlevel; ++i) {
/* 115 */       bytes[i] = this.levels[i].getBytes();
/*     */     }
/* 117 */     return bytes;
/*     */   }
/*     */ 
/*     */   public String getRootAtomType() {
/* 121 */     return this.rootAtomType;
/*     */   }
/*     */ 
/*     */   private static byte[][] intsToBytes(int[][] fp)
/*     */   {
/* 130 */     byte[][] bytes = new byte[fp.length][];
/* 131 */     for (int i = 0; i < fp.length; ++i) {
/* 132 */       int[] f = fp[i];
/* 133 */       bytes[i] = new byte[f.length];
/* 134 */       for (int j = 0; j < f.length; ++j) {
/* 135 */         bytes[i][j] = (byte)f[j];
/*     */       }
/*     */     }
/* 138 */     return bytes;
/*     */   }
/*     */ 
/*     */   public static Fingerprint parseString(String string) {
/* 142 */     byte[][] fp = getBytes(string);
/* 143 */     return new Fingerprint(fp);
/*     */   }
/*     */ 
/*     */   protected static byte[][] getBytes(String string) {
/* 147 */     byte[][] fp = new byte[6][33];
/* 148 */     for (int l = 0; l < 6; ++l) {
/* 149 */       for (int i = 0; i < 33; ++i) {
/* 150 */         char c = string.charAt(l * 33 + i);
/* 151 */         if ((c >= '0') && (c <= '9'))
/* 152 */           fp[l][i] = (byte)(c - '0');
/* 153 */         else if ((c >= 'A') && (c <= 'Z'))
/* 154 */           fp[l][i] = (byte)(c - 'A' + 10);
/*     */         else {
/* 156 */           throw new IllegalArgumentException("Illegal character: [" + c + "]\n[" + string + "]");
/*     */         }
/*     */       }
/*     */     }
/* 160 */     return fp;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 165 */     StringBuilder text = new StringBuilder();
/* 166 */     for (int l = 0; l < this.levels.length; ++l) {
/* 167 */       if (l != 0) {
/* 168 */         text.append(' ');
/*     */       }
/* 170 */       FPLevel f = this.levels[l];
				byte [] bytes = f.getBytes();
/* 171 */       for (int i = 0; i < bytes.length; ++i) {
/* 172 */         text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(bytes[i]));
/*     */       }
/*     */     }
/* 175 */     return text.toString();
/*     */   }
/*     */ 
/*     */   public String to6lvfString() {
/* 179 */     StringBuilder text = new StringBuilder();
				byte [] bytes;
/* 180 */     for (int l = 0; l < this.levels.length; ++l) {
					bytes = this.levels[l].getBytes();
/* 181 */       for (int i = 0; i < bytes.length; ++i) {
/* 182 */         text.append(bytes[i]);
/* 183 */         text.append(' ');
/*     */       }
/*     */     }
/* 186 */     return text.toString();
/*     */   }
/*     */ 
/*     */   public static Fingerprint parseFullString(String s)
/*     */   {
/* 195 */     String[] a = s.split(";");
/* 196 */     byte[][] bytes = new byte[6][33];
/* 197 */     bytes[0][((Integer)Constants.ATOM_TYPE_INDEX.get(a[0].toUpperCase())).intValue()] = 1;
/* 198 */     for (int i = 1; i < a.length; ++i) {
/* 199 */       int i0 = a[i].indexOf(45);
/* 200 */       int l = Integer.parseInt(a[i].substring(0, i0));
/* 201 */       int i1 = a[i].indexOf(45, i0 + 1);
/* 202 */       int n = Integer.parseInt(a[i].substring(i0 + 1, i1));
/* 203 */       String x = a[i].substring(i1 + 1);
/*     */ 
/* 205 */       int ix = ((Integer)Constants.ATOM_TYPE_INDEX.get(x.toUpperCase())).intValue();
/* 206 */       bytes[l][ix] = (byte)n;
/*     */     }
/* 208 */     return new Fingerprint(bytes);
/*     */   }
/*     */ 
/*     */   public String toFullString()
/*     */   {
				byte [] bytes = this.levels[0].getBytes();
/* 217 */     int len = bytes.length;
/* 218 */     StringBuilder text = new StringBuilder();
/* 219 */     for (int i = 0; i < len; ++i) {
/* 220 */       if (bytes[i] == 0) {
/*     */         continue;
/*     */       }
/* 223 */       text.append((String)Constants.ATOM_TYPE_LIST.get(i));
/* 224 */       text.append(';');
/*     */     }
/* 226 */     for (int l = 1; l < this.levels.length; ++l) {
				bytes = this.levels[l].getBytes();
/* 227 */       for (int i = 0; i < len; ++i) {
/* 228 */         int n = bytes[i];
/* 229 */         if (n != 0) {
/* 230 */           text.append(l);
/* 231 */           text.append('-');
/* 232 */           text.append(n);
/* 233 */           text.append('-');
/* 234 */           text.append((String)Constants.ATOM_TYPE_LIST.get(i));
/* 235 */           text.append(';');
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 241 */     return text.toString();
/*     */   }
/*     */ 
/*     */   public FPLevel getLevel(int i)
/*     */   {
/* 250 */     return this.levels[i];
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 257 */     if (obj == this) {
/* 258 */       return true;
/*     */     }
/* 260 */     if (obj == null) {
/* 261 */       return false;
/*     */     }
/* 263 */     FPLevel[] levels2 = ((Fingerprint)obj).levels;
/* 264 */     if (this.levels.length != levels2.length) {
/* 265 */       return false;
/*     */     }
/* 267 */     for (int i = this.levels.length - 1; i >= 0; --i) {
/* 268 */       if (!(this.levels[i].equals(levels2[i]))) {
/* 269 */         return false;
/*     */       }
/*     */     }
/* 272 */     return true;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 277 */     if (this.hashCode == 0) {
/* 278 */       int h = 0;
/* 279 */       for (int i = 0; i < this.levels.length; ++i) {
/* 280 */         h = 63 * h + this.levels[i].hashCode();
/*     */       }
/*     */ 
/* 283 */       this.hashCode = h;
/*     */     }
/* 285 */     return this.hashCode;
/*     */   }
/*     */ 
/*     */   public int compareTo(Fingerprint o) {
				byte[] thisBytes;
				byte [] thatBytes;
/* 289 */     for (int i = 0; i < 6; ++i) {
				thisBytes = this.levels[i].getBytes();
				thatBytes = o.levels[i].getBytes();
/* 290 */       for (int j = 0; j < 33; ++j) {
/* 291 */         byte a = thisBytes[j];
/* 292 */         byte b = thatBytes[j];
/* 293 */         if (a < b) {
/* 294 */           return -1;
/*     */         }
/* 296 */         if (a > b) {
/* 297 */           return 1;
/*     */         }
/*     */       }
/*     */     }
/* 301 */     return 0;
/*     */   }
/*     */ 
/*     */   public String toCompressedString()
/*     */   {
/* 307 */     StringBuilder s = new StringBuilder();
/* 308 */     s.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(this.levels.length));
/* 309 */     for (int i = 0; i < this.levels.length; ++i) {
/* 310 */       if (i != 0) {
/* 311 */         s.append("_");
/*     */       }
/* 313 */       
/* 314 */       byte[] bs = this.levels[i].getBytes();
/* 315 */       for (int j = 0; j < bs.length; ++j) {
/* 316 */         byte k = bs[j];
/* 317 */         if (k != 0) {
/* 318 */           s.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(j));
/* 319 */           s.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(k));
/*     */         }
/*     */       }
/*     */     }
/* 323 */     return s.toString();
/*     */   }
/*     */ 
/*     */   public static Fingerprint parseCompressedString(String string) {
/* 327 */     byte[][] fp = getBytesFromCompressedString(string);
/* 328 */     return new Fingerprint(fp);
/*     */   }
/*     */ 
/*     */   protected static byte[][] getBytesFromCompressedString(String string) {
/* 332 */     int nlevels = parseChar(string.charAt(0));
/* 333 */     byte[][] fp = new byte[nlevels][33];
/* 334 */     int ix = 1;
/* 335 */     int len = string.length();
/*     */ 
/* 337 */     for (int l = 0; l < nlevels; ++l) {
/* 338 */       while (ix < len)
/*     */       {
/*     */         char c;
/* 338 */         if ((c = string.charAt(ix)) == '_') break;
/* 339 */         byte fpix = parseChar(c);
/* 340 */         byte fpval = parseChar(string.charAt(ix + 1));
/* 341 */         fp[l][fpix] = fpval;
/* 342 */         ix += 2;
/*     */       }
/* 344 */       ++ix;
/*     */     }
/* 346 */     return fp;
/*     */   }
/*     */ 
/*     */   public static byte parseChar(char c) {
/* 350 */     if ((c >= '0') && (c <= '9')) {
/* 351 */       return (byte)(c - '0');
/*     */     }
/* 353 */     if ((c >= 'A') && (c <= 'Z')) {
/* 354 */       return (byte)('\n' + c - 65);
/*     */     }
/* 356 */     if ((c >= 'a') && (c <= 'z')) {
/* 357 */       return (byte)('$' + c - 97);
/*     */     }
/* 359 */     throw new IllegalArgumentException("Illegal character: " + c);
/*     */   }
/*     */ 

/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.Fingerprint
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
