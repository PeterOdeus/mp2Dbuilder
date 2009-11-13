/*    */ package metaprint2d.analyzer.data.processor;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class ProgressMonitor extends Thread
/*    */ {
/*    */   private MonitorableProcess proc;
/*    */   private String message;
/*    */ 
/*    */   public ProgressMonitor(MonitorableProcess proc)
/*    */   {
/* 14 */     this("Working...", proc);
/*    */   }
/*    */ 
/*    */   public ProgressMonitor(String message, MonitorableProcess proc) {
/* 18 */     this.proc = proc;
/* 19 */     this.message = message;
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/* 24 */     int i = 0;
/* 25 */     while (!(this.proc.isDone())) {
/* 26 */       int n = this.proc.getProgress();
/* 27 */       System.out.print(this.message);
/* 28 */       System.out.print(' ');
/* 29 */       switch (i)
/*    */       {
/*    */       case 0:
/* 31 */         System.out.print('-'); break;
/*    */       case 1:
/* 33 */         System.out.print('\\'); break;
/*    */       case 2:
/* 35 */         System.out.print('|'); break;
/*    */       case 3:
/* 37 */         System.out.print('/');
/*    */       }
/* 39 */       i = (i + 1) % 4;
/*    */ 
/* 41 */       System.out.print(" [" + n + "]\r");
/*    */       try
/*    */       {
/* 44 */         sleep(200L);
/*    */       } catch (InterruptedException e) {
/* 46 */         e.printStackTrace();
/*    */       }
/*    */     }
/* 49 */     System.out.println("Working... DONE              ");
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.processor.ProgressMonitor
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
