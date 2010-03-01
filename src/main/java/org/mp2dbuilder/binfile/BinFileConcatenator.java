package org.mp2dbuilder.binfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import metaprint2d.FingerprintData;
import metaprint2d.builder.data.BinFileBuilder;
import metaprint2d.data.BinFile;

public class BinFileConcatenator extends BinFileBuilder {

	private String outFileAbsolutePath = null; //i.e /home/podeus/aFileName.bin
	
	public BinFileConcatenator(File file) {
		super(file);
		this.outFileAbsolutePath = file.getAbsolutePath();
	}
	
	public int diff(File file) throws Exception{
		
		FileInputStream fis = null;
		
		List<FingerprintData> fingerprintDataList = null;
		
		fis = new FileInputStream(this.outFileAbsolutePath);
		
		fingerprintDataList = (ArrayList<FingerprintData>) BinFile.read(fis);
		
		put(fingerprintDataList);
		
		fis = new FileInputStream(file.getAbsolutePath());
		
		fingerprintDataList = (ArrayList<FingerprintData>) BinFile.read(fis);
		
		for(FingerprintData fpData: fingerprintDataList){
			if(diff(fpData) != 0){
				return -1;
			}
		}
		return 0;
	}
	
	public void concat() throws Exception{
		
		FileInputStream fis = null;
		
		List<FingerprintData> fingerprintDataList = null;
		
		int fileNumberCounter = 0;
		
		for(;;fileNumberCounter++){
			try{
				fis = new FileInputStream(this.outFileAbsolutePath + fileNumberCounter);
				fingerprintDataList = (ArrayList<FingerprintData>) BinFile.read(fis);
				put(fingerprintDataList);
			}catch(FileNotFoundException fillifjonka){
				break;
			}finally{
				try{
					fis.close();
				}catch(Exception e){
					//Ignore
				}
			}
		}
		
		if(fileNumberCounter > 0){
			close();
		}
	}
	
	public static void main (String [] args){
		try {
			
			if(args.length == 0 || (args.length > 0 && args[0].equals("-h"))){
				StringBuffer buf = new StringBuffer();
				buf.append("Syntax: <filename[s]>\n\n")				
				.append("Concatenation mode:\n\t")
				.append("If 1 file name is given, concatenation mode is used.\n\t")
				.append("The file name (e.g. /home/file.bin) will be used when scanning for a sequence of\n\t")
				.append("file names ending with 0, 1, 2 etcetera (e.g. /home/file.bin0 /home/file.bin1).\n\t")
				.append("The resulting concatenated file will have the argument file name (e.g. /home/file.bin).")
				.append("\n\nDiff mode:\n\t")
				.append("When 2 bin file names are given (e.g. /home/file1.bin /home/file2.bin),\n\t")
				.append("the app will tell whether the bin files differ.");
				System.out.println(buf.toString());
			}
			
			if(args.length == 1){
				new BinFileConcatenator(new File(args[0])).concat();
			}
			if(args.length == 2){
				int result = new BinFileConcatenator(new File(args[0])).diff(new File(args[1]));
				if(result == 0){
					System.out.println("The bin files are equal");
				}else{
					System.out.println("The bin files are different");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
