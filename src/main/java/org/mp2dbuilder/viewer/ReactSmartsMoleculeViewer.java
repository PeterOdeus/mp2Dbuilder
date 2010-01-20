package org.mp2dbuilder.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.junit.Assert;
import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.nonotify.NNReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.ReactionSmartsQueryTool;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.smiles.smarts.parser.ASTStart;
import org.openscience.cdk.smiles.smarts.parser.ParseException;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import org.openscience.cdk.smiles.smarts.parser.visitor.SmartsQueryVisitor;

import prototyping.ReacSmartsTest;

public class ReactSmartsMoleculeViewer extends MoleculeViewer {

	private static final long serialVersionUID = 1L;

	private List<Integer> riregMap = new ArrayList<Integer>();
	private int currentItemIndex=-1;
	protected JTextArea text2;
	private IReactionSet currentReactionSet;
	private int c;
	private List<IReaction> reactionList;

	public ReactSmartsMoleculeViewer(ReaccsMDLRXNReader reader) throws Exception {
		super(reader,null);
		c=0;
		initializeReactionList();
	}

	private void initializeReactionList() throws InvalidSmilesException {
		reactionList=new ArrayList<IReaction>();
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
		reactionList.add(sp.parseReactionSmiles("CCCN(C)C>>CCN(C)C"));
		reactionList.add(sp.parseReactionSmiles("CCCCCCCN(C)C>>CCCCCCCNC"));
		reactionList.add(sp.parseReactionSmiles("CNC(CC=OC)CCCCN(C)C>>CNC(CC=OC)CCCCN(C)C"));
		reactionList.add(sp.parseReactionSmiles("CNCC(O)CCCN(C)C>>CNCC(O)CCCNC"));
		
	}

	public ReactSmartsMoleculeViewer(String fileName) throws Exception {
		super(fileName);
		c=0;
	}

	@Override
	protected void addTextfields(JToolBar toolBar){
		toolBar.add(new JLabel(" SMARTS#1"));
		super.addTextfields(toolBar);
		text2 = new JTextArea(1,3);
		toolBar.add(new JLabel(" SMARTS#2"));
		toolBar.add(text2);
		text.setText(ReacSmartsTest.N_DEALKYLATION_REACTANT_SMARTS);
		text2.setText(ReacSmartsTest.N_DEALKYLATION_PRODUCT_SMARTS);
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
			
			//Get next reaction from list of SMILES
			IReaction reaction = getCurrentReaction();

			//Get the two input smarts and set up query tool
			//TODO: factor out to be separated by >>
			String reactantQuery = text.getText().trim();
			String productQuery = text2.getText().trim();
			ReactionSmartsQueryTool sqt = new ReactionSmartsQueryTool(reactantQuery,productQuery);
			
			//We also know we only have one reactant and one product
			IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
			IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);

			//If we have at least one match, highlight atoms
			if(sqt.matches(reaction)){

		        //List of reactant atom numbers to highlight
	        	List<List<Integer>> matchingReactantAtomsList = sqt.getUniqueReactantMatchingAtoms();
		        IAtom targetAtom = null;
		        for(List<Integer> list: matchingReactantAtomsList){
		        	for(Integer i: list){
		        		targetAtom = reactant.getAtom(i);
		        		targetAtom.setProperty(MetaboliteHandler.SMART_HIT_FIELD_NAME, new Boolean(true));
		        	}
		        }

				//Process product matches
	        	List<List<Integer>> matchingProductAtomsList = sqt.getUniqueProductMatchingAtoms();
		        targetAtom = null;
		        
		        //List of product atom numbers to highlight
		        for(List<Integer> list: matchingProductAtomsList){
		        	for(Integer i: list){
		        		targetAtom = product.getAtom(i);
		        		targetAtom.setProperty(MetaboliteHandler.SMART_HIT_FIELD_NAME, new Boolean(true));
		        	}
		        }

			}
			
			i1 = getImage(reactant, null, true, product, 2);
			i2 = getImage(product, null, true, null, 2);
		} catch(ReaccsFileEndedException e){
			i1 = getImage(null,null,false,null, 2);
			i2 = getImage(null,null,false,null, 2);
		}
		imagePanel.setImages(i1	,i2,null);
	}

	/**
	 * Get next reaction from predefined list
	 * @return
	 */
	private IReaction getCurrentReaction() {
		return reactionList.get(currentItemIndex);
	}

	private List<List<RMap>> getSubgraphMaps(
			IAtomContainer g1, 
			IAtomContainer g2, boolean isDollar) throws CDKException{
		if (g2.getAtomCount() > g1.getAtomCount()) return null;
		// test for single atom case
		if (g2.getAtomCount() == 1 && !isDollar) {
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

	/**
	 * 
	 * @param reactionSet
	 * @return true if both smarts match in rectant and product
	 * @throws CDKException
	 */
	private boolean isMatchingBothSmarts(IReactionSet reactionSet) throws CDKException{

		if (true)
			return true;
		
		String reactantQuery = text.getText().trim();
		String productQuery = text2.getText().trim();
		ReactionSmartsQueryTool sqt = new ReactionSmartsQueryTool(reactantQuery,productQuery);
		
		//We know we only have one reaction in the reactionset
		IReaction reaction = reactionSet.getReaction(0);
		
		//It seems we need to do atom typing for SMARTS matching to work. TODO: Confirm
		IAtomContainer reactant = (IAtomContainer) reaction.getReactants().getMolecule(0);
		SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher.getInstance(reactant.getBuilder());
		reactantMatcher.findMatchingAtomType(reactant);

		IAtomContainer product = (IAtomContainer) reaction.getProducts().getMolecule(0);
		SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher.getInstance(product.getBuilder());
		reactantMatcher.findMatchingAtomType(product);

		//If we have a match in both reactant and products
		if(sqt.matches(reaction)){
			return true;
		}
		else
			return false;
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
					JOptionPane.showMessageDialog(this, "This is the first structure.");
					return;
				}
				tempCurrentRireg = riregMap.get(currentItemIndex - 1);
				currentItemIndex -= 1;
			} else if (NEXT.equals(cmd)) { 
				if(currentItemIndex == (reactionList.size()-1)){
					JOptionPane.showMessageDialog(this, "This is the last structure.");
					return;
				}
				currentItemIndex++;
				if(currentItemIndex >= riregMap.size()){
					establishNextFilteredItem();
					currentItemIndex = riregMap.size() -1;
					tempCurrentRireg = riregMap.get(currentItemIndex);
				}else{
					tempCurrentRireg = riregMap.get(currentItemIndex);
					tryToReset();
					reader.setInitialRiregNo(tempCurrentRireg);
				}
			} else if (GOTO.equals(cmd)) { // third button clicked
				currentItemIndex = -1;
				riregMap.clear();
				tryToReset();
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
		MoleculeViewer gui = new ReactSmartsMoleculeViewer(fileName);
		//gui.setRireg(1);
		showGUI(gui);
	}

}
