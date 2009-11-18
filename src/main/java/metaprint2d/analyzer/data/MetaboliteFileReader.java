/*    */ package metaprint2d.analyzer.data;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.LoggingTool;


import metaprint2d.analyzer.data.processor.DataSource;
///*    */ import sea36.rdfile.RDFileEntry;
///*    */ import sea36.rdfile.RDFileReader;
/*    */ 
/*    */ public class MetaboliteFileReader
/*    */   implements DataSource<Transformation>
/*    */ {
/*    */   //public RDFileReader in;
/*    */   //private MetaboliteEntryReader reader;
	
			private static LoggingTool logger = new LoggingTool(MetaboliteFileReader.class);
			private ReaccsMDLRXNReader reader;
			private IReactionSet currentReactionSet;
			private MetaboliteHandler handler;
/*    */   private boolean skipMultistep;
/*    */ 
/*    */   public MetaboliteFileReader(InputStream is)
/*    */     throws IOException
/*    */   { 
	 			reader = new ReaccsMDLRXNReader(is);
	 			handler = new MetaboliteHandler();
///* 17 */     this.reader = new MetaboliteEntryReader();
/*    */ 
/* 21 */     //this.in = new RDFileReader(is);
/*    */   }
/*    */ 
/*    */   public MetaboliteFileReader(File file) throws FileNotFoundException, IOException {
/* 25 */     this(new FileInputStream(file));
/*    */   }
/*    */ 

			public void setInitialReaction(int i){
				this.reader.setInitialRiregNo(i);
			}

/*    */   public Transformation getNext()
/*    */     throws Exception
/*    */   {
	/*    */     Transformation t;
	/* 29 */     ensureOpen();
	/*    */     do{
					try {
						currentReactionSet = (IReactionSet)reader.read(new NNReactionSet());
					} catch (ReaccsFileEndedException e) {
						logger.info("eof");
						return null;
					} catch (CDKException e) {
						logger.fatal(e);
						throw new RuntimeException(e);
					}
					if (currentReactionSet == null) {
						return null;
					}
					t = this.handler.getTransformation(currentReactionSet);
				}while ((this.skipMultistep) && (t.isMultiStep()));
	/*    */ 
	/* 40 */     return t;
/*    */   }
/*    */ 
/*    */   private void ensureOpen()
/*    */     throws IOException
/*    */   {
/* 48 */     if (this.reader == null)
/* 49 */       throw new IOException("Stream closed");
/*    */   }
/*    */ 
/*    */   public synchronized void close()
/*    */     throws IOException
/*    */   {
/* 58 */     if (this.reader == null) {
/* 59 */       return;
/*    */     }
/* 61 */     this.reader.close();
/* 62 */     this.reader = null;
/*    */   }
/*    */ 
///*    */   public ReactionAnalyser getAnalyser()
///*    */   {
/////* 67 */     return this.reader.getAnalyser();
///*    */   }
/*    */ 
///*    */   public void setAnalyser(ReactionAnalyser analyser) {
/////* 71 */     this.reader.setAnalyser(analyser);
///*    */   }
/*    */ 
/*    */   public boolean isSkipMultistep() {
/* 75 */     return this.skipMultistep;
/*    */   }
/*    */ 
/*    */   public void setSkipMultistep(boolean skipMultistep) {
/* 79 */     this.skipMultistep = skipMultistep;
/*    */   }
/*    */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.MetaboliteFileReader
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
