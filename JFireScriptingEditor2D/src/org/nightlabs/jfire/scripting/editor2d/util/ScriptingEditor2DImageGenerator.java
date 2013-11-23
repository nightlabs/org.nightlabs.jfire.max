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
package org.nightlabs.jfire.scripting.editor2d.util;

import org.nightlabs.editor2d.render.RenderConstants;
import org.nightlabs.editor2d.render.RenderModeDescriptor;
import org.nightlabs.editor2d.render.RenderModeManager;
import org.nightlabs.editor2d.render.Renderer;
import org.nightlabs.editor2d.util.ImageGenerator;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.render.j2d.J2DBarcodeDefaultRenderer;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptingEditor2DImageGenerator
extends ImageGenerator
{
	private static RenderModeManager renderModeMan = null;
	public static RenderModeManager getScripting2DRenderModeManager()
	{
		if (renderModeMan == null) {
			renderModeMan = new RenderModeManager();
			// Category Renderer
			RenderModeDescriptor barcodeDesc = new RenderModeDescriptor(
					RenderConstants.DEFAULT_MODE, "Default");
			Renderer r = renderModeMan.addRenderModeDescriptor(barcodeDesc,
					BarcodeDrawComponent.class.getName());
			r.addRenderContext(new J2DBarcodeDefaultRenderer());
		}
		return renderModeMan;
	}
}
