 package metaprint2d.builder.data;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import metaprint2d.analyzer.data.Transformation;
import metaprint2d.data.BinFile;
 
 public class BinFileBuilder extends DataListBuilder
 {
   private File file;
 
   public BinFileBuilder(File file)
   {
     this.file = file;
   }
 
   @SuppressWarnings("unchecked")
public void writeFile() throws IOException {
     List list = getData();
     BinFile.write(new FileOutputStream(this.file), list);
   }
 
   public void close()
     throws IOException
   {
     writeFile();
     super.close();
   }
 
   public static void build(List<Transformation> list, File file) throws IOException {
     BinFileBuilder builder = new BinFileBuilder(file);
     for (Transformation t : list) {
       builder.put(t);
     }
     builder.close();
   }
 }

