/**
 * 
 */
package org.nightlabs.jfire.reporting.layout;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportRegistryItemParentResolver implements TreeNodeParentResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver#getParentObjectID(java.lang.Object)
	 */
	public ObjectID getParentObjectID(Object jdoObject) {
		return ((ReportRegistryItem)jdoObject).getParentCategoryID();
	}

}
