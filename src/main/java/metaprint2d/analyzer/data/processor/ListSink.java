 package metaprint2d.analyzer.data.processor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ListSink<T>
   implements DataSink<T>
 {
   private List<T> list = new ArrayList();
 
   public void put(T o) throws IOException {
     this.list.add(o);
   }
 
   public void close() throws IOException
   {
   }
 
   public void flush() throws IOException
   {
   }
 
   public List<T> getList() {
     return new ArrayList(this.list);
   }
 }

