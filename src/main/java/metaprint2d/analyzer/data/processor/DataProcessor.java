/*    */ package metaprint2d.analyzer.data.processor;
/*    */ 
/*    */ import java.io.IOException;

import metaprint2d.builder.DataBuilderApp;

import org.openscience.cdk.tools.LoggingTool;

//import sea36.util.LogTool;
/*    */ 
/*    */ public class DataProcessor<T>
/*    */   implements MonitorableProcess
/*    */ {
/*    */   private LoggingTool LOG;
/*    */   private DataSource<T> in;
/*    */   private DataSink<T> out;
/*    */   private int nn;
/* 15 */   private boolean started = false;
/* 16 */   private boolean done = false;
/*    */ 
/*    */   public DataProcessor(DataSource<T> in, DataSink<T> out) {
/* 19 */     this.in = in;
/* 20 */     this.out = out;
/*    */   }
/*    */ 
/*    */   public void run() throws Exception {
/* 24 */     checkState();
/* 25 */     this.started = true;
/*    */     try
/*    */     {
/*    */       while (true) {
/* 29 */         this.nn += 1;
/* 30 */         if (this.LOG != null) {
/* 31 */           this.LOG.debug("Reading #" + Integer.valueOf(this.nn));
/*    */         }
/* TODO changed from Object to T */         T o = this.in.getNext();
/* 34 */         if (o == null) {
/*    */           break;
/*    */         }
/*    */ 
/* 38 */         if (this.LOG != null) {
/* 39 */           this.LOG.debug("Processing #" + Integer.valueOf(this.nn));
/*    */         }
/* 41 */         if (acceptPreProcess(o));
/* 42 */         o = process(o);
/* 43 */         if (acceptPostProcess(o));
/* 44 */         this.out.put(o);
/*    */       }
/*    */ 
/* 48 */       this.out.flush();
/*    */     } finally {
/* 50 */       this.done = true;
/*    */     }
/*    */   }
/*    */ 
/*    */   protected void checkState() {
/* 55 */     if (this.started)
/* 56 */       throw new IllegalStateException("Already run");
/*    */   }
/*    */ 
/*    */   protected boolean acceptPreProcess(T o)
/*    */   {
/* 61 */     return true;
/*    */   }
/*    */ 
/*    */   protected boolean acceptPostProcess(T o) {
/* 65 */     return true;
/*    */   }
/*    */ 
/*    */   protected T process(T o) {
/* 69 */     return o;
/*    */   }
/*    */ 
/*    */   protected void setLogger(LoggingTool log) {
/* 73 */     this.LOG = log;
/*    */   }
/*    */ 
/*    */   public int getProgress() {
/* 77 */     return this.nn;
/*    */   }
/*    */ 
/*    */   public boolean isDone() {
/* 81 */     return this.done;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.processor.DataProcessor
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
