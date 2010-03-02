package metaprint2d.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import metaprint2d.analyzer.data.MetaboliteFileReader;
import metaprint2d.analyzer.data.processor.DataSink;
import metaprint2d.analyzer.data.processor.DataSource;
import metaprint2d.builder.data.BinFileBuilder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.builder.ReactionSmartsHandler;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class DataBuilderApp {
	public static ILoggingTool LOG = LoggingToolFactory
			.createLoggingTool(DataBuilderApp.class);
	private Integer threadPoolSize;
	public String infile;
	public String outfile;
	private String species;
	private String reactionTypes;
	private String smirks;
	private Map<String, String> reactionSmarts = null;
	private Boolean includeMultiStep;
	private Boolean includeNoRc;
	private int initialReactionIndex = -1;

	public void parseArgs(String[] args) throws Exception {
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}

		Iterator it = Arrays.asList(args).iterator();

		// break label665:

		String opt;

		// while (true) try {
		// Thread.sleep(60000L);
		// }
		// catch (InterruptedException exception)
		// {
		while (it.hasNext()) {
			opt = (String) it.next();
			if ("-debug".equals(opt)) {
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -debug missing required value");
				}
				throw new IllegalArgumentException(
						"DebugViewer not included in this build");
				// DebugViewer.main(new String[] { (String)it.next() });
			}

			if ("-h".equals(opt)) {
				printHelp();
				System.exit(0);
			} else if ("-i".equals(opt)) {
				if (this.infile != null) {
					throw new IllegalArgumentException(
							"Argument -i already specified");
				}
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -i missing required value");
				}
				this.infile = ((String) it.next());
				LOG.info("Input: " + this.infile);
			} else if ("-o".equals(opt)) {
				if (this.outfile != null) {
					throw new IllegalArgumentException(
							"Argument -o already specified");
				}
				this.outfile = ((String) it.next());
				LOG.info("Output: " + this.outfile);
			} else if ("-initialIndex".equals(opt)) {
				if (this.initialReactionIndex != -1) {
					throw new IllegalArgumentException(
							"Argument -o already specified");
				}
				this.initialReactionIndex = Integer.parseInt(((String) it
						.next()));
				LOG
						.info("Initial Reaction Index: "
								+ this.initialReactionIndex);
			}

			else if ("-s".equals(opt)) {
				if (this.species != null) {
					throw new IllegalArgumentException(
							"Argument -s already specified");
				}
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -s missing required value");
				}
				this.species = ((String) it.next());
				LOG.info(new Object[] { "Species: " + this.species });
			} else if ("-t".equals(opt)) {
				if (this.threadPoolSize != null) {
					throw new IllegalArgumentException(
							"Argument -t already specified");
				}
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -t missing required value");
				}
				this.threadPoolSize = Integer.decode(((String) it.next()));
				LOG.info(new Object[] { "Thread Pool Size: "
						+ this.threadPoolSize });
			} else if ("-r".equals(opt)) {
				if ((this.reactionTypes != null) || (this.smirks != null)) {
					throw new IllegalArgumentException(
							"Argument -r already specified");
				}
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -r missing required value");
				}
				String x = (String) it.next();
				if (x.contains(">>"))
					this.smirks = x;
				else {
					this.reactionTypes = x;
				}
				LOG
						.info(new Object[] { "Reaction types: "
								+ this.reactionTypes });
			} else if ("-rfile".equals(opt)) {
				if (this.reactionSmarts != null) {
					throw new IllegalArgumentException(
							"Argument -rfile already specified");
				}
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -rfile missing required file name");
				}
				String fileName = (String) it.next();
				readReactionSmarts(fileName);
				LOG.info(new Object[] { "Reaction SMARTS: "
						+ this.reactionSmarts });
			} else if ("-include-multistep".equals(opt)) {
				if (this.includeMultiStep != null) {
					throw new IllegalArgumentException(
							"Argument -include-multistep already specified");
				}
				this.includeMultiStep = Boolean.TRUE;
			} else if ("-include-norc".equals(opt)) {
				if (this.includeNoRc != null) {
					throw new IllegalArgumentException(
							"Argument -include-norc already specified");
				}
				this.includeNoRc = Boolean.TRUE;
			} else if ("-log".equals(opt)) {
				if (!(it.hasNext())) {
					throw new IllegalArgumentException(
							"Argument -log missing required value");
				}
				FileAppender logfile = new FileAppender(new SimpleLayout(),
						(String) it.next());
				Logger.getRootLogger().addAppender(logfile);
			} else {
				LOG.warn("Ignoring unknown argument: " + opt);
			}
		}

		if (this.infile == null) {
			throw new IllegalArgumentException(
					"Required argument -i not specified");
		}
		if (this.outfile == null)
			throw new IllegalArgumentException(
					"Required argument -o not specified");
		// }
	}

	private void readReactionSmarts(String fileName) throws Exception {
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		reactionSmarts = new HashMap<String, String>();
		String smirksName;
		String smirksDefinition;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			int indexOfFirstColon = line.indexOf(":");
			smirksName = line.substring(0, indexOfFirstColon);
			line = line.substring(indexOfFirstColon + 1);
			line = line.trim();
			smirksDefinition = line;
			reactionSmarts.put(smirksName, smirksDefinition);
		}
	}

	private void printUsage() {
		System.out.println();
		System.out.println("MetaPrint2D (pre-processor)");
		System.out.println();
		System.out.println("Usage:");
		System.out
				.println("-h                          display full help message");
		System.out
				.println("-i <input file>             path to input RDF file");
		System.out.println("-o <output file>            path to output file");
		System.out
				.println("-s <species>                species filter (optional)");
		System.out
				.println("-t <number of concurrently running threads> Thread Pool Size");
		System.out
				.println("-r <reaction types>         reaction type filter (optional)");
		System.out
				.println("-rfile <file>         	  reaction type filter file (optional)");
		System.out
				.println("-include-multistep          include multi-step transformations (optional)");
		System.out
				.println("-include-norc               include transformations with no RC (optional)");
		System.out
				.println("-debug <input file>         run debug viewer (takes precedence over other options)");
		System.out.println();
	}

	private void printHelp() {
		printUsage();
		System.out.println("Details");
		System.out.println();
		System.out.println("RDF File");
		System.out.println("Required fields:");
		System.out.println("      RXN:RXNREGNO");
		System.out.println("      RXN:VARIATION(1):MDLNUMBER");
		System.out.println("      RXN:REACTANT_LINK(1):MOL(1):MDLNUMBER");
		System.out.println("      RXN:PRODUCT_LINK(1):MOL(1):MDLNUMBER");
		System.out.println("      RXN:VARIATION(1):RXNREF(1):STEP");
		System.out.println("      RXN:VARIATION(1):RXNREF(1):PATH");
		System.out
				.println("      RXN:VARIATION(1):LITREF(*):ANIMAL(*):SPECIES");
		System.out.println();
		System.out.println("Species Filter");
		System.out
				.println("Species filter is a complete word e.g. 'human' or 'rat' to match");
		System.out.println("against the species field.");
		System.out.println();
		System.out.println("Reaction Type Filter");
		System.out
				.println("Reaction type filter can be either a comma separated list of MetaPrint2D");
		System.out.println("reaction centre types:");
		System.out.println("  A   - Phase I addition (single oxygen)");
		System.out.println("  A2  - Phase II addition");
		System.out.println("  E   - Elimination");
		System.out.println("  BOC - Bond order change");
		System.out.println("  BB  - Bond broken");
		System.out.println("  BM  - Bond made");
		System.out
				.println("Or a SMIRKS pattern. In the SMIRKS pattern mapped atoms are conserved");
		System.out
				.println("between the reactant and product, and unmapped atoms are deleted or");
		System.out
				.println("added, as appropriate. A match of all added/deleted atoms is not required");
		System.out
				.println("only those adjacent to the reaction centre; this allows defininitions");
		System.out.println("such as methylation and alkylation.");
		System.out.println("Examples:");
		System.out.println("  Epoxidation:   [*:1]=[*:2]>>[*:1]1-[*:2]-O-1");
		System.out.println("  Cyanidation:   [*:1]>>[*:1]-C#N");
		System.out.println("  Demethylation: [*:1][CH3]>>[*:1]");
		System.out.println("  Methylation:   [*:1]>>[*:1]-[CH3]");
		System.out.println("  Alkylation:    [*:1]>>[*:1]-C");
		System.out.println();
		System.out.println("Multi-step Transformations");
		System.out
				.println("By default, multi-step transformations are ignored. They can be included");
		System.out.println("through the -include-multistep flag.");
		System.out.println();
		System.out.println("No Reaction Centre");
		System.out
				.println("By default, transformations with no reaction centres identified (after");
		System.out
				.println("the reaction type filter has been applied) are ignored. They can be included");
		System.out.println("through the -include-norc flag.");
		System.out.println();
		System.out
				.println("------------------------------------------------------------");
		System.out.println("  Author: Sam Adams <sam.adams@cantab.net>");
		System.out.println("  Developed by:");
		System.out
				.println("  Sam Adams, Lars Carlsson, Scott Boyer, Robert Glen");
		System.out
				.println("  AstraZeneca, Molndal  /  University of Cambridge");
		System.out
				.println("------------------------------------------------------------");
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	public void run() throws Exception {
		DataSource in = null;
		DataSink out = null;
		boolean datain;
		boolean dataout;
		double maxMem = Runtime.getRuntime().maxMemory() / 1048576.0D;
		LOG.info(String.format("Maximum available memory: %.1fMB%n",
				new Object[] { Double.valueOf(maxMem) }));

		// ReactionTypes rtypes = MetaPrintReactionTypes.RXNTYPES;

		if (this.infile.toLowerCase().startsWith("data:")) {
			this.infile = this.infile.substring(5);
			datain = true;
		} else {
			datain = false;
		}

		File infile = new File(this.infile);
		if (!(infile.isFile())) {
			System.out.println("Input file not found: " + infile);
			throw new Exception("Input file not found: " + infile);
		}

		if (datain) {
			// in = new DataFileReader(infile, rtypes);
		} else {
			InputStream is;
			InputStream fis = new FileInputStream(infile);
			try {
				is = new GZIPInputStream(fis);
			} catch (IOException e) {
				fis.close();
				is = new FileInputStream(infile);
			}

			MetaboliteHandler metaboliteHandler = null;
			if (this.reactionSmarts == null) {
				metaboliteHandler = new MetaboliteHandler();
			} else {
				metaboliteHandler = new ReactionSmartsHandler(
						this.reactionSmarts);
			}

			MetaboliteFileReader metaboliteFileReader = new MetaboliteFileReader(
					is, metaboliteHandler);
			in = metaboliteFileReader;
			if (this.initialReactionIndex < 0) {
				this.initialReactionIndex = 1;
			}
			metaboliteFileReader.setInitialReaction(this.initialReactionIndex);
			// MetabolicSiteAnalyser analyser = new MetabolicSiteAnalyser();
			if (this.smirks != null) {
				// analyser.setReactionCentreTyper(new
				// SmirksTyper(this.smirks));
			}
			// ((MetaboliteFileReader)in).setAnalyser(analyser);
		}

		if (this.outfile.toLowerCase().startsWith("data:")) {
			dataout = true;
			this.outfile = this.outfile.substring(5);
		} else {
			dataout = false;
		}

		File outfile = new File(this.outfile);
		File dir = outfile.getParentFile();
		if ((dir != null) && (!(dir.exists()))) {
			dir.mkdirs();
		}

		if (dataout) {
			// out = new DataFileWriter(outfile);
		} else {
			out = new BinFileBuilder(outfile);
		}

		List speciesFilter = null;
		if (this.species != null) {
			// speciesFilter = new ArrayList();
			// for (String sp : this.species.split(",")) {
			// speciesFilter.add(new Species(sp));
			// }
		}

		List reactionTypeFilter = null;
		if (this.reactionTypes != null) {
			// reactionTypeFilter = new ArrayList();
			// for (String rt : this.reactionTypes.split(",")) {
			// ReactionTypes.ReactionType rtype = rtypes.get(rt);
			// reactionTypeFilter.add(rtype);
			// }
		}

		MetaPrintDataBuilder builder = new MetaPrintDataBuilder(in, out,
				this.initialReactionIndex);

		if (this.threadPoolSize != null) {
			builder.setPoolSize(this.threadPoolSize.intValue());
		}

		if (speciesFilter != null) {
			// builder.setSpeciesFilter(speciesFilter);
		}
		if (reactionTypeFilter != null) {
			// builder.setReactionTypeFilter(reactionTypeFilter);
		}
		if (this.includeMultiStep != null) {
			builder.setSkipMultistep(!(this.includeMultiStep.booleanValue()));
		}
		if (this.includeNoRc != null) {
			builder.setSkipNoRc(!(this.includeNoRc.booleanValue()));
		}

		long t0 = System.currentTimeMillis();
		// new ProgressMonitor(builder).start();
		try {
			builder.run();
		} catch (OutOfMemoryError e) {
			LOG.warn(e);
			System.out.println();
			System.out.println("OUT OF MEMORY ERROR");
			System.out
					.println("Try increasing the amount of memory available to java.");
			System.out.println("java -Xmx[max memory]");
			System.out.printf("Current maximum memory: %.1fMB%n",
					new Object[] { Double.valueOf(maxMem) });
			System.out.println();
			throw e;
		}
		in.close();
		out.close();
		long t1 = System.currentTimeMillis();

		LOG.info("Build data complete in " + ((t1 - t0) / 1000L) + " seconds.");
	}

	public static void main(String[] args) throws Exception

	{
		DataBuilderApp app = new DataBuilderApp();
		try {
			app.parseArgs(args);

			// app.infile = "/home/podeus/temp/rdfile/firstRiReg.rdf";
			// app.outfile = "/home/podeus/temp/rdfile/result1.bin";
			app.run();
		} catch (Exception e) {
			LOG.fatal(e);
			throw e;
		}
		System.out.println("Execution Finished. Please <ctrl+c> (at least on linux) to get back the prompt if absent.");
	}
}
