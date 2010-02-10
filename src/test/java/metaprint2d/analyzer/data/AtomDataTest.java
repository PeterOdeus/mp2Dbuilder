package metaprint2d.analyzer.data;

import java.util.HashSet;
import java.util.Set;

import metaprint2d.Fingerprint;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import prototyping.InitialTest;

public class AtomDataTest {
	
	private static ILoggingTool logger =  null;

	@BeforeClass public static void setup() {
		logger = LoggingToolFactory.createLoggingTool(AtomDataTest.class);
	}

	@Test public void testCloneAtomData() throws Exception {
		byte [][] byteMatrix = new byte[1][1];
		byteMatrix[0][0] = 1;
		Fingerprint fp = new Fingerprint(byteMatrix);
		Set<String> reactionNames = new HashSet<String>();
		reactionNames.add("TestReactionName");
		AtomData atomData = new AtomData(fp, true, reactionNames);
		AtomData clone = atomData.clone();
		Assert.assertArrayEquals(byteMatrix, clone.getFingerprint().getBytes());
		Assert.assertEquals(true, clone.getIsReactionCentre());
	}
	
	@Test public void testAtomDataWithNullReactionNames() throws Exception {
		byte [][] byteMatrix = new byte[1][1];
		byteMatrix[0][0] = 1;
		Fingerprint fp = new Fingerprint(byteMatrix);
		AtomData atomData = new AtomData(fp, true, null);
		Assert.assertNotNull(atomData.getTypeOfReactionCentres());
		AtomData clone = atomData.clone();
		Assert.assertNotNull(clone.getTypeOfReactionCentres());
	}
}
