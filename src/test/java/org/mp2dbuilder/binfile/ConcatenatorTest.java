package org.mp2dbuilder.binfile;

import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

import junit.framework.Assert;


import org.junit.BeforeClass;
import org.junit.Test;
import org.mp2dbuilder.SmartsTest;
import org.mp2dbuilder.binfile.BinFileConcatenator;
import org.openscience.cdk.tools.LoggingToolFactory;

public class ConcatenatorTest {
	
	private static Logger logger = null;
	
	@BeforeClass
	public static void setup() {
		logger = Logger.getLogger(ConcatenatorTest.class.getName());
	}
	
	
	@Test
	public void testBinFileConcatenator() throws Exception {
		String filename = "data/mdl/concat.bin";
		URL url = this.getClass().getClassLoader().getResource(filename + 0);
		Assert.assertNotNull(url);
		logger.debug(url);
		logger.debug("path is " + url.getPath());
		File file = new File(url.getPath().substring(0, url.getPath().length() - 1));
		Assert.assertNotNull(file);
		logger.debug("\n\tfile.getAbsolutePath() : " + file.getAbsolutePath());
		logger.debug("\n\tfile.getName() : " + file.getName());
		BinFileConcatenator concatenator = new BinFileConcatenator(file);
		concatenator.concat();
	}
	
	public void testBinFileConcatenatorMain() throws Exception {
		String filename = "data/mdl/concat.bin";
		URL url = this.getClass().getClassLoader().getResource(filename + 0);
		File file = new File(url.getPath().substring(0, url.getPath().length() - 1));
		BinFileConcatenator.main(new String[]{file.getAbsolutePath()});
	}
	
	@Test
	public void testBinFilesEqual() throws Exception {
		String filename = "data/mdl/concat.bin";
		URL url0 = this.getClass().getClassLoader().getResource(filename + 0);
		File file0 = new File(url0.getPath());
		URL url1 = this.getClass().getClassLoader().getResource(filename + 1);
		File file1 = new File(url1.getPath());
		BinFileConcatenator concatenator = new BinFileConcatenator(file0);
		Assert.assertEquals(0, concatenator.diff(file1));
	}
	
	public void testBinFilesMainAppEqual() throws Exception {
		String filename = "data/mdl/concat.bin";
		URL url0 = this.getClass().getClassLoader().getResource(filename + 0);
		File file0 = new File(url0.getPath());
		URL url1 = this.getClass().getClassLoader().getResource(filename + 1);
		File file1 = new File(url1.getPath());
		BinFileConcatenator.main(new String[]{file0.getAbsolutePath(),file1.getAbsolutePath()});
	}
	
	public void testBinFilesMainAppNotEqual() throws Exception {
		String filename = "data/mdl/concat.bin0";
		String failedFilename = "data/mdl/concatDiffFail.bin";
		URL url0 = this.getClass().getClassLoader().getResource(filename);
		File file0 = new File(url0.getPath());
		URL url1 = this.getClass().getClassLoader().getResource(failedFilename);
		File file1 = new File(url1.getPath());
		BinFileConcatenator.main(new String[]{file0.getAbsolutePath(),file1.getAbsolutePath()});
	}

	@Test
	public void testBinFilesDiffer() throws Exception {
		String filename = "data/mdl/concat.bin";
		String failedFilename = "data/mdl/concatDiffFail.bin";
		URL url0 = this.getClass().getClassLoader().getResource(filename + 0);
		File file0 = new File(url0.getPath());
		URL urlFail = this.getClass().getClassLoader().getResource(failedFilename);
		File failedFile = new File(urlFail.getPath());
		BinFileConcatenator concatenator = new BinFileConcatenator(file0);
		Assert.assertEquals(-1, concatenator.diff(failedFile));
	}
	
	public void testBinFileAppHelp() throws Exception {
		BinFileConcatenator.main(new String[0]);
	}
}
