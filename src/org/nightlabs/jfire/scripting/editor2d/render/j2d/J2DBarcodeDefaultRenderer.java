/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.scripting.editor2d.render.j2d;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import net.sourceforge.barbecue.output.OutputException;

import org.apache.log4j.Logger;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.render.j2d.J2DBaseRenderer;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.Orientation;

/**
 * @author Daniel.Mazurek <at> NightLabs <dot> de
 *
 */
public class J2DBarcodeDefaultRenderer
extends J2DBaseRenderer
{
	public static final Logger logger  = Logger.getLogger(J2DBarcodeDefaultRenderer.class);
	
	public J2DBarcodeDefaultRenderer() {
		super();
	}

	@Override
	public void paint(DrawComponent dc, Graphics2D g2d)
	{
		BarcodeDrawComponent barcode = (BarcodeDrawComponent) dc;
		try {
			if (barcode.getOrientation() == Orientation.HORIZONTAL) {
				barcode.getBarcode().draw(g2d, barcode.getX(), barcode.getY());	
			}
			else if (barcode.getOrientation() == Orientation.VERTICAL)
			{
				AffineTransform oldAT = g2d.getTransform();
				g2d.rotate(Math.toRadians(90), barcode.getX(), barcode.getY());
				g2d.translate(0, -barcode.getWidth());
				barcode.getBarcode().draw(g2d, barcode.getX(), barcode.getY());
				g2d.setTransform(oldAT);
			}
		} catch (OutputException e) {
			logger.error("An error occured during barcode painting", e);
		}
	}

}

