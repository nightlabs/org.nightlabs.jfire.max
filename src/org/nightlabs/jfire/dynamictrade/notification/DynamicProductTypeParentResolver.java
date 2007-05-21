package org.nightlabs.jfire.dynamictrade.notification;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class DynamicProductTypeParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		DynamicProductType vt = (DynamicProductType)jdoObject;
		return (ObjectID) JDOHelper.getObjectId(vt.getExtendedProductType());
	}
}
