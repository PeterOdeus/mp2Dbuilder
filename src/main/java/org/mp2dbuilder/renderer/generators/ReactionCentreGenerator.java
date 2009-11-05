package org.mp2dbuilder.renderer.generators;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

public class ReactionCentreGenerator implements IGenerator {

	private IAtomContainer mcsContainer;
	private IAtomContainer productContainer;

	public ReactionCentreGenerator(IAtomContainer mcsContainer, IAtomContainer productContainer){
		this.mcsContainer = mcsContainer;
		this.productContainer = productContainer;
	}
	
	public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
		Set<IAtom> reactionCentreAtoms = new HashSet<IAtom>();
		IAtom reactionCentreCandidate = null;
		IAtom reactionCentreOuterCandidate = null;
		boolean isReactionCentre;
		for(IAtom mcsAtom: this.mcsContainer.atoms()){
			isReactionCentre = false;
			reactionCentreCandidate = getAtomById(mcsAtom.getID(), ac);
			if(reactionCentreCandidate != null){
				//Iterate reactant
				for(IBond bond: ac.bonds()){
					IAtom [] connectedAtoms = bond.getConnectedAtoms(reactionCentreCandidate);
					if(connectedAtoms != null){
						for(int i = 0; i < connectedAtoms.length; i++){
							reactionCentreOuterCandidate = connectedAtoms[i];
							String id = reactionCentreOuterCandidate.getID();
							if(id == null || (id != null && getAtomById(id, this.mcsContainer) == null)){
								reactionCentreAtoms.add(reactionCentreOuterCandidate);
								isReactionCentre = true;
							}	
						}
					}
				}
				
				IAtom productAtom = getAtomById(mcsAtom.getID(), productContainer);
				for(IBond bond: productContainer.bonds()){
					IAtom [] connectedAtoms = bond.getConnectedAtoms(productAtom);
					if(connectedAtoms != null){
						for(int i = 0; i < connectedAtoms.length; i++){
							IAtom productCandidate = connectedAtoms[i];
							String id = productCandidate.getID();
							if(id == null || (id != null && getAtomById(id, this.mcsContainer) == null)){
								isReactionCentre = true;
							}	
						}
					}
				}
				if(isReactionCentre == true){
					reactionCentreAtoms.add(reactionCentreCandidate);
				}
			}
		}
		ElementGroup group = new ElementGroup();
		for(IAtom atom : reactionCentreAtoms){
			Point2d p = atom.getPoint2d();
			double r = model.getAtomRadius() / model.getScale();
			OvalElement textGroup = new OvalElement(p.x, p.y, r, new Color(255,140,0,150));
			group.add(textGroup);
		}
		return group;
	}
	
	private IAtom getAtomById(String id, IAtomContainer ac){
		IAtom matchedAtom = null;
		for(int i = 0; i < ac.getAtomCount(); i++){
			if(ac.getAtom(i).getID() != null && ac.getAtom(i).getID().equals(id)){
				matchedAtom = ac.getAtom(i);
				break;
			}
		}
		return matchedAtom;
	}

	public List<IGeneratorParameter> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
