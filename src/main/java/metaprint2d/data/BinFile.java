 package metaprint2d.data;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 import metaprint2d.FPLevel;
 import metaprint2d.FingerprintData;
 
 public class BinFile
 {
//   public static List<FingerprintData> read(InputStream is)
//     throws IOException
//   {
//     ArrayList list = new ArrayList();
// 
//     parse(is, new BinDataHandlerImpl(list));
//     return list;
//   }
// 
//   public static void parse(InputStream is, BinDataHandler handler)
//     throws IOException
//   {
//     DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
//     try
//     {
//       for (char c : "MetaPrint2D\n".toCharArray()) {
//         if (c != in.readChar()) {
//           throw new IOException("Datafile header missing");
//         }
//       }
//       StringBuilder v = new StringBuilder();
//       for (char c = in.readChar(); c != '\n'; c = in.readChar()) {
//         v.append(c);
//       }
//       double version = Double.parseDouble(v.toString());
//       if (version != 1.0D) {
//         throw new IOException("Version expected: 1.0, found: " + version);
//       }
// 
//       int numFingerprints = in.readInt();
//       handler.numFingerprints(numFingerprints);
// 
//       byte[][] bytes = new byte[6][];
//       FPLevel[] levels = new FPLevel[6];
// 
//       while (numFingerprints > 0) {
//         --numFingerprints;
// 
//         byte nlevels = in.readByte();
// 
//         for (int level = 6 - nlevels; level < 6; ++level) {
//           bytes[level] = new byte[33];
//           byte numBitsThisLevel = in.readByte();
//           for (int j = 0; j < numBitsThisLevel; ++j) {
//             byte fpPosn = in.readByte();
//             byte fpValue = in.readByte();
//             bytes[level][fpPosn] = fpValue;
//           }
//           FPLevel l = new FPLevel(bytes[level]);
//           levels[level] = l.intern();
//         }
// 
//         int rc = in.readInt();
//         int sc = in.readInt();
// 
//         byte b = in.readByte();
//         if (b != -1) {
//           throw new IOException("File error: " + b);
//         }
// 
//         handler.entry(levels, rc, sc);
//       }
// 
//     }
//     finally
//     {
//       in.close();
//     }
//   }
 
   public static void write(OutputStream os, Collection<? extends FingerprintData> data)
     throws IOException
   {
     FPLevel[] fplevels;
     Iterator i$;
     List fingerprints = new ArrayList(data);
     Collections.sort(fingerprints);
 
     DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(os)));
 
     int[] posSet = new int[33];
     try
     {
       out.writeChars("MetaPrint2D\n");
 
       out.writeChars("1.0\n");
 
       out.writeInt(fingerprints.size());
 
       fplevels = new FPLevel[6];
 
       for (i$ = fingerprints.iterator(); i$.hasNext(); )
       {
         FPLevel fpl;
         FingerprintData fp = (FingerprintData)i$.next();
 
         int newLevels = 6;
         for (int level = 0; level < 6; ++level) {
           fpl = fp.getLevel(level);
           if (!(fpl.equals(fplevels[level]))) break;
           --newLevels;
         }
 
         out.writeByte(newLevels);
 
         for (int level = 6 - newLevels; level < 6; ++level)
         {
           fpl = fp.getLevel(level);
           fplevels[level] = fpl;
           byte[] bytes = fpl.getBytes();
 
           int nset = 0;
           for (int i = 0; i < 33; ++i) {
             if (bytes[i] != 0) {
               posSet[(nset++)] = i;
             }
 
           }
 
           out.writeByte(nset);
 
           for (int i = 0; i < nset; ++i) {
             int ix = posSet[i];
 
             out.writeByte(ix);
 
             out.writeByte(bytes[ix]);
           }
 
         }
 
         out.writeInt(fp.getRcCount());
         out.writeInt(fp.getSubstrateCount());
 
         out.writeByte(-1);
       }
     }
     finally
     {
       out.close();
     }
   }
 
   
 }
