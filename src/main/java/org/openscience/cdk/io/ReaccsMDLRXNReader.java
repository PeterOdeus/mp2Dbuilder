package org.openscience.cdk.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class ReaccsMDLRXNReader extends MDLRXNReader {

	private ILoggingTool logger = null;
	private String riregNo = "";
	private long fileLengthLong;

	public ReaccsMDLRXNReader(Reader in, Mode mode) {
		super(in, mode);
		logger = LoggingToolFactory.createLoggingTool(ReaccsMDLRXNReader.class);
	}

	public ReaccsMDLRXNReader(InputStream input) {
		super(input, Mode.RELAXED);
		logger = LoggingToolFactory.createLoggingTool(ReaccsMDLRXNReader.class);
	}
	
	public void setInitialRiregNo(int riregNo){
		this.riregNo = " " + riregNo;
	}

	/**
	 * Special treatment to work on .rdf files from Reaccs database.
	 *
	 * @see org.openscience.cdk.io.MDLRXNReader#read(IChemObject) read
	 * @param  object                              The object that subclasses
	 *      IChemObject
	 * @return                                     The IChemObject read
	 * @exception  CDKException
	 */
	public IChemObject read(IChemObject object) throws ReaccsFileEndedException, CDKException {
		if (object instanceof IReactionSet) {
			readUntilRXN();
			Method m = null;
			try {
				m = MDLRXNReader.class.getDeclaredMethod("readReaction", IChemObjectBuilder.class);
				m.setAccessible(true);
				IReaction r = (IReaction) m.invoke((MDLRXNReader)this, object.getBuilder());
				if (r != null) {
					((IReactionSet)object).addReaction(r);
				}
			}catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return object;
			//return super.read(object);// readReactionSet((IReactionSet)object);
		} else {
			return super.read(object);
		}
	}

	private void readUntilRXN() throws CDKException, ReaccsFileEndedException {
		try {
			logger.debug("Looking for string \"$RIREG\"" + this.riregNo);
			String line = null;
			do{
				line = input.readLine();
				if(line == null){
					throw new ReaccsFileEndedException("eof");
				}
				logger.debug(line);
			}while(line.indexOf("$RIREG" + this.riregNo) < 0);
			this.riregNo = "";
		}
		catch(ReaccsFileEndedException e){
			throw e;
		}
		catch (Exception exception) {
			logger.debug(exception);
			throw new CDKException(
					"Error while reading header (or sub-header)" 
					+ " of Reaccs .rdf RXN file", exception);
		}

	}

	public void activateReset(long fileLengthLong) throws IOException {
		this.fileLengthLong = fileLengthLong;
		if(this.fileLengthLong > Integer.MAX_VALUE){
			logger.info("marking Integer.MAX_VALUE");
			input.mark(Integer.MAX_VALUE);
		}else{
			logger.info("marking " + (int)this.fileLengthLong);
			input.mark((int)this.fileLengthLong);
		}
	}

	public void reset() throws IOException {
		if(this.fileLengthLong > 0){
			input.reset();
		}
	}

}
