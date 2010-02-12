 package metaprint2d.analyzer.data.processor;
 
 import org.openscience.cdk.tools.ILoggingTool;


 
 public class DataProcessor<T>
   implements MonitorableProcess
 {
   private ILoggingTool LOG;
   private DataSource<T> in;
   private DataSink<T> out;
   private int nn;
   private boolean started = false;
   private boolean done = false;
 
   public DataProcessor(DataSource<T> in, DataSink<T> out, int nn) {
     this.in = in;
     this.out = out;
			this.nn = nn;
   }
 
   public void run() throws Exception {
     checkState();
     this.started = true;
     try
     {
       while (true) {
         
         if (this.LOG != null) {
           this.LOG.debug("Reading #" + Integer.valueOf(this.nn));
         }
/* TODO changed from Object to T */         T o = this.in.getNext();
         if (o == null) {
           break;
         }
 
         if (this.LOG != null) {
           this.LOG.debug("Processing #" + Integer.valueOf(this.nn));
         }
         if (acceptPreProcess(o));
         o = process(o);
         if (acceptPostProcess(o));
         this.out.put(o);
					this.nn += 1;
       }
 
       this.out.flush();
     } finally {
       this.done = true;
     }
   }
 
   protected void checkState() {
     if (this.started)
       throw new IllegalStateException("Already run");
   }
 
   protected boolean acceptPreProcess(T o)
   {
     return true;
   }
 
   protected boolean acceptPostProcess(T o) {
     return true;
   }
 
   protected T process(T o) {
     return o;
   }
 
   protected void setLogger(ILoggingTool log) {
     this.LOG = log;
   }
 
   public int getProgress() {
     return this.nn;
   }
 
   public boolean isDone() {
     return this.done;
   }
 }