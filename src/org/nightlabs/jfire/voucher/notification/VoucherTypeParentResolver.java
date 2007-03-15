package org.nightlabs.jfire.voucher.notification;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.jfire.voucher.store.VoucherType;

public class VoucherTypeParentResolver
implements TreeNodeParentResolver
{
	private static final long serialVersionUID = 1L;

	public ObjectID getParentObjectID(Object jdoObject)
	{
		VoucherType vt = (VoucherType)jdoObject;
		return (ObjectID) JDOHelper.getObjectId(vt.getExtendedProductType());
	}
}
