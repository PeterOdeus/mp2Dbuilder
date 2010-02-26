package org.mp2dbuilder.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.mp2dbuilder.viewer.CancelledException;
import org.mp2dbuilder.viewer.ReactSmartsMoleculeViewerWorker;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.MDLRXNReader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class ReaccsMDLRXNReader extends MDLRXNReader {

	private ILoggingTool logger = null;
	private String riregNo = "";
	private long fileLengthLong;
	private ReactSmartsMoleculeViewerWorker swingWorker;
	public int swingWorkerCounter;

	public ReaccsMDLRXNReader(Reader in, Mode mode) {
		super(in, mode);
		logger = LoggingToolFactory.createLoggingTool(ReaccsMDLRXNReader.class);
	}

	public ReaccsMDLRXNReader(InputStream input) {
		super(input, Mode.RELAXED);
		logger = LoggingToolFactory.createLoggingTool(ReaccsMDLRXNReader.class);
	}

	public void setInitialRiregNo(int riregNo) {
		this.riregNo = " " + riregNo;
	}

	public IChemObject read(IChemObject object,
			ReactSmartsMoleculeViewerWorker swingWorker, int swingWorkerCounter)
			throws ReaccsFileEndedException, CDKException {
		this.swingWorker = swingWorker;
		this.swingWorkerCounter = swingWorkerCounter;
		IChemObject returnObject = null;
		try {
			returnObject = this.read(object);
		} finally {
			this.swingWorker = null;
		}
		return returnObject;
	}

	/**
	 * Special treatment to work on .rdf files from Reaccs database.
	 * 
	 * @see org.openscience.cdk.io.MDLRXNReader#read(IChemObject) read
	 * @param object
	 *            The object that subclasses IChemObject
	 * @return The IChemObject read
	 * @exception CDKException
	 */
	@Override
	public IChemObject read(IChemObject object)
			throws ReaccsFileEndedException,
			ReadingReaccsFileCancelledException, CDKException {
		if (object instanceof IReactionSet) {
			readUntilRXN();
				IReaction r = (IReaction) super.read(new Reaction());
				if (r != null) {
					((IReactionSet) object).addReaction(r);
				}

			return object;
		} else {
			return super.read(object);
		}
	}

	private void readUntilRXN() throws CDKException,
			ReadingReaccsFileCancelledException, ReaccsFileEndedException {
		try {
			logger.debug("Looking for string \"$RIREG\"" + this.riregNo);
			String line = null;
			do {
				line = input.readLine();
				if (line == null) {
					throw new ReaccsFileEndedException("eof");
				}
				logger.debug(line);
				if (line.indexOf("$RFMT $RIREG ") >= 0) {
					if (this.swingWorker != null) {
						if (this.swingWorker.isCancelled()) {
							throw new CancelledException();
						}
						this.swingWorkerCounter = Integer.valueOf(line
								.substring("$RFMT $RIREG ".length()));
						this.swingWorker.publishToSwingWorker(""
								+ this.swingWorkerCounter);
						System.out.println("" + this.swingWorkerCounter);
					}
				}
			} while (line.indexOf("$RIREG" + this.riregNo) < 0);
			this.riregNo = "";
		} catch (ReaccsFileEndedException e) {
			throw e;
		} catch (CancelledException e) {
			throw new ReadingReaccsFileCancelledException("");
		} catch (Exception exception) {
			logger.debug(exception);
			throw new CDKException("Error while reading header (or sub-header)"
					+ " of Reaccs .rdf RXN file", exception);
		}

	}

	public void activateReset(long fileLengthLong) throws IOException {
		this.fileLengthLong = fileLengthLong;
		if (this.fileLengthLong > Integer.MAX_VALUE) {
			logger.info("marking Integer.MAX_VALUE");
			input.mark(Integer.MAX_VALUE);
		} else {
			logger.info("marking " + (int) this.fileLengthLong);
			input.mark((int) this.fileLengthLong);
		}
	}

	public void reset() throws IOException {
		if (this.fileLengthLong > 0) {
			input.reset();
		}
	}

}
