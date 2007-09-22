package org.nightlabs.jfire.dynamictrade.notification;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class DynamicProductTypeParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		DynamicProductType dpt = (DynamicProductType)jdoObject;
		return dpt.getExtendedProductTypeID();
	}
}
