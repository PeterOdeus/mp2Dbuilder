/*    */ package metaprint2d.analyzer.data.processor;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class ListSink<T>
/*    */   implements DataSink<T>
/*    */ {
/*  9 */   private List<T> list = new ArrayList();
/*    */ 
/*    */   public void put(T o) throws IOException {
/* 12 */     this.list.add(o);
/*    */   }
/*    */ 
/*    */   public void close() throws IOException
/*    */   {
/*    */   }
/*    */ 
/*    */   public void flush() throws IOException
/*    */   {
/*    */   }
/*    */ 
/*    */   public List<T> getList() {
/* 24 */     return new ArrayList(this.list);
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.processor.ListSink
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
