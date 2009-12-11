package org.mp2dbuilder.renderer.generators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;

import org.mp2dbuilder.builder.MetaboliteHandler;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

public class ReactionCentreGenerator implements IGenerator {

	@SuppressWarnings("unused")
	public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
		Set<IAtom> reactionCentreAtoms = new HashSet<IAtom>();
		for(IAtom atom: ac.atoms()){
			if(atom.getProperty(MetaboliteHandler.REACTION_CENTRE_FIELD_NAME) != null){
				reactionCentreAtoms.add(atom);
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

	@Override
	public List<IGeneratorParameter<?>> getParameters() {
		// TODO Auto-generated method stub
		return new ArrayList<IGeneratorParameter<?>>();
	}

	

}
