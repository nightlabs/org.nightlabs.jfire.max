package org.nightlabs.jfire.simpletrade.notification;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;

public class SimpleProductTypeParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		SimpleProductType spt = (SimpleProductType) jdoObject;
		return spt.getExtendedProductTypeID();
	}
}
