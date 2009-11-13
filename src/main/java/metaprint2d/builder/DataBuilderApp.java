package metaprint2d.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import metaprint2d.analyzer.data.MetaboliteFileReader;
import metaprint2d.analyzer.data.processor.DataSink;
import metaprint2d.analyzer.data.processor.DataSource;
import metaprint2d.analyzer.data.processor.ProgressMonitor;
import metaprint2d.builder.data.BinFileBuilder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.openscience.cdk.tools.LoggingTool;

//import sea36.util.LogTool;
/*     */ 
/*     */ public class DataBuilderApp
/*     */ {
/*  32 */   private static LoggingTool LOG = new LoggingTool(DataBuilderApp.class);
/*     */   public String infile;
/*     */   public String outfile;
/*     */   private String species;
/*     */   private String reactionTypes;
/*     */   private String smirks;
/*     */   private Boolean includeMultiStep;
/*     */   private Boolean includeNoRc;
/*     */ 
/*     */   private void parseArgs(String[] args)
/*     */     throws Exception
/*     */   {
/*  44 */     if (args.length == 0) {
/*  45 */       printUsage();
/*  46 */       System.exit(1);
/*     */     }
/*     */ 
/*  49 */     Iterator it = Arrays.asList(args).iterator();
/*     */ 
/*  51 */     //break label665:
/*     */ 
/*  53 */     String opt;
/*     */ 
/*  55 */     
/*     *///     while (true) try {
/*  62 */  //       Thread.sleep(60000L);
/*     */    //   }
/*     */      // catch (InterruptedException exception)
/*     */      // {
/*     */         while (it.hasNext())
/*     */         {
						opt = (String)it.next();
/*  60 */           if ("-debug".equals(opt)) {
		/*  56 */       if (!(it.hasNext())) {
			/*  57 */         throw new IllegalArgumentException("Argument -debug missing required value");
			/*     */       }
						throw new IllegalArgumentException("DebugViewer not included in this build");
		/*  59 */       //DebugViewer.main(new String[] { (String)it.next() });
		/*     */     }
/*     */ 
/*  69 */           if ("-h".equals(opt)) {
/*  70 */             printHelp();
/*  71 */             System.exit(0);
/*     */           }
/*  73 */           else if ("-i".equals(opt)) {
/*  74 */             if (this.infile != null) {
/*  75 */               throw new IllegalArgumentException("Argument -i already specified");
/*     */             }
/*  77 */             if (!(it.hasNext())) {
/*  78 */               throw new IllegalArgumentException("Argument -i missing required value");
/*     */             }
/*  80 */             this.infile = ((String)it.next());
/*  81 */             LOG.info(new Object[] { "Input: " + this.infile });
/*     */           }
/*  83 */           else if ("-o".equals(opt)) {
/*  84 */             if (this.outfile != null) {
/*  85 */               throw new IllegalArgumentException("Argument -o already specified");
/*     */             }
/*  87 */             this.outfile = ((String)it.next());
/*  88 */             LOG.info(new Object[] { "Output: " + this.outfile });
/*     */           }
/*  90 */           else if ("-s".equals(opt)) {
/*  91 */             if (this.species != null) {
/*  92 */               throw new IllegalArgumentException("Argument -s already specified");
/*     */             }
/*  94 */             if (!(it.hasNext())) {
/*  95 */               throw new IllegalArgumentException("Argument -s missing required value");
/*     */             }
/*  97 */             this.species = ((String)it.next());
/*  98 */             LOG.info(new Object[] { "Species: " + this.species });
/*     */           }
/* 100 */           else if ("-r".equals(opt)) {
/* 101 */             if ((this.reactionTypes != null) || (this.smirks != null)) {
/* 102 */               throw new IllegalArgumentException("Argument -r already specified");
/*     */             }
/* 104 */             if (!(it.hasNext())) {
/* 105 */               throw new IllegalArgumentException("Argument -r missing required value");
/*     */             }
/* 107 */             String x = (String)it.next();
/* 108 */             if (x.contains(">>"))
/* 109 */               this.smirks = x;
/*     */             else {
/* 111 */               this.reactionTypes = x;
/*     */             }
/* 113 */             LOG.info(new Object[] { "Reaction types: " + this.reactionTypes });
/*     */           }
/* 115 */           else if ("-include-multistep".equals(opt)) {
/* 116 */             if (this.includeMultiStep != null) {
/* 117 */               throw new IllegalArgumentException("Argument -include-multistep already specified");
/*     */             }
/* 119 */             this.includeMultiStep = Boolean.TRUE;
/*     */           }
/* 121 */           else if ("-include-norc".equals(opt)) {
/* 122 */             if (this.includeNoRc != null) {
/* 123 */               throw new IllegalArgumentException("Argument -include-norc already specified");
/*     */             }
/* 125 */             this.includeNoRc = Boolean.TRUE;
/*     */           }
/* 127 */           else if ("-log".equals(opt)) {
/* 128 */             if (!(it.hasNext())) {
/* 129 */               throw new IllegalArgumentException("Argument -log missing required value");
/*     */             }
/* 131 */             FileAppender logfile = new FileAppender(new SimpleLayout(), (String)it.next());
/* 132 */             Logger.getRootLogger().addAppender(logfile);
/*     */           }
/*     */           else {
/* 135 */             LOG.warn(new Object[] { "Ignoring unknown argument: " + opt });
/*     */           }
/*     */         }
/*  51 */         
/*     */ 
/* 139 */         if (this.infile == null) {
/* 140 */           throw new IllegalArgumentException("Required argument -i not specified");
/*     */         }
/* 142 */         if (this.outfile == null)
/* 143 */           throw new IllegalArgumentException("Required argument -o not specified");
/*     *///       }
/*     */   }
/*     */ 
/*     */   private void printUsage()
/*     */   {
/* 150 */     System.out.println();
/* 151 */     System.out.println("MetaPrint2D (pre-processor)");
/* 152 */     System.out.println();
/* 153 */     System.out.println("Usage:");
/* 154 */     System.out.println("-h                          display full help message");
/* 155 */     System.out.println("-i <input file>             path to input RDF file");
/* 156 */     System.out.println("-o <output file>            path to output file");
/* 157 */     System.out.println("-s <species>                species filter (optional)");
/* 158 */     System.out.println("-r <reaction types>         reaction type filter (optional)");
/* 159 */     System.out.println("-include-multistep          include multi-step transformations (optional)");
/* 160 */     System.out.println("-include-norc               include transformations with no RC (optional)");
/* 161 */     System.out.println("-debug <input file>         run debug viewer (takes precedence over other options)");
/* 162 */     System.out.println();
/*     */   }
/*     */ 
/*     */   private void printHelp() {
/* 166 */     printUsage();
/* 167 */     System.out.println("Details");
/* 168 */     System.out.println();
/* 169 */     System.out.println("RDF File");
/* 170 */     System.out.println("Required fields:");
/* 171 */     System.out.println("      RXN:RXNREGNO");
/* 172 */     System.out.println("      RXN:VARIATION(1):MDLNUMBER");
/* 173 */     System.out.println("      RXN:REACTANT_LINK(1):MOL(1):MDLNUMBER");
/* 174 */     System.out.println("      RXN:PRODUCT_LINK(1):MOL(1):MDLNUMBER");
/* 175 */     System.out.println("      RXN:VARIATION(1):RXNREF(1):STEP");
/* 176 */     System.out.println("      RXN:VARIATION(1):RXNREF(1):PATH");
/* 177 */     System.out.println("      RXN:VARIATION(1):LITREF(*):ANIMAL(*):SPECIES");
/* 178 */     System.out.println();
/* 179 */     System.out.println("Species Filter");
/* 180 */     System.out.println("Species filter is a complete word e.g. 'human' or 'rat' to match");
/* 181 */     System.out.println("against the species field.");
/* 182 */     System.out.println();
/* 183 */     System.out.println("Reaction Type Filter");
/* 184 */     System.out.println("Reaction type filter can be either a comma separated list of MetaPrint2D");
/* 185 */     System.out.println("reaction centre types:");
/* 186 */     System.out.println("  A   - Phase I addition (single oxygen)");
/* 187 */     System.out.println("  A2  - Phase II addition");
/* 188 */     System.out.println("  E   - Elimination");
/* 189 */     System.out.println("  BOC - Bond order change");
/* 190 */     System.out.println("  BB  - Bond broken");
/* 191 */     System.out.println("  BM  - Bond made");
/* 192 */     System.out.println("Or a SMIRKS pattern. In the SMIRKS pattern mapped atoms are conserved");
/* 193 */     System.out.println("between the reactant and product, and unmapped atoms are deleted or");
/* 194 */     System.out.println("added, as appropriate. A match of all added/deleted atoms is not required");
/* 195 */     System.out.println("only those adjacent to the reaction centre; this allows defininitions");
/* 196 */     System.out.println("such as methylation and alkylation.");
/* 197 */     System.out.println("Examples:");
/* 198 */     System.out.println("  Epoxidation:   [*:1]=[*:2]>>[*:1]1-[*:2]-O-1");
/* 199 */     System.out.println("  Cyanidation:   [*:1]>>[*:1]-C#N");
/* 200 */     System.out.println("  Demethylation: [*:1][CH3]>>[*:1]");
/* 201 */     System.out.println("  Methylation:   [*:1]>>[*:1]-[CH3]");
/* 202 */     System.out.println("  Alkylation:    [*:1]>>[*:1]-C");
/* 203 */     System.out.println();
/* 204 */     System.out.println("Multi-step Transformations");
/* 205 */     System.out.println("By default, multi-step transformations are ignored. They can be included");
/* 206 */     System.out.println("through the -include-multistep flag.");
/* 207 */     System.out.println();
/* 208 */     System.out.println("No Reaction Centre");
/* 209 */     System.out.println("By default, transformations with no reaction centres identified (after");
/* 210 */     System.out.println("the reaction type filter has been applied) are ignored. They can be included");
/* 211 */     System.out.println("through the -include-norc flag.");
/* 212 */     System.out.println();
/* 213 */     System.out.println("------------------------------------------------------------");
/* 214 */     System.out.println("  Author: Sam Adams <sam.adams@cantab.net>");
/* 215 */     System.out.println("  Developed by:");
/* 216 */     System.out.println("  Sam Adams, Lars Carlsson, Scott Boyer, Robert Glen");
/* 217 */     System.out.println("  AstraZeneca, Molndal  /  University of Cambridge");
/* 218 */     System.out.println("------------------------------------------------------------");
/* 219 */     System.out.println();
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("unchecked")
private void run()
/*     */     throws Exception
/*     */   {
/*     */     DataSource in = null;
/*     */     DataSink out = null;
/*     */     boolean datain;
/*     */     boolean dataout;
/* 225 */     double maxMem = Runtime.getRuntime().maxMemory() / 1048576.0D;
/* 226 */     LOG.info(new Object[] { String.format("Maximum available memory: %.1fMB%n", new Object[] { Double.valueOf(maxMem) }) });
/*     */ 
///* 230 */     ReactionTypes rtypes = MetaPrintReactionTypes.RXNTYPES;
/*     */ 
/* 233 */     if (this.infile.toLowerCase().startsWith("data:")) {
/* 234 */       this.infile = this.infile.substring(5);
/* 235 */       datain = true;
/*     */     } else {
/* 237 */       datain = false;
/*     */     }
/*     */ 
/* 240 */     File infile = new File(this.infile);
/* 241 */     if (!(infile.isFile())) {
/* 242 */       System.out.println("Input file not found: " + infile);
/* 243 */       System.exit(1);
/*     */     }
/*     */ 
/* 246 */     if (datain) {
/* 247 */       //in = new DataFileReader(infile, rtypes);
/*     */     }
/*     */     else
/*     */     {
/*     */       InputStream is;
/* 250 */       InputStream fis = new FileInputStream(infile);
/*     */       try {
/* 252 */         is = new GZIPInputStream(fis);
/*     */       } catch (IOException e) {
/* 254 */         fis.close();
/* 255 */         is = new FileInputStream(infile);
/*     */       }
/* 257 */       in = new MetaboliteFileReader(is);
/* 258 */       //MetabolicSiteAnalyser analyser = new MetabolicSiteAnalyser();
/* 259 */       if (this.smirks != null) {
///* 260 */         analyser.setReactionCentreTyper(new SmirksTyper(this.smirks));
/*     */       }
/* 262 */       //((MetaboliteFileReader)in).setAnalyser(analyser);
/*     */     }
/*     */ 
/* 267 */     if (this.outfile.toLowerCase().startsWith("data:")) {
/* 268 */       dataout = true;
/* 269 */       this.outfile = this.outfile.substring(5);
/*     */     } else {
/* 271 */       dataout = false;
/*     */     }
/*     */ 
/* 274 */     File outfile = new File(this.outfile);
/* 275 */     File dir = outfile.getParentFile();
/* 276 */     if ((dir != null) && (!(dir.exists()))) {
/* 277 */       dir.mkdirs();
/*     */     }
/*     */ 
/* 280 */     if (dataout){
///* 281 */       out = new DataFileWriter(outfile);
/*     */     }else {
/* 283 */       out = new BinFileBuilder(outfile);
/*     */     }
/*     */ 
/* 286 */     List speciesFilter = null;
/* 287 */     if (this.species != null) {
///* 288 */       speciesFilter = new ArrayList();
///* 289 */       for (String sp : this.species.split(",")) {
///* 290 */         speciesFilter.add(new Species(sp));
///*     */       }
/*     */     }
/*     */ 
/* 294 */     List reactionTypeFilter = null;
/* 295 */     if (this.reactionTypes != null) {
///* 296 */       reactionTypeFilter = new ArrayList();
///* 297 */       for (String rt : this.reactionTypes.split(",")) {
///* 298 */         ReactionTypes.ReactionType rtype = rtypes.get(rt);
///* 299 */         reactionTypeFilter.add(rtype);
///*     */       }
/*     */     }
/*     */ 
/* 303 */     MetaPrintDataBuilder builder = new MetaPrintDataBuilder(in, out);
/* 304 */     if (speciesFilter != null) {
///* 305 */       builder.setSpeciesFilter(speciesFilter);
/*     */     }
/* 307 */     if (reactionTypeFilter != null) {
///* 308 */       builder.setReactionTypeFilter(reactionTypeFilter);
/*     */     }
/* 310 */     if (this.includeMultiStep != null) {
/* 311 */       builder.setSkipMultistep(!(this.includeMultiStep.booleanValue()));
/*     */     }
/* 313 */     if (this.includeNoRc != null) {
/* 314 */       builder.setSkipNoRc(!(this.includeNoRc.booleanValue()));
/*     */     }
/*     */ 
/* 317 */     long t0 = System.currentTimeMillis();
///* 318 */     new ProgressMonitor(builder).start();
/*     */     try {
/* 320 */       builder.run();
/*     */     } catch (OutOfMemoryError e) {
/* 322 */       LOG.warn(new Object[] { e });
/* 323 */       System.out.println();
/* 324 */       System.out.println("OUT OF MEMORY ERROR");
/* 325 */       System.out.println("Try increasing the amount of memory available to java.");
/* 326 */       System.out.println("java -Xmx[max memory]");
/* 327 */       System.out.printf("Current maximum memory: %.1fMB%n", new Object[] { Double.valueOf(maxMem) });
/* 328 */       System.out.println();
/* 329 */       System.exit(1);
/*     */     }
/* 331 */     in.close();
/* 332 */     out.close();
/* 333 */     long t1 = System.currentTimeMillis();
/*     */ 
/* 335 */     LOG.info(new Object[] { "Build data complete: " + ((t1 - t0) / 1000L) + "s" });
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */     throws Exception
/*     */   {
/* 345 */     DataBuilderApp app = new DataBuilderApp();
/* 346 */     app.parseArgs(args);
//app.infile = "/home/podeus/temp/rdfile/firstRiReg.rdf";
//app.outfile = "/home/podeus/temp/rdfile/result1.bin";
/* 347 */     app.run();
/*     */   }
/*     */ }

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.builder.DataBuilderApp
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1