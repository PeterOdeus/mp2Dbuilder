package org.mp2dbuilder.renderer.visitor;

import java.awt.Graphics2D;

import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

public class BugfixAWTDrawVisitor extends AWTDrawVisitor {

	private Graphics2D g;
	
	public BugfixAWTDrawVisitor(Graphics2D g) {
		super(g);
		this.g = g;
	}
	
	public void visit(OvalElement oval) {
        this.g.setColor(oval.color);
        int[] min = 
            this.transformPoint(oval.x - oval.radius, oval.y + oval.radius);
        int[] max = 
            this.transformPoint(oval.x + oval.radius, oval.y - oval.radius);
        int w = max[0] - min[0];
        int h = max[1] - min[1];;
        if (oval.fill) {
            this.g.fillOval(min[0], min[1], w, h);
        } else {
            this.g.drawOval(min[0], min[1], w, h);
        }
    }

}
