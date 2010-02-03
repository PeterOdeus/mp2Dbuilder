package org.mp2dbuilder.renderer.generators;

import java.awt.Color;
import java.util.Stack;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.RingElement;
import org.openscience.cdk.renderer.elements.TextGroupElement;
import org.openscience.cdk.renderer.elements.TextGroupElement.Position;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator;

public class MCSOverlayAtomGenerator extends ExtendedAtomGenerator {

	private IAtomContainer mcsContainer;

	public MCSOverlayAtomGenerator(IAtomContainer mcsContainer){
		this.mcsContainer = mcsContainer;
	}

	public IRenderingElement generate(
			IAtomContainer ac, IAtom atom, RendererModel model) {
		ElementGroup elementGroup = new ElementGroup();
		IRenderingElement renderingElement = super.generate(ac, atom, model);
		Point2d p = atom.getPoint2d();
		if(renderingElement== null){
			Color c = getAtomColor(atom); //getColorForAtom(atom, model);
			TextGroupElement textGroup = new TextGroupElement(p.x, p.y, "", c);
			decorate(textGroup, ac, atom, model);
			renderingElement = textGroup;
		}
		elementGroup.add(renderingElement);
		return elementGroup;
	}

	public void decorate(TextGroupElement textGroup, 
			IAtomContainer ac, 
			IAtom atom, 
			RendererModel model) {
		Stack<Position> unused = getUnusedPositions(ac, atom);

		Position position = getNextPosition(unused);
		String id = atom.getID();
		if(id == null){
			boolean idIsNull = true;
			//TODO is this a problem?
		}
		IAtom mcsAtom = getMCSAtomById(id);
		if(mcsAtom != null){
			String number = String.valueOf(this.mcsContainer.getAtomNumber(mcsAtom) + 1 );
			textGroup.addChild(number, position);
		}

		super.decorate(textGroup, ac, atom, model);
	}

	private IAtom getMCSAtomById(String id){
		IAtom matchedAtom = null;
		String currId = null;
		for(int i = 0; i < this.mcsContainer.getAtomCount(); i++){
			currId = this.mcsContainer.getAtom(i).getID();
			if(currId == null){
				boolean currIdIsNull = true;
				//TODO is this a problem?
			}else if(currId.equals(id)){
				matchedAtom = this.mcsContainer.getAtom(i);
				break;
			}
		}
		return matchedAtom;
	}

	private Position getNextPosition(Stack<Position> unused) {
		if (unused.size() > 0) {
			return unused.pop();
		} else {
			return Position.N;
		}
	}
}
