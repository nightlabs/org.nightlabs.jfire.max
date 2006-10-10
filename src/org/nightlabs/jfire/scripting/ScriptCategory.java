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

package org.nightlabs.jfire.scripting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.apache.log4j.Logger;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.scripting.ScriptRegistryItem"
 *		detachable="true"
 *		table="JFireScripting_ScriptCategory"
 * 
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="ScriptCategory.children" fetch-groups="default" fields="children"
 * @jdo.fetch-group name="ScriptCategory.this" fetch-groups="default, ScriptRegistryItem.this" fields="children"
 * 
 */
public class ScriptCategory
		extends ScriptRegistryItem
		implements NestableScriptRegistryItem
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_CHILDREN = "ScriptCategory.children";
	public static final String FETCH_GROUP_THIS_SCRIPT_CATEGORY = "ScriptCategory.this";

	/**
	 * value: {@link ReportRegistryItem} childItem
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.scripting.ScriptRegistryItem"
	 *		mapped-by="parent"
	 *		dependent-element="true"
	 */
	private Set children;	

	/**
	 * @deprecated Only for JDO!
	 */
	protected ScriptCategory() { }

	/**
	 * @param organisationID The owner-organisation.
	 * @param scriptRegistryItemType The type of the ScriptCategory
	 * @param scriptRegistryItemID The local id in the scope of organisationID and scriptRegistryItemType
	 */
	public ScriptCategory(
			String organisationID,
			String scriptRegistryItemType,
			String scriptRegistryItemID)
	{
		this(null, organisationID, scriptRegistryItemType, scriptRegistryItemID);
	}

	/**
	 * @param parent Can be <code>null</code>, if this is the root category.
	 * @param organisationID The owner-organisation. Must not be <code>null</code>.
	 * @param scriptRegistryItemType The type of the ScriptCategory. Must not be <code>null</code>.
	 * @param scriptRegistryItemID The local id in the scope of <code>organisationID</code> and <code>scriptRegistryItemType</code>. Must not be null. 
	 */
	public ScriptCategory(
			ScriptCategory parent,
			String organisationID,
			String scriptRegistryItemType,
			String scriptRegistryItemID)
	{
		super(organisationID, scriptRegistryItemType, scriptRegistryItemID);
		this.setParent(parent);
		init();
	}

	private static String _getScriptRegistryItemType(ScriptCategory parent)
	{
		if (parent == null)
			throw new IllegalArgumentException("parent must not be null! Use the other constructor, if you want to create a root-category!");

		return parent.getScriptRegistryItemType();
	}
	
	/**
	 * @param organisationID The owner-organisation.
	 * @param scriptRegistryItemType The type of the ScriptCategory
	 * @param scriptRegistryItemID The local id in the scope of organisationID and scriptRegistryItemType
	 */
	public ScriptCategory(
			ScriptCategory parent,
			String organisationID,
			String scriptRegistryItemID)
	{
		super(organisationID, _getScriptRegistryItemType(parent), scriptRegistryItemID);
		this.setParent(parent);
		init();
	}

	/**
	 * Because there are multiple constructors, we have this method with common logic
	 * which is called by all constructors EXCEPT the default constructor.
	 */
	private void init()
	{
		children = new HashSet();

		try {
			ScriptCategory parent = getParent();
			if (parent != null)
				setParameterSet(parent.getParameterSet());
		} catch (JDODetachedFieldAccessException x) {
			Logger.getLogger(ScriptCategory.class).warn("Could not inherit ParameterSet! Will try it in jdoPreStore()...", x);
		}
	}

	@Override
	public ScriptCategory getParent()
	{
		return super.getParent();
	}

	/**
	 * Returns and unmodifiableSet with 
	 * all sub-categories and scripts of this category.
	 */
	public Set getChildren()
	{
		return Collections.unmodifiableSet(children);
	}

	/**
	 * Clears the set of children of this category.
	 * Note that the set is marked with dependent-element="true"
	 * and therefore all sub elements will be deleted recursively.
	 */
	public void clearChildren() {
		children.clear();
	}

	/**
	 * Add the given child to the set of sub-elements.
	 * 
	 * @param child The new child to add.
	 */
	public void addChild(ScriptRegistryItem child)
	{
		if (!this.equals(child.getParent()))
			throw new IllegalArgumentException("The child's parent is not me! Cannot add!");

		children.add(child);
	}

//	public ScriptCategory createChildScriptCategory(String organisationID, String scriptRegistryItemID)
//	{
//		ScriptCategory scriptCategory = new ScriptCategory(this, organisationID, scriptRegistryItemID);
//		children.add(scriptCategory);
//		return scriptCategory;
//	}
//
//	public Script createChildScript(String organisationID, String scriptRegistryItemID)
//	{
//		Script script = new Script(this, organisationID, scriptRegistryItemID);
//		children.add(script);
//		return script;
//	}
}
