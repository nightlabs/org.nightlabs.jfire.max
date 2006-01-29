/**
 * 
 */
package org.nightlabs.jfire.scripting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.scripting.ScriptRegistryItem"
 *		detachable="true"
 *		table="JFireScripting_ScriptCategory"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ScriptCategory
		extends ScriptRegistryItem
{
	private static final long serialVersionUID = 1L;

	/**
	 * value: {@link ReportRegistryItem} childItem
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.scripting.ScriptRegistryItem"
	 *		mapped-by="parent"
	 */
	private Set<ScriptRegistryItem> children;	

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
		super(organisationID, scriptRegistryItemType, scriptRegistryItemID);
		init();
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
		super(organisationID, parent.getScriptRegistryItemType(), scriptRegistryItemID);
		this.setParent(parent);
		init();
	}

	/**
	 * Because there are multiple constructors, we have this method with common logic
	 * which is called by all constructors EXCEPT the default constructor.
	 */
	private void init()
	{
		children = new HashSet<ScriptRegistryItem>();

		ScriptRegistryItem parent = getParent();
		if (parent != null)
			this.setParameterSet(parent.getParameterSet());
	}

	@Override
	public ScriptCategory getParent()
	{
		return super.getParent();
	}

	public Set<ScriptRegistryItem> getChildren()
	{
		return Collections.unmodifiableSet(children);
	}

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
