package metaprint2d;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*     */   public class FPLevel
/*     */   {
/* 366 */     private static Map<FPLevel, WeakReference<FPLevel>> internMap = new WeakHashMap();
/*     */     private byte[] bytes;
/*     */     private int hashCode;
/*     */ 
				public FPLevel(SortedMap<String,AtomicInteger> aSortedMap)
/*     */     {
/* 372 */       this.bytes = new byte[aSortedMap.size()];
				Iterator<AtomicInteger> iter = aSortedMap.values().iterator();
				int i = 0;
				while(iter.hasNext()){
					this.bytes[i] = iter.next().byteValue();
					i++;
				}
/*     */     }

/*     */     public FPLevel(byte[] bytes)
/*     */     {
/* 372 */       this.bytes = new byte[bytes.length];
/* 373 */       System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
/*     */     }
/*     */ 
/*     */     public FPLevel(FPLevel level) {
/* 377 */       this.bytes = new byte[level.bytes.length];
/* 378 */       System.arraycopy(level.bytes, 0, this.bytes, 0, this.bytes.length);
/* 379 */       this.hashCode = level.hashCode;
/*     */     }
/*     */ 
/*     */     public FPLevel intern()
/*     */     {
/* 396 */       WeakReference<FPLevel> ref = (WeakReference)internMap.get(this);
/*     */ 		FPLevel ifpl;
/* 398 */       if (ref != null) {
/* 399 */         ifpl = (FPLevel)ref.get();
/* 400 */         if (ifpl != null) {
/* 401 */           return ifpl;
/*     */         }
/*     */       }
/* 404 */       ifpl = new FPLevel(this);
/* 405 */       internMap.put(this, new WeakReference<FPLevel>(this));
/* 406 */       return this;
/*     */     }
/*     */ 
/*     */     public int hashCode()
/*     */     {
/* 411 */       if (this.hashCode == 0) {
/* 412 */         int h = 0;
/* 413 */         for (int i = 0; i < this.bytes.length; ++i)
/*     */         {
/* 415 */           h = 15 * h + this.bytes[i];
/*     */         }
/* 417 */         this.hashCode = h;
/*     */       }
/* 419 */       return this.hashCode;
/*     */     }
/*     */ 
/*     */     public boolean equals(Object obj)
/*     */     {
/* 424 */       if ((obj == null) || (!(obj instanceof FPLevel))) {
/* 425 */         return false;
/*     */       }
/* 427 */       return Arrays.equals(this.bytes, ((FPLevel)obj).bytes);
/*     */     }
/*     */ 
/*     */     public double calcSoergelDistance(FPLevel that)
/*     */     {
/* 432 */       byte[] xs = this.bytes;
/* 433 */       byte[] ys = that.bytes;
/*     */ 
/* 435 */       int length = xs.length;
/* 436 */       if (length != ys.length) {
/* 437 */         throw new IllegalArgumentException("Fingerprints have different lengths (" + xs.length + ", " + ys.length + ")");
/*     */       }
/*     */ 
/* 440 */       double numerator = 0.0D;
/* 441 */       double denominator = 0.0D;
/*     */ 
/* 443 */       for (int i = 0; i < length; ++i) {
/* 444 */         byte x = xs[i]; byte y = ys[i];
/* 445 */         numerator += x * y;
/* 446 */         denominator += x * x + y * y - (x * y);
/*     */       }
/*     */ 
/* 449 */       return ((denominator == 0.0D) ? 0.0D : 1.0D - (numerator / denominator));
/*     */     }
/*     */ 
/*     */     public double calcHammingDistance(FPLevel that)
/*     */     {
/* 454 */       byte[] xs = this.bytes;
/* 455 */       byte[] ys = that.bytes;
/*     */ 
/* 457 */       int length = xs.length;
/* 458 */       if (length != ys.length) {
/* 459 */         throw new IllegalArgumentException("Fingerprints have different lengths (" + xs.length + ", " + ys.length + ")");
/*     */       }
/*     */ 
/* 462 */       double dist = 0.0D;
/*     */ 
/* 464 */       for (int i = 0; i < length; ++i) {
/* 465 */         byte x = xs[i]; byte y = ys[i];
/* 466 */         dist += Math.abs(x - y);
/*     */       }
/*     */ 
/* 469 */       return dist;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 475 */       StringBuilder text = new StringBuilder();
/* 476 */       for (int i = 0; i < this.bytes.length; ++i) {
/* 477 */         text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(this.bytes[i]));
/*     */       }
/* 479 */       return text.toString();
/*     */     }
/*     */ 
/*     */     public String toCompressedString() {
/* 483 */       StringBuilder text = new StringBuilder();
/* 484 */       text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".length()));
/* 485 */       for (int j = 0; j < this.bytes.length; ++j) {
/* 486 */         byte k = this.bytes[j];
/* 487 */         if (k != 0) {
/* 488 */           text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(j));
/* 489 */           text.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(k));
/*     */         }
/*     */       }
/* 492 */       return text.toString();
/*     */     }
/*     */ 
/*     */     public String to6lvfString() {
/* 496 */       StringBuilder text = new StringBuilder();
/* 497 */       for (int i = 0; i < this.bytes.length; ++i) {
/* 498 */         if (i != 0) {
/* 499 */           text.append(' ');
/*     */         }
/* 501 */         text.append(this.bytes[i]);
/*     */       }
/* 503 */       return text.toString();
/*     */     }
/*     */ 
/*     */     public byte[] getBytes() {
/* 507 */       byte[] copy = new byte[this.bytes.length];
/* 508 */       System.arraycopy(this.bytes, 0, copy, 0, this.bytes.length);
/* 509 */       return copy;
/*     */     }
/*     */ 
/*     */     public static FPLevel parseString(String string) {
/* 513 */       byte[] fp = new byte[33];
/* 514 */       for (int j = 0; j < 33; ++j) {
/* 515 */         char c = string.charAt(j);
/* 516 */         if ((c >= '0') && (c <= '9'))
/* 517 */           fp[j] = (byte)(c - '0');
/* 518 */         else if ((c >= 'A') && (c <= 'Z'))
/* 519 */           fp[j] = (byte)(c - 'A' + 10);
/*     */         else {
/* 521 */           throw new IllegalArgumentException("Illegal character: [" + c + "]\n[" + string + "]");
/*     */         }
/*     */       }
/* 524 */       return new FPLevel(fp);
/*     */     }
/*     */ 
/*     */     public static FPLevel parseCompressedString(String string) {
/* 528 */       byte[] fp = new byte[33];
/* 529 */       for (int i = 0; i < string.length(); i += 2) {
/* 530 */         byte fpix = Fingerprint.parseChar(string.charAt(i));
/* 531 */         byte fpval = Fingerprint.parseChar(string.charAt(i + 1));
/* 532 */         fp[fpix] = fpval;
/*     */       }
/* 534 */       return new FPLevel(fp);
/*     */     }
/*     */ 
/*     */     public boolean matches(byte[] bytes) {
/* 538 */       if (bytes == null) {
/* 539 */         return false;
/*     */       }
/* 541 */       if (bytes.length != this.bytes.length) {
/* 542 */         return false;
/*     */       }
/* 544 */       for (int i = bytes.length - 1; i >= 0; --i) {
/* 545 */         if (bytes[i] != this.bytes[i]) {
/* 546 */           return false;
/*     */         }
/*     */       }
/* 549 */       return true;
/*     */     }
/*     */ 
/*     */     public int getByte(int index) {
/* 553 */       return this.bytes[index];
/*     */     }
/*     */   }
