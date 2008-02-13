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
package org.nightlabs.jfire.scripting.editor2d.impl;

import java.awt.Font;
import java.util.Set;

import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.RootDrawComponent;
import org.nightlabs.editor2d.impl.Editor2DFactoryImpl;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.ScriptEditor2DFactory;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.TextScriptDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.Orientation;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.Type;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent.WidthScale;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptEditor2DFactoryImpl
extends Editor2DFactoryImpl
implements ScriptEditor2DFactory
{
	public BarcodeDrawComponent createBarcode() {
		return new BarcodeDrawComponentImpl();
	}

	public BarcodeDrawComponent createBarcode(Type type, String value, int x,
			int y, WidthScale widthScale, int height, Orientation orientation,
			boolean printHumanReadable, DrawComponentContainer parent,
			ScriptRegistryItemID scriptID)
	{
		return new BarcodeDrawComponentImpl(type, value, x, y, widthScale, height, orientation,
				printHumanReadable, parent, scriptID);
	}

	public ScriptRootDrawComponent createScriptRootDrawComponent(boolean validate) {
		ScriptRootDrawComponent scriptRoot = new ScriptRootDrawComponentImpl();
		if (validate)
			validateRoot(scriptRoot);
		return scriptRoot;
	}

	public TextScriptDrawComponent createTextScriptDrawComponent() {
		return new TextScriptDrawComponentImpl();
	}

	public TextScriptDrawComponent createTextScriptDrawComponent(String text,
			Font font, int x, int y, DrawComponentContainer parent)
	{
		return new TextScriptDrawComponentImpl(text, font, x, y, parent);
	}

	public TextScriptDrawComponent createTextScriptDrawComponent(String text,
			String fontName, int fontSize, int fontStyle, int x, int y,
			DrawComponentContainer parent)
	{
		return new TextScriptDrawComponentImpl(text, fontName, fontSize, fontStyle, x, y, parent);
	}
	
	@Override
	public DrawComponent createDrawComponent(Class<?> clazz) {
		if (clazz.isAssignableFrom(TextScriptDrawComponent.class))
			return createTextScriptDrawComponent();
		if (clazz.isAssignableFrom(BarcodeDrawComponent.class))
			return createBarcode();
		
		return super.createDrawComponent(clazz);
	}

	@Override
	public Set<Class<? extends DrawComponent>> getSupportedDrawComponentClasses()
	{
		Set<Class<? extends DrawComponent>> supportedClasses = super.getSupportedDrawComponentClasses();
		supportedClasses.add(TextScriptDrawComponent.class);
		supportedClasses.add(BarcodeDrawComponent.class);
		return super.getSupportedDrawComponentClasses();
	}

	@Override
	public RootDrawComponent createRootDrawComponent(boolean validate) {
		return createScriptRootDrawComponent(validate);
	}
	
}
