package org.mp2dbuilder.viewer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openscience.cdk.atomtype.SybylAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;
import org.openscience.cdk.nonotify.NNReactionSet;

public class FilteringMoleculeViewer extends MoleculeViewer {

	private List<Integer> riregMap = new ArrayList<Integer>();
	private int currentItemIndex=0;
	
	public FilteringMoleculeViewer(ReaccsMDLRXNReader reader) throws Exception {
		super(reader);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void generateImage() throws Exception{
    	Image i1 = null;
    	Image i2 = null;
    	
    	try{
	    	IReactionSet reactionSet = (IReactionSet)reader.read(new NNReactionSet());
	    	
	    	IAtomContainer reactant = reactionSet.getReaction(0).getReactants().getMolecule(0);
			SybylAtomTypeMatcher reactantMatcher = SybylAtomTypeMatcher.getInstance(reactant.getBuilder());
			reactantMatcher.findMatchingAtomType(reactant);
			
			IAtomContainer product = reactionSet.getReaction(0).getProducts().getMolecule(0);
			SybylAtomTypeMatcher productMatcher = SybylAtomTypeMatcher.getInstance(product.getBuilder());
			// we don't care about the types result,just the transformation the product goes through.
			reactantMatcher.findMatchingAtomType(product);
			
			i1 = getImage(reactant, mcs, true, product);
			i2 = getImage(product, mcs, false, null);
    	} catch(ReaccsFileEndedException e){
    		i1 = getImage(null,null,false,null);
    		i2 = getImage(null,null,false,null);
    	}
		imagePanel.setImages(i1	,i2,null);
    }
	
	@Override
	protected void initImagePanel() throws CDKException{
    	Image i1 = getImage(null,null,false,null);
    	Image i2 = getImage(null,null,false,null);
    	imagePanel = new ImagePanel(i1,i2,null);
    }
	
	private void establishNextFilteredItem(){
		int i = 0;
		while(i++ < 5){
			try {
				IReactionSet reactionSet = this.getNextReactionSet();
				this.currentRireg++;
				this.riregNoLabel.setText(this.currentRireg + "");
				//if(i%2==0){
					riregMap.add(this.currentRireg);
					currentItemIndex = riregMap.size()-1;
					break;
				//}
			} catch (ReaccsFileEndedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        String description = null;
        int currentRireg = 0;
        try{
	        if (PREVIOUS.equals(cmd)) { //first button clicked
	        	currentRireg = riregMap.get(currentItemIndex - 1);
	        	currentItemIndex -= 1;
	        } else if (NEXT.equals(cmd)) { 
	        	if((currentItemIndex + 1) >= riregMap.size()){
	        		establishNextFilteredItem();
	        	}
	        	currentRireg = riregMap.get(currentItemIndex);
	        } else if (GOTO.equals(cmd)) { // third button clicked
	        	currentItemIndex = 0;
	        	riregMap.clear();
	        	this.reader.reset();
	        	this.currentRireg = 0;
	        	establishNextFilteredItem();
	        	currentRireg = riregMap.get(currentItemIndex);
	        }
        }catch(IndexOutOfBoundsException outOfBounds){
        	    currentRireg = riregMap.get(currentItemIndex);
        }
        catch (Exception e1) {
        	final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e1.printStackTrace(printWriter);
            JOptionPane.showMessageDialog(this, result.toString());
			throw new RuntimeException(e1);
		}
        try {
			this.setRireg(currentRireg);
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
    	ReaccsMDLRXNReader reader = getReaccsReader(fileName);
		MoleculeViewer gui = new FilteringMoleculeViewer(reader);
		gui.setRireg(1);
		showGUI(gui);
    }

}
