/*     */ package metaprint2d.data;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.zip.GZIPInputStream;
/*     */ import java.util.zip.GZIPOutputStream;
/*     */ import metaprint2d.FPLevel;
/*     */ import metaprint2d.FingerprintData;
/*     */ 
/*     */ public class BinFile
/*     */ {
///*     */   public static List<FingerprintData> read(InputStream is)
///*     */     throws IOException
///*     */   {
///*  48 */     ArrayList list = new ArrayList();
///*     */ 
///*  50 */     parse(is, new BinDataHandlerImpl(list));
///*  61 */     return list;
///*     */   }
///*     */ 
///*     */   public static void parse(InputStream is, BinDataHandler handler)
///*     */     throws IOException
///*     */   {
///*  70 */     DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
///*     */     try
///*     */     {
///* 102 */       for (char c : "MetaPrint2D\n".toCharArray()) {
///* 103 */         if (c != in.readChar()) {
///* 104 */           throw new IOException("Datafile header missing");
///*     */         }
///*     */       }
///* 107 */       StringBuilder v = new StringBuilder();
///* 108 */       for (char c = in.readChar(); c != '\n'; c = in.readChar()) {
///* 109 */         v.append(c);
///*     */       }
///* 111 */       double version = Double.parseDouble(v.toString());
///* 112 */       if (version != 1.0D) {
///* 113 */         throw new IOException("Version expected: 1.0, found: " + version);
///*     */       }
///*     */ 
///* 117 */       int numFingerprints = in.readInt();
///* 118 */       handler.numFingerprints(numFingerprints);
///*     */ 
///* 120 */       byte[][] bytes = new byte[6][];
///* 121 */       FPLevel[] levels = new FPLevel[6];
///*     */ 
///* 123 */       while (numFingerprints > 0) {
///* 124 */         --numFingerprints;
///*     */ 
///* 127 */         byte nlevels = in.readByte();
///*     */ 
///* 129 */         for (int level = 6 - nlevels; level < 6; ++level) {
///* 130 */           bytes[level] = new byte[33];
///* 131 */           byte numBitsThisLevel = in.readByte();
///* 132 */           for (int j = 0; j < numBitsThisLevel; ++j) {
///* 133 */             byte fpPosn = in.readByte();
///* 134 */             byte fpValue = in.readByte();
///* 135 */             bytes[level][fpPosn] = fpValue;
///*     */           }
///* 137 */           FPLevel l = new FPLevel(bytes[level]);
///* 138 */           levels[level] = l.intern();
///*     */         }
///*     */ 
///* 142 */         int rc = in.readInt();
///* 143 */         int sc = in.readInt();
///*     */ 
///* 146 */         byte b = in.readByte();
///* 147 */         if (b != -1) {
///* 148 */           throw new IOException("File error: " + b);
///*     */         }
///*     */ 
///* 152 */         handler.entry(levels, rc, sc);
///*     */       }
///*     */ 
///*     */     }
///*     */     finally
///*     */     {
///* 164 */       in.close();
///*     */     }
///*     */   }
/*     */ 
/*     */   public static void write(OutputStream os, Collection<? extends FingerprintData> data)
/*     */     throws IOException
/*     */   {
/*     */     FPLevel[] fplevels;
/*     */     Iterator i$;
/* 173 */     List fingerprints = new ArrayList(data);
/* 174 */     Collections.sort(fingerprints);
/*     */ 
/* 176 */     DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(os)));
/*     */ 
/* 182 */     int[] posSet = new int[33];
/*     */     try
/*     */     {
/* 186 */       out.writeChars("MetaPrint2D\n");
/*     */ 
/* 188 */       out.writeChars("1.0\n");
/*     */ 
/* 191 */       out.writeInt(fingerprints.size());
/*     */ 
/* 193 */       fplevels = new FPLevel[6];
/*     */ 
/* 195 */       for (i$ = fingerprints.iterator(); i$.hasNext(); )
/*     */       {
/*     */         FPLevel fpl;
/* 195 */         FingerprintData fp = (FingerprintData)i$.next();
/*     */ 
/* 198 */         int newLevels = 6;
/* 199 */         for (int level = 0; level < 6; ++level) {
/* 200 */           fpl = fp.getLevel(level);
/* 201 */           if (!(fpl.equals(fplevels[level]))) break;
/* 202 */           --newLevels;
/*     */         }
/*     */ 
/* 209 */         out.writeByte(newLevels);
/*     */ 
/* 212 */         for (int level = 6 - newLevels; level < 6; ++level)
/*     */         {
/* 214 */           fpl = fp.getLevel(level);
/* 215 */           fplevels[level] = fpl;
/* 216 */           byte[] bytes = fpl.getBytes();
/*     */ 
/* 219 */           int nset = 0;
/* 220 */           for (int i = 0; i < 33; ++i) {
/* 221 */             if (bytes[i] != 0) {
/* 222 */               posSet[(nset++)] = i;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 227 */           out.writeByte(nset);
/*     */ 
/* 229 */           for (int i = 0; i < nset; ++i) {
/* 230 */             int ix = posSet[i];
/*     */ 
/* 232 */             out.writeByte(ix);
/*     */ 
/* 234 */             out.writeByte(bytes[ix]);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 239 */         out.writeInt(fp.getRcCount());
/* 240 */         out.writeInt(fp.getSubstrateCount());
/*     */ 
/* 243 */         out.writeByte(-1);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 248 */       out.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.data.BinFile
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
