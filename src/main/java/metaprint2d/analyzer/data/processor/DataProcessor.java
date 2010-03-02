 package metaprint2d.analyzer.data.processor;
 
 import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import metaprint2d.analyzer.data.MetaboliteFileReader;
import metaprint2d.analyzer.data.Transformation;

import org.mp2dbuilder.io.ReaccsFileEndedException;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.ILoggingTool;


 
 public class DataProcessor<T>
   implements MonitorableProcess
 {
   ILoggingTool LOG;
   DataSource<T> in;
   DataSink<T> out;
   private int nn;
   private boolean started = false;
   private boolean done = false;
   private ExecutorService pool;
   int poolSize = 1;
   
List<Future> futureList;
 
   public DataProcessor(DataSource<T> in, DataSink<T> out, int nn) {
     this.in = in;
     this.out = out;
     this.nn = nn;
     futureList = new ArrayList<Future>(100);
   }
 
   public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
   
   public void run() throws Exception {
     checkState();
     this.started = true;
     IReactionSet currentReactionSet = null;
     ReaccsMDLRXNReader reader = ((MetaboliteFileReader)this.in).getReader();
     int numberOfRunsSinceLastRun = 1;
     
     pool = Executors.newFixedThreadPool(poolSize);
     
     try
     {
       while (true) {
         
         if (this.LOG != null) {
           System.out.println("Reading #" + Integer.valueOf(this.nn));
         }
         
 			try {
 				currentReactionSet = (IReactionSet) reader.read(new NNReactionSet());
 				
 			} catch (ReaccsFileEndedException e) {
 				this.LOG.info("eof");
 				break;
 			} catch (CDKException e) {
 				this.LOG.fatal(e);
 				throw new RuntimeException(e);
 			}
 			if (currentReactionSet == null) {
 				break;
 			}
         
 			while(futureList.size() > poolSize){
 	    	   for(int i = 0; i < futureList.size(); i++){
 					if(futureList.get(i).isDone()){
 						futureList.remove(i);
 					}
 				}
 	    	   if(futureList.size() <= poolSize){
 	    		   break;
 	    	   }
 	    	   try{
 	    		   Thread.currentThread().sleep(3000);
 	    	   }catch(Exception e){
 	    		   this.LOG.warn(e);
 	    	   }
 	    	   System.out.println("Waiting for threads to be queued.");
 	       }
 			
 			IReactionSet tempReactionSet = currentReactionSet;
 			Future future = pool.submit(new Handler(tempReactionSet, this, new Transformation()));
 			
 			futureList.add(future);
         
         this.nn += 1;
       }
       
       while(futureList.size() > 0){
    	   for(int i = 0; i < futureList.size(); i++){
				if(futureList.get(i).isDone()){
					futureList.remove(i);
				}
			}
    	   if(futureList.size() == 0){
    		   break;
    	   }
    	   try{
    		   Thread.currentThread().sleep(5000);
    	   }catch(Exception e){
    		   this.LOG.warn(e);
    	   }
    	   System.out.println("Waiting for threads to complete.");
       }
       System.out.println("Ready to flush.");
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
 
 class Handler implements Runnable {
	 private IReactionSet reactionSet;
	 private DataProcessor dataProcessor;
	 private Transformation t;
	   Handler(IReactionSet reactionSet, DataProcessor dataProcessor, Transformation t) { 
		   this.reactionSet = reactionSet; 
		   this.dataProcessor = dataProcessor;
		   this.t = t;
		  }
	   public void run() {
		   try {
		   this.t = (Transformation)this.dataProcessor.in.getNext(this.reactionSet);
	         if (this.t == null) {
	           return;
	         }        
	         if (this.dataProcessor.acceptPreProcess(this.t)){
	        	 this.t = (Transformation)this.dataProcessor.process(this.t);
	         }
	         
				this.dataProcessor.out.put(this.t);
			} catch (Exception e) {
				this.dataProcessor.LOG.fatal("Thread failed: " + e.getMessage());
				e.printStackTrace();
			}
	   }
	 }