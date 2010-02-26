package org.mp2dbuilder.viewer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.mp2dbuilder.io.ReaccsFileEndedException;
import org.mp2dbuilder.io.ReadingReaccsFileCancelledException;
import org.mp2dbuilder.smiles.smarts.ReactionSmartsQueryTool;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

public class ReactSmartsMoleculeViewerWorker extends SwingWorker<Void, String> {

	protected static ILoggingTool logger = LoggingToolFactory
			.createLoggingTool(ReactSmartsMoleculeViewerWorker.class);

	private String _cmd;
	private ReactSmartsMoleculeViewer _viewer;

	public ReactSmartsMoleculeViewerWorker(ReactSmartsMoleculeViewer viewer,
			String cmd) {
		this._viewer = viewer;
		this._cmd = cmd;
	}

	public void publishToSwingWorker(String s) {
		this.publish(s);
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			_viewer.repaint();
		}
		_viewer.cancelButton.setEnabled(false);
		//_viewer.logTextArea.append(_viewer.imagePanel.getSize().toString());
	}

	@Override
	protected void process(List<String> paramList) {
		for (String s : paramList) {
			if (s.startsWith("log:")) {
				_viewer.logTextArea.append(s.substring(4) + "\n");
			} else if (s.startsWith("deleteLog")) {
				_viewer.logTextArea.setText("");
			} else {
				_viewer.riregNoLabel.setText(s);
			}
		}
	}

	@SuppressWarnings("static-access")
	@Override
	protected Void doInBackground() throws Exception {
		_viewer.currentReactionSet = null;
		int tempCurrentRireg = 0;
		try {
			if (_viewer.PREVIOUS.equals(_cmd)) { // first button clicked
				if (_viewer.currentItemIndex == 0) {
					JOptionPane.showMessageDialog(_viewer,
							"This is the first structure, starting from RIREG#"
									+ _viewer.riregNoText.getText() + ".");
					return null;
				}
				tempCurrentRireg = _viewer.riregMap
						.get(_viewer.currentItemIndex - 1);
				_viewer.currentItemIndex -= 1;
			} else if (_viewer.NEXT.equals(_cmd)) {
				// if(_viewer.currentItemIndex ==
				// (_viewer.reactionList.size()-1)){
				// JOptionPane.showMessageDialog(_viewer,
				// "This is the last structure.");
				// return null;
				// }
				_viewer.currentItemIndex++;
				if (_viewer.currentItemIndex >= _viewer.riregMap.size()) {
					establishNextFilteredItem();
					_viewer.currentItemIndex = _viewer.riregMap.size() - 1;
					tempCurrentRireg = _viewer.riregMap
							.get(_viewer.currentItemIndex);
				} else {
					tempCurrentRireg = _viewer.riregMap
							.get(_viewer.currentItemIndex);
					_viewer.tryToReset();
					_viewer.reader.setInitialRiregNo(tempCurrentRireg);
				}
			} else if (_viewer.GOTO.equals(_cmd)) { // third button clicked
				_viewer.currentItemIndex = -1;
				_viewer.riregMap.clear();
				_viewer.tryToReset();
				this.publish("deleteLog");
				int targetRiregNo = Integer.parseInt(_viewer.riregNoText
						.getText());
				if (targetRiregNo == 1) {
					_viewer.currentRireg = 0;
				} else {
					_viewer.currentRireg = targetRiregNo - 1;
					_viewer.reader.setInitialRiregNo(targetRiregNo);
				}
				establishNextFilteredItem();
				_viewer.currentItemIndex = _viewer.riregMap.size() - 1;
				tempCurrentRireg = _viewer.riregMap
						.get(_viewer.currentItemIndex);
			}
		} catch (ReaccsFileEndedException reaccsFileEndedException) {
			JOptionPane.showMessageDialog(_viewer, "Last structure reached.");
			return null;
		} catch (CancelledException cancelledException) {
			return null;
		} catch (Exception e1) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e1.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(_viewer, result.toString());
			throw new RuntimeException(e1);
		}
		try {
			setRireg(tempCurrentRireg);
		} catch (Exception e1) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e1.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(_viewer, result.toString());
			throw new RuntimeException(e1);
		}
		return null;
	}

	private void establishNextFilteredItem() throws ReaccsFileEndedException,
			CDKException, CancelledException {
		int i = 0;
		int targetRireg = _viewer.currentRireg;
		while (true) {
			if (this.isCancelled()) {
				throw new CancelledException();
			}
			try {
				_viewer.currentReactionSet = (IReactionSet) _viewer.reader
						.read(new NNReactionSet(), this, targetRireg);
			} catch (ReadingReaccsFileCancelledException r) {
				throw new CancelledException();
			}
			// targetRireg++;
			// publish(""+targetRireg);
			// System.out.println(""+targetRireg);
			targetRireg = _viewer.reader.swingWorkerCounter;
			if (isMatchingBothSmarts(_viewer.currentReactionSet, targetRireg) == true) {
				_viewer.riregMap.add(targetRireg);
				break;
			}
		}
	}

	/**
	 * 
	 * @param reactionSet
	 * @return true if both smarts match in rectant and product
	 * @throws CDKException
	 */
	private boolean isMatchingBothSmarts(IReactionSet reactionSet,
			int currentRireg) throws CDKException {

		// Get next reaction from list of SMILES
		IReaction reaction = reactionSet.getReaction(0);

		// Get the two input smarts and set up query tool
		// TODO: factor out to be separated by >>
		String reactantQuery = _viewer.text.getText().trim();
		String productQuery = _viewer.text2.getText().trim();

		if ("".equals(reactantQuery) && "".equals(productQuery)) {
			return true;
		}

		ReactionSmartsQueryTool sqt = new ReactionSmartsQueryTool(
				reactantQuery +">>" + productQuery);

		// We also know we only have one reactant and one product
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants()
				.getMolecule(0);
		IAtomContainer product = (IAtomContainer) reaction.getProducts()
				.getMolecule(0);

		// If we have at least one match, highlight atoms
		boolean isMatch = false;
		try {
			isMatch = sqt.matches(reaction);
		} catch (Exception e) {
			if (e.getMessage().indexOf("Timeout for AllringsFinder exceeded") >= 0) {
				String msg = "RIREG#" + currentRireg
						+ " skipped because of timeout";
				logger.warn(msg);
				this.publish("log:" + msg);
				return false;
			}
		}
		if (isMatch) {

			// List of reactant atom numbers to highlight
			List<List<Integer>> matchingReactantAtomsList = sqt
					.getUniqueReactantMatchingAtoms();
			IAtom targetAtom = null;
			for (List<Integer> list : matchingReactantAtomsList) {
				for (Integer i : list) {
					targetAtom = reactant.getAtom(i);
					targetAtom.setProperty(
							MetaboliteHandler.SMART_HIT_FIELD_NAME,
							new Boolean(true));
				}
			}

			// Process product matches
			List<List<Integer>> matchingProductAtomsList = sqt
					.getUniqueProductMatchingAtoms();
			targetAtom = null;

			// List of product atom numbers to highlight
			for (List<Integer> list : matchingProductAtomsList) {
				for (Integer i : list) {
					targetAtom = product.getAtom(i);
					targetAtom.setProperty(
							MetaboliteHandler.SMART_HIT_FIELD_NAME,
							new Boolean(true));
				}
			}
			
			this._viewer.setCurrentMCSS(sqt.getMCSS());
			return true;
		} else {
			this._viewer.setCurrentMCSS(null);
			return false;
		}
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
