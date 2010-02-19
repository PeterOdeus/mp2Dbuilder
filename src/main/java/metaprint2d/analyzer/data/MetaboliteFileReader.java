package metaprint2d.analyzer.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import metaprint2d.analyzer.data.processor.DataSource;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.io.ReaccsFileEndedException;
import org.mp2dbuilder.io.ReaccsMDLRXNReader;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class MetaboliteFileReader implements DataSource<Transformation> {
	// public RDFileReader in;
	// private MetaboliteEntryReader reader;

	private static ILoggingTool logger = LoggingToolFactory
			.createLoggingTool(MetaboliteFileReader.class);
	private ReaccsMDLRXNReader reader;
	private IReactionSet currentReactionSet;
	private MetaboliteHandler handler;
	private boolean skipMultistep;

	public MetaboliteFileReader(InputStream is,
			MetaboliteHandler metaboliteHandler) throws IOException {
		reader = new ReaccsMDLRXNReader(is);
		handler = metaboliteHandler;
		// this.reader = new MetaboliteEntryReader();

		// this.in = new RDFileReader(is);
	}

	public MetaboliteFileReader(File file, MetaboliteHandler metaboliteHandler)
			throws FileNotFoundException, IOException {
		this(new FileInputStream(file), metaboliteHandler);
	}

	public void setInitialReaction(int i) {
		this.reader.setInitialRiregNo(i);
	}

	public Transformation getNext() throws Exception {
		Transformation t;
		ensureOpen();
		do {
			try {
				currentReactionSet = (IReactionSet) reader
						.read(new NNReactionSet());
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
		} while ((this.skipMultistep) && (t.isMultiStep()));

		return t;
	}

	private void ensureOpen() throws IOException {
		if (this.reader == null)
			throw new IOException("Stream closed");
	}

	public synchronized void close() throws IOException {
		if (this.reader == null) {
			return;
		}
		this.reader.close();
		this.reader = null;
	}

	// public ReactionAnalyser getAnalyser()
	// {
	// // return this.reader.getAnalyser();
	// }

	// public void setAnalyser(ReactionAnalyser analyser) {
	// // this.reader.setAnalyser(analyser);
	// }

	public boolean isSkipMultistep() {
		return this.skipMultistep;
	}

	public void setSkipMultistep(boolean skipMultistep) {
		this.skipMultistep = skipMultistep;
	}
}
