package org.mp2dbuilder.renderer.generators;

import java.awt.Color;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.ISingleElectron;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.TextGroupElement;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

public class ReactionCentreGenerator implements IGenerator {

	public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
		ElementGroup group = new ElementGroup();
		for(IAtom atom : ac.atoms()){
			Point2d p = atom.getPoint2d();
			double r = model.getAtomRadius() / model.getScale();
			OvalElement textGroup = new OvalElement(p.x, p.y, r, new Color(255,140,0,150));
			group.add(textGroup);
		}
		return group;
//		ElementGroup group = new ElementGroup();
//        
//        // TODO : put into RendererModel
//        final double SCREEN_RADIUS = 2.0;
//        final Color RADICAL_COLOR = Color.BLACK;
//        
//        // XXX : is this the best option?
//        final double ATOM_RADIUS = model.getAtomRadius() / model.getScale();
//        
//        double modelRadius = SCREEN_RADIUS / model.getScale(); 
//        IAtom atom = ac.getAtom(0);
//        Point2d p = atom.getPoint2d();
//        int align = GeometryTools.getBestAlignmentForLabelXY(ac, atom);
//        double rx = p.x;
//        double ry = p.y;
//        if (align == 1) {
//            rx += ATOM_RADIUS;
//        } else if (align == -1) {
//            rx -= ATOM_RADIUS;
//        } else if (align == 2) {
//            ry -= ATOM_RADIUS;
//        } else if (align == -2) {
//            ry += ATOM_RADIUS;
//        }
//          //  group.add(
//        return new OvalElement(rx, ry, 10, true, RADICAL_COLOR);//);
//        //return group;
	}

	public List<IGeneratorParameter> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
