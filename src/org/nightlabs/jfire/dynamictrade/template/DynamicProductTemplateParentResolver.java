package org.nightlabs.jfire.dynamictrade.template;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class DynamicProductTemplateParentResolver implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	@Override
	public ObjectID getParentObjectID(Object jdoObject) {
		return ((DynamicProductTemplate)jdoObject).getParentCategoryID();
	}

}
