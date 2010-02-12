 package metaprint2d.analyzer.data.processor;
 
 import java.io.PrintStream;
 
 public class ProgressMonitor extends Thread
 {
   private MonitorableProcess proc;
   private String message;
 
   public ProgressMonitor(MonitorableProcess proc)
   {
     this("Working...", proc);
   }
 
   public ProgressMonitor(String message, MonitorableProcess proc) {
     this.proc = proc;
     this.message = message;
   }
 
   public void run()
   {
     int i = 0;
     while (!(this.proc.isDone())) {
       int n = this.proc.getProgress();
       System.out.print(this.message);
       System.out.print(' ');
       switch (i)
       {
       case 0:
         System.out.print('-'); break;
       case 1:
         System.out.print('\\'); break;
       case 2:
         System.out.print('|'); break;
       case 3:
         System.out.print('/');
       }
       i = (i + 1) % 4;
 
       System.out.print(" [" + n + "]\r");
       try
       {
         sleep(200L);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
     }
     System.out.println("Working... DONE              ");
   }
 }
