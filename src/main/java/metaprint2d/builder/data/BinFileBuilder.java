/*    */ package metaprint2d.builder.data;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.util.List;
/*    */ import metaprint2d.analyzer.data.Transformation;
import metaprint2d.data.BinFile;
/*    */ 
/*    */ public class BinFileBuilder extends DataListBuilder
/*    */ {
/*    */   private File file;
/*    */ 
/*    */   public BinFileBuilder(File file)
/*    */   {
/* 17 */     this.file = file;
/*    */   }
/*    */ 
/*    */   @SuppressWarnings("unchecked")
public void writeFile() throws IOException {
/* 21 */     List list = getData();
/* 22 */     BinFile.write(new FileOutputStream(this.file), list);
/*    */   }
/*    */ 
/*    */   public void close()
/*    */     throws IOException
/*    */   {
/* 29 */     writeFile();
/* 30 */     super.close();
/*    */   }
/*    */ 
/*    */   public static void build(List<Transformation> list, File file) throws IOException {
/* 34 */     BinFileBuilder builder = new BinFileBuilder(file);
/* 35 */     for (Transformation t : list) {
/* 36 */       builder.put(t);
/*    */     }
/* 38 */     builder.close();
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.builder.data.BinFileBuilder
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
