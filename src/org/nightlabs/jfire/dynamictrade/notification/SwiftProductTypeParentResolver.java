package org.nightlabs.jfire.dynamictrade.notification;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.dynamictrade.store.SwiftProductType;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;

public class SwiftProductTypeParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		SwiftProductType vt = (SwiftProductType)jdoObject;
		return (ObjectID) JDOHelper.getObjectId(vt.getExtendedProductType());
	}
}
