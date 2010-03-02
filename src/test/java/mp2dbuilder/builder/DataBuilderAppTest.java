package mp2dbuilder.builder;

import java.net.URL;

import metaprint2d.builder.DataBuilderApp;
import mp2dbuilder.binfile.ConcatenatorTest;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataBuilderAppTest {

	private static Logger logger = null;

	@BeforeClass
	public static void setup() {
		logger = Logger.getLogger(ConcatenatorTest.class.getName());
	}

	@Test
	public void testDataBuilderAppSimpleIO() throws Exception {
		logger.debug("Running testDataBuilderAppSimpleIO");
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/firstRiReg.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/out.bin";
		String[] args = { "-i", inFile, "-o", outFile };
		DataBuilderApp.main(args);
	}

	@Test
	public void testDataBuilderAppSimpleSmirks() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/firstRiReg.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/simpleSMIRKSout.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.simple");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp.main(args);
	}

	@Test
	public void testDataBuilderAppTwoSmirks() throws Exception {
		logger.debug("Running testDataBuilderAppTwoSmirks");
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/firstRiReg.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/twoSMIRKSout.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.two");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp.main(args);
	}
	
	@Test
	public void testDataBuilderAppTwoSmirksMultipleRiregs() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/First5DB2005AllFields.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/twoSMIRKSMultipleRiregs1ThreadOut.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.two");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-t", "1", "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp.main(args);
	}
	
	@Test
	public void testDataBuilderTwoSmirksMultipleRiregsNoThreadSpec() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/First5DB2005AllFields.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/twoSMIRKSMultipleRiregs1ThreadOut.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.two");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp app = new DataBuilderApp();
		try {
			app.parseArgs(args);
			app.run();
		} catch (Exception e) {
			logger.fatal(e);
			throw e;
		}
	}
	
	@Test
	public void testDataBuilderAppTwoSmirksMultipleRiregs4Threads() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/First20DB2005AllFields.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/twoSMIRKSMultipleRiregs4ThreadsOut.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.two");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-t", "4", "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp.main(args);
	}
	
	@Test
	public void testDataBuilderApp20FirstRiregs1Thread() throws Exception {
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/First20DB2005AllFields.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/First20DB2005AllFieldsFromSingle.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.hydroxylation");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-t", "1", "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp app = new DataBuilderApp();
		try {
			app.parseArgs(args);
			app.run();
		} catch (Exception e) {
			logger.fatal(e);
			throw e;
		}
	}
	
	@Test
	public void testGenerateMultipleBinFilesFromMultipleRiregFiles() throws Exception {
		String fileName = "data/mdl/First50DB2005AllFields.rdf.";
		URL url = null;
		String inFile = null;
		String outFile = null;
		String reactionSmartsFilterFile = null;
		String[] args = null;
		
		for(int counter = 0; counter < 3; counter++){
			url = this.getClass().getClassLoader().getResource(fileName + (counter + 1));
			inFile = url.getPath();
			
			url = this.getClass().getClassLoader().getResource("data/mdl");
			outFile = url.getPath() + "/First50DB2005AllFields.bin" + counter;
			url = this.getClass().getClassLoader().getResource(
					"data/mdl/ReactionSMARTSFilter.hydroxylation");
			reactionSmartsFilterFile = url.getPath();
			args = new String[]{ "-i", inFile, "-o", outFile, "-t", "1", "-rfile",reactionSmartsFilterFile };
			DataBuilderApp app = new DataBuilderApp();
			try {
				app.parseArgs(args);
				app.run();
			} catch (Exception e) {
				logger.fatal(e);
				throw e;
			}
		}
	}
	
}
