package org.nightlabs.jfire.swifttrade.notification;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.jfire.swifttrade.store.SwiftProductType;

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
