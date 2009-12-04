package org.mp2dbuilder.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

public class FilteringMoleculeViewer extends MoleculeViewer {

	private List<Integer> riregMap = new ArrayList<Integer>();
	private int currentItemIndex=-1;
	protected JTextArea text2;
	private IReactionSet currentReactionSet;

	public FilteringMoleculeViewer(ReaccsMDLRXNReader reader) throws Exception {
		super(reader);
	}

	public FilteringMoleculeViewer(String fileName) throws Exception {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addTextfields(JToolBar toolBar){
		toolBar.add(new JLabel(" SMARTS#1"));
		super.addTextfields(toolBar);
		text2 = new JTextArea(1,3);
		toolBar.add(new JLabel(" SMARTS#2"));
		toolBar.add(text2);
		text.setText("[#6]Br");
		text2.setText("[#6]Br");
	}

	@Override
	protected IReactionSet getNextReactionSetForRendering() throws ReaccsFileEndedException, CDKException{
		if(this.currentReactionSet == null){
			return (IReactionSet)reader.read(new NNReactionSet());
		}else{
			return this.currentReactionSet;
		}
	}

	@Override
	protected void generateImage() throws Exception{
		Image i1 = null;
		Image i2 = null;

		try{
			IReactionSet reactionSet = getNextReactionSetForRendering();

			IAtomContainer reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher.getInstance(reactant.getBuilder());
			reactantMatcher.findMatchingAtomType(reactant);
			QueryAtomContainer query = SMARTSParser.parse(text.getText().trim());//"[#6][#7]"
			//			List<List<RMap>> res = UniversalIsomorphismTester.search(
			//					reactant, query, new BitSet(), UniversalIsomorphismTester.getBitSet(query), true, true);
			List<List<RMap>> res = getSubgraphMaps(reactant, query);
			setSmartsHitsForBonds(reactant, res);


			IAtomContainer product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher.getInstance(product.getBuilder());
			// we don't care about the types result,just the transformation the product goes through.
			reactantMatcher.findMatchingAtomType(product);
			query = SMARTSParser.parse(text2.getText().trim());
			//			res = UniversalIsomorphismTester.search(
			//					product, query, new BitSet(), UniversalIsomorphismTester.getBitSet(query), true, true);
			res = getSubgraphMaps(product, query);
			setSmartsHitsForBonds(product, res);

			i1 = getImage(reactant, null, true, product, 2);
			i2 = getImage(product, null, true, null, 2);
		} catch(ReaccsFileEndedException e){
			i1 = getImage(null,null,false,null, 2);
			i2 = getImage(null,null,false,null, 2);
		}
		imagePanel.setImages(i1	,i2,null);
	}

	private List<List<RMap>> getSubgraphMaps(IAtomContainer g1, IAtomContainer g2) throws CDKException{
		if (g2.getAtomCount() > g1.getAtomCount()) return null;
		// test for single atom case
		if (g2.getAtomCount() == 1) {
			List<List<RMap>> listList = new ArrayList<List<RMap>>();
			List<RMap> rMapList = new ArrayList<RMap>();
			listList.add(rMapList);
			IAtom atom = g2.getAtom(0);
			for (int i=0; i<g1.getAtomCount(); i++) {
				IAtom atom2 = g1.getAtom(i);
				if (atom instanceof IQueryAtom) {
					IQueryAtom qAtom = (IQueryAtom)atom;
					if (qAtom.matches(atom2)){
						atom2.setProperty(MetaboliteHandler.SMART_HIT_FIELD_NAME, new Boolean(true));
					}
				} else if (atom2 instanceof IQueryAtom) {
					if(true){throw new WhatToDoHereException();}
					IQueryAtom qAtom = (IQueryAtom)atom2;
					if (qAtom.matches(atom)){
						return null;
					}
				} else {
					if(true){throw new WhatToDoHereException();}
					if (atom2.getSymbol().equals(atom.getSymbol())){
						return null;
					}
				}
			}
			return listList;
		}
		return UniversalIsomorphismTester.getSubgraphMaps(g1, g2);

	}

	private void setSmartsHitsForBonds(IAtomContainer atomContainer, List<List<RMap>> res){
		IBond bond = null;
		Boolean trueValue = new Boolean(true);
		for(List<RMap> rMapList: res){
			for(RMap rMap:rMapList){
				bond = atomContainer.getBond(rMap.getId1());
				for(IAtom atom : bond.atoms()){
					atom.setProperty(MetaboliteHandler.SMART_HIT_FIELD_NAME, trueValue);
				}
			}
		}
	}

	@Override
	protected void initImagePanel() throws CDKException{
		Image i1 = getImage(null,null,false,null, 2);
		Image i2 = getImage(null,null,false,null, 2);
		imagePanel = new ImagePanel(i1,i2,null);
	}

	private boolean isMatchingBothSmarts(IReactionSet reactionSet) throws CDKException{
		IAtomContainer reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher.getInstance(reactant.getBuilder());
		reactantMatcher.findMatchingAtomType(reactant);
		QueryAtomContainer query = SMARTSParser.parse(text.getText().trim());//"[#6][#7]"
		//		List<List<RMap>> res = UniversalIsomorphismTester.search(
		//				reactant, query, new BitSet(), UniversalIsomorphismTester.getBitSet(query), false, false);
		boolean hasSubgraph = UniversalIsomorphismTester.isSubgraph(reactant, query);

		if(hasSubgraph == false){
			return false;
		}	

		IAtomContainer product = reactionSet.getReaction(0).getProducts().getMolecule(0);
		SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher.getInstance(product.getBuilder());
		// we don't care about the types result,just the transformation the product goes through.
		reactantMatcher.findMatchingAtomType(product);
		query = SMARTSParser.parse(text2.getText().trim());
		//		res = UniversalIsomorphismTester.search(
		//				product, query, new BitSet(), UniversalIsomorphismTester.getBitSet(query), false, false);
		hasSubgraph = UniversalIsomorphismTester.isSubgraph(product, query);

		if(hasSubgraph == false){
			return false;
		}
		return true;
	}

	private void establishNextFilteredItem() throws ReaccsFileEndedException, CDKException{
		int i = 0;
		int targetRireg = this.currentRireg;
		while(true){
			currentReactionSet = (IReactionSet)reader.read(new NNReactionSet());
			targetRireg++;
			System.out.println(""+targetRireg);
			if(isMatchingBothSmarts(currentReactionSet) == true){
				riregMap.add(targetRireg);
				break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		currentReactionSet = null;
		String cmd = e.getActionCommand();
		int tempCurrentRireg = 0;
		try{
			if (PREVIOUS.equals(cmd)) { //first button clicked
				if(currentItemIndex == 0){
					JOptionPane.showMessageDialog(this, "Don't go there.");
					return;
				}
				tempCurrentRireg = riregMap.get(currentItemIndex - 1);
				currentItemIndex -= 1;
			} else if (NEXT.equals(cmd)) { 
				currentItemIndex++;
				if(currentItemIndex >= riregMap.size()){
					establishNextFilteredItem();
					currentItemIndex = riregMap.size() -1;
					tempCurrentRireg = riregMap.get(currentItemIndex);
				}else{
					tempCurrentRireg = riregMap.get(currentItemIndex);
					reader.reset();
					reader.setInitialRiregNo(tempCurrentRireg);
				}
			} else if (GOTO.equals(cmd)) { // third button clicked
				currentItemIndex = -1;
				riregMap.clear();
				reader.reset();
				currentRireg = 0;
				establishNextFilteredItem();
				currentItemIndex = riregMap.size() -1;
				tempCurrentRireg = riregMap.get(currentItemIndex);
			}
		}catch(ReaccsFileEndedException reaccsFileEndedException){
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			reaccsFileEndedException.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(this, result.toString());
			try{
				try {
					reader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}finally{
				if(this.readerFileName != null){
					currentItemIndex = -1;
					riregMap.clear();
					currentRireg = 0;
					text.setText("[#6]Br");
					text2.setText("[#6]Br");
					try {
						this.reader = getReaccsReader(this.readerFileName);
						establishNextFilteredItem();
						currentItemIndex = riregMap.size() -1;
						tempCurrentRireg = riregMap.get(currentItemIndex);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						System.exit(0);
					}
				}
			}
		}catch (Exception e1) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e1.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(this, result.toString());
			throw new RuntimeException(e1);
		}
		try {
			setRireg(tempCurrentRireg);
		} catch (Exception e1) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e1.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(this, result.toString());
			throw new RuntimeException(e1);
		}
	}

	public static void main(String[] args) throws Exception, IOException{
		String fileName = null;
		if(args ==  null || (args != null && args.length == 0)){
			System.out.println("syntax: java [-Dcdk.debugging=true|false] -jar mp2Dbuilder-0.0.1-SNAPSHOT.jar <file path, e.g. /tmp/rdffile.rdf>");
			return;
		}
		fileName = args[0];
		MoleculeViewer gui = new FilteringMoleculeViewer(fileName);
		//gui.setRireg(1);
		showGUI(gui);
	}

}
