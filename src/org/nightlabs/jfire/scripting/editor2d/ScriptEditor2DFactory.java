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

import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.Editor2DFactory;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.Orientation;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.Type;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.WidthScale;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface ScriptEditor2DFactory 
extends Editor2DFactory 
{
	TextScriptDrawComponent createTextScriptDrawComponent();
	
	TextScriptDrawComponent createTextScriptDrawComponent(String text, Font font, int x, int y, DrawComponentContainer parent);	
	
	TextScriptDrawComponent createTextScriptDrawComponent(String text, String fontName, int fontSize, int fontStyle, int x, int y, DrawComponentContainer parent);
	
	ScriptMultiLayerDrawComponent createScriptMultiLayerDrawComponent();
	
	BarcodeDrawComponent createBarcode();
		
	BarcodeDrawComponent createBarcode(Type type, String value, int x, int y, WidthScale widthScale, 
			int height, Orientation orientation, boolean printHumanReadable, 
			DrawComponentContainer parent, ScriptRegistryItemID scriptID);	
}
