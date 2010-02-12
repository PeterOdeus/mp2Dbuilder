package org.mp2dbuilder.viewer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

public class FilteringMoleculeViewerWorker extends SwingWorker<Void, String> {

	private String _cmd;
	private FilteringMoleculeViewer _viewer;

	public FilteringMoleculeViewerWorker(FilteringMoleculeViewer viewer,
			String cmd) {
		this._viewer = viewer;
		this._cmd = cmd;
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			_viewer.repaint();
		}
		_viewer.cancelButton.setEnabled(false);
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
		_viewer.currentReactionSet = null;
		int tempCurrentRireg = 0;
		try {
			if (_viewer.PREVIOUS.equals(_cmd)) { // first button clicked
				if (_viewer.currentItemIndex == 0) {
					JOptionPane.showMessageDialog(_viewer,
							"This is the first structure.");
					return null;
				}
				tempCurrentRireg = _viewer.riregMap
						.get(_viewer.currentItemIndex - 1);
				_viewer.currentItemIndex -= 1;
			} else if (_viewer.NEXT.equals(_cmd)) {
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
			JOptionPane.showMessageDialog(_viewer, "End of file reached");
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
			CDKException {
		int i = 0;
		int targetRireg = _viewer.currentRireg;
		while (true) {
			_viewer.currentReactionSet = (IReactionSet) _viewer.reader
					.read(new NNReactionSet());
			targetRireg++;
			System.out.println("" + targetRireg);
			publish("" + targetRireg);
			if (_viewer.isMatchingBothSmarts(_viewer.currentReactionSet) == true) {
				_viewer.riregMap.add(targetRireg);
				break;
			}
		}
	}

	// private boolean isMatchingBothSmarts(IReactionSet reactionSet) throws
	// CDKException{
	//		
	// boolean status = false;
	// try {
	// IAtomContainer reactant =
	// reactionSet.getReaction(0).getReactants().getMolecule(0);
	// SybylAtomTypeMatcher reactantMatcher =
	// SybylAtomTypeMatcher.getInstance(reactant.getBuilder());
	// reactantMatcher.findMatchingAtomType(reactant);
	// //QueryAtomContainer query =
	// null;//SMARTSParser.parse(text.getText().trim());//"[#6][#7]"
	// String q = _viewer.text.getText().trim();
	// SMARTSQueryTool sqt = new
	// SMARTSQueryTool(q);//("[NX3;h1,h2,H1,H2;!$(NC=O)]");
	// status = sqt.matches(reactant);
	//
	// if(status == false){
	// return false;
	// }
	//
	// IAtomContainer product =
	// reactionSet.getReaction(0).getProducts().getMolecule(0);
	// SybylAtomTypeMatcher productMatcher =
	// SybylAtomTypeMatcher.getInstance(product.getBuilder());
	// // we don't care about the types result,just the transformation the
	// product goes through.
	// //I.e. CDKHueckelAromaticityDetector
	// reactantMatcher.findMatchingAtomType(product);
	// //query = SMARTSParser.parse(text2.getText().trim());
	// //status = UniversalIsomorphismTester.isSubgraph(product, query);
	// q = _viewer.text2.getText().trim();
	// sqt = new SMARTSQueryTool(q);//("[NX3;h1,h2,H1,H2;!$(NC=O)]");
	// status = sqt.matches(product);
	// } catch (Exception e) {
	// if("Timeout for AllringsFinder exceeded".equals(e.getMessage())){
	// _viewer.logger.warn("RIREG skipped because of timeout");
	// System.out.println("RIREG skipped because of timeout");
	// return false;
	// }
	// }
	//
	// if(status == false){
	// return false;
	// }
	// return true;
	// }

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
