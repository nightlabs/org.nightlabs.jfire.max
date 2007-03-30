/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderParentResolver implements TreeNodeParentResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver#getParentObjectID(java.lang.Object)
	 */
	public ObjectID getParentObjectID(Object jdoObject) {
		if (jdoObject instanceof ValueProviderCategory)
			return ((ValueProviderCategory)jdoObject).getParentID();
		else
			return ((ValueProvider)jdoObject).getCategoryID();
	}

}
