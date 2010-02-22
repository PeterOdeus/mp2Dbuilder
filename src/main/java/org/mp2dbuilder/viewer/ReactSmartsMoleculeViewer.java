package org.mp2dbuilder.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

//import prototyping.ReacSmartsTest;

public class ReactSmartsMoleculeViewer extends MoleculeViewer {

//	public static String N_DEALKYLATION_REACTANT_SMARTS="[$([C][N:1]([C:3])[*:2])]";
//	public static String N_DEALKYLATION_PRODUCT_SMARTS="[C:3][N:1][*:2]";

	public static String SIMPLE_REACTANT_SMARTS="[$([*:1])]";
	public static String SIMPLE_PRODUCT_SMARTS="[*:1]";
	
	private static final long serialVersionUID = 1L;

	List<Integer> riregMap = new ArrayList<Integer>();
	public int currentItemIndex = -1;
	protected JTextArea text2;
	IReactionSet currentReactionSet;
	public List<IReaction> reactionList;
	protected JCheckBox chkShouldDrawNumbers;
	protected JCheckBox chkShouldShowMCSS;

	JTextArea riregNoText;

	public ReactSmartsMoleculeViewer(ReaccsMDLRXNReader reader, String filename) throws Exception {
		super(reader,filename);
//		initializeReactionList();
	}

	private void initializeReactionList() throws InvalidSmilesException {
		reactionList = new ArrayList<IReaction>();
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder
				.getInstance());
		reactionList.add(sp.parseReactionSmiles("CCCN(C)C>>CCN(C)C"));
		reactionList.add(sp.parseReactionSmiles("CCCCCCCN(C)C>>CCCCCCCNC"));
		reactionList
				.add(sp
						.parseReactionSmiles("CNC(CC=OC)CCCCN(C)C>>CNC(CC=OC)CCCCN(C)C"));
		reactionList.add(sp
				.parseReactionSmiles("CNCC(O)CCCN(C)C>>CNCC(O)CCCNC"));

	}

	public ReactSmartsMoleculeViewer(String fileName) throws Exception {
		super(fileName);
	}

	@Override
	protected void addTextfields(JToolBar toolBar) {
		toolBar.add(new JLabel(" SMARTS#1"));
		super.addTextfields(toolBar);
		text2 = new JTextArea(1, 3);
		toolBar.add(new JLabel(" SMARTS#2"));
		toolBar.add(text2);
		riregNoText = new JTextArea(1, 3);
		toolBar.add(new JLabel("Start @ RIREG#"));
		toolBar.add(riregNoText);
		riregNoText.setText("1");
		text.setText(SIMPLE_REACTANT_SMARTS);
		text2.setText(SIMPLE_PRODUCT_SMARTS);
	}
	
	@Override
	protected void addOptions(JToolBar optionsBar) {
		chkShouldDrawNumbers = new JCheckBox("Show Atom Numbers");
		optionsBar.add(chkShouldDrawNumbers);
		chkShouldShowMCSS = new JCheckBox("Show MCSS");
		chkShouldShowMCSS.setSelected(true);
		optionsBar.add(chkShouldShowMCSS);
	}

	@Override
	protected IReactionSet getNextReactionSetForRendering()
			throws ReaccsFileEndedException, CDKException {
		if (this.currentReactionSet == null) {
			return (IReactionSet) reader.read(new NNReactionSet());
		} else {
			return this.currentReactionSet;
		}
	}

	@Override
	protected void generateImage() throws Exception {
		Image i1 = null;
		Image i2 = null;
		Image i3 = null;
		try {

			IReactionSet reactionSet = getNextReactionSetForRendering();
			
			IAtomContainer reactant = (IAtomContainer) reactionSet.getReaction(0).getReactants().getMolecule(0);
			IAtomContainer product = (IAtomContainer) reactionSet.getReaction(0).getProducts().getMolecule(0);
/*
	         // Detect aromaticity and add explicit hydrogens
	         CDKHueckelAromaticityDetector.detectAromaticity(reactant);
	         AtomContainerManipulator.convertImplicitToExplicitHydrogens(reactant);
	         // Percieve atom types again to assign hydrogens atom types
	         AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(reactant);

	         // Detect aromaticity
	         CDKHueckelAromaticityDetector.detectAromaticity(product);
	         AtomContainerManipulator.convertImplicitToExplicitHydrogens(product);
	         // Percieve atom types again to assign hydrogens atom types
	         AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(product);
*/
	         
			i1 = getImage(reactant, null, true, product, 2);
			i2 = getImage(product, null, true, null, 2);
		} catch(ReaccsFileEndedException e){
			i1 = getImage(null,null,false,null, 2);
			i2 = getImage(null,null,false,null, 2);
		}
		imagePanel.setImages(i1, i2, i3);
	}

	/**
	 * Get next reaction from predefined list
	 * 
	 * @return
	 */
	private IReaction getCurrentReaction() {
		return reactionList.get(currentItemIndex);
	}

	private List<List<RMap>> getSubgraphMaps(IAtomContainer g1,
			IAtomContainer g2, boolean isDollar) throws CDKException {
		if (g2.getAtomCount() > g1.getAtomCount())
			return null;
		// test for single atom case
		if (g2.getAtomCount() == 1 && !isDollar) {
			List<List<RMap>> listList = new ArrayList<List<RMap>>();
			List<RMap> rMapList = new ArrayList<RMap>();
			listList.add(rMapList);
			IAtom atom = g2.getAtom(0);
			for (int i = 0; i < g1.getAtomCount(); i++) {
				IAtom atom2 = g1.getAtom(i);
				if (atom instanceof IQueryAtom) {
					IQueryAtom qAtom = (IQueryAtom) atom;
					if (qAtom.matches(atom2)) {
						atom2.setProperty(
								MetaboliteHandler.SMART_HIT_FIELD_NAME,
								new Boolean(true));
					}
				} else if (atom2 instanceof IQueryAtom) {
					if (true) {
						throw new WhatToDoHereException();
					}
					IQueryAtom qAtom = (IQueryAtom) atom2;
					if (qAtom.matches(atom)) {
						return null;
					}
				} else {
					if (true) {
						throw new WhatToDoHereException();
					}
					if (atom2.getSymbol().equals(atom.getSymbol())) {
						return null;
					}
				}
			}
			return listList;
		}
		return UniversalIsomorphismTester.getSubgraphMaps(g1, g2);

	}

	private void setSmartsHitsForBonds(IAtomContainer atomContainer,
			List<List<RMap>> res) {
		IBond bond = null;
		Boolean trueValue = new Boolean(true);
		for (List<RMap> rMapList : res) {
			for (RMap rMap : rMapList) {
				bond = atomContainer.getBond(rMap.getId1());
				for (IAtom atom : bond.atoms()) {
					atom.setProperty(MetaboliteHandler.SMART_HIT_FIELD_NAME,
							trueValue);
				}
			}
		}
	}

	@Override
	protected void initImagePanel() throws CDKException {
		Image i1 = getImage(null, null, false, null, 3, false);
		Image i2 = getImage(null, null, false, null, 3, false);
		Image i3 = getImage(null, null, false, null, 3, false);
		imagePanel = new ImagePanel(i1, i2, i3);
	}


	public void actionPerformed(ActionEvent e) {
		if (!CANCEL.equals(e.getActionCommand())) {
			cancelButton.setEnabled(true);
			swingWorker = new ReactSmartsMoleculeViewerWorker(this, e
					.getActionCommand());
			swingWorker.execute();
		} else {
			swingWorker.cancel(true);
			cancelButton.setEnabled(false);
		}
	}

	public static void main(String[] args) throws Exception, IOException {
		String fileName = null;
		if (args == null || (args != null && args.length == 0)) {
			System.out
					.println("syntax: java [-Dcdk.debugging=true|false] -jar mp2Dbuilder-0.0.1-SNAPSHOT.jar <file path, e.g. /tmp/rdffile.rdf>");
			return;
		}
		fileName = args[0];
		MoleculeViewer gui = new ReactSmartsMoleculeViewer(fileName);
		// gui.setRireg(1);
		showGUI(gui, false);
	}

	public void setCurrentMCSS(IAtomContainer mcss) {
		this.mcss = mcss;
	}

}
