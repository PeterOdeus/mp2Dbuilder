package metaprint2d.analyzer.data.processor;

import metaprint2d.analyzer.data.Transformation;

import org.apache.log4j.Logger;
import org.openscience.cdk.interfaces.IReactionSet;

public class Handler implements Runnable {
	 private static Logger logger = Logger.getLogger(Handler.class.getName());
	 private int reactionId;
	 private IReactionSet reactionSet;
	 private DataProcessor dataProcessor;
	 private Transformation t;
	   Handler(int reactionId, IReactionSet reactionSet, DataProcessor dataProcessor, Transformation t) {
		   this.reactionId = reactionId;
		   this.reactionSet = reactionSet; 
		   this.dataProcessor = dataProcessor;
		   this.t = t;
		  }
	   public void run() {
		   try {
		   this.t = (Transformation)this.dataProcessor.in.getNext(this.reactionId, this.reactionSet);
	         if (this.t == null) {
	           return;
	         }        
	         if (this.dataProcessor.acceptPreProcess(this.t)){
	        	 this.t = (Transformation)this.dataProcessor.process(this.t);
	         }
	         
				this.dataProcessor.out.put(this.t);
			} catch (Exception e) {
				logger.fatal("Thread failed: " + e.getMessage());
				e.printStackTrace();
			}
	   }
	 }