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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Registry which allows the registration of {@link IConditionGenerator} for
 * a certain language
 * 
 * This class is a singleton
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class GeneratorRegistry
{
	private static GeneratorRegistry sharedInstance;
	public static GeneratorRegistry sharedInstance()
	{
		if (sharedInstance == null)
		{
			sharedInstance = new GeneratorRegistry();
			// register JavaScriptConditionGenerator
			sharedInstance.registerGenerator(new JavaScriptConditionGenerator());
		}
		return sharedInstance;
	}
	
	protected GeneratorRegistry() {
		super();
	}
	
	private Map<String, IConditionGenerator> language2Generator = new HashMap<String, IConditionGenerator>();
	
	public void registerGenerator(IConditionGenerator generator) {
		language2Generator.put(generator.getLanguage(), generator);
	}
	
//	public IConditionGenerator getGenerator(String language) {
//		return language2Generator.get(language);
//	}
	public IConditionGenerator getGenerator(String language, Collection<ScriptConditioner> scriptConditioner)
	{
		IConditionGenerator generator = language2Generator.get(language);
		generator.setScriptConditioner(scriptConditioner);
		return generator;
	}
	
}
