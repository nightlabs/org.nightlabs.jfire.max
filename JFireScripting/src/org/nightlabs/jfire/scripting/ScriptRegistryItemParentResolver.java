package org.nightlabs.jfire.scripting;


import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;


/**
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */
public class ScriptRegistryItemParentResolver implements TreeNodeParentResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver#getParentObjectID(java.lang.Object)
	 */
	public ObjectID getParentObjectID(Object jdoObject) {
		if(jdoObject instanceof ScriptCategory)	
		{
			ScriptCategory c = (ScriptCategory)jdoObject;
			return c.getParent() == null ? null : (ScriptRegistryItemID)JDOHelper.getObjectId(c.getParent());
		}
		Script s = (Script)jdoObject;
		return s.getParent() == null ? null : (ScriptRegistryItemID)JDOHelper.getObjectId(s.getParent());
	}
}
