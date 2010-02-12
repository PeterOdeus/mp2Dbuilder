package org.mp2dbuilder.viewer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class MoleculeViewerWorker extends SwingWorker<Void, String> {

	private String _cmd;
	private MoleculeViewer _viewer;

	public MoleculeViewerWorker(MoleculeViewer viewer, String cmd) {
		this._viewer = viewer;
		this._cmd = cmd;
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			_viewer.repaint();
		}
	}

	@Override
	protected void process(List<String> paramList) {
		for (String s : paramList) {
			_viewer.riregNoLabel.setText(s);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	protected Void doInBackground() throws Exception {
		@SuppressWarnings("unused")
		String description = null;

		// Handle each button.
		try {
			if (_viewer.PREVIOUS.equals(_cmd)) { // first button clicked
				this.setRireg(_viewer.currentRireg - 1);
			} else if (_viewer.NEXT.equals(_cmd)) {
				this.setRireg(_viewer.currentRireg + 1);
			} else if (_viewer.GOTO.equals(_cmd)) { // third button clicked
				_viewer.tryToReset();
				_viewer.reader.setInitialRiregNo(Integer.valueOf(_viewer.text
						.getText().trim()));
				this.setRireg(Integer.valueOf(_viewer.text.getText().trim()));
			}
		} catch (Exception e1) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e1.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(_viewer, result.toString());
			throw new RuntimeException(e1);
		}
		return null;
	}

	public void setRireg(int targetRireg) throws Exception {
		boolean isReset = false;
		if (targetRireg <= _viewer.currentRireg) {
			_viewer.tryToReset();
			isReset = true;
		}
		_viewer.currentRireg = targetRireg;
		if (_viewer.currentRireg < 1) {
			_viewer.currentRireg = 1;
		}
		this.publish("" + _viewer.currentRireg);
		// this.riregNoLabel.setText(this.currentRireg + "");
		if (isReset == true) {
			_viewer.reader.setInitialRiregNo(_viewer.currentRireg);
		}
		_viewer.generateImage();
	}

}
