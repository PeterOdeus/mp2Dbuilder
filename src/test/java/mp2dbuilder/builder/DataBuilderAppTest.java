package mp2dbuilder.builder;

import java.net.URL;

import metaprint2d.builder.DataBuilderApp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import prototyping.InitialTest;

public class DataBuilderAppTest {

	private static ILoggingTool logger = null;

	@BeforeClass
	public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(DataBuilderAppTest.class);
	}

	@Test
	public void testDataBuilderAppSimpleIO() throws Exception {
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
		URL url = this.getClass().getClassLoader().getResource(
				"data/mdl/firstRiReg.rdf");
		String inFile = url.getPath();
		url = this.getClass().getClassLoader().getResource("data/mdl");
		String outFile = url.getPath() + "/simpleSMIRKSout.bin";
		url = this.getClass().getClassLoader().getResource(
				"data/mdl/ReactionSMARTSFilter.two");
		String reactionSmartsFilterFile = url.getPath();
		String[] args = { "-i", inFile, "-o", outFile, "-rfile",
				reactionSmartsFilterFile };
		DataBuilderApp.main(args);
	}
}
