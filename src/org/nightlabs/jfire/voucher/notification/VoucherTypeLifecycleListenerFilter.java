package org.nightlabs.jfire.voucher.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.TreeLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * @deprecated We don't need this as we'll better use {@link TreeLifecycleListenerFilter} with {@link TreeNodeParentResolver}
 *		- i.e. {@link VoucherTypeParentResolver}!
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class VoucherTypeLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private JDOLifecycleState[] lifecycleStates;

	private Set<ProductTypeID> parentVoucherTypeIDs;

	/**
	 * @param lifecycleStates The states we are interested in.
	 * @param parentVoucherTypeIDs If <code>parentVoucherTypeIDs</code> is not <code>null</code>,
	 *		then only children of these parents will cause a notification. If
	 *		<code>parentVoucherTypeIDs</code> is <code>null</code>
	 *		then all created/modified/deleted {@link VoucherType}s. This <code>Set</code> may contain
	 *		<code>null</code> in order to specify that it is interested in notifications about root-elements
	 *		(not having a parent).
	 */
	public VoucherTypeLifecycleListenerFilter(
			JDOLifecycleState[] lifecycleStates,
			Set<ProductTypeID> parentVoucherTypeIDs)
	{
		this.lifecycleStates = lifecycleStates;
		this.parentVoucherTypeIDs = parentVoucherTypeIDs;
	}

	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (parentVoucherTypeIDs == null)
			return event.getDirtyObjectIDs();

		PersistenceManager pm = event.getPersistenceManager();
		pm.getExtent(VoucherType.class);

		ArrayList<DirtyObjectID> res = new ArrayList<DirtyObjectID>(event.getDirtyObjectIDs().size());
		for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			VoucherType voucherType = (VoucherType) pm.getObjectById(dirtyObjectID.getObjectID());
			ProductTypeID parentID = (ProductTypeID) JDOHelper.getObjectId(voucherType.getExtendedProductType());
			if (parentVoucherTypeIDs.contains(parentID))
				res.add(dirtyObjectID);
		}
		return res;
	}

	private static Class[] candidateClasses = { VoucherType.class };

	public Class[] getCandidateClasses()
	{
		return candidateClasses;
	}

	public JDOLifecycleState[] getLifecycleStates()
	{
		return lifecycleStates;
	}

	/**
	 * If <code>parentVoucherTypeIDs</code> is not <code>null</code>,
	 * then only children of these parents will cause a notification. If
	 * <code>parentVoucherTypeIDs</code> is <code>null</code>
	 * then all created/modified/deleted {@link VoucherType}s
	 * will cause notification. This <code>Set</code> may contain
	 * <code>null</code> in order to specify that it is interested in notifications about root-elements
	 * (not having a parent).
	 */
	public Set<ProductTypeID> getParentVoucherTypeIDs()
	{
		return parentVoucherTypeIDs;
	}
}
