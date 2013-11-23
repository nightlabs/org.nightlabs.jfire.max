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
package org.nightlabs.jfire.scripting.editor2d;

import java.awt.Font;

import net.sourceforge.barbecue.Barcode;

import org.nightlabs.editor2d.DrawComponent;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface BarcodeDrawComponent
extends DrawComponent, ScriptDrawComponent
{
	public static final Type TYPE_DEFAULT = Type.TYPE_128;
	public static final String VALUE_DEFAULT = " ";
	public static final boolean HUMAN_READABLE_DEFAULT = true;
	public static final double DEFAULT_BAR_WIDTH = 0.21f; // mm
	public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
	public static final Orientation ORIENTATION_DEFAULT = Orientation.VERTICAL;
	public static final WidthScale WIDTH_SCALE_DEFAULT = WidthScale.SCALE_4;

	public static final String PROP_TYPE = "Type";
	public static final String PROP_VALUE = "Value";
	public static final String PROP_HUMAN_READABLE = "HumanReadable";
	public static final String PROP_WIDTH_SCALE = "WidthScale";
	public static final String PROP_ORIENTATION = "Orientation";
	public static final String PROP_BARCODE_HEIGHT = "barcodeHeight";

	public enum Type {
		TYPE_128
	}

	public enum Orientation {
		HORIZONTAL,
		VERTICAL
	}

	public enum WidthScale {
		SCALE_1,
		SCALE_2,
		SCALE_3,
		SCALE_4
	}

	Type getType();
	void setType(Type type);

	Orientation getOrientation();
	void setOrientation(Orientation orientation);

	WidthScale getWidthScale();
	void setWidthScale(WidthScale widthScale);

	boolean isHumanReadable();
	void setHumanReadable(boolean b);

	Barcode getBarcode();

	String getText();

	void setBarcodeHeight(int barcodeHeight);
	int getBarcodeHeight();
}
