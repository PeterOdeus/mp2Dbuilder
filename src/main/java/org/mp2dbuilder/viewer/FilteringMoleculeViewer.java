package org.mp2dbuilder.viewer;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.ReaccsFileEndedException;
import org.openscience.cdk.io.ReaccsMDLRXNReader;

public class FilteringMoleculeViewer extends MoleculeViewer {

	private List<Integer> riregMap = new ArrayList<Integer>();
	private int currentItemIndex=0;
	
	public FilteringMoleculeViewer(ReaccsMDLRXNReader reader) throws Exception {
		super(reader);
		// TODO Auto-generated constructor stub
	}
	
	private void establishNextFilteredItem(){
		int i = 0;
		while(i++ < 5){
			try {
				IReactionSet reactionSet = this.getNextReactionSet();
				this.currentRireg++;
				this.riregNoLabel.setText(this.currentRireg + "");
				if(i%2==0){
					riregMap.add(this.currentRireg);
					currentItemIndex = riregMap.size()-1;
					break;
				}
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

}
