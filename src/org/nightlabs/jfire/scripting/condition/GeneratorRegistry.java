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
package org.nightlabs.jfire.scripting.condition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class GeneratorRegistry 
{
	private static GeneratorRegistry sharedInstance;
	public static GeneratorRegistry sharedInstance()  
	{
		if (sharedInstance == null)
			sharedInstance = new GeneratorRegistry();
		return sharedInstance;
	}
	
	protected GeneratorRegistry() {
		super();
	}
	
	private Map<String, ISyntaxGenerator> language2Generator = new HashMap<String, ISyntaxGenerator>();
	
	public void registerGenerator(String language, ISyntaxGenerator generator) {
		language2Generator.put(language, generator);
	}
	
	public ISyntaxGenerator getGenerator(String language) {
		return language2Generator.get(language);
	}
}
