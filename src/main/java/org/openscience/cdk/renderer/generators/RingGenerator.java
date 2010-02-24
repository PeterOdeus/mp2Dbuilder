/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.renderer.generators;

import static org.openscience.cdk.CDKConstants.ISAROMATIC;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module renderbasic
 */
public class RingGenerator extends BasicBondGenerator {

	private Collection<IRing> painted_rings;

	public RingGenerator() {
		this.painted_rings = new HashSet<IRing>();
	}

	@Override
	public IRenderingElement generateRingElements(
	        IBond bond, IRing ring, RendererModel model) {
		if (ringIsAromatic(ring) && model.getShowAromaticity()) {
			ElementGroup pair = new ElementGroup();
			if (model.getShowAromaticityCDKStyle()) {
			    pair.add(generateBondElement(bond, IBond.Order.SINGLE, model));
			    super.setOverrideColor(Color.LIGHT_GRAY);
			    pair.add(generateInnerElement(bond, ring, model));
			    super.setOverrideColor(null);
			} else {
    			pair.add(generateBondElement(bond, IBond.Order.SINGLE, model));
    			if (!painted_rings.contains(ring)) {
    				painted_rings.add(ring);
    				pair.add(generateRingRingElement(bond, ring, model));
    			}
			}
			return pair;
		} else {
			return super.generateRingElements(bond, ring, model);
		}
	}

	private IRenderingElement generateRingRingElement(
	        IBond bond, IRing ring, RendererModel model) {
		Point2d c = GeometryTools.get2DCenter(ring);

		double[] minmax = GeometryTools.getMinMax(ring);
		double width  = minmax[2] - minmax[0];
		double height = minmax[3] - minmax[1];
		double radius = Math.min(width, height) * model.getRingProportion();

		return new OvalElement(
		        c.x, c.y, radius, false, getColorForBond(bond, model));
	}

	private boolean ringIsAromatic(final IRing ring) {
		boolean isAromatic = true;
		for (IAtom atom : ring.atoms()) {
			if (!atom.getFlag(ISAROMATIC)) {
				isAromatic = false;
				break;
			}
		}
		if (!isAromatic) {
		  isAromatic = true;
			for (IBond b : ring.bonds()) {
				if (!b.getFlag(ISAROMATIC)) {
					return false;
				}
			}
		}
		return isAromatic;
	}
}