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

import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.TextDrawComponent;
import org.nightlabs.editor2d.impl.ConstrainedTextDrawComponentImpl;
import org.nightlabs.jfire.scripting.editor2d.TextScriptDrawComponent;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ConstrainedScriptEditor2DFactoryImpl 
extends ScriptEditor2DFactoryImpl 
{
	@Override
	public TextScriptDrawComponent createTextScriptDrawComponent() {
		return new ConstrainedTextScriptDrawComponentImpl();
	}

	@Override
	public TextScriptDrawComponent createTextScriptDrawComponent(String text, Font font, int x, int y, DrawComponentContainer parent) {
		return new ConstrainedTextScriptDrawComponentImpl(text, font, x, y, parent);
	}

	@Override
	public TextScriptDrawComponent createTextScriptDrawComponent(String text, 
			String fontName, int fontSize, int fontStyle, int x, int y, 
			DrawComponentContainer parent) 
	{
		return new ConstrainedTextScriptDrawComponentImpl(text, fontName, 
				fontSize, fontStyle, x, y, parent);
	}

	@Override
	public TextDrawComponent createTextDrawComponent() {
		return new ConstrainedTextDrawComponentImpl();
	}

	@Override
	public TextDrawComponent createTextDrawComponent(String text, Font font, 
			int x, int y, DrawComponentContainer parent) 
	{
		return new ConstrainedTextDrawComponentImpl(text, font, x, y, parent);
	}

	@Override
	public TextDrawComponent createTextDrawComponent(String text, String fontName, 
			int fontSize, int fontStyle, int x, int y, DrawComponentContainer parent) 
	{
		return new ConstrainedTextDrawComponentImpl(text, fontName, fontSize, fontStyle, x, y,
				parent);
	}
	}
